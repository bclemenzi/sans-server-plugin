# SansServer Plugin
The SansServer Plugin serves two purposed, one as a development SDK and the other as a Maven plugin to build, provision, and deploy SansServer-based applications.  To learn more about SansServer-based applications, visit:  https://github.com/bclemenzi/sans-server

Features
--------

  * Custom Java Annotations for AWS configurations
  * Published on Maven Central Repository
  
Getting started with the SDK
---------------
To use the features provided by
```xml
	<dependencies>
			.
			. OTHER DEPENDENCIES
			.
		<dependency>
			<groupId>com.nfbsoftware</groupId>
			<artifactId>sans-server-plugin</artifactId>
			<version>1.0.0</version>
		</dependency>
	</dependencies>
```

Available Annotations in the SDK
---------------
 * @AWSLambda
  * Used to identify class files used for Java-based Lambda functions
  * @AwsLambda(name="View User", desc="Function to view a given user record", handlerMethod="handleRequest")
 * @AwsLambdaWithGateway - COMING SOON
  * Used to identify class files used for Java-based Lambda functions with an API Gateway
  * @AwsLambdaWithGateway(name="Authenticate User", desc="Custom authentication service", handlerMethod="handleRequest", resourceName="Login", resourcePath="/login", method=AwsLambdaWithGateway.MethodTypes.POST, authorization=AwsLambdaWithGateway.AuthorizationTypes.OPEN, keyRequired=false, enableCORS=true)
  
Getting started with the Maven Plugin
---------------
Including the Maven plugin in your project by adding the following configuration to your `pom.xml`:

```xml
	<build>
		<plugins>
			.
			. OTHER PLUGINS
			.
			<plugin>
                <groupId>com.nfbsoftware</groupId>
				<artifactId>sans-server-plugin</artifactId>
				<version>1.0.0</version>
            	<executions>
                	<execution>
                		<id>first-execution</id>
                    	<goals>
                        	<goal>build-properties</goal>
                    	</goals>
                    	<phase>generate-resources</phase>
                	</execution>
                	<execution>
                		<id>second-execution</id>
                    	<goals>
                        	<goal>deploy-lambda</goal>
                    	</goals>
                    	<phase>install</phase>
                	</execution>
            	</executions>
            </plugin>
		</plugins>
	</build>
```

Maven Plugin Usage
--------
The plugin is launched during the install phase of your build.  This is to ensure we have all the required artifacts to properly deploy your SansServer-based application.  

When executing your Maven build, make sure to include the "install" goal with your list of build goals.

Maven Plugin Requirements
--------
The sans-server-plugin expects there to be build.properties file found under your project's ${project.basedir} folder.  The following chart will explain each of the file's properties:

TODO - Add Chart

Available Maven Goals
--------
 * deploy-lambda-gateway
  * Creates our S3 bucker for hosting your SansServer-based application
  * A deployment folder used to store the deployed versions of our Lambda functions.  The default is set to:  bucket_name/deploy
  * Configures the bucket for Static Website Hosting setting the Index Doc to "index.html" and the Error Doc to "error.html"
  * Creates a bucket policy statement that allows s3:GetObject on "arn:aws:s3:::bucket_name/*"
  * Creates a bucket policy statement that denies s3:GetObject on "arn:aws:s3:::bucket_name/deploy/*"
  
  