package com.nfbsoftware.sansserverplugin.sdk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation class to allow use to configure Java-based Lambda functions
 * 
 * @author Brendan Clemenzi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AwsLambda 
{
    String name();
    String desc();
    String handlerMethod();
    String memorySize() default "128";
    String timeout() default "60";
}