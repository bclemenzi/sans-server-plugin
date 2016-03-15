package com.nfbsoftware.sansserverplugin.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.nfbsoftware.sansserverplugin.maven.amazon.AmazonGatewayUtility;
import com.nfbsoftware.sansserverplugin.sdk.util.Entity;
import com.nfbsoftware.sansserverplugin.sdk.util.StringUtil;

/**
 * Goal which generates a project.properties file used to configure our Lambda function a runtime.
 * 
 * @goal build-properties
 * @phase generate-resources
 */
public class PropertiesFileBuilder extends AbstractMojo
{
    private Log m_logger;
    
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
     * @parameter expression="${project.basedir}"
     * @required
     */
    private File rootDirectory;
    
    /**
     * The main execution method for the plugin.
     */
    public void execute() throws MojoExecutionException
    {
        // Grab a handle to our logger
        m_logger = getLog();

        try
        {
            m_logger.info("Creating runtime properties");
            generateRuntimePropertiesFile();
        }
        catch (IOException e)
        {
            throw new MojoExecutionException("Error creating our runtime properties file");
        }
    }

    private void generateRuntimePropertiesFile() throws IOException
    {
        m_logger.info("Generating runtime properties");
        
        File buildProperties = new File(rootDirectory.getAbsolutePath() + "/build.properties");
        File projectProperties = new File(outputDirectory.getAbsolutePath() + "/classes/project.properties");
        
        FileUtils.copyFile(buildProperties, projectProperties);
        
        m_logger.info("Runtime properties file has been generated: " + projectProperties.getAbsolutePath());
    }
}
