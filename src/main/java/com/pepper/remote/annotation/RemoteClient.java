package com.pepper.remote.annotation;

import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;
/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Documented
public @interface RemoteClient {
    @AliasFor("url")
    String value() default "";

    @AliasFor("value")
    String url() default "";

    String name() default "";

    boolean loadBalanced() default false;

    RemoteType type() default RemoteType.HTTP;
}
