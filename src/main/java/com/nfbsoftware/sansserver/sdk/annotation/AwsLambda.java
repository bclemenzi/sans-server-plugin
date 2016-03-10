package com.nfbsoftware.sansserver.sdk.annotation;

/**
 * Annotation class to allow use to configure Java-based Lambda functions
 * 
 * @author Brendan Clemenzi
 */
public @interface AwsLambda 
{
    String desc();
    String apiResourceName();
    String apiResourcePath();
    String apiMethod();
    String apiSecurity();
    boolean enableCORS();
}

