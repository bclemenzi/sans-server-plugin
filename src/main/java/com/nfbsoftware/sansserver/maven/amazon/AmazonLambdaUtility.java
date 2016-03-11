package com.nfbsoftware.sansserver.maven.amazon;

import java.util.Properties;

import org.apache.maven.plugin.logging.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.lambda.AWSLambdaClient;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionResult;
import com.amazonaws.services.lambda.model.FunctionCode;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.GetFunctionRequest;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionCodeResult;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationResult;
import com.nfbsoftware.sansserver.sdk.util.Entity;

/**
 * 
 * @author brendanclemenzi
 */
public class AmazonLambdaUtility
{
    private Log m_logger;
    private AWSLambdaClient m_amazonLambdaClient;
    private Properties m_properties;

    public AmazonLambdaUtility(Log logger, Properties properties)
    {
        // Save our logger
        m_logger = logger;
        m_properties = properties;
        
        String regionName = m_properties.getProperty(Entity.FrameworkProperties.AWS_REGION);
        String accessKey = m_properties.getProperty(Entity.FrameworkProperties.AWS_ACCESS_KEY);
        String secretKey = m_properties.getProperty(Entity.FrameworkProperties.AWS_SECRET_KEY);
        
        // Init our aws credentials
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        m_amazonLambdaClient = new AWSLambdaClient(credentials);

        // Set our region
        m_amazonLambdaClient.setRegion(Region.getRegion(Regions.fromName(regionName)));
    }
    
    /**
     * 
     * @param getFunctionResult
     * @param updateFunctionConfigurationRequest
     * @return
     */
    public boolean hasFunctionConfigChanged(GetFunctionResult getFunctionResult, UpdateFunctionConfigurationRequest updateFunctionConfigurationRequest) 
    {
        FunctionConfiguration functionConfiguration = getFunctionResult.getConfiguration();
        
        if(functionConfiguration == null)
        {
            return false;
        }
        else
        {
            if(!functionConfiguration.getDescription().equalsIgnoreCase(updateFunctionConfigurationRequest.getDescription()))
            {
                return true;
            }
            if(!functionConfiguration.getHandler().equalsIgnoreCase(updateFunctionConfigurationRequest.getHandler()))
            {
                return true;
            }
            if(!functionConfiguration.getRole().equalsIgnoreCase(updateFunctionConfigurationRequest.getRole()))
            {
                return true;
            }
            if(functionConfiguration.getTimeout().compareTo(updateFunctionConfigurationRequest.getTimeout()) == 0)
            {
                return true;
            }
            if(functionConfiguration.getMemorySize().compareTo(updateFunctionConfigurationRequest.getMemorySize()) == 0)
            {
                return true;
            }
            
            return false;
        }
    }

    /**
     * 
     * @param functionName
     * @return
     */
    public GetFunctionResult getFunction(String functionName) 
    {
        GetFunctionRequest getFunctionRequest = new GetFunctionRequest();
        getFunctionRequest.setFunctionName(functionName);

        return m_amazonLambdaClient.getFunction(getFunctionRequest);
    }
    
    /**
     * 
     * @param updateFunctionConfigurationRequest
     * @throws Exception
     */
    public void updateFunctionConfiguration(UpdateFunctionConfigurationRequest updateFunctionConfigurationRequest) throws Exception
    {
        UpdateFunctionConfigurationResult result = m_amazonLambdaClient.updateFunctionConfiguration(updateFunctionConfigurationRequest);
                
        if(result != null)
        {
            m_logger.info("Lambda function (" + updateFunctionConfigurationRequest.getFunctionName() + ") configuration has been updated with ARN: " + result.getFunctionArn());
        }
        else
        {
            throw new Exception("Error updating Lambda function configuration: " + updateFunctionConfigurationRequest.getFunctionName());
        }
    }
    
    /**
     * 
     * @param deploymentJarFileName
     * @param functionName
     * @throws Exception
     */
    public void updateFunction(String deploymentJarFileName, String functionName) throws Exception
    {
        // Get our deployment bucket information
        String bucketName = m_properties.getProperty(Entity.FrameworkProperties.AWS_S3_BUCKET_NAME);
        String deploymentFolder = m_properties.getProperty(Entity.FrameworkProperties.AWS_S3_DEPLOYMENT_FOLDER);
        
        UpdateFunctionCodeRequest updateFunctionRequest = new UpdateFunctionCodeRequest();
        
        updateFunctionRequest.setS3Bucket(bucketName);
        updateFunctionRequest.setS3Key(deploymentFolder + "/" + deploymentJarFileName);
        updateFunctionRequest.setFunctionName(functionName);
        updateFunctionRequest.setPublish(Boolean.TRUE);
        
        UpdateFunctionCodeResult result = m_amazonLambdaClient.updateFunctionCode(updateFunctionRequest);
        
        if(result != null)
        {
            m_logger.info("Lambda function (" + updateFunctionRequest.getFunctionName() + ") has been updated with ARN: " + result.getFunctionArn());
        }
        else
        {
            throw new Exception("Error updating Lambda function: " + functionName);
        }
    }
    
    /**
     * 
     * @param deploymentJarFileName
     * @param createFunctionRequest
     * @return
     */
    public void createFunction(String deploymentJarFileName, CreateFunctionRequest createFunctionRequest) throws Exception
    {
        FunctionCode functionCode = new FunctionCode();
        
        // Get our deployment bucket information
        String bucketName = m_properties.getProperty(Entity.FrameworkProperties.AWS_S3_BUCKET_NAME);
        String deploymentFolder = m_properties.getProperty(Entity.FrameworkProperties.AWS_S3_DEPLOYMENT_FOLDER);
        
        functionCode.setS3Bucket(bucketName);
        functionCode.setS3Key(deploymentFolder + "/" + deploymentJarFileName);
        
        createFunctionRequest.setCode(functionCode);

        CreateFunctionResult result = m_amazonLambdaClient.createFunction(createFunctionRequest);
        
        if(result != null)
        {
            m_logger.info("Lambda function (" + createFunctionRequest.getFunctionName() + ") has been created with ARN: " + result.getFunctionArn());
        }
        else
        {
            throw new Exception("Error creating Lambda function: " + createFunctionRequest.getFunctionName());
        }
    }
}
