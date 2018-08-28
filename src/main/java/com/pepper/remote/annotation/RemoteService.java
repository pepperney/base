package com.pepper.remote.annotation;

import java.lang.annotation.*;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Inherited
public @interface RemoteService {
    Class<?> value();

    Class<?> serviceInterface() default Class.class;

    String name() default "";

    String path() default "/remoting";

    RemoteType type() default RemoteType.HTTP;
}
