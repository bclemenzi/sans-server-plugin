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
import com.amazonaws.services.simpleemail.AmazonSimpleEmailServiceClient;
import com.amazonaws.services.simpleemail.model.Body;
import com.amazonaws.services.simpleemail.model.Content;
import com.amazonaws.services.simpleemail.model.Destination;
import com.amazonaws.services.simpleemail.model.Message;
import com.amazonaws.services.simpleemail.model.SendEmailRequest;
import com.nfbsoftware.sansserverplugin.sdk.util.Entity;
import com.nfbsoftware.sansserverplugin.sdk.util.StringUtil;

/**
 * The AmazonSESManager...
 * 
 * @author Brendan Clemenzi
 */
public class AmazonSESManager
{
    private Log m_logger;
    
    private AmazonSimpleEmailServiceClient m_amazonSimpleEmailServiceClient;
    private Properties m_properties;
    
    /**
     * 
     * @param logger
     * @param properties
     */
    public AmazonSESManager(Properties properties)
    {
        m_logger = LogFactory.getLog(this.getClass());
        
        m_properties = properties;
        
        String regionName = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_REGION));
        String accessKey = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_ACCESS_KEY));
        String secretKey = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_SECRET_KEY));

        // Init our aws credentials
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        m_amazonSimpleEmailServiceClient = new AmazonSimpleEmailServiceClient(credentials);

        // Set our region
        m_amazonSimpleEmailServiceClient.setRegion(Region.getRegion(Regions.fromName(regionName)));
    }
    

    /**
     * 
     * @param toEmail
     * @param messageSubject
     * @param messageBody
     * @throws Exception
     */
    public void buildEmailMessage(String toEmail, String messageSubject, String messageBody) throws Exception
    {
        List<String> toAddresses = new ArrayList<String>();
        toAddresses.add(toEmail);
        
        buildEmailMessage(toAddresses, messageSubject, messageBody);
    }
    
    /**
     * 
     * @param toAddresses
     * @param messageSubject
     * @param messageBody
     * @throws Exception
     */
    public void buildEmailMessage(List<String> toAddresses, String messageSubject, String messageBody) throws Exception
    {
        String replyEmailAddress = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_SES_REPLY_EMAIL_ADDRESS));
        
        SendEmailRequest emailRequest = new SendEmailRequest();
        emailRequest.withSource(replyEmailAddress);
    
        Destination dest = new Destination().withToAddresses(toAddresses);
        emailRequest.setDestination(dest);

        Content subjContent = new Content().withData(messageSubject);
        Message msg = new Message().withSubject(subjContent);
    
        // Include a body in both text and HTML formats
        Content htmlContent = new Content().withData(messageBody);
        
        Body body = new Body().withHtml(htmlContent);
        msg.setBody(body);

        emailRequest.setMessage(msg);
        
        m_amazonSimpleEmailServiceClient.sendEmail(emailRequest);
    }
}
