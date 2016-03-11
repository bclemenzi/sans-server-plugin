package com.nfbsoftware.sansserverplugin.maven.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.amazonaws.services.apigateway.model.CreateRestApiRequest;
import com.amazonaws.services.apigateway.model.GetRestApiResult;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;
import com.nfbsoftware.sansserverplugin.maven.amazon.AmazonGatewayUtility;
import com.nfbsoftware.sansserverplugin.maven.amazon.AmazonLambdaUtility;
import com.nfbsoftware.sansserverplugin.maven.amazon.AmazonS3Utility;
import com.nfbsoftware.sansserverplugin.sdk.annotation.AwsLambda;
import com.nfbsoftware.sansserverplugin.sdk.util.Entity;
import com.nfbsoftware.sansserverplugin.sdk.util.StringUtil;

/**
 * Goal which configures and deploys our Lambda functions with API Gateway endpoints.
 * 
 * @goal deploy-lambda
 * @phase install
 */
public class LambdaConfiguration extends AbstractMojo
{
    private Log m_logger;
    
    private AmazonS3Utility m_amazonS3Utility;
    private AmazonLambdaUtility m_awsLambdaClient;
    private AmazonGatewayUtility m_awsGatewayClient;
    
    private Properties m_properties = new Properties();
    
    private Map<String, Class<?>> m_lambdaClassMap = new HashMap<String, Class<?>>();
    
    private boolean m_hasGateway = false;
    
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
            File propertiesFile = new File(rootDirectory.getAbsolutePath() + "/build.properties");
            InputStream inStream = new FileInputStream(propertiesFile);
            m_properties.load(inStream);
            
            m_logger.info("Loading properties");
            String deploymentFolder = m_properties.getProperty(Entity.FrameworkProperties.AWS_S3_DEPLOYMENT_FOLDER);
            
            m_logger.info("Initializing AWS S3");
            m_amazonS3Utility = new AmazonS3Utility(m_logger, m_properties);
            
            m_logger.info("Initializing AWS Gateway API");
            m_awsGatewayClient = new AmazonGatewayUtility(m_logger, m_properties);
            
            m_logger.info("Initializing AWS Lambda");
            m_awsLambdaClient = new AmazonLambdaUtility(m_logger, m_properties);
            
            m_logger.info("Search for Lambda functions");
            List<String> lambdaClassFiles = getClassFileList();
            
