package de.plpt.ArgumentParser;

import java.lang.annotation.*;

//region ParameterInfo

/**
 * ParameterInfo annotation is a annotation for Method parameters
 * It represent parameter regex and integer ranges
 */
@Documented
@Target(ElementType.PARAMETER)
@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterInfo {

    /**
     * OPTIONAL: Regex Value of parameter.
     * Will me matched before Method is called
     *
     * @return string regex for Parameter
     */
    String regex() default "";

    /**
     * minValue of Integer range
     *
     * @return minValue of Integer range
     */
    int minValue() default Integer.MIN_VALUE;

    /**
     * maxValue of Integer range
     *
     * @return maxValue of Integer range
     */
    int maxValue() default Integer.MAX_VALUE;
}
//endregion

