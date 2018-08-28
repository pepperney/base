package com.pepper.remote.annotation;

import com.pepper.remote.RemoteServiceScannerRegister;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @Auther: pei.nie
 * @Date:2018-07-2018/7/27
 * @Description:
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
@Import(RemoteServiceScannerRegister.class)
public @interface RemoteServiceScan {
    String[] value() default {};
}