            // Only upload our Lambda code if there is some
            if(!lambdaClassFiles.isEmpty())
            {
                String jarFileName = generateDeploymentJarFileName();
                File jarFile = new File(outputDirectory.getAbsolutePath() + "/" + jarFileName);
                
                m_logger.info("Uploading Lambda JAR to S3: " + jarFileName);
                m_amazonS3Utility.uploadFile(deploymentFolder, jarFileName, jarFile);
                
                m_logger.info("Configure SansServer Lambda functions");
                configureLambdaFunctions(lambdaClassFiles);
                
                m_logger.info("Deleting Lambda JAR from S3: " + jarFileName);
                m_amazonS3Utility.deleteFile(deploymentFolder, jarFileName);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            
            throw new MojoExecutionException("Error processing LambdaGatewayApiConfiguration plugin", e);
        }
    }

    /**
     * 
     * @return
     */
    private String generateDeploymentJarFileName()
    {
        String jarFileName = projectName + "-" + projectVersion + ".jar";
        return jarFileName;
    }
    
    /**
     * 
     * @param lambdaClassFiles
     * @throws Exception
     */
    private void configureLambdaFunctions(List<String> lambdaClassFiles) throws Exception
    {
        // Loop through our class files
        for(String classFileName : lambdaClassFiles)
        {            
            // Get our class file from the loader
            Class classObject = m_lambdaClassMap.get(classFileName);
            
            // process our AwsLambda annotations
            if(classObject.isAnnotationPresent(AwsLambda.class))
            {
                AwsLambda awsLambdaAnnotation = (AwsLambda)classObject.getAnnotation(AwsLambda.class);
                
                if(awsLambdaAnnotation != null)
                {
                    deployLambdaFunction(classFileName, awsLambdaAnnotation.name(), awsLambdaAnnotation.desc(), awsLambdaAnnotation.handlerMethod(), awsLambdaAnnotation.memorySize(), awsLambdaAnnotation.timeout());
                }
            }
            /*
            if(classObject.isAnnotationPresent(AwsLambdaWithGateway.class))
            {
                AwsLambdaWithGateway awsLambdaWithGatewayAnnotation = (AwsLambdaWithGateway)classObject.getAnnotation(AwsLambdaWithGateway.class);
                
                if(awsLambdaWithGatewayAnnotation != null)
                {
                    // Deploy out Lambda function
                    deployLambdaFunction(classFileName, awsLambdaAnnotation.name(), awsLambdaAnnotation.desc(), awsLambdaAnnotation.handlerMethod(), awsLambdaAnnotation.memorySize(), awsLambdaAnnotation.timeout());
                    
                    // Make sure we have our API Gateway to link our Lambda functions to
                    if(!m_hasGateway)
                    {
                        createAPIGateway();
                    }
                    
                    // Deploy our API Gateway
                    deployGatewayAPIforLambdaFunction(awsLambdaWithGatewayAnnotation);
                }
            }
            */
        }
    }
    
    /**
     * 
     * @throws Exception
     */
    private void createAPIGateway() throws Exception
    {
        String environmentePrefix = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.ENVIRONEMNT_PREFIX));
        
        // Generate our environment-based gateway name
        String projectGatewayName = environmentePrefix + "_" + projectName;
        
        GetRestApiResult restApiResult = m_awsGatewayClient.getRestApiByName(projectGatewayName);
        
        if(restApiResult != null)
        {
            m_hasGateway = true;
        }
        else
        {
            // Create our new API
            CreateRestApiRequest createRestApiRequest = new CreateRestApiRequest();
            createRestApiRequest.setName(projectGatewayName);
            createRestApiRequest.setDescription("Auto-generated API for " + projectGatewayName);
            
            // Create our api
            m_awsGatewayClient.createRestApi(createRestApiRequest);
            
            m_hasGateway = true;
        }
    }
    
    /**
     * 
     * @param awsLambdaAnnotation
     * @throws Exception
     */
    private void deployLambdaFunction(String classFileName, String name, String description, String handlerMethod, String memorySize, String timeout) throws Exception
    {
        String environmentePrefix = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.ENVIRONEMNT_PREFIX));
        String lambdaRoleArn = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_LAMBDA_ROLE_ARN));
        
        // Create a generated function name so that we can isolate multiple deployments
        String generatedlambdaName = StringUtil.replaceSubstr(environmentePrefix + "_" + name, " ", "");
        String generatedHandlerName = generateHandlerFunctionName(classFileName, handlerMethod);
        
        // Get a handle to the existing function if there is one
        GetFunctionResult getFunctionResult = m_awsLambdaClient.getFunction(generatedlambdaName);
        
        if(getFunctionResult != null)
        {
            UpdateFunctionConfigurationRequest updateFunctionConfigurationRequest = new UpdateFunctionConfigurationRequest();
            updateFunctionConfigurationRequest.setDescription(description);
            updateFunctionConfigurationRequest.setRole(lambdaRoleArn);
            updateFunctionConfigurationRequest.setFunctionName(generatedlambdaName);
            updateFunctionConfigurationRequest.setHandler(generatedHandlerName);
            updateFunctionConfigurationRequest.setTimeout(new Integer(timeout));
            updateFunctionConfigurationRequest.setMemorySize(new Integer(memorySize));
            
            // Lets look if we have changed the configuration since our last update
            if(m_awsLambdaClient.hasFunctionConfigChanged(getFunctionResult, updateFunctionConfigurationRequest))
            {
                m_awsLambdaClient.updateFunctionConfiguration(updateFunctionConfigurationRequest);
            }
            
            // Get the name of our deployment jar
            String deploymentJarFileName = generateDeploymentJarFileName();
            
            // Update our function
            m_awsLambdaClient.updateFunction(deploymentJarFileName, generatedlambdaName);
        }
        else
        {
            // No function with that name found, so lets create it now
            String deploymentJarFileName = generateDeploymentJarFileName();
        
            CreateFunctionRequest createFunctionRequest = new CreateFunctionRequest();
            
            createFunctionRequest.setFunctionName(generatedlambdaName);
            createFunctionRequest.setDescription(description);
            createFunctionRequest.setRole(lambdaRoleArn);
            createFunctionRequest.setHandler(generatedHandlerName);
            createFunctionRequest.setRuntime(com.amazonaws.services.lambda.model.Runtime.Java8);
            createFunctionRequest.setTimeout(new Integer(timeout));
            createFunctionRequest.setMemorySize(new Integer(memorySize));
            
            // Create our function
            m_awsLambdaClient.createFunction(deploymentJarFileName, createFunctionRequest);
        }
    }

    private String generateHandlerFunctionName(String classFileName, String handlerMethod)
    {
        File seedDir = new File(outputDirectory.getAbsolutePath() + "/classes");
        String seedPathString = seedDir.getAbsolutePath();
        
        // Remove the seed path
        classFileName = StringUtil.replaceSubstr(classFileName, seedPathString + "/", "");
        
        // Remove the file extension
        classFileName = StringUtil.replaceSubstr(classFileName, ".class", "");
        
        // Create a package name
        String javaClassName = StringUtil.replaceSubstr(classFileName, "/", ".");
        
        String generatedHandlerName = javaClassName + "::" + handlerMethod;
        return generatedHandlerName;
    }
    
    /**
     * 
     * @return
     * @throws MojoExecutionException
     */
    private List<String> getClassFileList() throws MojoExecutionException
    {
        // Search out spring configuration files.
        boolean recursive = true;

        FilenameFilter filter = new FilenameFilter()
        {
            public boolean accept(File dir, String name)
            {
                return name.endsWith(".class");
            }
        };

        List<String> classfiles = new ArrayList<String>();
        
        try
        {
            File seedDir = new File(outputDirectory.getAbsolutePath() + "/classes");
            Collection<File> files = listFiles(seedDir, filter, recursive);

            File classesDirectory = new File(outputDirectory.getAbsolutePath() + "/classes"); 
            
            URL classesUrl = classesDirectory.toURI().toURL();
            URL[] classesUrls = new URL[]{classesUrl}; 
            
            URLClassLoader classLoader = URLClassLoader.newInstance(classesUrls, getClass().getClassLoader());

            // Copy the configuration files to a resource folder.
            for (File file : files)
            {
                String seedPathString = seedDir.getAbsolutePath();
                String filePathString = file.getAbsolutePath();
                
                // Remove the seed path
                filePathString = StringUtil.replaceSubstr(filePathString, seedPathString + "/", "");
                
                // Remove the file extension
                filePathString = StringUtil.replaceSubstr(filePathString, ".class", "");
                
                // Create a package name
                String javaClassName = StringUtil.replaceSubstr(filePathString, "/", ".");
                
                // Check each file for our annotations
                Class classObject = classLoader.loadClass(javaClassName); 

                // Look for our custom annotations
                if(classObject.isAnnotationPresent(AwsLambda.class)) 
                {
                    classfiles.add(file.getAbsolutePath());
                    
                    m_lambdaClassMap.put(file.getAbsolutePath(), classObject);
                }
            }
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
            
            throw new MojoExecutionException("Error searching for annotated classes");
        }
        
        // Sort the file names
        Collections.sort(classfiles);

        return classfiles;

    }
    
    /**
     * 
     * @param directory
     * @param filter
     * @param recurse
     * @return
     */
    public Collection<File> listFiles(File directory, FilenameFilter filter, boolean recurse)
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
            if (filter == null || filter.accept(directory, entry.getName()))
            {
                files.add(entry);
            }

            // If the file is a directory and the recurse flag
            // is set, recurse into the directory
            if (recurse && entry.isDirectory())
            {
                files.addAll(listFiles(entry, filter, recurse));
            }
        }

        // Return collection of files
        return files;
    }
}
