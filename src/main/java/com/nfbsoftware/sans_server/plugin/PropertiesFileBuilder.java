package com.nfbsoftware.sans_server.plugin;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

/**
 * Goal which generates a lambda.properties file used to configure our Lambda function a runtime.
 * 
 * @goal build-properties
 * @phase process-sources
 */
public class PropertiesFileBuilder extends AbstractMojo
{
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
        File f = new File(outputDirectory.getAbsolutePath() + "/classes");

        if (!f.exists())
        {
            f.mkdirs();
        }

        File touch = new File(f, "test.properties");
        
        FileWriter w = null;
        try
        {
            w = new FileWriter(touch);

            // Generate the project properties file
            StringBuffer defaultXml = new StringBuffer();
            
            defaultXml.append("aws.region=us-east-1");
            defaultXml.append("aws.accessKey=AKIAJMMZ72O6FIH3SCEQ");
            defaultXml.append("aws.secretKey=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");
            defaultXml.append("aws.cognito.identityPoolId=us-east-1:62b6420c-927c-486e-b9fc-b7e9a363f353");
            defaultXml.append("aws.cognito.providerName=nfbsw.login.services");
            defaultXml.append("aws.dynamodb.tableNamePrefix=BCLEMENZI");
            defaultXml.append("aws.s3.bucketName=bclemenzi.nfbsoftware.com");

            w.write(defaultXml.toString());
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
    }
}
