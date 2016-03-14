package info.hearthsim.brazier.parsing;

import com.google.gson.JsonPrimitive;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Executable;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jtrim.collections.CollectionsEx;
import org.jtrim.utils.ExceptionHelper;

public final class JsonDeserializer {
    private static final String CLASS_FIELD_NAME = "class";

    /**
     * Mappings from primitive type {@code Class}es to wrapper type {@code Class}es
     */
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TO_WRAPPER;
    /**
     * Mappings from primitive and wrapper type {@code Class}es to {@link JsonPrimitive} getter functions,
     * which returns the {@code JsonPrimitive} as the requested type.
     */
    private static final Map<Class<?>, Function<JsonPrimitive, Object>> JSON_PRIMITIVE_CONVERSIONS;

    static {
        Map<Class<?>, Class<?>> primitiveToWrapper = new HashMap<>();
        primitiveToWrapper.put(boolean.class, Boolean.class);
        primitiveToWrapper.put(char.class, Character.class);
        primitiveToWrapper.put(byte.class, Byte.class);
        primitiveToWrapper.put(short.class, Short.class);
        primitiveToWrapper.put(int.class, Integer.class);
        primitiveToWrapper.put(long.class, Long.class);
        primitiveToWrapper.put(float.class, Float.class);
        primitiveToWrapper.put(double.class, Double.class);
        primitiveToWrapper.put(void.class, Void.class);
        PRIMITIVE_TO_WRAPPER = Collections.unmodifiableMap(primitiveToWrapper);

        Map<Class<?>, Function<JsonPrimitive, Object>> jsonPrimitiveConversions = new HashMap<>();
        jsonPrimitiveConversions.put(boolean.class, JsonPrimitive::getAsBoolean);
        jsonPrimitiveConversions.put(Boolean.class, JsonPrimitive::getAsBoolean);
        jsonPrimitiveConversions.put(byte.class, JsonPrimitive::getAsByte);
        jsonPrimitiveConversions.put(Byte.class, JsonPrimitive::getAsByte);
        jsonPrimitiveConversions.put(short.class, JsonPrimitive::getAsShort);
        jsonPrimitiveConversions.put(Short.class, JsonPrimitive::getAsShort);
        jsonPrimitiveConversions.put(int.class, JsonPrimitive::getAsInt);
        jsonPrimitiveConversions.put(Integer.class, JsonPrimitive::getAsInt);
        jsonPrimitiveConversions.put(long.class, JsonPrimitive::getAsLong);
        jsonPrimitiveConversions.put(Long.class, JsonPrimitive::getAsLong);
        jsonPrimitiveConversions.put(float.class, JsonPrimitive::getAsFloat);
        jsonPrimitiveConversions.put(Float.class, JsonPrimitive::getAsFloat);
        jsonPrimitiveConversions.put(double.class, JsonPrimitive::getAsDouble);
        jsonPrimitiveConversions.put(Double.class, JsonPrimitive::getAsDouble);
        jsonPrimitiveConversions.put(String.class, JsonPrimitive::getAsString);
        JSON_PRIMITIVE_CONVERSIONS = Collections.unmodifiableMap(jsonPrimitiveConversions);
    }

    private final CustomClassNameResolver classNameResolver;
    private final Map<Class<?>, List<ObjectConverterWrapper<?>>> typeConverters;
    private final Map<Class<?>, CustomStringParser<?>> customStringParsers;
    private final Map<Class<?>, CollectionMergerWrapper<?>> typeMergers;

    private JsonDeserializer(Builder builder) {
        this.classNameResolver = builder.classNameResolver;
        this.typeConverters = copyMapOfMap(builder.typeConverters);
        this.customStringParsers = new HashMap<>(builder.customStringParsers);
        this.typeMergers = new HashMap<>(builder.typeMergers);
    }

    private static <K, V> Map<K, List<V>> copyMapOfMap(Map<K, List<V>> src) {
        Map<K, List<V>> result = CollectionsEx.newHashMap(src.size());
        for (Map.Entry<K, List<V>> entry: src.entrySet()) {
            result.put(entry.getKey(), CollectionsEx.readOnlyCopy(entry.getValue()));
        }
        return result;
    }

