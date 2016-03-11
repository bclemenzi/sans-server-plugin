package com.nfbsoftware.sansserver.maven.amazon;

import java.util.List;
import java.util.Properties;

import org.apache.maven.plugin.logging.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.apigateway.AmazonApiGatewayClient;
import com.amazonaws.services.apigateway.model.CreateResourceRequest;
import com.amazonaws.services.apigateway.model.CreateResourceResult;
import com.amazonaws.services.apigateway.model.CreateRestApiRequest;
import com.amazonaws.services.apigateway.model.CreateRestApiResult;
import com.amazonaws.services.apigateway.model.GetResourceRequest;
import com.amazonaws.services.apigateway.model.GetResourceResult;
import com.amazonaws.services.apigateway.model.GetResourcesRequest;
import com.amazonaws.services.apigateway.model.GetResourcesResult;
import com.amazonaws.services.apigateway.model.GetRestApiRequest;
import com.amazonaws.services.apigateway.model.GetRestApiResult;
import com.amazonaws.services.apigateway.model.GetRestApisRequest;
import com.amazonaws.services.apigateway.model.GetRestApisResult;
import com.amazonaws.services.apigateway.model.Resource;
import com.amazonaws.services.apigateway.model.RestApi;
import com.amazonaws.services.apigateway.model.UpdateResourceRequest;
import com.amazonaws.services.apigateway.model.UpdateResourceResult;
import com.amazonaws.services.apigateway.model.UpdateRestApiRequest;
import com.amazonaws.services.apigateway.model.UpdateRestApiResult;
import com.nfbsoftware.sansserver.sdk.util.Entity;

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
            String tempName = restApi.getName();
            
            if(tempName.equalsIgnoreCase(restApiName))
            {
                restApiResult = getRestApi(restApi.getId());
            }
        }

        return restApiResult;
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
        GetResourceRequest getResourceRequest = new GetResourceRequest();
        getResourceRequest.setResourceId(resourceId);
        
        return m_amazonApiGatewayClient.getResource(getResourceRequest);
    }
    
    /**
     * 
     * @param restApiId
     * @param pathPart
     * @return
     * @throws Exception
     */
    public GetResourceResult getResourceByPathPart(String restApiId, String pathPart) throws Exception
    {
        GetResourceResult getResourceResult = null;
        
        GetResourcesRequest getResoursesRequest = new GetResourcesRequest();
        getResoursesRequest.setRestApiId(restApiId);
        
        GetResourcesResult result = m_amazonApiGatewayClient.getResources(getResoursesRequest);
        
        List<Resource> resources = result.getItems();
        
        for(Resource resource : resources)
        {
            String tempPathPart = resource.getPathPart();
            
            if(tempPathPart.equalsIgnoreCase(pathPart))
            {
                getResourceResult = getResource(resource.getId());
            }
        }

        return getResourceResult;
    }
    
    /**
     * 
     * @param createResourceRequest
     * @throws Exception
     */
    public void createResource(CreateResourceRequest createResourceRequest) throws Exception
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
}
