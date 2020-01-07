package com.small.job.core.annotation;

import java.lang.annotation.*;

/***
 * 注解在方法上
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
public @interface SmallJob {

    /**
     * jobhandler name
     */
    String value() default "";

    /**
     * init handler, invoked when JobThread init
     */
    String init() default "";

    /**
     * destroy handler, invoked when JobThread destroy
     */
    String destroy() default "";
}
