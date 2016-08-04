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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.Vector;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.logging.Log;

import com.amazonaws.services.apigateway.model.CreateDeploymentRequest;
import com.amazonaws.services.apigateway.model.CreateResourceRequest;
import com.amazonaws.services.apigateway.model.CreateResourceResult;
import com.amazonaws.services.apigateway.model.CreateRestApiRequest;
import com.amazonaws.services.apigateway.model.Deployment;
import com.amazonaws.services.apigateway.model.GetIntegrationRequest;
import com.amazonaws.services.apigateway.model.GetIntegrationResult;
import com.amazonaws.services.apigateway.model.GetMethodRequest;
import com.amazonaws.services.apigateway.model.GetMethodResult;
import com.amazonaws.services.apigateway.model.GetRestApiResult;
import com.amazonaws.services.apigateway.model.IntegrationType;
import com.amazonaws.services.apigateway.model.PutIntegrationRequest;
import com.amazonaws.services.apigateway.model.PutIntegrationResponseRequest;
import com.amazonaws.services.apigateway.model.PutMethodRequest;
import com.amazonaws.services.apigateway.model.PutMethodResponseRequest;
import com.amazonaws.services.apigateway.model.Resource;
import com.amazonaws.services.apigateway.model.UpdateIntegrationRequest;
import com.amazonaws.services.apigateway.model.UpdateMethodRequest;
import com.amazonaws.services.apigateway.model.UpdateResourceRequest;
import com.amazonaws.services.lambda.model.AddPermissionRequest;
import com.amazonaws.services.lambda.model.CreateFunctionRequest;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.GetFunctionResult;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.UpdateFunctionConfigurationRequest;
import com.nfbsoftware.sansserverplugin.maven.amazon.AmazonGatewayUtility;
import com.nfbsoftware.sansserverplugin.maven.amazon.AmazonLambdaUtility;
import com.nfbsoftware.sansserverplugin.maven.amazon.AmazonS3Utility;
import com.nfbsoftware.sansserverplugin.sdk.annotation.AwsLambda;
import com.nfbsoftware.sansserverplugin.sdk.annotation.AwsLambdaWithGateway;
import com.nfbsoftware.sansserverplugin.sdk.util.Entity;
import com.nfbsoftware.sansserverplugin.sdk.util.SecureUUID;
import com.nfbsoftware.sansserverplugin.sdk.util.StringUtil;

