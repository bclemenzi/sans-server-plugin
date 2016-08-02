package com.nfbsoftware.sansserverplugin.sdk.lambda;

import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.nfbsoftware.sansserverplugin.sdk.lambda.model.HandlerResponse;

/**
 * The BaseHandler should be used as a base function for Lambda.  It will create a baseline set of objects needed by all our functions.
 * When configuring a Lambda function, do so with the function name "handlerRequest".  When implementing your own extension to this
 * class, simply override the "processRequest" method.
 * 
 * @author Brendan Clemenzi
 */
public class BaseLambdaHandler implements ILambdaFunction
{
    protected LambdaLogger m_logger;
    protected Object m_input;
    protected Context m_context;
    
    protected HashMap<String, String> m_requestHeaders;
    protected HashMap<String, String> m_requestParams;
    protected HashMap<String, String> m_requestQuery;
    protected HashMap<String, String> m_requestBody;
    
    protected HashMap<String, Object> m_inputHashMap;
    
    protected Properties m_properties = new Properties();
    
    /**
     * Basic constructor
     */
    public BaseLambdaHandler()
    {
        try
        {
            // Load our properties file
            initialize();
            
            // TODO Load other default resources
        }
        catch(Exception e)
        {
            // Since we are out of options in Lambda, just print the stacktrace.
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize our handler with whatever it needs upfront
     * 
     * @throws Exception
     */
    protected void initialize() throws Exception
    {
        // Load our properties file.  This is hard coded into our deployment
        InputStream is = BaseLambdaHandler.class.getResourceAsStream("/project.properties");
        
        // Load our properties file into memory
        m_properties.load(is);
    }
    
    /**
     * Returns the raw context object passed to the lambda function
     * 
     * @return
     */
    protected Context getFunctionContext()
    {
        return m_context;
    }
    
    /**
     * Returns the raw input object passed to the lambda function
     * 
     * @return
     */
    protected Object getFunctionInput()
    {
        return m_input;
    }
    
    /**
     * The getProperty method can be used to get a property value found within the default lambda.properties file created during deployment 
     * 
     * @param propertyKey
     * @return
     */
    protected String getProperty(String propertyKey)
    {
        String tmpValue = m_properties.getProperty(propertyKey);
        
        return tmpValue;
    }
    
    /**
     * The getInputObject method can be used as a simple way to get our function's raw input event elements.
     * 
     * @param objectKey
     * @return
     */
    protected Object getInputObject(String objectKey)
    {
        Object tmpObject = m_inputHashMap.get(objectKey);
        
        return tmpObject;
    }
    
    /**
     * The getHeader method can be used as a simple way to get our function's input header attributes.
     * 
     * @param headerKey
     * @return
     */
    protected String getHeader(String headerKey)
    {
        String tmpValue = null;

        if(m_requestBody != null)
        {
            tmpValue = m_requestHeaders.get(headerKey);
        }
        
        return tmpValue;
    }
    
    /**
     * Returns all the element keys found in the request header
     * 
     * @return
     */
    protected Set<String> getHeaderKeySet()
    {
        Set<String> tmpValue = new HashSet<String>();

        if(m_requestHeaders != null)
        {
            tmpValue = m_requestHeaders.keySet();
        }
        
        return tmpValue;
    }
    
    /**
     * The getParameter method can be used as a simple way to get our function's input parameters.
     * 
     * @param propertyKey
     * @return
     */
    protected String getParameter(String parameterKey)
    {
        String tmpValue = null;

        if(m_requestBody != null)
        {
            tmpValue = m_requestParams.get(parameterKey);
        }
        
        return tmpValue;
    }
    
    /**
     * Returns all the element keys found in the request params
     * 
     * @return
     */
    protected Set<String> getParameterKeySet()
    {
        Set<String> tmpValue = new HashSet<String>();

        if(m_requestParams != null)
        {
            tmpValue = m_requestParams.keySet();
        }
        
        return tmpValue;
    }
    
    /**
     * The getQuery method can be used as a simple way to get our function's input query string parameters.
     * 
     * @param queryStringKey
     * @return
     */
    protected String getQuery(String queryStringKey)
    {
        String tmpValue = null;

        if(m_requestBody != null)
        {
            tmpValue = m_requestQuery.get(queryStringKey);
        }
        
        return tmpValue;
    }
    
    /**
     * Returns all the element keys found in the request query string
     * 
     * @return
     */
    protected Set<String> getQueryKeySet()
    {
        Set<String> tmpValue = new HashSet<String>();

        if(m_requestQuery != null)
        {
            tmpValue = m_requestQuery.keySet();
        }
        
        return tmpValue;
    }
    
    /**
     * The getBody method can be used as a simple way to get our function's input body string parameters.
     * 
     * @param bodyKey
     * @return
     */
    protected String getBody(String bodyKey)
    {
        String tmpValue = null;

        if(m_requestBody != null)
        {
            tmpValue = m_requestBody.get(bodyKey);
        }
        
        return tmpValue;
    }
    
    /**
     * Returns all the element keys found in the request body
     * 
     * @return
     */
    protected Set<String> getBodyKeySet()
    {
        Set<String> tmpValue = new HashSet<String>();

        if(m_requestBody != null)
        {
            tmpValue = m_requestBody.keySet();
        }
        
        return tmpValue;
    }
    
    /**
     * This is the primary method used/configured for Lambda.  Be default, we do not want to override this method.
     * 
     * @param input
     * @param context
     * @return
     */
    public HandlerResponse handleRequest(Object input, Context context)
    {
        // Set out imputs:
        m_input = input;
        m_context = context;
        
        // Set our logger
        m_logger = m_context.getLogger();
        
        @SuppressWarnings("unchecked")
        HashMap<String, Object> inputHashMap = (HashMap<String, Object>)input;
        m_inputHashMap = inputHashMap;
        
        m_requestHeaders = (HashMap<String, String>)m_inputHashMap.get("headers");
        m_requestParams = (HashMap<String, String>)m_inputHashMap.get("params");
        m_requestQuery = (HashMap<String, String>)m_inputHashMap.get("query");
        m_requestBody = (HashMap<String, String>)m_inputHashMap.get("body");

        try
        {
            // Process our request
            HandlerResponse handlerResponse =  processRequest();
            
            // Return our response object
            return handlerResponse;
        }
        catch (Exception e)
        {
            // Log our exception
            m_logger.log("ERROR: " + e.getMessage());
            
            // Always return at least a failure response
            HandlerResponse handlerResponse = new HandlerResponse();
            
            handlerResponse.setStatus(HandlerResponse.StatusKeys.FAILURE);
            handlerResponse.setStatusMessage(e.getMessage());
            
            // Return our response object
            return handlerResponse;
        }
    }
    
    /**
     * The processRequest method should be overridden in your implementations of this base class
     * 
     * @return
     * @throws Exception
     */
    public HandlerResponse processRequest() throws Exception
    {
        HandlerResponse handlerResponse = new HandlerResponse();
        
        handlerResponse.setStatus(HandlerResponse.StatusKeys.SUCCESS);
        handlerResponse.setStatusMessage("");
        
        return handlerResponse;
    }
}