    /**
     * Converts the given {@link JsonTree} to an instance of the given expected type.
     * @param element the given {@link JsonTree}.
     * @param expectedType the given expected type.
     * @return the result of the conversion.
     * @throws ObjectParsingException if failed to convert the given {@link JsonTree}.
     */
    public <T> T toJavaObject(JsonTree element, Class<? extends T> expectedType) throws ObjectParsingException {
        return toJavaObject(element, expectedType, (type) -> {});
    }

    /**
     * Parses the given {@link JsonTree} to the expected type.
     *
     * @param element the given {@code JsonTree}.
     * @param expectedType the {@code Class} of the expected type.
     * @param typeChecker the {@link TypeChecker} that will be used to check the result.
     * @return the converted result.
     * @throws ObjectParsingException if failed to parse the given {@code JsonTree}.
     * @throws NullPointerException if any of the arguments is {@code null}.
     */
    public <T> T toJavaObject(
            JsonTree element,
            Class<? extends T> expectedType,
            TypeChecker typeChecker) throws ObjectParsingException {
        ExceptionHelper.checkNotNullArgument(element, "element");
        ExceptionHelper.checkNotNullArgument(expectedType, "expectedType");
        ExceptionHelper.checkNotNullArgument(typeChecker, "typeChecker");

        Object result = toJavaObjectUnsafe(element, expectedType, typeChecker);

        // Tries to wrap the result if it's of primitive type.
        Class<?> wrapperClass = PRIMITIVE_TO_WRAPPER.get(expectedType);
        if (wrapperClass != null) {
            @SuppressWarnings("unchecked")
            T boxedValue = (T) wrapperClass.cast(result);
            return boxedValue;
        }

        if (expectedType.isInstance(result)) {
            return expectedType.cast(result);
        }
        else {
            List<ObjectConverterWrapper<?>> converters = typeConverters.getOrDefault(expectedType, Collections.emptyList());
            for (ObjectConverterWrapper<?> converter: converters) {
                if (converter.appliesTo(result)) {
                    return expectedType.cast(converter.convert(result));
                }
            }
        }

        throw new ObjectParsingException("Unexpected object type: " + result.getClass().getName()
                + ". Expected: " + expectedType.getName());
    }

    /**
     * Converts the given {@code String} to the expected enum type.
     */
    private Object fromEnum(String str, Class<?> enumClass) throws ObjectParsingException {
        CustomStringParser<?> customResolver = customStringParsers.get(enumClass);
        if (customResolver != null) {
            Object result = customResolver.parse(str);
            if (result != null) {
                return result;
            }
        }

        Object[] enumConstants = enumClass.getEnumConstants();
        Object matchWithCaseError = null;
        for (Object candidate: enumConstants) {
            String name = candidate.toString();
            if (str.equals(name)) {
                return candidate;
            }
            if (str.equalsIgnoreCase(name)) {
                matchWithCaseError = candidate;
            }
        }

        if (matchWithCaseError == null) {
            throw new ObjectParsingException("Invalid enum value for "
                    + enumClass.getSimpleName() + ": " + str
                    + ". Possible names: " + Arrays.toString(enumConstants));
        }
        return matchWithCaseError;
    }

    /**
     * Converts the given {@link JsonTree} to a Java object unsafely, as its return type is {@link Object} instead
     * of any generic type.
     */
    private Object toJavaObjectUnsafe(
            JsonTree element,
            Class<?> expectedType,
            TypeChecker typeChecker) throws ObjectParsingException {

        if (expectedType.isEnum()) {
            return fromEnum(element.getAsString(), expectedType);
        }
        else if (expectedType.isArray()) {
            Class<?> arrayElementType = expectedType.getComponentType();

            if (element.isJsonArray()) {
                int size = element.getChildCount();
                Object result = Array.newInstance(arrayElementType, size);
                for (int i = 0; i < size; i++) {
                    Array.set(result, i, toJavaObject(element.getChild(i), arrayElementType));
                }
                return result;
            }
            else {
                Object result = Array.newInstance(arrayElementType, 1);
                Array.set(result, 0, toJavaObject(element, arrayElementType));
                return result;
            }
        }
        else if (element.isJsonArray()) {
            CollectionMergerWrapper<?> merger = typeMergers.get(expectedType);
            if (merger != null) {
                return merger.parseFrom(this, element, typeChecker);
            }
        }

        if (element.isJsonObject()) {
            return toComplexJavaObject(element, expectedType, typeChecker);
        }
        else {
            if (element.isJsonPrimitive()) {
                return parsePrimitive(element.getAsJsonPrimitive(), expectedType, typeChecker);
            }

            throw new ObjectParsingException("Unexpected JSON element type: " + element);
        }
    }


