package com.nfbsoftware.sansserverplugin.maven.amazon;

import java.util.Properties;

import org.apache.maven.plugin.logging.Log;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.cloudwatchevents.AmazonCloudWatchEventsClient;
import com.amazonaws.services.cloudwatchevents.model.PutRuleRequest;
import com.amazonaws.services.cloudwatchevents.model.PutRuleResult;
import com.nfbsoftware.sansserverplugin.sdk.util.Entity;

/**
 * 
 * @author Brendan Clemenzi
 */
public class AmazonCloudWatchEventUtility
{
    private Log m_logger;
    private AmazonCloudWatchEventsClient m_amazonCloudWatchEventsClient;
    private Properties m_properties;

    public AmazonCloudWatchEventUtility(Log logger, Properties properties)
    {
        // Save our logger
        m_logger = logger;
        m_properties = properties;
        
        String regionName = m_properties.getProperty(Entity.FrameworkProperties.AWS_REGION);
        String accessKey = m_properties.getProperty(Entity.FrameworkProperties.AWS_ACCESS_KEY);
        String secretKey = m_properties.getProperty(Entity.FrameworkProperties.AWS_SECRET_KEY);

        // Init our aws credentials
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        m_amazonCloudWatchEventsClient = new AmazonCloudWatchEventsClient(credentials);

        // Set our region
        m_amazonCloudWatchEventsClient.setRegion(Region.getRegion(Regions.fromName(regionName)));
    }
    
    /**
     * 
     * @param putRuleRequest
     * @throws Exception
     */
    public PutRuleResult createRule(PutRuleRequest putRuleRequest) throws Exception
    {
        PutRuleResult result = m_amazonCloudWatchEventsClient.putRule(putRuleRequest);
        
        if(result != null)
        {
            m_logger.info("CloudWatch Event (" + putRuleRequest.getName() + ") has been created with id: " + result.getRuleArn());
        }
        else
        {
            throw new Exception("Error creating Gateway API: " + putRuleRequest.getName());
        }
        
        return result;
    }
}
