package com.nfbsoftware.sansserverplugin.maven.amazon;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGatewayClient;
import com.amazonaws.services.apigateway.model.ApiKey;
import com.amazonaws.services.apigateway.model.CreateApiKeyRequest;
import com.amazonaws.services.apigateway.model.CreateApiKeyResult;
import com.amazonaws.services.apigateway.model.CreateDeploymentRequest;
import com.amazonaws.services.apigateway.model.CreateDeploymentResult;
import com.amazonaws.services.apigateway.model.CreateResourceRequest;
import com.amazonaws.services.apigateway.model.CreateResourceResult;
import com.amazonaws.services.apigateway.model.CreateRestApiRequest;
import com.amazonaws.services.apigateway.model.CreateRestApiResult;
import com.amazonaws.services.apigateway.model.DeleteApiKeyRequest;
import com.amazonaws.services.apigateway.model.DeleteResourceRequest;
import com.amazonaws.services.apigateway.model.GetApiKeyRequest;
import com.amazonaws.services.apigateway.model.GetApiKeyResult;
import com.amazonaws.services.apigateway.model.GetApiKeysRequest;
import com.amazonaws.services.apigateway.model.GetApiKeysResult;
import com.amazonaws.services.apigateway.model.GetIntegrationRequest;
import com.amazonaws.services.apigateway.model.GetIntegrationResult;
import com.amazonaws.services.apigateway.model.GetMethodRequest;
import com.amazonaws.services.apigateway.model.GetMethodResult;
import com.amazonaws.services.apigateway.model.GetResourceRequest;
import com.amazonaws.services.apigateway.model.GetResourceResult;
import com.amazonaws.services.apigateway.model.GetResourcesRequest;
import com.amazonaws.services.apigateway.model.GetResourcesResult;
import com.amazonaws.services.apigateway.model.GetRestApiRequest;
import com.amazonaws.services.apigateway.model.GetRestApiResult;
import com.amazonaws.services.apigateway.model.GetRestApisRequest;
import com.amazonaws.services.apigateway.model.GetRestApisResult;
import com.amazonaws.services.apigateway.model.PutIntegrationRequest;
import com.amazonaws.services.apigateway.model.PutIntegrationResponseRequest;
import com.amazonaws.services.apigateway.model.PutIntegrationResponseResult;
import com.amazonaws.services.apigateway.model.PutIntegrationResult;
import com.amazonaws.services.apigateway.model.PutMethodRequest;
import com.amazonaws.services.apigateway.model.PutMethodResponseRequest;
import com.amazonaws.services.apigateway.model.PutMethodResponseResult;
import com.amazonaws.services.apigateway.model.PutMethodResult;
import com.amazonaws.services.apigateway.model.Resource;
import com.amazonaws.services.apigateway.model.RestApi;
import com.amazonaws.services.apigateway.model.UpdateIntegrationRequest;
import com.amazonaws.services.apigateway.model.UpdateIntegrationResult;
import com.amazonaws.services.apigateway.model.UpdateMethodRequest;
import com.amazonaws.services.apigateway.model.UpdateMethodResult;
import com.amazonaws.services.apigateway.model.UpdateResourceRequest;
import com.amazonaws.services.apigateway.model.UpdateResourceResult;
import com.amazonaws.services.apigateway.model.UpdateRestApiRequest;
import com.amazonaws.services.apigateway.model.UpdateRestApiResult;
import com.nfbsoftware.sansserverplugin.sdk.util.Entity;
import com.nfbsoftware.sansserverplugin.sdk.util.StringUtil;

/**
 * 
 * @author Brendan Clemenzi
 */
public class AmazonGatewayUtility
{
    private Log m_logger;
    private AmazonApiGatewayClient m_amazonApiGatewayClient;
    private Properties m_properties;

