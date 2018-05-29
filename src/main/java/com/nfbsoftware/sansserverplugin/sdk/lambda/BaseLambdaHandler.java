package com.nfbsoftware.sansserverplugin.sdk.lambda;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.nfbsoftware.sansserverplugin.sdk.lambda.model.HandlerResponse;
import com.nfbsoftware.sansserverplugin.sdk.util.StringUtil;

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
    
    protected HashMap<String, String> m_requestHeaders = new HashMap<String, String>();
    protected HashMap<String, String> m_requestParams = new HashMap<String, String>();
    protected HashMap<String, String> m_requestQuery = new HashMap<String, String>();
    protected HashMap<String, String> m_requestBody = new HashMap<String, String>();
    
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
     * @param parameterKey
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
     * The getRequestParameter method is a convenience method that will loop though our three possible 
     * 
     * @param key
     * @return
     */
    protected String getRequestParameter(String key)
    {
        String tmpValue = getBody(key);

        // If body is null then try from the query string
        if(StringUtil.isNullOrEmpty(tmpValue))
        {
            tmpValue = getQuery(key);
        }
        
        // If body/query are null then try from the input object
        if(StringUtil.isNullOrEmpty(tmpValue))
        {
            try
            {
                tmpValue = (String)getInputObject(key);
            }
            catch (Exception e)
            {
                tmpValue = null;
            }
        }
        
        // If body/query/Input are null then try from the header object
        if(StringUtil.isNullOrEmpty(tmpValue))
        {
            tmpValue = getHeader(key);
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
        
        Object tmpHeaders = m_inputHashMap.get("headers");
        if(tmpHeaders != null)
        {
            try
            {
                m_requestHeaders = (HashMap<String, String>)m_inputHashMap.get("headers");
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        Object tmpParams = m_inputHashMap.get("params");
        if(tmpParams != null)
        {
            try
            {
                m_requestParams = (HashMap<String, String>)m_inputHashMap.get("params");
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        Object tmpQuery = m_inputHashMap.get("query");
        if(tmpQuery != null)
        {
            try
            {
                m_requestQuery = (HashMap<String, String>)m_inputHashMap.get("query");
            }
            catch (Exception e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        
        Object tmpBody = m_inputHashMap.get("body");
        if(tmpBody != null)
        {
            try
            {
                // If the body is coming to us like "take=6&skip=0&page=1&pageSize=6" then
                // we will need to parse the string into the individual parameters
                if(tmpBody instanceof String)
                {
                    String rawBody = tmpBody.toString();
                    
                    String[] parsedBody = StringUtil.parseString(rawBody, "&");
                    List<String> parameterList = new ArrayList<String>(Arrays.asList(parsedBody));
                    
                    for(String tmpParam : parameterList)
                    {
                        try
                        {
                            if(tmpParam.contains("="))
                            {
                                String[] parsedParam = StringUtil.parseString(tmpParam, "=");
                                
                                String tmpValue = StringUtil.EMPTY_STRING;
                                if(parsedParam.length == 2)
                                {
                                    tmpValue = java.net.URLDecoder.decode(StringUtil.emptyIfNull(parsedParam[1]), "UTF-8");
                                }
                                
                                m_requestBody.put(parsedParam[0], tmpValue);
                            }
                        }
                        catch (Exception e)
                        {
                            m_logger.log("Error parsing body parameter: " + tmpParam);
                        }
                    }
                }
                else
                {
                    // Cast the body map to 
                    m_requestBody = (HashMap<String, String>)m_inputHashMap.get("body");
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

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
