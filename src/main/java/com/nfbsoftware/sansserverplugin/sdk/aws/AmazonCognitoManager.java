package com.nfbsoftware.sansserverplugin.sdk.aws;

import java.util.HashMap;
import java.util.Properties;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cognitoidentity.AmazonCognitoIdentityClient;
import com.amazonaws.services.cognitoidentity.model.GetOpenIdTokenForDeveloperIdentityRequest;
import com.amazonaws.services.cognitoidentity.model.GetOpenIdTokenForDeveloperIdentityResult;
import com.nfbsoftware.sansserverplugin.sdk.util.Entity;
import com.nfbsoftware.sansserverplugin.sdk.util.StringUtil;

/**
 * The AmazonCognitoManager is used to manage the connection to Amazon's Cognito service.  There are a handful of methods to make using the service a little easier.
 * 
 * @author Brendan Clemenzi
 */
public class AmazonCognitoManager
{
    private Properties m_properties;
    
    private AmazonCognitoIdentityClient m_amazonCognitoIdentityClient;
    
    /**
     * 
     * @param properties
     */
    public AmazonCognitoManager(Properties properties)
    {
        m_properties = properties;
        
        String regionName = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_REGION));
        String accessKey = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_ACCESS_KEY));
        String secretKey = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_SECRET_KEY));

        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        m_amazonCognitoIdentityClient = new AmazonCognitoIdentityClient(credentials);
        
        // Set our region
        m_amazonCognitoIdentityClient.setRegion(Region.getRegion(Regions.fromName(regionName)));
    }
    
    /**
     * 
     * @param userId
     * @return
     * @throws Exception
     */
    public GetOpenIdTokenForDeveloperIdentityResult getDeveloperIdentityResult(String userId)
    {
        String identityPoolId = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_COGNITO_IDENTITY_POOL_ID));
        String providerName = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_COGNITO_PROVIDER_NAME));
        
        GetOpenIdTokenForDeveloperIdentityRequest tokenRequest = new GetOpenIdTokenForDeveloperIdentityRequest();
        tokenRequest.setIdentityPoolId(identityPoolId);

        HashMap<String, String> map = new HashMap<String, String>();
        map.put(providerName, userId);

        tokenRequest.setLogins(map);
        tokenRequest.setTokenDuration(new Long(10001));
        
        return m_amazonCognitoIdentityClient.getOpenIdTokenForDeveloperIdentity(tokenRequest);
    }
}