    public AmazonGatewayUtility(Log logger, Properties properties)
    {
        // Save our logger
        m_logger = logger;
        m_properties = properties;
        
        String regionName = m_properties.getProperty(Entity.FrameworkProperties.AWS_REGION);
        String accessKey = m_properties.getProperty(Entity.FrameworkProperties.AWS_ACCESS_KEY);
        String secretKey = m_properties.getProperty(Entity.FrameworkProperties.AWS_SECRET_KEY);

        // Init our aws credentials
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        m_amazonApiGatewayClient = new AmazonApiGatewayClient(credentials);

        // Set our region
        m_amazonApiGatewayClient.setRegion(Region.getRegion(Regions.fromName(regionName)));
    }
    
    /**
     * 
     * @param restApiName
     * @return
     */
    public String getRestApiInvokeUrl(String restApiName)
    {
        String invokeUrl = "";

        GetRestApiResult getRestApiResult = getRestApiByName(restApiName);
        
        if(getRestApiResult != null)
        {
            String regionName = m_properties.getProperty(Entity.FrameworkProperties.AWS_REGION);
            String stateName = m_properties.getProperty(Entity.FrameworkProperties.ENVIRONEMNT_STAGE);
            
            invokeUrl = "https://" + getRestApiResult.getId() + ".execute-api." + regionName + ".amazonaws.com/" + stateName;
        }
        
        return invokeUrl;
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
     * @param restApiName
     * @return
     */
    public GetResourceResult getRestApiRootResource(String restApiId) throws Exception
    {
        GetResourceResult getResourceResult = null;
        GetRestApiResult restApiResult = getRestApi(restApiId);
        
        if(restApiResult != null)
        {
            GetResourcesRequest getResoursesRequest = new GetResourcesRequest();
            getResoursesRequest.setRestApiId(restApiResult.getId());
            
            GetResourcesResult result = m_amazonApiGatewayClient.getResources(getResoursesRequest);
            
            List<Resource> resources = result.getItems();
            
            for(Resource resource : resources)
            {
                String tempPath = StringUtil.emptyIfNull(resource.getPath());
                m_logger.info("getRestApiRootResource tempPath: " + tempPath + "  " + resource.toString());
                
                if(tempPath.equalsIgnoreCase("/"))
                {
                    m_logger.info("getRestApiRootResource tempPath found: " + resource.getId());
                    getResourceResult = getResource(resource.getId());
                }
            }
        }
        
        return getResourceResult;
    }
    
    /**
     * 
     * @param updateRestApiRequest
     * @throws Exception
     */
    public void updateRestApi(UpdateRestApiRequest updateRestApiRequest) throws Exception
    {
        UpdateRestApiResult result = m_amazonApiGatewayClient.updateRestApi(updateRestApiRequest);
        
        if(result != null)
        {
            m_logger.info("Gateway API (" + updateRestApiRequest.getRestApiId() + ") has been updated.");
        }
        else
        {
            throw new Exception("Error creating Gateway API: " + updateRestApiRequest.getRestApiId());
        }
    }

    /**
     * 
     * @param createRestApiRequest
     * @throws Exception
     */
    public void createRestApi(CreateRestApiRequest createRestApiRequest) throws Exception
    {
        CreateRestApiResult result = m_amazonApiGatewayClient.createRestApi(createRestApiRequest);
        
        if(result != null)
        {
            m_logger.info("Gateway API (" + createRestApiRequest.getName() + ") has been created with id: " + result.getId());
        }
        else
        {
            throw new Exception("Error creating Gateway API: " + createRestApiRequest.getName());
        }
    }
    
    /**
     * 
     * @param resourceId
     * @throws Exception
     */
    public GetResourceResult getResource(String resourceId) throws Exception
    {
        GetResourceResult getResourceResult = null;
        
        try
        {
            GetResourceRequest getResourceRequest = new GetResourceRequest();
            getResourceRequest.setResourceId(resourceId);
            
            getResourceResult = m_amazonApiGatewayClient.getResource(getResourceRequest);
        }
        catch (Exception e)
        {
            m_logger.error("Error loading resource: " + resourceId);
            
            e.printStackTrace();
        }
        
        return getResourceResult;
    }
    
    /**
     * 
     * @param restApiId
     * @param pathPart
     * @return
     * @throws Exception
     */
    public Resource getResourceByPath(String restApiId, String path) throws Exception
    {
        Resource resourceObject = null;
        
        GetResourcesRequest getResoursesRequest = new GetResourcesRequest();
        getResoursesRequest.setRestApiId(restApiId);
        
        GetResourcesResult result = m_amazonApiGatewayClient.getResources(getResoursesRequest);
        
        List<Resource> resources = result.getItems();
        
        for(Resource resource : resources)
        {
            String tempPath = StringUtil.emptyIfNull(resource.getPath());
            m_logger.info("tempPath: " + tempPath + "   " + resource.toString());
            
            if(tempPath.equalsIgnoreCase(path))
            {
                m_logger.info("Resource found by path (" + path + "): " + resource.getId());
                resourceObject = resource;
            }
        }

        return resourceObject;
    }
    
    /**
     * 
     * @param restApiId
     * @return
     */
    public List<Resource> getResources(String restApiId)
    {
        GetResourcesRequest getResoursesRequest = new GetResourcesRequest();
        getResoursesRequest.setRestApiId(restApiId);
        
        GetResourcesResult result = m_amazonApiGatewayClient.getResources(getResoursesRequest);
        
        return result.getItems();
    }
    
    /**
     * 
     * @param restApiId
     * @param resourceId
     */
    public void deleteResource(String restApiId, String resourceId)
    {
        DeleteResourceRequest deleteResourceRequest = new DeleteResourceRequest();
        deleteResourceRequest.setRestApiId(restApiId);
        deleteResourceRequest.setResourceId(resourceId);
        
        m_amazonApiGatewayClient.deleteResource(deleteResourceRequest);
    }
    
    /**
     * 
     * @param restApiId
     * @param pathPart
     * @return
     * @throws Exception
     */
    public Resource getResourceByPathPart(String restApiId, String pathPart) throws Exception
    {
        Resource resourceObject = null;

        List<Resource> resources = getResources(restApiId);
        
        for(Resource resource : resources)
        {
            String tempPathPart = StringUtil.emptyIfNull(resource.getPathPart());
            m_logger.info("tempPathPart: " + tempPathPart + "   " + resource.toString());
            
            if(tempPathPart.equalsIgnoreCase(pathPart))
            {
                resourceObject = resource;
            }
        }

        return resourceObject;
    }
    
    /**
     * 
     * @param createResourceRequest
     * @throws Exception
     */
    public CreateResourceResult createResource(CreateResourceRequest createResourceRequest) throws Exception
    {
        CreateResourceResult result = m_amazonApiGatewayClient.createResource(createResourceRequest);
        
        if(result != null)
        {
            m_logger.info("Gateway API Resource (" + createResourceRequest.getPathPart() + ") has been created with id: " + result.getId());
        }
        else
        {
            throw new Exception("Error creating Gateway API Resource: " + createResourceRequest.getPathPart());
        }
        
        return result;
    }
    
    /**
     * 
     * @param updateResourceRequest
     * @throws Exception
     */
    public void updateResource(UpdateResourceRequest updateResourceRequest) throws Exception
    {
        UpdateResourceResult result = m_amazonApiGatewayClient.updateResource(updateResourceRequest);
        
        if(result != null)
        {
            m_logger.info("Gateway API Resource (" + updateResourceRequest.getResourceId() + ") has been updated");
        }
        else
        {
            throw new Exception("Error updating Gateway API Resource: " + updateResourceRequest.getResourceId());
        }
    }
    
    /**
     * 
     * @param getMethodRequest
     * @return
     * @throws Exception
     */
    public GetMethodResult getMethod(GetMethodRequest getMethodRequest) throws Exception
    {
        GetMethodResult getMethodResult = null;
        
        try
        {
            getMethodResult = m_amazonApiGatewayClient.getMethod(getMethodRequest);
        }
        catch (Exception e)
        {
            m_logger.info("Gateway API Resource Method (" + getMethodRequest.getHttpMethod() + ") not found");
        }
        
        return getMethodResult;
    }
    
    /**
     * 
     * @param putMethodRequest
     * @return
     * @throws Exception
     */
    public PutMethodResult createMethod(PutMethodRequest putMethodRequest) throws Exception
    {
        PutMethodResult putMethodResult = m_amazonApiGatewayClient.putMethod(putMethodRequest);
        
        if(putMethodResult != null)
        {
            m_logger.info("Gateway API Resource Method (" + putMethodRequest.getHttpMethod() + ") has been created");
        }
        else
        {
            throw new Exception("Error creating Gateway API Resource Method: " + putMethodRequest.getHttpMethod());
        }
        
        return putMethodResult;
    }
    
    /**
     * 
     * @param updateMethodRequest
     * @return
     * @throws Exception
     */
    public UpdateMethodResult updateMethod(UpdateMethodRequest updateMethodRequest) throws Exception
    {
        UpdateMethodResult updateMethodResult = m_amazonApiGatewayClient.updateMethod(updateMethodRequest);
        
        if(updateMethodResult != null)
        {
            m_logger.info("Gateway API Resource Method (" + updateMethodRequest.getHttpMethod() + ") has been updated");
        }
        else
        {
            throw new Exception("Error updating Gateway API Resource Method: " + updateMethodRequest.getHttpMethod());
        }
        
        return updateMethodResult;
    }
    
    
    /**
     * 
     * @param putMethodResponseRequest
     * @return
     * @throws Exception
     */
    public PutMethodResponseResult createMethodResponse(PutMethodResponseRequest putMethodResponseRequest) throws Exception
    {
        PutMethodResponseResult putMethodResponseResult = m_amazonApiGatewayClient.putMethodResponse(putMethodResponseRequest);
        
        if(putMethodResponseResult != null)
        {
            m_logger.info("Gateway API Resource Method Response (" + putMethodResponseRequest.getResourceId() + ") has been created");
        }
        else
        {
            throw new Exception("Error creating Gateway API Resource Method Response: " + putMethodResponseRequest.getResourceId());
        }
        
        return putMethodResponseResult;
    }
    
    /**
     * 
     * @param getIntegrationRequest
     * @return
     * @throws Exception
     */
    public GetIntegrationResult getIntegrationt(GetIntegrationRequest getIntegrationRequest) throws Exception
    {
        GetIntegrationResult getIntegrationResult = null;
        
        try
        {
            getIntegrationResult = m_amazonApiGatewayClient.getIntegration(getIntegrationRequest);
        }
        catch (Exception e)
        {
            m_logger.info("Gateway API Resource Method Integration Request(" + getIntegrationRequest.getHttpMethod() + ") not found");
        }
        
        return getIntegrationResult;
    }
    
    /**
     * 
     * @param putIntegrationRequest
     * @return
     * @throws Exception
     */
    public PutIntegrationResult createIntegration(PutIntegrationRequest putIntegrationRequest) throws Exception
    {
        PutIntegrationResult putIntegrationResult = m_amazonApiGatewayClient.putIntegration(putIntegrationRequest);
        
        if(putIntegrationResult != null)
        {
            m_logger.info("Gateway API Resource Method Integration (" + putIntegrationRequest.getUri() + ") has been created");
        }
        else
        {
            throw new Exception("Error creating Gateway API Resource Method Integration: " + putIntegrationRequest.getUri());
        }
        
        return putIntegrationResult;
    }
    
    /**
     * 
     * @param updateIntegrationRequest
     * @return
     * @throws Exception
     */
    public UpdateIntegrationResult updateIntegration(UpdateIntegrationRequest updateIntegrationRequest) throws Exception
    {
        UpdateIntegrationResult updateIntegrationResult = m_amazonApiGatewayClient.updateIntegration(updateIntegrationRequest);
        
        if(updateIntegrationResult != null)
        {
            m_logger.info("Gateway API Resource Method Integration (" + updateIntegrationRequest.getHttpMethod() + ") has been updated");
        }
        else
        {
            throw new Exception("Error updating Gateway API Resource Method Integration: " + updateIntegrationRequest.getHttpMethod());
        }
        
        return updateIntegrationResult;
    }
    
    /**
     * 
     * @param putIntegrationResponseRequest
     * @return
     * @throws Exception
     */
    public PutIntegrationResponseResult createIntegrationResponse(PutIntegrationResponseRequest putIntegrationResponseRequest) throws Exception
    {
        PutIntegrationResponseResult putIntegrationResponseResult = m_amazonApiGatewayClient.putIntegrationResponse(putIntegrationResponseRequest);
        
        if(putIntegrationResponseResult != null)
        {
            m_logger.info("Gateway API Resource Method Integration Response (" + putIntegrationResponseResult.getStatusCode() + ") has been created");
        }
        else
        {
            throw new Exception("Error creating Gateway API Resource Method Integration Response: " + putIntegrationResponseRequest.getStatusCode());
        }
        
        return putIntegrationResponseResult;
    }
    
    /**
     * 
     * @param createDeploymentRequest
     * @return
     * @throws Exception
     */
    public CreateDeploymentResult createDeployment(CreateDeploymentRequest createDeploymentRequest) throws Exception
    {
        CreateDeploymentResult createDeploymentResult = m_amazonApiGatewayClient.createDeployment(createDeploymentRequest);
        
        if(createDeploymentResult != null)
        {
            m_logger.info("Gateway API Deployment Stage (" + createDeploymentRequest.getStageName() + ") has been created");
        }
        else
        {
            throw new Exception("Error creating Gateway API Deployment Stage: " + createDeploymentRequest.getStageName());
        }
        
        return createDeploymentResult;
    }
    
    /**
     * 
     * @param getApiKeysRequest
     * @return
     * @throws Exception
     */
    public GetApiKeysResult getApiKeysResult(GetApiKeysRequest getApiKeysRequest) throws Exception
    {
        GetApiKeysResult getApiKeysResult = null;
        
        try
        {
            getApiKeysResult = m_amazonApiGatewayClient.getApiKeys(getApiKeysRequest);
        }
        catch (Exception e)
        {
            m_logger.info("Gateway API Keys not found");
        }
        
        return getApiKeysResult;
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
        
        try
        {
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
        }
        catch (Exception e)
        {
            m_logger.info("Gateway API Keys not found");
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
        CreateApiKeyResult createApiKeyResult = null;
        
        try
        {
            createApiKeyResult = m_amazonApiGatewayClient.createApiKey(createApiKeyRequest);
        }
        catch (Exception e)
        {
            m_logger.info("Gateway API Key not created");
        }
        
        return createApiKeyResult;
    }
    
    /**
     * 
     * @param apiKey
     * @return
     * @throws Exception
     */
    public void deleteApiKey(String apiKey) throws Exception
    {
        try
        {
            DeleteApiKeyRequest deleteApiKeyRequest = new DeleteApiKeyRequest();
            deleteApiKeyRequest.setApiKey(apiKey);
            
            m_amazonApiGatewayClient.deleteApiKey(deleteApiKeyRequest);
        }
        catch (Exception e)
        {
            m_logger.info("Gateway API Key not deleted");
        }
    }
    
    /**
     * 
     * @param apiKey
     * @return
     * @throws Exception
     */
    public GetApiKeyResult getApiKey(String apiKey) throws Exception
    {
        GetApiKeyResult getApiKeyResult = null;
        
        try
        {
            GetApiKeyRequest getApiKeyRequest = new GetApiKeyRequest();
            getApiKeyRequest.setApiKey(apiKey);
            
            getApiKeyResult = m_amazonApiGatewayClient.getApiKey(getApiKeyRequest);
        }
        catch (Exception e)
        {
            m_logger.info("Gateway API Key not found");
        }
        
        return getApiKeyResult;
    }
}
