package com.wigen.annotations;

import java.lang.annotation.*;

/**
 * @Author wwq
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestParam {

    String value() default "";
}