    /**
     * Parses the given {@link JsonPrimitive} to the expected type.
     *
     * @param element the given {@link JsonPrimitive}.
     * @param expectedType the expected type.
     * @param typeChecker {@link TypeChecker} that will used to check the result.
     * @return the converted result.
     * @throws ObjectParsingException if failed to parse the given {@link JsonPrimitive}.
     */
    private Object parsePrimitive(
            JsonPrimitive element,
            Class<?> expectedType,
            TypeChecker typeChecker) throws ObjectParsingException {
        CustomStringParser<?> customResolver = customStringParsers.get(expectedType);
        if (customResolver != null) {
            Object result = customResolver.parse(element.getAsString());
            if (result != null) {
                return result;
            }
        }

        Function<JsonPrimitive, Object> converter = JSON_PRIMITIVE_CONVERSIONS.get(expectedType);
        return converter != null
                ? converter.apply(element)
                : getFieldObject(element.getAsString(), typeChecker);
    }

    /**
     * Treats the given field definition as the qualified name of a {@code public static} field or no-arg method in a
     * {@code public} class and returns the value or the result of it.
     *
     * @param fieldDef the given qualified name of a {@code public static} field or no-arg method.
     * @param typeChecker {@link TypeChecker} that will be used to check the result.
     * @return the value or the result of the wanting field or method.
     *
     * @throws ObjectParsingException if failed to find such field or method or to fetch the respective value.
     */
    private Object getFieldObject(String fieldDef, TypeChecker typeChecker) throws ObjectParsingException {
        int fieldSepIndex = fieldDef.lastIndexOf('.');
        if (fieldSepIndex < 0) {
            throw new ObjectParsingException("Invalid field definition: " + fieldDef);
        }
        String fieldName = fieldDef.substring(fieldSepIndex + 1);

        Class<?> declaringClass;

        String className = fieldDef.substring(0, fieldSepIndex);
        if (className.indexOf('.') < 0) {
            declaringClass = classNameResolver.toClass(className);
        }
        else {
            try {
                declaringClass = Class.forName(className);
            } catch (ClassNotFoundException ex) {
                throw new ObjectParsingException("Missing class: " + className, ex);
            }
        }

        return getFieldObject(declaringClass, fieldName, typeChecker);
    }

    /**
     * Util method for printing the given declaring class name and field name.
     */
    private static String fieldDef(Class<?> declaringClass, String fieldName) {
        return declaringClass.getName() + '.' + fieldName;
    }

    /**
     * Tries to return the {@code public static} field with the given field name in the given {@code Class}; if no
     * such field can be found, the given field name will be treated as a no-arg method's name and invoke
     * {@link #getNoArgMethodObject(Class, String, TypeChecker)}.
     *
     * @param declaringClass the declaring class of the field
     * @param fieldName the field name
     * @param typeChecker {@link TypeChecker} that will be used to check the field's generic type.
     * @return the converted result
     * @throws ObjectParsingException if there is no such field or method in the given class, or the given class is not
     *                                {@code public} or the field is not {@code public} and {@code static}.
     * @throws RuntimeException if unexpected {@link IllegalAccessException} occurred when trying to get the value of
     *                          the field.
     *
     * @see #getNoArgMethodObject(Class, String, TypeChecker)
     */
    private Object getFieldObject(Class<?> declaringClass, String fieldName, TypeChecker typeChecker) throws ObjectParsingException {
        if (!Modifier.isPublic(declaringClass.getModifiers())) {
            throw new ObjectParsingException("Class is not public: " + declaringClass.getName());
        }

        Field field;
        try {
            field = declaringClass.getField(fieldName);
        } catch (NoSuchFieldException ex) {
            return getNoArgMethodObject(declaringClass, fieldName, typeChecker);
        }

        if (!Modifier.isPublic(field.getModifiers())) {
            throw new ObjectParsingException("Field is not public: " + fieldDef(declaringClass, fieldName));
        }

        if (!Modifier.isStatic(field.getModifiers())) {
            throw new ObjectParsingException("Field is not static: " + fieldDef(declaringClass, fieldName));
        }

        typeChecker.checkType(field.getGenericType());

        try {
            return field.get(null);
        } catch (IllegalAccessException ex) {
            throw new RuntimeException("Unexpected IllegalAccessException for field: " + fieldDef(declaringClass, fieldName), ex);
        }
    }

