package com.nfbsoftware.sansserver.maven.plugin;

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
