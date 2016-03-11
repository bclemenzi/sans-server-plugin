package com.nfbsoftware.sansserverplugin.sdk.aws;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.commons.io.IOUtils;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.HttpMethod;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.GetObjectMetadataRequest;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.nfbsoftware.sansserverplugin.sdk.util.Entity;
import com.nfbsoftware.sansserverplugin.sdk.util.StringUtil;

/**
 * 
 * @author Brendan Clemenzi
 */
public class AmazonS3Manager
{
    protected Log m_logger;
    
    private String m_regionName;
    private String m_bucketName;
    private AmazonS3 m_amazonS3Client;
    private Properties m_properties;

    /**
     * 
     * @param logger
     * @param properties
     */
    public AmazonS3Manager(Properties properties)
    {
        m_logger = LogFactory.getLog(this.getClass());
        
        m_properties = properties;
        
        String regionName = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_REGION));
        String accessKey = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_ACCESS_KEY));
        String secretKey = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_SECRET_KEY));
        String bucketName = StringUtil.emptyIfNull(m_properties.getProperty(Entity.FrameworkProperties.AWS_S3_BUCKET_NAME));

        // Save out bucket name for use later
        m_bucketName = bucketName;

        // Init our aws credentials
        AWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);
        m_amazonS3Client = new AmazonS3Client(credentials);

        // Set our region
        m_amazonS3Client.setRegion(Region.getRegion(Regions.fromName(regionName)));
    }

    /**
     * 
     * @throws Exception
     */
    public String getWebsitePath()
    {
        String path = m_bucketName + ".s3-website-" + m_regionName + ".amazonaws.com";
        
        return path;
    }

    /**
     * 
     * @throws Exception
     */
    public String getConfiguredPath(String folderName)
    {
        String path = "https://s3.amazonaws.com/" + m_bucketName + "/" + folderName + "/";
        return path;
    }

    /**
     * 
     * @param fileName
     * @param fileObject
     * @throws Exception
     */
    public void uploadFile(String folderName, String fileName, File fileObject) throws Exception
    {
        uploadFile(folderName, fileName, fileObject, false);
    }

    /**
     * 
     * @param fileName
     * @param fileObject
     * @throws Exception
     */
    public void uploadFile(String folderName, String fileName, File fileObject, boolean skipScan) throws Exception
    {
        boolean isSuccessful = uploadToAwsS3(folderName, fileName, fileObject, skipScan);

        if (!isSuccessful)
        {
            throw new Exception("Error uploading file to Amazon S3 bucket: " + folderName + "  File Name: " + fileName);
        }
    }

    /**
     * 
     * @param fileName
     * @return
     * @throws Exception
     */
    public File downloadFile(String folderName, String fileName) throws Exception
    {
        File fileObject = downloadFromAwsS3(folderName, fileName);

        m_logger.info("********************* Download From bucket " + folderName + "fileName " + fileName);

        if (fileObject == null)
        {
            throw new Exception("Error downloading file from Amazon S3 bucket: " + folderName + "  File Name: " + fileName);
        }

        return fileObject;
    }

    /**
     * 
     * @param folderName
     * @param fileName
     * @return
     * @throws Exception
     */
    public byte[] downloadContentAsBytes(String folderName, String fileName) throws Exception
    {
        byte[] fileBytes = downloadFromAwsS3AsBytes(folderName, fileName);

        if (fileBytes == null)
        {
            throw new Exception("Error downloading file bytes from Amazon S3 bucket: " + folderName + "  File Name: " + fileName);
        }

        return fileBytes;
    }

    /**
     * 
     * @param fileName
     * @throws Exception
     */
    public void deleteFile(String folderName, String fileName) throws Exception
    {
        boolean isSuccessful = deleteInAwsS3(folderName, fileName);

        if (!isSuccessful)
        {
            throw new Exception("Error deleting file from Amazon S3 bucket: " + folderName + "  File Name: " + fileName);
        }
    }

    /**
     * 
     * @param folderName
     * @param originalFileName
     * @param newFileName
     * @throws Exception
     */
    public void copyFile(String folderName, String originalFileName, String newFileName) throws Exception
    {
        try
        {
            m_logger.error("Copying remote file from " + originalFileName + " to " + newFileName);

            String tempOriginalName = folderName + "/" + originalFileName;
            String tempNewName = folderName + "/" + newFileName;

            m_amazonS3Client.copyObject(m_bucketName, tempOriginalName, m_bucketName, tempNewName);
        }
        catch (AmazonServiceException exception)
        {
            m_logger.error("Caught an AmazonServiceException, which means your request made it  to Amazon S3, but was rejected with an error response for some reason.");
            m_logger.error("Error Message: " + exception.getMessage());
            m_logger.error("HTTP  Code: " + exception.getStatusCode());
            m_logger.error("AWS Error Code:" + exception.getErrorCode());
            m_logger.error("Error Type:    " + exception.getErrorType());
            m_logger.error("Request ID:    " + exception.getRequestId());
        }
        catch (AmazonClientException ace)
        {
            m_logger.error("Caught an AmazonClientException, which means the client encountered an internal error while trying to communicate with S3, such as not being able to access the network.");
            m_logger.error("Error Message: " + ace.getMessage());
        }
    }

    /**
     * 
     * @param folderName
     * @param fileName
     * @param fileObject
     * @return
     * @throws IOException
     */
    private boolean uploadToAwsS3(String folderName, String fileName, File fileObject, boolean skipScan)
    {
        boolean uploadSuccessful = false;

        if ((!StringUtil.isNullOrEmpty(fileName)) && (fileObject != null))
        {
            String tempName = folderName + "/" + fileName;

            try
            {
                PutObjectRequest objectRequest = new PutObjectRequest(m_bucketName, tempName, fileObject);

                m_amazonS3Client.putObject(objectRequest);

                uploadSuccessful = true;
            }
            catch (AmazonServiceException ase)
            {
                m_logger.error("Caught an AmazonServiceException, which means the request made it to Amazon S3, but was rejected with an error response. File Name: "
                        + tempName);
                m_logger.error("Error Message:    " + ase.getMessage());
                m_logger.error("HTTP Status Code: " + ase.getStatusCode());
                m_logger.error("AWS Error Code:   " + ase.getErrorCode());
                m_logger.error("Error Type:       " + ase.getErrorType());
                m_logger.error("Request ID:       " + ase.getRequestId());
            }
            catch (AmazonClientException ace)
            {
                m_logger.error("Caught an AmazonClientException, which means the client encountered an internal problem while trying to communicate with S3. File Name: "
                        + tempName);
                m_logger.error("Error Message: " + ace.getMessage());
            }
            catch (Exception ex)
            {
                m_logger.error("ERROR FileServiceImpl.uploadToAwsS3: File Name: " + tempName + ", " + ex.getMessage());
            }
        }

        return uploadSuccessful;
    }

    /**
     * 
     * @param fileName
     * @return
     * @throws IOException
     */
    private File downloadFromAwsS3(String folderName, String fileName)
    {
        File fileObject = null;

        if (!StringUtil.isNullOrEmpty(fileName))
        {
            String tempName = folderName + "/" + fileName;

            try
            {
                GetObjectRequest objectRequest = new GetObjectRequest(m_bucketName, tempName);

                S3Object tmpFileObject = m_amazonS3Client.getObject(objectRequest);

                int pos = fileName.lastIndexOf(".");
                String fileExt = fileName.substring(pos + 1, fileName.length());
                fileObject = File.createTempFile(fileName, "." + fileExt);

                OutputStream outputStream = new FileOutputStream(fileObject);

                byte buf[] = new byte[1024];
                int len;

                while ((len = tmpFileObject.getObjectContent().read(buf)) > 0)
                {
                    outputStream.write(buf, 0, len);
                }

                outputStream.close();
            }
            catch (AmazonServiceException ase)
            {
                m_logger.error("Caught an AmazonServiceException, which means the request made it to Amazon S3, but was rejected with an error response. File Name: "
                        + fileName);
                m_logger.error("Error Message:    " + ase.getMessage());
                m_logger.error("HTTP Status Code: " + ase.getStatusCode());
                m_logger.error("AWS Error Code:   " + ase.getErrorCode());
                m_logger.error("Error Type:       " + ase.getErrorType());
                m_logger.error("Request ID:       " + ase.getRequestId());
            }
            catch (AmazonClientException ace)
            {
                m_logger.error("Caught an AmazonClientException, which means the client encountered an internal problem while trying to communicate with S3. File Name: "
                        + fileName);
                m_logger.error("Error Message: " + ace.getMessage());
            }
            catch (Exception ex)
            {
                m_logger.error("ERROR FileServiceImpl.downloadFromAwsS3: File Name: " + tempName + ", " + ex.getMessage());
            }
        }

        return fileObject;
    }

    /**
     * 
     * @param fileName
     * @return
     * @throws IOException
     */
    private byte[] downloadFromAwsS3AsBytes(String folderName, String fileName)
    {
        byte[] fileBytes = null;

        if (!StringUtil.isNullOrEmpty(fileName))
        {
            String tempName = folderName + "/" + fileName;

            try
            {
                GetObjectRequest objectRequest = new GetObjectRequest(m_bucketName, tempName);

                S3Object tmpFileObject = m_amazonS3Client.getObject(objectRequest);

                InputStream inputStream = new BufferedInputStream(tmpFileObject.getObjectContent());
                fileBytes = IOUtils.toByteArray(inputStream);
                inputStream.close();
            }
            catch (AmazonServiceException ase)
            {
                m_logger.error("Caught an AmazonServiceException, which means the request made it to Amazon S3, but was rejected with an error response. File Name: "
                        + fileName);
                m_logger.error("Error Message:    " + ase.getMessage());
                m_logger.error("HTTP Status Code: " + ase.getStatusCode());
                m_logger.error("AWS Error Code:   " + ase.getErrorCode());
                m_logger.error("Error Type:       " + ase.getErrorType());
                m_logger.error("Request ID:       " + ase.getRequestId());
            }
            catch (AmazonClientException ace)
            {
                m_logger.error("Caught an AmazonClientException, which means the client encountered an internal problem while trying to communicate with S3. File Name: "
                        + fileName);
                m_logger.error("Error Message: " + ace.getMessage());
            }
            catch (Exception ex)
            {
                m_logger.error("ERROR FileServiceImpl.downloadFromAwsS3: File Name: " + tempName + ", " + ex.getMessage());
            }
        }

        return fileBytes;
    }

    /**
     * 
     * @param fileName
     * @return
     */
    private boolean deleteInAwsS3(String folderName, String fileName)
    {
        boolean deleteSuccessful = false;

        if (!StringUtil.isNullOrEmpty(fileName))
        {
            String tempName = folderName + "/" + fileName;

            try
            {
                DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(m_bucketName, tempName);

                m_amazonS3Client.deleteObject(deleteObjectRequest);

                // Check that it was deleted by looking at the meta data.
                // Then take what code the error message gives back to determine
                // if delete succeeded.
                try
                {
                    GetObjectMetadataRequest getObjectMetadataRequest = new GetObjectMetadataRequest(m_bucketName, tempName);
                    m_amazonS3Client.getObjectMetadata(getObjectMetadataRequest);
                }
                catch (AmazonServiceException ase)
                {
                    if (ase.getStatusCode() == 404)
                    {
                        deleteSuccessful = true;
                    }
                }
            }
            catch (AmazonServiceException ase)
            {
                m_logger.error("Caught an AmazonServiceException, which means the request made it to Amazon S3, but was rejected with an error response. Content Name: "
                        + tempName);
                m_logger.error("Error Message:    " + ase.getMessage());
                m_logger.error("HTTP Status Code: " + ase.getStatusCode());
                m_logger.error("AWS Error Code:   " + ase.getErrorCode());
                m_logger.error("Error Type:       " + ase.getErrorType());
                m_logger.error("Request ID:       " + ase.getRequestId());
            }
            catch (AmazonClientException ace)
            {
                m_logger.error("Caught an AmazonClientException, which means the client encountered an internal problem while trying to communicate with S3. Content Name: "
                        + tempName);
                m_logger.error("Error Message: " + ace.getMessage());
            }
            catch (Exception ex)
            {
                m_logger.error("ERROR in FileServiceImpl.deleteInAwsS3: Content ID: " + tempName + ", " + ex.getMessage());
            }
        }

        return deleteSuccessful;
    }
    
    /**
     * Generates a pre-signed access url for the resource stored in Amazon S3
     * 
     * @param folderName
     * @param fileName
     * @throws Exception
     */
    public String getPreSignedUrl(String folderName, String fileName) throws Exception
    {
        try
        {
            m_logger.info("Generating pre-signed URL.");
            
            java.util.Date expiration = new java.util.Date();
            long milliSeconds = expiration.getTime();
            milliSeconds += 1000 * 60 * 60; // Add 1 hour.
            expiration.setTime(milliSeconds);
            
            String tempName = folderName + "/" + fileName;

            GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(m_bucketName, tempName);
            generatePresignedUrlRequest.setMethod(HttpMethod.GET);
            generatePresignedUrlRequest.setExpiration(expiration);

            URL url = m_amazonS3Client.generatePresignedUrl(generatePresignedUrlRequest);

            m_logger.debug("PreSigned Url Generated for " + fileName + ":    " + url.toString());
            return url.toString();
        }
        catch (AmazonServiceException exception)
        {
            m_logger.error("Caught an AmazonServiceException, which means your request made it  to Amazon S3, but was rejected with an error response for some reason.");
            m_logger.error("Error Message: " + exception.getMessage());
            m_logger.error("HTTP  Code: " + exception.getStatusCode());
            m_logger.error("AWS Error Code:" + exception.getErrorCode());
            m_logger.error("Error Type:    " + exception.getErrorType());
            m_logger.error("Request ID:    " + exception.getRequestId());
        }
        catch (AmazonClientException ace)
        {
            m_logger.error("Caught an AmazonClientException, which means the client encountered an internal error while trying to communicate with S3, such as not being able to access the network.");
            m_logger.error("Error Message: " + ace.getMessage());
        }
        
        return null;
    }
}