    /**
     * Returns the result of a {@code public static} no-arg method with the given method name in the
     * given declaring {@code public} {@code Class}.
     *
     * @param declaringClass the given declaring {@code Class}.
     * @param methodName the given method name.
     * @param typeChecker {@link TypeChecker} that will be used to check the method's generic return type.
     * @return the evaluated result of the method.
     * @throws ObjectParsingException if there is no such method in the given class, or the given {@code Class} is not
     *                                {@code public} or the method is not {@code public} and {@code static}.
     * @throws RuntimeException if unexpected {@link IllegalAccessException} or {@link InvocationTargetException}
     *                          occurred when trying to invoke the method.
     */
    private Object getNoArgMethodObject(Class<?> declaringClass, String methodName, TypeChecker typeChecker) throws ObjectParsingException {
        if (!Modifier.isPublic(declaringClass.getModifiers())) {
            throw new ObjectParsingException("Class is not public: " + declaringClass.getName());
        }

        Method method;
        try {
            method = declaringClass.getMethod(methodName);
        } catch (NoSuchMethodException ex) {
            throw new ObjectParsingException("No such method: " + fieldDef(declaringClass, methodName), ex);
        }

        if (!Modifier.isPublic(method.getModifiers())) {
            throw new ObjectParsingException("Field is not public: " + fieldDef(declaringClass, methodName));
        }

        if (!Modifier.isStatic(method.getModifiers())) {
            throw new ObjectParsingException("Field is not static: " + fieldDef(declaringClass, methodName));
        }

        typeChecker.checkType(method.getGenericReturnType());

        try {
            return method.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException ex) {
            throw new RuntimeException("Unexpected exception for method: " + fieldDef(declaringClass, methodName), ex);
        }
    }