/**
 * Goal which configures and deploys our Lambda functions with API Gateway endpoints.
 * 
 * @goal deploy-lambda
 * @phase deploy
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
            
            m_logger.info("Loading SansServer build.properties file");
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
                
                m_logger.info("Clean old old Lambda functions");
                cleanUpOldLambdaFunctions(lambdaClassFiles);
                
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
    private void cleanUpOldLambdaFunctions(List<String> lambdaClassFiles) throws Exception
    {
        Set<String> activeFunctionSet = new HashSet<String>();
        Set<String> activeApiResourceSet = new HashSet<String>();
        
        String environmentPrefix = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.ENVIRONEMNT_PREFIX));
        
        // Create a generated function name so that we can isolate multiple deployments
        String projectGatewayName = environmentPrefix + "_" + projectName;
        
        // Loop through our class files to collect the names of our active functions
        for(String classFileName : lambdaClassFiles)
        {  
            // Get our class file from the loader
            Class classObject = m_lambdaClassMap.get(classFileName);
            
            if(classObject.isAnnotationPresent(AwsLambda.class))
            {
                AwsLambda awsLambdaAnnotation = (AwsLambda)classObject.getAnnotation(AwsLambda.class);
                
                if(awsLambdaAnnotation != null)
                {
                    String generatedlambdaName = StringUtil.replaceSubstr(environmentPrefix + "_" + awsLambdaAnnotation.name(), " ", "");
                    
                    activeFunctionSet.add(generatedlambdaName);
                }
            }
            if(classObject.isAnnotationPresent(AwsLambdaWithGateway.class))
            {
                AwsLambdaWithGateway awsLambdaWithGatewayAnnotation = (AwsLambdaWithGateway)classObject.getAnnotation(AwsLambdaWithGateway.class);
                
                if(awsLambdaWithGatewayAnnotation != null)
                {
                    String generatedlambdaName = StringUtil.replaceSubstr(environmentPrefix + "_" + awsLambdaWithGatewayAnnotation.name(), " ", "");
                    
                    activeFunctionSet.add(generatedlambdaName);
                    activeApiResourceSet.add(awsLambdaWithGatewayAnnotation.resourceName());
                }
            }
        }
        
        // Loop through the API resources to delete ones that are no longer needed
        GetRestApiResult restApiResult = m_awsGatewayClient.getRestApiByName(projectGatewayName);
        
        if(restApiResult != null)
        {
            List<Resource> deployedResources = m_awsGatewayClient.getResources(restApiResult.getId());
            
            for(Resource resource : deployedResources)
            {
                if(!StringUtil.isNullOrEmpty(resource.getPathPart()))
                {
                    if(!activeApiResourceSet.contains(resource.getPathPart()))
                    {
                        // Sleep for a 10th of a second as to not overload our AWS throttling limits
                        Thread.sleep(1000);
                        
                        m_logger.info("Deleting API Resource: " + resource.getId() + "  " + resource.getPathPart());
                        m_awsGatewayClient.deleteResource(restApiResult.getId(), resource.getId());
                    }
                }
            }
        }
        
        // Loop through deployed functions to see which ones we should delete
        List<FunctionConfiguration> functionConfigurations = m_awsLambdaClient.getFunctions();
        
        for(FunctionConfiguration functionConfiguration : functionConfigurations)
        {
            String functionName = functionConfiguration.getFunctionName();
            
            // Only delete functions with our env prefix
            if(functionName.startsWith(environmentPrefix))
            {
                // Make sure this function isn't on our active deploy list
                if(!activeFunctionSet.contains(functionName))
                {
                    // Sleep for a 10th of a second as to not overload our AWS throttling limits
                    Thread.sleep(1000);
                    
                    m_logger.info("Deleting Lambda Function: " + functionName);
                    m_awsLambdaClient.deleteFunction(functionName);
                }
            }
        }
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
            if(classObject.isAnnotationPresent(AwsLambdaWithGateway.class))
            {
                AwsLambdaWithGateway awsLambdaWithGatewayAnnotation = (AwsLambdaWithGateway)classObject.getAnnotation(AwsLambdaWithGateway.class);
                
                if(awsLambdaWithGatewayAnnotation != null)
                {
                    // Deploy out Lambda function
                    deployLambdaFunction(classFileName, awsLambdaWithGatewayAnnotation.name(), awsLambdaWithGatewayAnnotation.desc(), awsLambdaWithGatewayAnnotation.handlerMethod(), awsLambdaWithGatewayAnnotation.memorySize(), awsLambdaWithGatewayAnnotation.timeout());
                    
                    // Make sure we have our API Gateway to link our Lambda functions to
                    if(!m_hasGateway)
                    {
                        createAPIGateway();
                    }
                    
                    // TODO finish the API integration because at the time of this writing there was no support for Lambda configurations in the AWS SDK
                    deployGatewayAPIforLambdaFunction(classFileName, awsLambdaWithGatewayAnnotation.name(), awsLambdaWithGatewayAnnotation);
                }
            }
        }
        
        // Deploy the APIs for public use if we configured a gateway
        if(m_hasGateway)
        {
            
            pubishAPIGateway(true);
        }
    }
    
    /**
     * 
     * @throws Exception
     */
    private void createAPIGateway() throws Exception
    {
        // Sleep for a second as to not overload our AWS throttling limits
        Thread.sleep(1000);
        
        String environmentPrefix = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.ENVIRONEMNT_PREFIX));
        
        // Generate our environment-based gateway name
        String projectGatewayName = environmentPrefix + "_" + projectName;
        
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
     * @throws Exception
     */
    private void pubishAPIGateway(boolean allowRetry) throws Exception
    {
        // Sleep for a second as to not overload our AWS throttling limits
        Thread.sleep(1000);
        
        String environmentPrefix = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.ENVIRONEMNT_PREFIX));
        String deploymentStage = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.ENVIRONEMNT_STAGE));
        
        // Generate our environment-based gateway name
        String projectGatewayName = environmentPrefix + "_" + projectName;
        
        GetRestApiResult restApiResult = m_awsGatewayClient.getRestApiByName(projectGatewayName);
        
        if(restApiResult != null)
        {
            try
            {
                m_logger.info("Deploying (" + restApiResult.getName() + ") API Gateway for public consumption: " + restApiResult.getId());
                
                CreateDeploymentRequest createDeploymentRequest = new CreateDeploymentRequest();
                createDeploymentRequest.setRestApiId(restApiResult.getId());
                createDeploymentRequest.setStageName(deploymentStage);
                createDeploymentRequest.setStageDescription(deploymentStage + " deployment stage created by the SansServerPlugin");
                
                m_awsGatewayClient.createDeployment(createDeploymentRequest);
                
                m_logger.info("Gateway API (" + restApiResult.getName() + ") has been deployed: " + restApiResult.getId());
            }
            catch (Exception e)
            {
                if(allowRetry)
                {
                    m_logger.error("Failed to deploy Gateway API (" + restApiResult.getName() + ") retrying in 2 seconds: " + restApiResult.getId());
                    
                    // Sleep for a second as to not overload our AWS throttling limits
                    Thread.sleep(2000);
                    
                    pubishAPIGateway(false);
                }
                else
                {
                    m_logger.error("Failed to deploy Gateway API (" + restApiResult.getName() + ") for a second time: " + restApiResult.getId());
                }
            }
        }
    }
    
    /**
     * 
     * @param awsLambdaWithGatewayAnnotation
     */
    private void deployGatewayAPIforLambdaFunction(String classFileName, String name, AwsLambdaWithGateway awsLambdaWithGatewayAnnotation) throws Exception
    {
        // Sleep for a 1/2 a second as to not overload our AWS throttling limits
        Thread.sleep(2000);
        
        String environmentPrefix = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.ENVIRONEMNT_PREFIX));
        String regionName = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_REGION));
        String accountId = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_ACCOUNT_ID));
        String stageName = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.ENVIRONEMNT_STAGE));
        
        // Create a generated function name so that we can isolate multiple deployments
        String projectGatewayName = environmentPrefix + "_" + projectName;
        String generatedlambdaName = StringUtil.replaceSubstr(environmentPrefix + "_" + name, " ", "");
        
        m_logger.info("projectGatewayName: " + projectGatewayName);
        m_logger.info("generatedlambdaName: " + generatedlambdaName);
        
        // Get a handle to the existing function if there is one
        GetFunctionResult getFunctionResult = m_awsLambdaClient.getFunction(generatedlambdaName);
        
        if(getFunctionResult != null)
        {
            GetRestApiResult getRestApiResult = m_awsGatewayClient.getRestApiByName(projectGatewayName);
            
            if(getRestApiResult != null)
            {
                Resource resourceObject = m_awsGatewayClient.getResourceByPathPart(getRestApiResult.getId(), awsLambdaWithGatewayAnnotation.resourceName());
                
                if(resourceObject != null)
                {
                    // Update the existing api
                    m_logger.info("Resource (" + awsLambdaWithGatewayAnnotation.resourceName() + ") Found, no updates will be made: " + resourceObject.getId());
                    
                    // TODO Add code to update the various method/integration objects for the resource.
                }
                else
                {
                    m_logger.info("Resource Not Found: " + awsLambdaWithGatewayAnnotation.resourceName());
                    
                    Resource rootResource = m_awsGatewayClient.getResourceByPath(getRestApiResult.getId(), "/");
                    
                    // create a new api resourse
                    CreateResourceRequest createResourceRequest = new CreateResourceRequest();
                    createResourceRequest.setPathPart(awsLambdaWithGatewayAnnotation.resourceName());
                    createResourceRequest.setRestApiId(getRestApiResult.getId());
                    createResourceRequest.setParentId(rootResource.getId());
                    
                    m_logger.info("Creating Resource Request"); 
                    CreateResourceResult createResourceResult = m_awsGatewayClient.createResource(createResourceRequest);
                    
                    if(createResourceResult != null)
                    {
                        Thread.sleep(1000);
                        m_logger.info("Create our method with type: " + awsLambdaWithGatewayAnnotation.method().name() + "  authorization: " + awsLambdaWithGatewayAnnotation.authorization().name()); 
                        PutMethodRequest putMethodRequest = new PutMethodRequest();
                        putMethodRequest.setRestApiId(getRestApiResult.getId());
                        putMethodRequest.setResourceId(createResourceResult.getId());
                        putMethodRequest.setApiKeyRequired(awsLambdaWithGatewayAnnotation.keyRequired());
                        putMethodRequest.setAuthorizationType(awsLambdaWithGatewayAnnotation.authorization().name());
                        putMethodRequest.setHttpMethod(awsLambdaWithGatewayAnnotation.method().name());
                        
                        m_awsGatewayClient.createMethod(putMethodRequest);
                        
                        Thread.sleep(1000);
                        m_logger.info("Create our integration"); 
                        PutIntegrationRequest putIntegrationRequest = new PutIntegrationRequest();
                        putIntegrationRequest.setRestApiId(getRestApiResult.getId());
                        putIntegrationRequest.setResourceId(createResourceResult.getId());
                        putIntegrationRequest.setHttpMethod(awsLambdaWithGatewayAnnotation.method().name());
                        putIntegrationRequest.setType(IntegrationType.AWS);
                        putIntegrationRequest.setIntegrationHttpMethod(awsLambdaWithGatewayAnnotation.method().name());
                        
                        // When using the gateway api, pass through the body, headers, parameters, query string parameters to our lambda functions when the context-type is application/json
                        putIntegrationRequest.setPassthroughBehavior("WHEN_NO_TEMPLATES");
                        putIntegrationRequest.addRequestTemplatesEntry("application/json", "{\"body\":$input.json('$'),\"headers\":{ #foreach($header in $input.params().header.keySet()) \"$header\":\"$util.escapeJavaScript($input.params().header.get($header))\" #if($foreach.hasNext),#end #end }, \"method\":\"$context.httpMethod\", \"params\": { #foreach($param in $input.params().path.keySet()) \"$param\":\"$util.escapeJavaScript($input.params().path.get($param))\" #if($foreach.hasNext),#end #end }, \"query\": { #foreach($queryParam in $input.params().querystring.keySet()) \"$queryParam\":\"$util.escapeJavaScript($input.params().querystring.get($queryParam))\" #if($foreach.hasNext),#end #end } }");
                        
                        String lambdaUriArn = "arn:aws:apigateway:" + regionName + ":lambda:path/2015-03-31/functions/arn:aws:lambda:" + regionName + ":" + accountId + ":function:" + generatedlambdaName + "/invocations";
                        putIntegrationRequest.setUri(lambdaUriArn);
                        
                        m_awsGatewayClient.createIntegration(putIntegrationRequest);
                        
                        Thread.sleep(1000);
                        m_logger.info("Create our method response"); 
                        PutMethodResponseRequest putMethodResponseRequest = new PutMethodResponseRequest();
                        putMethodResponseRequest.setRestApiId(getRestApiResult.getId());
                        putMethodResponseRequest.setResourceId(createResourceResult.getId());
                        putMethodResponseRequest.setHttpMethod(awsLambdaWithGatewayAnnotation.method().name());
                        putMethodResponseRequest.setStatusCode("200");
                        
                        Map<String, String> responseModels = new HashMap<String, String>();
                        responseModels.put("application/json", "Empty");
                        putMethodResponseRequest.setResponseModels(responseModels);
                        
                        if(awsLambdaWithGatewayAnnotation.enableCORS())
                        {
                            m_logger.info("Enable CORS for our method response"); 
                            Map<String,Boolean> methodResponseParameters = new HashMap<String,Boolean>();
                            methodResponseParameters.put("method.response.header.Access-Control-Allow-Methods", new Boolean("true"));
                            methodResponseParameters.put("method.response.header.Access-Control-Allow-Origin", new Boolean("true"));
                            methodResponseParameters.put("method.response.header.Access-Control-Allow-Headers", new Boolean("true"));
                            
                            putMethodResponseRequest.setResponseParameters(methodResponseParameters);
                        }
                        
                        m_awsGatewayClient.createMethodResponse(putMethodResponseRequest);
                        
                        Thread.sleep(1000);
                        m_logger.info("Create our integration response"); 
                        PutIntegrationResponseRequest putIntegrationResponseRequest = new PutIntegrationResponseRequest();
                        putIntegrationResponseRequest.setRestApiId(getRestApiResult.getId());
                        putIntegrationResponseRequest.setResourceId(createResourceResult.getId());
                        putIntegrationResponseRequest.setHttpMethod(awsLambdaWithGatewayAnnotation.method().name());
                        putIntegrationResponseRequest.setStatusCode("200");
                        
                        if(awsLambdaWithGatewayAnnotation.enableCORS())
                        {
                            m_logger.info("Enable CORS for our integration response"); 
                            Map<String,String> integrationResponseParameters = new HashMap<String,String>();
                            integrationResponseParameters.put("method.response.header.Access-Control-Allow-Methods", "'GET,POST,OPTIONS'");
                            integrationResponseParameters.put("method.response.header.Access-Control-Allow-Origin", "'*'");
                            integrationResponseParameters.put("method.response.header.Access-Control-Allow-Headers", "'Content-Type,X-Amz-Date,Authorization,X-Api-Key'");
                            
                            putIntegrationResponseRequest.setResponseParameters(integrationResponseParameters);
                        }
                        
                        Map<String, String> responseTemplates = new HashMap<String, String>();
                        responseTemplates.put("application/json", "");
                        putIntegrationResponseRequest.setResponseTemplates(responseTemplates);
                        
                        m_awsGatewayClient.createIntegrationResponse(putIntegrationResponseRequest);
                        
                        m_logger.info("Waiting for provisioning operation to complete before starting deployment........"); 
                        Thread.sleep(5000);
                        
                        m_logger.info("Create our api deployment"); 
                        CreateDeploymentRequest createDeploymentRequest = new CreateDeploymentRequest();
                        createDeploymentRequest.setRestApiId(getRestApiResult.getId());
                        createDeploymentRequest.setStageName(stageName);
                        createDeploymentRequest.setStageDescription("Auto Generated State: " + stageName);
                        
                        m_awsGatewayClient.createDeployment(createDeploymentRequest);
                        
                        Thread.sleep(1000);
                        m_logger.info("Create our function permissions for testing"); 
                        AddPermissionRequest testinAddPermissionRequest = new AddPermissionRequest();
                        testinAddPermissionRequest.setFunctionName(generatedlambdaName);
                        
                        String testStatementId = "apigateway-" + environmentPrefix + "-test-" + SecureUUID.generateUniqueNumber(4);
                        testinAddPermissionRequest.setStatementId(testStatementId.toLowerCase());
                        testinAddPermissionRequest.setAction("lambda:InvokeFunction");
                        testinAddPermissionRequest.setPrincipal("apigateway.amazonaws.com");
                        
                        String testSourceArn = "arn:aws:execute-api:" + regionName + ":" + accountId + ":" + getRestApiResult.getId() + "/*/" + awsLambdaWithGatewayAnnotation.method().name() + "/" + awsLambdaWithGatewayAnnotation.resourceName();
                        testinAddPermissionRequest.setSourceArn(testSourceArn);
                        
                        m_awsLambdaClient.addPermission(testinAddPermissionRequest);
                        
                        Thread.sleep(1000);
                        m_logger.info("Create our function permissions for the deployment"); 
                        AddPermissionRequest deployAddPermissionRequest = new AddPermissionRequest();
                        deployAddPermissionRequest.setFunctionName(generatedlambdaName);
                        
                        String deployStatementId = "apigateway-" + environmentPrefix + "-" + stageName + "-" + SecureUUID.generateUniqueNumber(4);
                        deployAddPermissionRequest.setStatementId(deployStatementId.toLowerCase());
                        deployAddPermissionRequest.setAction("lambda:InvokeFunction");
                        deployAddPermissionRequest.setPrincipal("apigateway.amazonaws.com");
                        
                        String deploySourceArn = "arn:aws:execute-api:" + regionName + ":" + accountId + ":" + getRestApiResult.getId() + "/" + stageName + "/" + awsLambdaWithGatewayAnnotation.method().name() + "/" + awsLambdaWithGatewayAnnotation.resourceName();
                        deployAddPermissionRequest.setSourceArn(deploySourceArn);
                        
                        m_awsLambdaClient.addPermission(deployAddPermissionRequest);
                        
                        // Check if we should enable CORS
                        if(awsLambdaWithGatewayAnnotation.enableCORS())
                        {
                            m_logger.info("Unable to configure CORS on your resource method.  SansServer-Plugin does not currectly support this function.  Please manage this through the AWS Console for the time being.");
                            
                            // TODO Add CORS support
                        }
                        
                        m_logger.info("Lambda Gateway API Complete"); 
                    }
                    else
                    {
                        throw new Exception("Unable to find new REST API resource: " + awsLambdaWithGatewayAnnotation.resourceName());
                    }
                }
            }
        }
    }
    
    /**
     * 
     * @param awsLambdaAnnotation
     * @throws Exception
     */
    private void deployLambdaFunction(String classFileName, String name, String description, String handlerMethod, String memorySize, String timeout) throws Exception
    {
        // Sleep for a 1/2 a second as to not overload our AWS throttling limits
        Thread.sleep(2000);
        
        String environmentPrefix = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.ENVIRONEMNT_PREFIX));
        String lambdaRoleArn = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_LAMBDA_ROLE_ARN));
        
        // Create a generated function name so that we can isolate multiple deployments
        String generatedlambdaName = StringUtil.replaceSubstr(environmentPrefix + "_" + name, " ", "");
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
                if(classObject.isAnnotationPresent(AwsLambda.class) 
                        || classObject.isAnnotationPresent(AwsLambdaWithGateway.class)) 
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
