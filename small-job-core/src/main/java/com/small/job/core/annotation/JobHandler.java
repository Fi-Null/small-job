package com.small.job.core.annotation;

import java.lang.annotation.*;

/**
 * 注解在类上
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface JobHandler {

    String value() default "";

}
