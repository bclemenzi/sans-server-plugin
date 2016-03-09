# sans-server-plugin
A maven plugin used to configure and deploy your sans-server based applications.

Features
--------

  * Published on Maven Central Repository
  
Getting started
---------------
Including the Maven plugin in your project by adding the following configuration to your `pom.xml`:

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
                         	  				<goal>deploy-lambda-gateway</goal>
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
-----
The plugin is launched during the install phase of your build.  This is to ensure we have all the required artifacts to properly deploy your SansServer-based application.  

When executing your Maven build, make sure to include the "install" goal with your list of build goals.