package info.hearthsim.brazier.parsing;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;

import org.jtrim.utils.ExceptionHelper;

public final class TypeCheckers {
    private static Class<?>[] toClasses(Type[] types) {
        Class<?>[] result = new Class<?>[types.length];
        for (int i = 0; i < result.length; i++) {
            Type type = types[i];
            if (type instanceof Class) {
                result[i] = (Class<?>) type;
            } else {
                return null;
            }
        }
        return result;
    }

    /**
     * Checks if it is assignable from the source type to the destination type, that is, checks if the class or interface
     * represented by the destination type is either the same as, or is a superclass or superinterface of, the class
     * or interface represented by the source type.
     *
     * @param srcType the given source type.
     * @param destType the given destination type.
     * @throws ObjectParsingException if it is not assignable from the source type to the destination type.
     */
    private static void checkAssignable(Class<?> srcType, Type destType) throws ObjectParsingException {
        if (destType instanceof Class) {
            if (!((Class<?>) destType).isAssignableFrom(srcType)) {
                throw new ObjectParsingException("Not assignable. From: " + srcType.getName() + ". To: " + destType.getTypeName());
            }
            return;
        }

        if (destType instanceof TypeVariable) {
            TypeVariable<?> destTypeVar = (TypeVariable<?>) destType;
            Class<?>[] bounds = toClasses(destTypeVar.getBounds());
            if (bounds != null) {
                for (Class<?> bound : bounds) {
                    if (!bound.isAssignableFrom(srcType)) {
                        throw new ObjectParsingException("Not assignable. From: " + srcType.getName() + ". To: " + destType.getTypeName());
                    }
                }
                return;
            }
        }

        throw new ObjectParsingException("Unsupported declaration type: " + destType.getClass().getName());
    }

    /**
     * Returns a {@link TypeChecker} which checks if it is assignable from the actual type arguments of the testing
     * generic type to the given argument types when the raw type of the testing generic type equals to the given
     * raw type.
     *
     * @param rawType the given raw type.
     * @param argumentTypes the given argument types.
     *
     * @throws NullPointerException if any of the parameters is {@code null}.
     */
    public static TypeChecker genericTypeChecker(Class rawType, Class... argumentTypes) {
        ExceptionHelper.checkNotNullArgument(rawType, "rawType");
        ExceptionHelper.checkNotNullElements(argumentTypes, "argumentTypes");

        Class[] argTypesCopy = argumentTypes.clone();

        return (Type type) -> {
            if (type instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) type;
                if (pType.getRawType() == rawType) {
                    Type[] typeArgs = pType.getActualTypeArguments();
                    if (typeArgs.length != argumentTypes.length) {
                        throw new ObjectParsingException(rawType.getName() +
                            " has an unexpected number of type arguments: "
                            + Arrays.toString(typeArgs));
                    }

                    for (int i = 0; i < argTypesCopy.length; i++) {
                        checkAssignable(argTypesCopy[i], typeArgs[i]);
                    }
                }
            }
        };
    }

    private TypeCheckers() {
        throw new AssertionError();
    }
}
