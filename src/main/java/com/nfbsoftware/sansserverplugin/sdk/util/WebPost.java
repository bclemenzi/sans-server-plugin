package com.nfbsoftware.sansserverplugin.sdk.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.codec.binary.Base64;

/**
 * This class is responsible for posting to remote Web Server using HTTP or
 * HTTPS and getting a response back. The Web server must send a response back
 * and must set content length field in HTML header.
 * 
 * <P/>
 * Sample code for HTTP:
 * 
 * <Pre/>
 * WebPost poster = new WebPost();
 * 
 * <Pre/>
 * poster.connect("http://localhost:81","username","password");
 * 
 * <Pre/>
 * poster.post("Hello Wolrd!");
 * 
 * <Pre/>
 * String response = poster.receive();
 * 
 * <Pre/>
 * poster.disconnect();
 * <P/>
 * Sample code for HTTPS:<Pre/>
 *       WebPost poster = new WebPost();
 * <Pre/>
 *       poster.secureConnect("https://localhost:81","username","password");
 * <Pre/>
 *       poster.securePost("Hello Wolrd!");
 * <Pre/>
 *       String response = poster.secureReceive();
 * <Pre/>
 *       poster.secureDisconnect();
 *       
 * @author brendanclemenzi
 */
public class WebPost
{
    /**
     * URL for the remote web server
     */
    private OutputStream m_outputStream = null;
    private InputStream m_inputStream = null;
    private boolean m_isConnected = false;
    private URL m_url = null;
    private HttpURLConnection m_httpUrlConnection = null;
    private HttpsURLConnection m_httpsUrlConnection = null;

    private HashMap m_headerPropertiesMap = new HashMap(11);

    TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager()
    {
        public java.security.cert.X509Certificate[] getAcceptedIssuers()
        {
            return null;
        }

        public void checkClientTrusted(X509Certificate[] certs, String authType)
        {
        }

        public void checkServerTrusted(X509Certificate[] certs, String authType)
        {
        }

    } };

    /**
     * Post the input string to the remote web server.
     * 
     * @param sRequest
     *            String to be posted
     */
    public void post(String sRequest) throws Exception
    {
        try
        {
            if (m_outputStream == null)
            {
                m_outputStream = m_httpUrlConnection.getOutputStream();
            }

            m_outputStream.write(sRequest.getBytes("UTF-8"));
            m_outputStream.flush();
        }
        catch (IOException e)
        {
            throw new Exception("WebPost::post->Exception type is " + e.getClass().toString());
        }

    }

    /**
     * Post the input string to the remote web server.
     * 
     * @param sRequest
     *            String to be posted
     */
    public void securePost(String sRequest) throws Exception
    {
        try
        {
            if (m_outputStream == null)
            {
                m_outputStream = m_httpsUrlConnection.getOutputStream();
            }

            m_outputStream.write(sRequest.getBytes("UTF-8"));
            m_outputStream.flush();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            throw new Exception("WebPost::securePost->Exception type is " + e.getClass().toString());
        }

    }

    /**
     * Connect to Web Server.
     * 
     * @param url
     *            Web Server URL
     */
    public void connect(String url) throws Exception
    {
        connect(url, null, "POST", null);
    }

    /**
     * Connect to Web Server using username and password.
     * 
     * @param username
     *            User name
     * @param password
     *            Password
     * @param url
     *            Web Server URL
     */
    public void connect(String username, String password, String url, String requestMethod) throws Exception
    {
        connect(username, password, url, null, requestMethod);
    }

    /**
     * Post input to web server using content type.
     * 
     * @param url
     *            Web server URL
     * @param contentType
     *            Context type, like text/xml.
     */
    public void connect(String url, String contentType, String requestMethod) throws Exception
    {
        try
        {
            m_url = new URL(url);

            m_httpUrlConnection = (HttpURLConnection) m_url.openConnection();

            m_isConnected = true;

            m_httpUrlConnection.setRequestMethod(requestMethod);
            m_httpUrlConnection.setDoInput(true);
            m_httpUrlConnection.setDoOutput(true);

            for (Iterator it = m_headerPropertiesMap.entrySet().iterator(); it.hasNext();)
            {
                Map.Entry entry = (Map.Entry) it.next();
                m_httpUrlConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
            }

            if (!StringUtil.isNullOrEmpty(contentType))
            {
                m_httpUrlConnection.setRequestProperty("content-type", contentType);
            }
        }
        catch (IOException e)
        {

            throw new Exception("WebPost::connect->Exception type is " + e.getClass().toString() + "Exceprion is + " + e.getMessage());
        }
    }

    /**
     * Post input to web server using content type.
     * 
     * @param url
     *            Web server URL
     * @param contentType
     *            Context type, like text/xml.
     */
    public void secureConnect(String url, String contentType, String requestMethod) throws Exception
    {
        try
        {
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier()
            {
                public boolean verify(String hostname, SSLSession session)
                {
                    return true;
                }
            };
            
            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);

            m_url = new URL(url);

            m_httpsUrlConnection = (HttpsURLConnection) m_url.openConnection();

            m_isConnected = true;

            m_httpsUrlConnection.setRequestMethod(requestMethod);
            m_httpsUrlConnection.setDoInput(true);
            m_httpsUrlConnection.setDoOutput(true);

            for (Iterator it = m_headerPropertiesMap.entrySet().iterator(); it.hasNext();)
            {
                Map.Entry entry = (Map.Entry) it.next();
                m_httpsUrlConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
            }

            if (!StringUtil.isNullOrEmpty(contentType))
            {
                m_httpsUrlConnection.setRequestProperty("content-type", contentType);
            }
        }
        catch (IOException e)
        {
            throw new Exception("WebPost::connect->Exception type is " + e.getClass().toString());
        }
    }

    /**
     * Post input to web server using user name, password, and content type.
     * 
     * @param username
     *            User name
     * @param password
     *            Password
     * @param url
     *            Web server URL
     * @param contentType
     *            Context type, like text/xml.
     */
    public void connect(String username, String password, String url, String contentType, String requestMethod) throws Exception
    {
        try
        {

            m_url = new URL(url);

            m_httpUrlConnection = (HttpURLConnection) m_url.openConnection();

            m_isConnected = true;

            m_httpUrlConnection.setRequestMethod(requestMethod);
            m_httpUrlConnection.setDoInput(true);
            m_httpUrlConnection.setDoOutput(true);

            String sEncoding = username + ":" + password;

            // There is no change from this point on
            Base64 encoder = new Base64();
            String encoding = encoder.encodeToString(sEncoding.getBytes("UTF-8"));

            String sUserNamePasswd = "Basic " + encoding;

            // Set the request property to authorize the encoded username and
            // password.
            m_httpUrlConnection.setRequestProperty("authorization", sUserNamePasswd);

            for (Iterator it = m_headerPropertiesMap.entrySet().iterator(); it.hasNext();)
            {
                Map.Entry entry = (Map.Entry) it.next();
                m_httpUrlConnection.setRequestProperty((String) entry.getKey(), (String) entry.getValue());
            }

            // Set the content type if we have one
            if (!StringUtil.isNullOrEmpty(contentType))
            {
                m_httpUrlConnection.setRequestProperty("content-type", contentType);
            }
        }
        catch (IOException e)
        {
            throw new Exception("WebPost::connect->Exception type is " + e.getClass().toString());
        }
    }

    /**
     * This method gets a HTTP header field.
     * 
     * @param header
     * @return
     */
    public String getHeaderProperty(String header)
    {
        String headerValue = "";

        if (!StringUtil.isNullOrEmpty(header))
        {
            headerValue = StringUtil.emptyIfNull(m_httpUrlConnection.getHeaderField(header));
        }

        return headerValue;
    }

    /**
     * 
     * @param header
     * @return
     */
    public List<String> getHeaderPropertyList(String header)
    {
        List<String> headerList = new ArrayList<String>();

        if (!StringUtil.isNullOrEmpty(header))
        {
            Map<String, List<String>> headers = getHeaderProperties();

            for (String headerKey : headers.keySet())
            {
                if (headerKey != null)
                {
                    if (headerKey.equalsIgnoreCase(header))
                    {
                        headerList = headers.get(header);
                    }
                }
            }
        }

        return headerList;
    }

    /**
     * 
     * @return
     */
    public Map<String, List<String>> getHeaderProperties()
    {
        return m_httpUrlConnection.getHeaderFields();
    }

    /**
     * This method sets a HTTP header field.
     * 
     * @param header
     *            Header string
     * @param value
     *            Header value
     */
    public void setHeaderProperty(String header, String value)
    {
        if (header == null)
        {
            return;
        }

        m_headerPropertiesMap.put(header, StringUtil.emptyIfNull(value));
    }

    public void setRequestProperty(String key, String value)
    {
        m_httpUrlConnection.setRequestProperty(key, value);
    }

    /**
     * 
     * @param requestParameters
     * @throws UnsupportedEncodingException
     * @throws IOException
     */
    private void addRequestParameters(String requestParameters) throws UnsupportedEncodingException, IOException
    {
        if (!StringUtil.isNullOrEmpty(requestParameters))
        {
            String encodedData = URLEncoder.encode(requestParameters, "UTF-8");

            m_httpUrlConnection.setRequestProperty("Content-Length", String.valueOf(encodedData.length()));
            OutputStream os = m_httpUrlConnection.getOutputStream();
            os.write(encodedData.getBytes());
        }
    }

    /**
     * Retrieve response from the web server. HTML header's content length field
     * must be set.
     * 
     * @throws Exception
     *             When fail to retrieve response from web server or the
     *             retrieved content does not match content length set in the
     *             HTML header field.
     * @return Retrieved response
     */
    public String receive() throws Exception
    {
        try
        {
            StringBuffer responseBuffer = new StringBuffer(500);

            if (m_inputStream == null)
            {
                m_inputStream = m_httpUrlConnection.getInputStream();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(m_inputStream, Charset.forName("UTF-8")));
            String readLine;

            while ((readLine = in.readLine()) != null)
            {
                responseBuffer.append(readLine);
            }

            return new String(responseBuffer);
        }
        catch (IOException e)
        {
            throw new Exception("WebPost::receive->Exception type is " + e.getClass().toString(), e);
        }
    }

    public String secureReceive() throws Exception
    {
        try
        {
            StringBuffer responseBuffer = new StringBuffer(500);

            if (m_inputStream == null)
            {
                m_inputStream = m_httpsUrlConnection.getInputStream();
            }

            BufferedReader in = new BufferedReader(new InputStreamReader(m_inputStream, Charset.forName("UTF-8")));
            String readLine;

            while ((readLine = in.readLine()) != null)
            {
                responseBuffer.append(readLine);
            }

            return new String(responseBuffer);
        }
        catch (IOException e)
        {
            throw new Exception("WebPost::receive->Exception type is " + e.getClass().toString(), e);
        }
    }

    /**
     * Disconnect from the web server.
     * 
     * @throws xception
     *             When fail to disconnect from the web server
     */
    public void disconnect() throws Exception
    {
        try
        {
            if (m_isConnected)
            {
                if (m_inputStream != null)
                {
                    m_inputStream.close();
                }
                if (m_outputStream != null)
                {
                    m_outputStream.close();
                }
                m_httpUrlConnection.disconnect();

                m_inputStream = null;
                m_outputStream = null;
                m_isConnected = false;
            }
        }
        catch (IOException e)
        {
            throw new Exception("WebPost::disconnect->Exception type is " + e.getClass().toString());
        }
    }

    public void secureDisconnect() throws Exception
    {
        try
        {
            if (m_isConnected)
            {
                if (m_inputStream != null)
                {
                    m_inputStream.close();
                }
                if (m_outputStream != null)
                {
                    m_outputStream.close();
                }
                m_httpsUrlConnection.disconnect();

                m_inputStream = null;
                m_outputStream = null;
                m_isConnected = false;
            }
        }
        catch (IOException e)
        {
            throw new Exception("WebPost::disconnect->Exception type is " + e.getClass().toString());
        }
    }

    /**
     * Make sure we have closed the connection
     */
    protected void finalize()
    {
        try
        {
            disconnect();
        }
        catch (Exception ex)
        {
            // do nothing
        }
    }
}
