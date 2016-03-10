package com.nfbsoftware.sansserver.sdk.util;

/**
 * The Entity class is used to store static values of our various objects
 *
 * @author Brendan Clemenzi
 *
 */
public class Entity
{
    /**
     * The FrameworkProperties class is used to store known properties that will be loaded by the BaseLambdaHandler
     */
    public class FrameworkProperties
    {
        public static final String AWS_REGION                                           = "aws.region";
        public static final String AWS_ACCESS_KEY                                       = "aws.accessKey";
        public static final String AWS_SECRET_KEY                                       = "aws.secretKey";
        
        public static final String AWS_COGNITO_IDENTITY_POOL_ID                         = "aws.cognito.identityPoolId";
        public static final String AWS_CONGITO_PROVIDER_NAME                            = "aws.cognito.providerName";
        
        public static final String AWS_LAMBDA_FUNCTION_NAME_PREFIX                      = "aws.lambda.functionNamePrefix";
        public static final String AWS_LAMBDA_FUNCTION_MEMORY                           = "aws.lambda.functionMemory";
        public static final String AWS_LAMBDA_FUNCTION_TIMEOUT                          = "aws.lambda.functionTimeout";
        public static final String AWS_LAMBDA_FUNCTION_VPC                              = "aws.lambda.functionVPC";
        
        public static final String AWS_APIGATEWAY_DEVELOPMENT_STAGE                     = "aws.apigateway.deploymentStage";
        public static final String AWS_APIGATEWAY_DEVELOPMENT_DESC                      = "aws.apigateway.deploymentDescription";
        
        public static final String AWS_DYNAMODB_TABLE_NAME_PREFIX                       = "aws.dynamodb.tableNamePrefix";
        
        public static final String AWS_S3_BUCKET_NAME                                   = "aws.s3.bucketName";
        public static final String AWS_S3_DEPLOYMENT_FOLDER                             = "aws.s3.deploymentFolder";
    }
}
