package info.hearthsim.brazier.parsing;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Parameter;

/**
 * Util annotation for method's parameter to give out parameter's name during runtime.
 * Using annotated argument name in {@code .card} and {@code .power} files can invoke
 * {@code public static} methods from any {@code public} class to implement different cards.
 *
 * @see JsonDeserializer#getNoArgMethodObject(Class, String, TypeChecker)
 * @see JsonDeserializer#getParameterName(Parameter)
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.PARAMETER})
public @interface NamedArg {
    public String value();

    public String defaultValue() default "";
}
