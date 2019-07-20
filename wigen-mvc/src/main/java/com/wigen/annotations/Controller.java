package com.wigen.annotations;

import java.lang.annotation.*;

/**
 * @Author wwq
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Controller {

    /**
     * controller 别名
     * @return
     */
    String value() default "";
}
