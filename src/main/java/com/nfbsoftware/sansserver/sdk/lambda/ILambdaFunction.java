package com.nfbsoftware.sansserver.sdk.lambda;

import com.nfbsoftware.sansserver.sdk.lambda.model.HandlerResponse;

/**
 * The ILambdaFunction interface...
 * 
 * @author Brendan Clemenzi
 */
public interface ILambdaFunction
{
    public HandlerResponse processRequest() throws Exception;
}
