package com.nfbsoftware.sansserverplugin.sdk.lambda;

import com.nfbsoftware.sansserverplugin.sdk.lambda.model.HandlerResponse;

/**
 * The ILambdaFunction interface...
 * 
 * @author Brendan Clemenzi
 */
public interface ILambdaFunction
{
    public HandlerResponse processRequest() throws Exception;
}