    /**
     * Returns if the given {@link JsonTree} has all the arguments of the given method.
     */
    private static boolean hasAllArguments(Executable method, JsonTree tree) {
        for (Parameter param: method.getParameters()) {
            if (tree.getChild(getParameterName(param)) == null) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns the annotated parameter name of the given {@link Parameter}. The parameter name is provided via
     * {@link NamedArg} annotation.
     */
    private static String getParameterName(Parameter param) {
        NamedArg nameAnnotation = param.getAnnotation(NamedArg.class);
        return nameAnnotation != null ? nameAnnotation.value() : "?";
    }

    /**
     * Creates a new Java object by calling the given factory method with the parameters given in the given
     * {@link JsonTree}.
     */
    private <T> T newObject(
            JsonTree tree,
            Factory<? extends T> factory) throws ObjectParsingException {

        Parameter[] parameters = factory.getParameters();
        Object[] passedArgs = new Object[parameters.length];

        for (int i = 0; i < passedArgs.length; i++) {
            Parameter parameter = parameters[i];
            JsonTree parameterElement = tree.getChild(getParameterName(parameter));
            passedArgs[i] = toJavaObject(parameterElement, parameter.getType());
        }

        return factory.newInstance(passedArgs);
    }

    /**
     * Returns the most suitable constructor of the given class for the given {@link JsonTree}, which means the
     * {@code public} constructor with the most arguments in the given {@code JsonTree}.
     */
    private static Constructor<?> findConstructor(JsonTree tree, Class<?> actionClass) {
        Constructor<?> result = null;
        int argCount = 0;

        Constructor<?>[] constructors = actionClass.getConstructors();
        for (Constructor<?> candidate: constructors) {
            if (!Modifier.isPublic(candidate.getModifiers())) {
                continue;
            }

            if (result == null || argCount < candidate.getParameterCount()) {
                if (hasAllArguments(candidate, tree)) {
                    result = candidate;
                    argCount = candidate.getParameterCount();
                }
            }
        }
        return result;
    }


    /**
     * Finds the most suitable {@code public static} factory method with the given method name from the given class,
     * which means the {@code public static} method with the most arguments in the given {@code JsonTree}.
     */
    private static Method findFactoryMethod(
            JsonTree tree,
            Class<?> actionClass,
            String methodName) {

        Method result = null;
        int argCount = 0;

        Method[] methods = actionClass.getMethods();
        for (Method method: methods) {
            if (!methodName.equals(method.getName())) {
                continue;
            }

            if (!Modifier.isPublic(method.getModifiers())) {
                continue;
            }

            if (!Modifier.isStatic(method.getModifiers())) {
                continue;
            }

            if (method.getReturnType() == Void.TYPE) {
                continue;
            }

            if (result == null || argCount < method.getParameterCount()) {
                if (hasAllArguments(method, tree)) {
                    result = method;
                    argCount = method.getParameterCount();
                }
            }
        }
        return result;
    }

    /**
     * Wraps the given {@link Constructor} in a {@link Factory}.
     */
    private static <T> Factory<T> toFactory(Constructor<? extends T> constructor) {
        return new Factory<T>() {
            @Override
            public Parameter[] getParameters() {
                return constructor.getParameters();
            }

            @Override
            public T newInstance(Object... arguments) throws ObjectParsingException {
                try {
                    return constructor.newInstance(arguments);
                } catch (InstantiationException | IllegalAccessException ex) {
                    String className = constructor.getDeclaringClass().getName();
                    throw new RuntimeException("Unexpected constructor error for " + className, ex);
                } catch (InvocationTargetException ex) {
                    String className = constructor.getDeclaringClass().getName();
                    throw new ObjectParsingException("Creating an instance of " + className + " failed.", ex.getCause());
                }
            }
        };
    }

    /**
     * Wraps the given {@link Method} in a {@link Factory}.
     */
    private static Factory<Object> toFactory(Method factoryMethod) {
        return new Factory<Object>() {
            @Override
            public Parameter[] getParameters() {
                return factoryMethod.getParameters();
            }

            @Override
            public Object newInstance(Object... arguments) throws ObjectParsingException {
                try {
                    return factoryMethod.invoke(null, arguments);
                } catch (IllegalAccessException ex) {
                    String className = factoryMethod.getReturnType().getName();
                    throw new RuntimeException("Unexpected constructor error for " + className, ex);
                } catch (InvocationTargetException ex) {
                    String className = factoryMethod.getReturnType().getName();
                    throw new ObjectParsingException("Creating an instance of " + className + " failed.", ex.getCause());
                }
            }
        };
    }

    /**
     * Converts the given {@link JsonTree} to the expected class by using the factory method with the given name.
     *
     * @param tree the given {@link JsonTree}.
     * @param actionClass the expected class.
     * @param factoryMethodName the given factory method name; if it's {@code null}, the method will try to use the
     *                          constructor of the given class if it's not {@code abstract}.
     * @param typeChecker {@link TypeChecker} that will be used to check the result.
     * @return the converted result.
     * @throws ObjectParsingException if the given class is not {@code public} or the factory method name is not provided
     *                                and the class is {@code abstract}.
     */
    private Object toComplexJavaObject(
            JsonTree tree,
            Class<?> actionClass,
            String factoryMethodName,
            TypeChecker typeChecker) throws ObjectParsingException {

        if (!Modifier.isPublic(actionClass.getModifiers())) {
            throw new ObjectParsingException("The class " + actionClass.getName() + " is not public.");
        }

        Factory<Object> factory;
        if (factoryMethodName == null) {
            if (Modifier.isAbstract(actionClass.getModifiers())) {
                throw new ObjectParsingException("The class " + actionClass.getName() + " is abstract.");
            }

            Constructor<?> constructor = findConstructor(tree, actionClass);
            factory = constructor != null ? toFactory(constructor) : null;
        }
        else {
            Method factoryMethod = findFactoryMethod(tree, actionClass, factoryMethodName);
            if (factoryMethod != null) {
                typeChecker.checkType(factoryMethod.getGenericReturnType());
                factory = toFactory(factoryMethod);
            }
            else {
                factory = null;
            }
        }

        if (factory == null) {
            // No constructor or factory method can be found for this name
            // Treating it as a public static field
            return getFieldObject(actionClass, factoryMethodName, typeChecker);
        }
        else {
            return newObject(tree, factory);
        }
    }

    /**
     * Resolves the given class name with the {@code classNameResolver} if it's an unqualified class name;
     * otherwise, resolve it by using {@link Class#forName(String)}.
     *
     * @throws ObjectParsingException if failed to resolve the class name.
     */
    private Class<?> resolveClass(String className) throws ObjectParsingException {
        if (className.indexOf('.') < 0) {
            return classNameResolver.toClass(className);
        }
        else {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ex) {
                throw new ObjectParsingException("Cannot find class: " + className, ex);
            }
        }
    }

    /**
     * Converts the given {@link JsonTree} to Java object of the given expected type, and checks its runtime type
     * with the given {@link TypeChecker}.
     */
    private Object toComplexJavaObject(
            JsonTree tree,
            Class<?> expectedType,
            TypeChecker typeChecker) throws ObjectParsingException {
        String className = ParserUtils.tryGetStringField(tree, CLASS_FIELD_NAME);
        if (className == null) {
            return expectedType.cast(toComplexJavaObject(tree, expectedType, null, typeChecker));
        }

        String factoryMethodName = null;

        Class<?> actionClass = null;
        int methodSepCandidateIndex = className.lastIndexOf('.');
        if (methodSepCandidateIndex >= 0) {
            String methodNameCandidate = className.substring(methodSepCandidateIndex + 1);
            boolean methodName = !methodNameCandidate.isEmpty()
                    && Character.isLowerCase(methodNameCandidate.charAt(0));

            if (!methodName) {
                try {
                    actionClass = resolveClass(className);
                } catch (ObjectParsingException ex) {
                    methodName = true;
                    // Maybe it is a field declaration which is treated the same way as a method here.
                }
            }

            if (methodName) {
                factoryMethodName = methodNameCandidate;
                className = className.substring(0, methodSepCandidateIndex);
            }
        }

        if (actionClass == null) {
            actionClass = resolveClass(className);
        }
        return toComplexJavaObject(tree, actionClass, factoryMethodName, typeChecker);
    }

    private interface Factory<T> {
        public Parameter[] getParameters();
        public T newInstance(Object... arguments) throws ObjectParsingException;
    }


    /**
     * Wrapper for {@link ObjectConverter}, contains the wrapping {@code ObjectConverter} and the {@link Class} object
     * of its source type. Used only in {@link #typeConverters}.
     */
    private static final class ObjectConverterWrapper <Src> {
        private final Class<? extends Src> srcType;
        private final ObjectConverter<? super Src, ?> objectConverter;

        public ObjectConverterWrapper(
            Class<? extends Src> srcType,
            ObjectConverter<? super Src, ?> objectConverter) {
            ExceptionHelper.checkNotNullArgument(srcType, "srcType");
            ExceptionHelper.checkNotNullArgument(objectConverter, "objectConverter");

            this.srcType = srcType;
            this.objectConverter = objectConverter;
        }

        public boolean appliesTo(Object src) {
            return srcType.isInstance(src);
        }

        public Object convert(Object src) throws ObjectParsingException {
            return objectConverter.convertFrom(srcType.cast(src));
        }
    }

    /**
     * Wrapper for {@link CollectionMerger}, contains the wrapping {@code CollectionMerger} and the {@code Class}
     * object of its generic type.
     */
    private static final class CollectionMergerWrapper<T> {
        private final Class<T> type;
        private final CollectionMerger<T> merger;

        public CollectionMergerWrapper(Class<T> type, CollectionMerger<T> merger) {
            this.type = type;
            this.merger = merger;
        }

        /**
         * Parses the given json array in {@link JsonTree} and merges the results with the underlying merger.
         *
         * @param deserializer the {@link JsonDeserializer} used to parse the given {@code JsonTree}.
         * @param array the given json array in {@link JsonTree}.
         * @param typeChecker {@link TypeChecker} that will be used to check the converted elements.
         * @return a collection of parsed results.
         * @throws ObjectParsingException if failed to parse one of the element.
         */
        public T parseFrom(
                JsonDeserializer deserializer,
                JsonTree array,
                TypeChecker typeChecker) throws ObjectParsingException {

            int elementCount = array.getChildCount();
            List<T> elements = new ArrayList<>(elementCount);
            for (int i = 0; i < elementCount; i++) {
                T element = deserializer.toJavaObject(array.getChild(i), type, typeChecker);
                elements.add(element);
            }
            return merger.merge(elements);
        }
    }

    /**
     * Functional interface with its sole un-implemented method {@link #merge(Collection)},
     * which merges the given {@link Collection} of {@code T} into one {@code T} instance.
     *
     * @see ParserUtils#addTypeMergers(Builder)
     */
    public interface CollectionMerger<T> {
        /**
         * Merges the given {@link Collection} of {@code T} into one {@code T} instance.
         */
        public T merge(Collection<? extends T> elements);
    }

    /**
     * Functional interface with its sole un-implemented method {@link #parse(String)},
     * which parses the given {@link String} and creates the respective {@code T} instance.
     *
     * @see ParserUtils#addCustomStringParsers(Supplier, Builder)
     */
    public interface CustomStringParser<T> {
        /**
         * Creates an instance of {@code T} with the given {@code String}.
         * @throws ObjectParsingException if failed to parse the given {@code String}.
         */
        public T parse(String str) throws ObjectParsingException;
    }

    /**
     * Functional interface with its sole un-implemented method {@link #toClass(String)},
     * which converts the given unqualified class name to the respective {@link Class} object.
     *
     * @see ParserUtils#resolveClassName(String)
     */
    public interface CustomClassNameResolver {
        /**
         * Parses the given unqualified class name and returns the respective {@link Class} object.
         *
         * @throws ObjectParsingException if failed to resolve the class.
         */
        public Class<?> toClass(String unqualifiedName) throws ObjectParsingException;
    }

    /**
     * Functional interface with its sole un-implemented method {@link #convertFrom(Object)},
     * which converts the given object to another object.
     *
     * @see ParserUtils#addTypeConversions(Builder)
     */
    public interface ObjectConverter <Src, Dest> {
        /**
         * Converts the given object to the requested type.
         *
         * @throws ObjectParsingException if failed to convert the given object.
         */
        public Dest convertFrom(Src obj) throws ObjectParsingException;
    }

    public static final class Builder {
        private final CustomClassNameResolver classNameResolver;
        private final Map<Class<?>, List<ObjectConverterWrapper<?>>> typeConverters;
        private final Map<Class<?>, CustomStringParser<?>> customStringParsers;
        private final Map<Class<?>, CollectionMergerWrapper<?>> typeMergers;

        public Builder(CustomClassNameResolver classNameResolver) {
            ExceptionHelper.checkNotNullArgument(classNameResolver, "classNameResolver");
            this.classNameResolver = classNameResolver;
            this.typeConverters = new HashMap<>();
            this.customStringParsers = new HashMap<>();
            this.typeMergers = new HashMap<>();
        }

        /**
         * Sets the {@link CollectionMerger} for the given type.
         */
        public <T> void setTypeMerger(
            Class<T> type,
            CollectionMerger<T> typeMerger) {
            ExceptionHelper.checkNotNullArgument(type, "type");
            ExceptionHelper.checkNotNullArgument(typeMerger, "typeMerger");

            typeMergers.put(type, new CollectionMergerWrapper<>(type, typeMerger));
        }

        /**
         * Adds a new {@link ObjectConverter} to the builder.
         */
        public <Src, Dest> void addTypeConversion(
            Class<? extends Src> srcType,
            Class<Dest> destType,
            ObjectConverter<? super Src, ? extends Dest> objectConverter) {

            ObjectConverterWrapper<Src> rawConverter = new ObjectConverterWrapper<>(srcType, objectConverter);
            ExceptionHelper.checkNotNullArgument(destType, "destType");

            typeConverters.computeIfAbsent(destType, (key) -> new LinkedList<>()).add(rawConverter);
        }

        /**
         * Sets the {@link CustomStringParser} for the given destination type.
         */
        public <T> void setCustomStringParser(
            Class<T> destType,
            CustomStringParser<? extends T> customStringParser) {
            ExceptionHelper.checkNotNullArgument(destType, "destType");
            ExceptionHelper.checkNotNullArgument(customStringParser, "customStringParser");

            customStringParsers.put(destType, customStringParser);
        }

        public JsonDeserializer create() {
            return new JsonDeserializer(this);
        }
    }
}
