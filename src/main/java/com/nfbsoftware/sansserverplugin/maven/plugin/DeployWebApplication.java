package com.nfbsoftware.sansserverplugin.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.Vector;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.nfbsoftware.sansserverplugin.maven.amazon.AmazonGatewayUtility;
import com.nfbsoftware.sansserverplugin.maven.amazon.AmazonS3Utility;
import com.nfbsoftware.sansserverplugin.sdk.util.Entity;
import com.nfbsoftware.sansserverplugin.sdk.util.StringUtil;

/**
 * This goal will deploy out static webapp resources to S3 along with generating a $project.name-config.js in the /src/main/webapp/generated folder.  This file will contain some of the important 
 * properties needed by the user interface code.
 * 
 * @goal deploy-webapp
 * @phase install
 */
public class DeployWebApplication extends AbstractMojo
{
    private Log m_logger;
    
    private Properties m_properties = new Properties();
    
    private AmazonS3Utility m_amazonS3Utility;
    private AmazonGatewayUtility m_awsGatewayClient;

    /**
     * Location of the file.
     * 
     * @parameter expression="${project.basedir}"
     * @required
     */
    private File rootDirectory;
    
    /**
     * Location of the file.
     * 
     * @parameter expression="${project.name}"
     * @required
     */
    private String projectName;
    
    /**
     * The main execution method for the plugin.
     */
    public void execute() throws MojoExecutionException
    {
        // Grab a handle to our logger
        m_logger = getLog();

        try
        {
            m_logger.info("Loading SansServer build.properties file");
            File propertiesFile = new File(rootDirectory.getAbsolutePath() + "/build.properties");
            InputStream inStream = new FileInputStream(propertiesFile);
            m_properties.load(inStream);
            
            m_logger.info("Initializing AWS Gateway API");
            m_awsGatewayClient = new AmazonGatewayUtility(m_logger, m_properties);
            
            m_logger.info("Initializing AWS S3");
            m_amazonS3Utility = new AmazonS3Utility(m_logger, m_properties);
            
            m_logger.info("Creating javascript configuration utility file");
            generateJavascriptUtility();
            
            m_logger.info("Clean webapp destination");
            cleanWebappDestination();
            
            m_logger.info("Deploy webapp to S3");
            copyWebApplicationToS3();
        }
        catch (Exception e)
        {
            throw new MojoExecutionException("Error deploying out web application");
        }
    }
    
    /**
     * 
     * @throws Exception
     */
    private void cleanWebappDestination() throws Exception
    {
        // TODO Clear out all the static files on S3
    }
    
    /**
     * 
     * @throws MojoExecutionException
     */
    private void copyWebApplicationToS3() throws Exception
    {
        // Gather all the files to push out to the S3 bucket
        String sourcePath = rootDirectory.getAbsolutePath() + "/src/main/webapp";
        Collection<File> assetFileList = getAssetFileList(sourcePath);
        
        for(File assetFile : assetFileList)
        {
            String contentName = StringUtil.replaceSubstr(assetFile.getAbsolutePath(), sourcePath + "/", "");
            
            // Make sure not to copy files that begin with "." as those are hidden files on the disk
            if(!contentName.startsWith("."))
            {
                m_logger.info("Uploading assest: " + contentName);
                m_amazonS3Utility.uploadFile(null, contentName, assetFile);
            }
        }
    }
    
    /**
     * 
     * @return
     */
    private Collection<File> getAssetFileList(String sourcePath) throws Exception
    {
        // Search out spring configuration files.
        boolean recursive = true;

        File seedDir = new File(sourcePath);
        Collection<File> files = listFiles(seedDir, recursive);

        return files;
    }
    
    /**
     * 
     * @param directory
     * @param filter
     * @param recurse
     * @return
     */
    public Collection<File> listFiles(File directory, boolean recurse)
    {
        // List of files / directories
        Vector<File> files = new Vector<File>();

        // Get files / directories in the directory
        File[] entries = directory.listFiles();

        // Go over entries
        for (File entry : entries)
        {
            // If there is no filter or the filter accepts the
            // file / directory, add it to the list
            if(!entry.isDirectory())
            {
                files.add(entry);
            }

            // If the file is a directory and the recurse flag
            // is set, recurse into the directory
            if (recurse && entry.isDirectory())
            {
                files.addAll(listFiles(entry, recurse));
            }
        }

        // Return collection of files
        return files;
    }

    /**
     * 
     * @throws MojoExecutionException
     */
    private void generateJavascriptUtility() throws MojoExecutionException
    {
        m_logger.info("Generating javascript utility");
        
        String outputPath = rootDirectory.getAbsolutePath() + "/src/main/webapp/generated";
        
        File f = new File(outputPath);

        if (!f.exists())
        {
            f.mkdirs();
        }

        File touch = new File(f, projectName + "-config.js");
        
        FileWriter w = null;
        try
        {
            w = new FileWriter(touch);

            StringBuffer defaultJavascript = new StringBuffer();

            defaultJavascript.append("/*\n");
            defaultJavascript.append("=================================================================\n");
            defaultJavascript.append("DO NOT EDIT THIS FILE EVER!!!\n");
            defaultJavascript.append("This file is auto generated by the build process.\n");
            defaultJavascript.append("It generates a configuration utility js file that includes\n");
            defaultJavascript.append("some of the high-level project IDs and properties needed\n");
            defaultJavascript.append("by the UI at runtime.  This file is set to be ignored by Git.\n");
            defaultJavascript.append("Once Again...Please do not edit this file, we WILL find you!!!\n");
            defaultJavascript.append("=================================================================\n");
            defaultJavascript.append("*/\n");
            
            String environmentPrefix = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.ENVIRONEMNT_PREFIX));
            String idenityPoolId = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_COGNITO_IDENTITY_POOL_ID));
            String regionName = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_REGION));
            
            String projectGatewayName = environmentPrefix + "_" + projectName;
            String invokeUrl = StringUtil.emptyIfNull(m_awsGatewayClient.getRestApiInvokeUrl(projectGatewayName));
            
            defaultJavascript.append("var sansServerConfig = new function()\n");
            defaultJavascript.append("{\n");
            defaultJavascript.append("  this.environmentPrefix = '" + environmentPrefix + "_';\n");
            defaultJavascript.append("  this.region = '" + regionName + "';\n");
            defaultJavascript.append("  this.identityPoolId = '" + idenityPoolId + "';\n");
            defaultJavascript.append("  this.baseServiceUrl = '" + invokeUrl + "';\n");
            defaultJavascript.append("}\n");

            w.write(defaultJavascript.toString());
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error creating file " + touch, e);
        }
        finally
        {
            if (w != null)
            {
                try
                {
                    w.close();
                }
                catch (IOException e)
                {
                    // ignore
                }
            }
        }
        
        m_logger.info("Javascript utility file has been generated: " + touch.getAbsolutePath());
    }
}
