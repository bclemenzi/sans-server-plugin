package com.nfbsoftware.sansserverplugin.sdk.util;

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
        public static final String ENVIRONEMNT_PREFIX                   = "environment.namePrefix";
        
        public static final String AWS_REGION                           = "aws.region";
        public static final String AWS_ACCESS_KEY                       = "aws.accessKey";
        public static final String AWS_SECRET_KEY                       = "aws.secretKey";
        
        public static final String AWS_COGNITO_IDENTITY_POOL_ID         = "aws.cognito.identityPoolId";
        public static final String AWS_COGNITO_PROVIDER_NAME            = "aws.cognito.providerName";
        
        public static final String AWS_LAMBDA_ROLE_ARN                  = "aws.lambda.roleArn";
        
        public static final String AWS_APIGATEWAY_DEVELOPMENT_STAGE     = "aws.apigateway.deploymentStage";
        public static final String AWS_APIGATEWAY_DEVELOPMENT_DESC      = "aws.apigateway.deploymentDescription";
        
        public static final String AWS_DYNAMODB_TABLE_NAME_PREFIX       = "aws.dynamodb.tableNamePrefix";
        
        public static final String AWS_S3_BUCKET_NAME                   = "aws.s3.bucketName";
        public static final String AWS_S3_DEPLOYMENT_FOLDER             = "aws.s3.deploymentFolder";
        
        public static final String AWS_SES_REPLY_EMAIL_ADDRESS          = "aws.ses.replyEmailAddress";
    }
}
