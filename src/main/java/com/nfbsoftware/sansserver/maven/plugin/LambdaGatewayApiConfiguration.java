package com.nfbsoftware.sansserver.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.nfbsoftware.sansserver.maven.amazon.AmazonS3Impl;
import com.nfbsoftware.sansserver.sdk.util.Entity;

/**
 * Goal which configures and deploys our Lambda functions with API Gateway endpoints.
 * 
 * @goal deploy-lambda-gateway
 * @phase install
 */
public class LambdaGatewayApiConfiguration extends AbstractMojo
{
    private Log m_logger;
    private AmazonS3Impl m_amazonS3Impl;
    private Properties m_properties = new Properties();
    
    /**
     * Location of the file.
     * 
     * @parameter expression="${project.build.directory}"
     * @required
     */
    private File outputDirectory;
    
    /**
     * Location of the file.
     * 
     * @parameter expression="${project.name}"
     * @required
     */
    private String projectName;

    /**
     * Location of the file.
     * 
     * @parameter expression="${project.version}"
     * @required
     */
    private String projectVersion;
    
    /**
     * The main execution method for the plugin.
     */
    public void execute() throws MojoExecutionException
    {
        try
        {
            // Grab a handle to our logger
            m_logger = getLog();
            
            m_logger.info("Loading SansServer lambda.properties file");
            File propertiesFile = new File(outputDirectory.getAbsolutePath() + "/classes/lambda.properties");
            InputStream inStream = new FileInputStream(propertiesFile);
            m_properties.load(inStream);
            
            m_logger.info("Loading properties");
            String region = m_properties.getProperty(Entity.FrameworkProperties.AWS_REGION);
            String accessKey = m_properties.getProperty(Entity.FrameworkProperties.AWS_ACCESS_KEY);
            String secretKey = m_properties.getProperty(Entity.FrameworkProperties.AWS_SECRET_KEY);
            String bucketName = m_properties.getProperty(Entity.FrameworkProperties.AWS_S3_BUCKET_NAME);
            String deploymentFolder = m_properties.getProperty(Entity.FrameworkProperties.AWS_S3_DEPLOYMENT_FOLDER);
            
            m_logger.info("Initializing AWS S3");
            
            m_logger.info("Initializing AmazonS3Impl");
            m_amazonS3Impl = new AmazonS3Impl(m_logger, accessKey, secretKey, region, deploymentFolder, bucketName);
            
            String jarFileName = projectName + "-" + projectVersion + ".jar";
            File jarFile = new File(outputDirectory.getAbsolutePath() + "/" + jarFileName);
            
            m_logger.info("Uploading Lambda JAR to S3: " + jarFileName);
            m_amazonS3Impl.uploadFile(deploymentFolder, jarFileName, jarFile);
            
            m_logger.info("Deploying SansServer Lambda functions");
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Error processing LambdaGatewayApiConfiguration plugin", e);
        }
    }
}
