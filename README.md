SansServer Plugin/SDK
==============

The SansServer Plugin/SDK provides developers with a tool set to allow them to rapidly build and deploy scalable and highly available applications without the hassle of maintaining or paying for servers.  It leverages Amazon Web Services to allow development teams to go "serverless".  By “serverless”, we mean no explicit infrastructure required, as in: no servers, no deployments onto servers, no installed software of any kind. We’ll use only managed cloud services and a laptop.

To see an application built using this framework, please visit our SansServer project (https://github.com/bclemenzi/sans-server).

Plugin/SDK Features
--------

  * Custom Java Annotations for the installation and configuration of AWS artifacts
   * Lambda Functions (Java8)
   * AWS Gateway APIs used for RESTful access to your Lambda functions with public deployment
   * AWS S3 Storage
  * SansServer SDK includes a number of Java wrapper classes to make working with the AWS SDK easier.
   * AmazonS3Manager
   * AmazonSESManager
   * AmazonGatewayManager
   * AmazonCognitoManager
   * AbstractDynamoDbDao
  * JUnit test harness for locally testing your Lambda functions before deployment
  * Configuration schema to allow for the isolation of multiple deployments:  Multi-Engineer Development, QA, Production
  * Published on Maven Central Repository
  
Questions or Comments
--------
Join the conversation on Gitter by clicking on the badge below:

[![Join the chat at https://gitter.im/bclemenzi/sans-server-plugin](https://badges.gitter.im/Join%20Chat.svg)](https://gitter.im/bclemenzi/sans-server-plugin?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)
  
Getting started with the SDK
---------------
To use the features provided by the SDK, include the following dependency to your project's pom.xml
```xml
	<dependencies>
			.
			. OTHER DEPENDENCIES
			.
		<dependency>
			<groupId>com.nfbsoftware</groupId>
			<artifactId>sans-server-plugin</artifactId>
			<version>1.0.46</version>
		</dependency>
	</dependencies>
```

Available Annotations in the SDK
---------------
 * @AWSLambda
  * Used to identify class files used for Java-based Lambda functions
  * @AwsLambda(name="ViewUser", desc="Function to view a given user record", handlerMethod="handleRequest")
  * @AwsLambda(name="ViewUser", desc="Function to view a given user record", handlerMethod="handleRequest", memorySize="512", timeout="60")
  * Example:  https://github.com/bclemenzi/sans-server/blob/master/src/main/java/com/nfbsoftware/sansserver/user/lambda/CreateUser.java
  
 * @AwsLambdaWithGateway
  * Used to identify class files used for Java-based Lambda functions with an API Gateway
  * @AwsLambdaWithGateway(name="AuthenticateUser", desc="Custom authentication service", handlerMethod="handleRequest", memorySize="512", timeout="60", resourceName="Login", method=AwsLambdaWithGateway.MethodTypes.POST, authorization=AwsLambdaWithGateway.AuthorizationTypes.OPEN, keyRequired=false, enableCORS=true)
  * @AwsLambdaWithGateway(name="AuthenticateUser", desc="Custom authentication service", handlerMethod="handleRequest", resourceName="Login", method=AwsLambdaWithGateway.MethodTypes.POST, authorization=AwsLambdaWithGateway.AuthorizationTypes.OPEN, keyRequired=false, enableCORS=true)
  * Example: https://github.com/bclemenzi/sans-server/blob/master/src/main/java/com/nfbsoftware/sansserver/user/lambda/AuthenticateUser.java
  
* @AWSLambdaScheduled - COMING SOON
 * Used to identify class files used for Java-based Lambda functions that execute on a scheduled basis.  These are great for those jobs that you need to run while humans are off living their lives. 
 * @AwsLambdaScheduled(name="WorkQueue", desc="A scheduled function to process a queue", handlerMethod="handleRequest", cronExpression="0 0 12 1/1 * ? *")
  
Available AWS utility classes in the SDK
---------------
 * AmazonCognitoManager
 * AmazonS3Manager
 * AmazonSESManager
 * AbstractDynamoDbDao
 * BaseLambdaHandler
  
Getting started with the Maven Plugin
---------------
Including the Maven plugin in your project by adding the following configuration to your project's pom.xml

```xml
	<build>
		<plugins>
			.
			. OTHER PLUGINS
			.
			<plugin>
                <groupId>com.nfbsoftware</groupId>
				<artifactId>sans-server-plugin</artifactId>
				<version>1.0.46</version>
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
                	<execution>
                		<id>third-execution</id>
                    	<goals>
                        	<goal>deploy-webapp</goal>
                    	</goals>
                    	<phase>site-deploy</phase>
                	</execution>
            	</executions>
            </plugin>
		</plugins>
	</build>
```

If you are running your Maven builds from inside Eclipse, you may need to add the following plugin management to configure our lifecycle mappings

```xml
	<build>
		<pluginManagement>
			<plugins>
				<plugin>
					<groupId>org.eclipse.m2e</groupId>
                	<artifactId>lifecycle-mapping</artifactId>
                	<version>1.0.0</version>              
                	<configuration>
                  		<lifecycleMappingMetadata>
                     		<pluginExecutions>
                     			<pluginExecution>                     
                         			<pluginExecutionFilter>
                           				<groupId>com.nfbsoftware</groupId>
                           				<artifactId>sans-server-plugin</artifactId>
                           				<versionRange>[1.0,)</versionRange>                
                         				<goals>
                         	  				<goal>build-properties</goal>
                         	  				<goal>deploy-lambda</goal>
                         	  				<goal>deploy-webapp</goal>
                           				</goals>
                         			</pluginExecutionFilter>
                         			<action>
                           				<ignore />
                         			</action>
                       			</pluginExecution>
					   			<pluginExecution>
                         			<pluginExecutionFilter>
                           				<groupId>org.apache.maven.plugins</groupId>
						   				<artifactId>maven-resources-plugin</artifactId>
                           				<versionRange>[1.0,)</versionRange>
                           				<goals>
                              				<goal>resources</goal>
                              				<goal>testResources</goal>
                           				</goals>
                         			</pluginExecutionFilter>
                         			<action>
                           				<ignore />
                         			</action>
                       			</pluginExecution>
                       			<pluginExecution>
                         			<pluginExecutionFilter>
                           				<groupId>org.codehaus.mojo</groupId>
						   				<artifactId>aspectj-maven-plugin</artifactId>
                           				<versionRange>[1.0.1,)</versionRange>
                           				<goals>
                              				<goal>test-compile</goal>
                              				<goal>compile</goal>
                           				</goals>
                         			</pluginExecutionFilter>
                         			<action>
                           				<ignore />
                         			</action>
                       			</pluginExecution>                       
                     		</pluginExecutions>
                  		</lifecycleMappingMetadata>                
                	</configuration>
            	</plugin>
          	</plugins>
      	</pluginManagement>
      	<plugins>
			.
			. BUILD PLUGINS, INCLUDING OUR SANS-SERVER-PLUGIN 
			.
		</plugins>
	</build>
```

Maven Plugin Usage
--------
The plugin is launched during the install phase of your build.  This is to ensure we have all the required artifacts to properly deploy your SansServer-based application.  

When executing your Maven build, make sure to include the "install" goal with your list of build goals.

Available Maven Goals
--------
 * build-properties
  * Converts the required build.properties into our runtime properties file used by our Lambda functions
 * deploy-lambda
  * Creates our S3 bucket for hosting your SansServer-based application
  * Creates a deployment folder used to store the deployed versions of our Lambda functions.  The default is set to:  "deploy".  Deployment JARs are removed from this directory once the functions have been created/updated in AWS.
  * Creates a bucket policy statement that allows s3:GetObject on "arn:aws:s3:::<bucket_name>/*"
 * deploy-webapp
  * Creates our S3 bucket for hosting your SansServer-based application
  * Configures the bucket for Static Website Hosting setting the Index Doc to "index.html" and the Error Doc to "error.html"
  * Creates a bucket policy statement that allows s3:GetObject on "arn:aws:s3:::<bucket_name>/*"
  * Uploads the contents of the project's src/main/webapp folder to your configured S3 bucket.

SansServer-Plugin Requirements
--------
The SansServer-Plugin (SDK/Maven) expects there to be build.properties file found under your project's ${project.basedir} folder.  This project includes a template file (build.properties.template) as an example of a properties file.  The following outline will explain each of the file's properties:

 * environment.namePrefix
  * The environment.namePrefix value is used to isolate multiple deployments.  For example, a production .vs. development deployment.
  * I recommend creating an environment name prefix to isolate multiple environments QA, PRODUCTION, or developer's name to isolate this environment in AWS
 * environment.siteUrl
  * The domain CNAME you have created as an alias to your S3 bucket defined in this file under aws.s3.bucketName
 * aws.region
  * This is the String value of Amazon's region that your deployment should be provisioned
 * aws.accountId
  * Your AWS account ID.  This can be found on the AWS Console's Support page.
 * aws.accessKey
  * Your user's AWS access key.
 * aws.secretKey
  * Your user's AWS secret access key.
 * aws.cognito.identityPoolId
  * The pool id for AWS Cognito instance
  * Amazon Cognito allows you to store user data for your mobile apps, such as user preferences, mobile login, and game state, in the Amazon Cognito sync store. You can then sync this data across a user’s devices to help make their experience consistent across their devices. Amazon Cognito can automatically send a push notification to a user’s devices when data in the sync store changes.
  * When setting up Cognito, you will be creating two new IAM Roles in AWS.  A recommended Auth/Unauth Role Policy for a new IAM Role can be found under this project at /IAM/Roles/Cognito-Auth-Policy.json and /IAM/Roles/Cognito-Unauth-Policy.json.  Make sure to update the auto-generated Role policy files with these settings.
 * aws.cognito.providerName
 * aws.lambda.roleArn
  * The ARN created for executing Lambda functions
  * A recommended Role Policy for a new IAM Role can be found under this project at /IAM/Roles/Lambda-Basic-Execution-Policy.json
 * aws.s3.bucketName
  * The name of your projects S3 bucket.  If this bucket doesn't exist, the build process will create it for you.
  * The S3 bucket will be used for deployment artifacts and static web files used in the UI side of the SansServer framework 
 * aws.s3.deploymentFolder
  * The name of the S3 bucket folder used to upload the Lambda deployment JAR
 * aws.ses.replyEmailAddress
  * The email address to use as your "from" address when sending email through the service

Along with the required build.properties file.  The sans-server-plugin assumes that your Maven project follows the following folder structure at a minimum:

```xml
	-- project folder (name of your choosing)
	---- src
	------ main
	-------- java
	-------- resources
	-------- webapp
	---------- static
	---------- index.html
	---------- error.html
	------ test
	-------- java
	-------- resources
	---- build.properties
	---- pom.xml
```


  
  