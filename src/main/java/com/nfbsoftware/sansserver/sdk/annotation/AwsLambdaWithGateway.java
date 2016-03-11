package com.nfbsoftware.sansserver.sdk.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation class to allow use to configure Java-based Lambda functions with an API Gateway
 *
 * @author Brendan Clemenzi
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AwsLambdaWithGateway
{
    /**
     * Define our Authorization types
     */
    public enum AuthorizationTypes
    {
        OPEN, KEY, AWS_IAM
    }

    /**
     * Define our method types
     */
    public enum MethodTypes
    {
        GET, POST, PUT, DELETE, PATCH, HEAD, OPTIONS
    }

    String name();
    String desc();
    String handlerMethod();
    String resourceName();

    MethodTypes method() default MethodTypes.POST;

    AuthorizationTypes authorization() default AuthorizationTypes.AWS_IAM;

    boolean keyRequired() default false;
    boolean enableCORS() default false;
}
