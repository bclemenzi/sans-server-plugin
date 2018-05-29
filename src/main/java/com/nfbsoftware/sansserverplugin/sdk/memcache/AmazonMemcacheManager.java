package com.nfbsoftware.sansserverplugin.sdk.memcache;

import java.net.InetSocketAddress;
import java.util.Properties;

import com.nfbsoftware.sansserverplugin.sdk.util.Entity;
import com.nfbsoftware.sansserverplugin.sdk.util.StringUtil;

import net.spy.memcached.MemcachedClient;

/**
 * The AmazonMemcacheManager...
 * 
 * @author Brendan Clemenzi
 */
public class AmazonMemcacheManager
{
    private MemcachedClient m_amazonMemcachedClient;
    private Properties m_properties;
    
    /**
     * 
     * @param properties
     */
    public AmazonMemcacheManager(Properties properties) throws Exception
    {
        m_properties = properties;
        
        String memcachedHost = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_MEMCACHED_HOST));
        String memcachedPortString = StringUtil.replaceIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_MEMCACHED_PORT), "11211");
        int memcachedPort = new Integer(memcachedPortString).intValue();

        m_amazonMemcachedClient = new MemcachedClient(new InetSocketAddress(memcachedHost, memcachedPort));
    }
    
    /**
     * 
     * @param key
     * @return
     */
    public Object get(String key)
    {
        return m_amazonMemcachedClient.get(key);
    }
    
    /**
     * 
     * @param key
     * @param value
     * @param expiration
     */
    public void set(String key, Object value, Integer expiration)
    {
        m_amazonMemcachedClient.set(key, expiration, value);
    }
    
    /**
     * 
     * @param key
     */
    public void delete(String key)
    {
        m_amazonMemcachedClient.delete(key);
    }
}
