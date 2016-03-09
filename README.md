# SansServer Maven Plugin
A maven plugin used to configure and deploy your SansServer-based applications.  To learn more about SansServer-based applications, visit:  https://github.com/bclemenzi/sans-server

Features
--------

  * Published on Maven Central Repository
  
Getting started
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
                        <phase>install</phase>
                        <goals>
                        	<goal>deploy-lambda-gateway</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
		</plugins>
	</build>
```

Usage
--------
The plugin is launched during the install phase of your build.  This is to ensure we have all the required artifacts to properly deploy your SansServer-based application.  

When executing your Maven build, make sure to include the "install" goal with your list of build goals.

Requirements
--------
The sans-server-plugin expects there to be project.properties file found under your project's ${project.basedir} folder.

Available Goals
--------
 * deploy-lambda-gateway
  * Creates our S3 bucker for hosting your SansServer-based application
  * A deployment folder used to store the deployed versions of our Lambda functions.  The default is set to:  bucket_name/deploy
  * Configures the bucket for Static Website Hosting setting the Index Doc to "index.html" and the Error Doc to "error.html"
  * Creates a bucket policy statement that allows s3:GetObject on "arn:aws:s3:::bucket_name/*"
  * Creates a bucket policy statement that denies s3:GetObject on "arn:aws:s3:::bucket_name/deploy/*"
  
  