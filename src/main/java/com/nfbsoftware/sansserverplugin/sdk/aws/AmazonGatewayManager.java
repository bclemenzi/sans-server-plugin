package com.nfbsoftware.sansserverplugin.sdk.aws;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGatewayClient;
import com.amazonaws.services.apigateway.model.ApiKey;
import com.amazonaws.services.apigateway.model.CreateApiKeyRequest;
import com.amazonaws.services.apigateway.model.CreateApiKeyResult;
import com.amazonaws.services.apigateway.model.DeleteApiKeyRequest;
import com.amazonaws.services.apigateway.model.GetApiKeyRequest;
import com.amazonaws.services.apigateway.model.GetApiKeyResult;
import com.amazonaws.services.apigateway.model.GetApiKeysRequest;
import com.amazonaws.services.apigateway.model.GetApiKeysResult;
import com.amazonaws.services.apigateway.model.GetRestApiRequest;
import com.amazonaws.services.apigateway.model.GetRestApiResult;
import com.amazonaws.services.apigateway.model.GetRestApisRequest;
import com.amazonaws.services.apigateway.model.GetRestApisResult;
import com.amazonaws.services.apigateway.model.RestApi;
import com.nfbsoftware.sansserverplugin.sdk.util.Entity;
import com.nfbsoftware.sansserverplugin.sdk.util.StringUtil;

/**
 * The AmazonGatewayManager is used to manage the connection to Amazon's Gateway API service.  There are a handful of methods to make using the service a little easier.
 * 
 * @author Brendan Clemenzi
 */
public class AmazonGatewayManager
{
    private Log m_logger;
    private Properties m_properties;
    private AmazonApiGatewayClient m_amazonApiGatewayClient;
    
    /**
     * 
     * @param properties
     */
    public AmazonGatewayManager(Properties properties)
    {
        m_logger = LogFactory.getLog(this.getClass());
        
        m_properties = properties;
        
        String regionName = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_REGION));
        String accessKey = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_ACCESS_KEY));
        String secretKey = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_SECRET_KEY));

        // Init our aws credentials
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        m_amazonApiGatewayClient = new AmazonApiGatewayClient(credentials);

        // Set our region
        m_amazonApiGatewayClient.setRegion(Region.getRegion(Regions.fromName(regionName)));
    }
    
    /**
     * 
     * @param restApiId
     * @return
     */
    public GetRestApiResult getRestApi(String restApiId) 
    {
        GetRestApiResult getRestApiResult = null;
        
        try
        {
            GetRestApiRequest getRestApiRequest = new GetRestApiRequest();
            getRestApiRequest.setRestApiId(restApiId);

            getRestApiResult =  m_amazonApiGatewayClient.getRestApi(getRestApiRequest);
        }
        catch (Exception e)
        {
            // Do nothing
        }
        
        return getRestApiResult;
    }

    /**
     * 
     * @param restApiName
     * @return
     */
    public GetRestApiResult getRestApiByName(String restApiName) 
    {
        GetRestApiResult restApiResult = null;
        
        GetRestApisRequest getRestApisRequest = new GetRestApisRequest();
        GetRestApisResult result = m_amazonApiGatewayClient.getRestApis(getRestApisRequest);
        
        List<RestApi> restApis = result.getItems();
        
        for(RestApi restApi : restApis)
        {
            String tempApiName = StringUtil.emptyIfNull(restApi.getName());
            m_logger.info("tempApiName: " + tempApiName + "  " + restApi.toString());
            
            if(tempApiName.equalsIgnoreCase(restApiName))
            {
                m_logger.info("RestAPI Found: " + restApi.getId());
                restApiResult = getRestApi(restApi.getId());
            }
        }

        return restApiResult;
    }

    /**
     * 
     * @param getApiKeysRequest
     * @return
     * @throws Exception
     */
    public GetApiKeysResult getApiKeysResult(GetApiKeysRequest getApiKeysRequest) throws Exception
    {
        return m_amazonApiGatewayClient.getApiKeys(getApiKeysRequest);
    }
    
    /**
     * 
     * @param apiKeyPrefix
     * @return
     * @throws Exception
     */
    public List<ApiKey> getApiKeysByPrefix(String apiKeyPrefix) throws Exception
    {
        List<ApiKey> apiKeys = new ArrayList<ApiKey>();

        GetApiKeysRequest getApiKeysRequest = new GetApiKeysRequest();
        
        GetApiKeysResult results = m_amazonApiGatewayClient.getApiKeys(getApiKeysRequest);
        
        if(results != null)
        {
            for(ApiKey apiKey : results.getItems())
            {
                if(apiKey.getName().startsWith(apiKeyPrefix))
                {
                    apiKeys.add(apiKey);
                }
            }
        }
        
        return apiKeys;
    }
    
    /**
     * 
     * @param createApiKeyRequest
     * @return
     * @throws Exception
     */
    public CreateApiKeyResult createApiKey(CreateApiKeyRequest createApiKeyRequest) throws Exception
    {
        return m_amazonApiGatewayClient.createApiKey(createApiKeyRequest);
    }
    
    /**
     * 
     * @param apiKey
     * @throws Exception
     */
    public void deleteApiKey(String apiKey) throws Exception
    {
        DeleteApiKeyRequest deleteApiKeyRequest = new DeleteApiKeyRequest();
        deleteApiKeyRequest.setApiKey(apiKey);
        
        m_amazonApiGatewayClient.deleteApiKey(deleteApiKeyRequest);
    }
    
    /**
     * 
     * @param apiKey
     * @return
     * @throws Exception
     */
    public GetApiKeyResult getApiKey(String apiKey) throws Exception
    {
        GetApiKeyRequest getApiKeyRequest = new GetApiKeyRequest();
        getApiKeyRequest.setApiKey(apiKey);
        
        return m_amazonApiGatewayClient.getApiKey(getApiKeyRequest);
    }
}
