package com.wigen.annotations;

import java.lang.annotation.*;

/**
 * @Author wwq
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RequestMapping {

    /**
     * value
     * @return
     */
    String value() default "";
}
