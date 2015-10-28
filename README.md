# aempackagediff-maven-plugin
Maven plugin to generate an AEM Package only containing the changes between two commits, thus enabling to build a diff package

##How it works?
When executed the plugin calls an external process that will generate the list of changes between the two specified commits.
Then the configured filter.xml file is changed so that it contains the filter definition only for the changed files

##Configuration
Here is a sample configuration of the plugin:
 ```xml 
  <profile>
    <!-- We define a custom profile for this, so that it is not executed by default -->
  	<id>aempackagediff</id>
  	<build>
  		<plugins>
  			<plugin>
  				<groupId>com.insideaem.maven.plugin</groupId>
  				<artifactId>aempackagediff</artifactId>
  				<executions>
  					<execution>
  						<id>aempackagediff</id>
  						<!-- We bind the execution of the aempackagediff goal to the prepare-package phase -->
  						<phase>prepare-package</phase>
  						<goals>
  							<goal>aempackagediff</goal>
  						</goals>
  					</execution>
  				</executions>
  				<configuration>
  				  <!-- This is key to the execution: Here we configure the command that will return the changed files. This can also be passed as a maven property -Daempackagediff.diffCmd when triggered through a CI tool like Bamboo or Jenkins. You can therefore have a step in the CI that computes the commit from the last build and the one of the current and pass it as parameter. In this case simply remove diffCmd from the xml and call maven with -Daempackagediff.diffCmd -->
  				  <diffCmd>git diff --name-only HEAD~3 HEAD~1 or git diff --name-only SHA1 SHA2</diffCmd>
  				  <!-- This is the directory where the newly generated filter.xml file will be created created-->
  					<outputDirectory>${project.build.directory}/aempackagediff</outputDirectory>
  				</configuration>
  			</plugin>
  			
  			<plugin>
  				<groupId>com.day.jcr.vault</groupId>
  				<artifactId>content-package-maven-plugin</artifactId>
  				<extensions>true</extensions>
  				<configuration>
  				  <!-- This must point to the directory defined in outputDirectory so that the content-package plugin uses that filter.xml file -->
  					<filterSource>target/aempackagediff/filter.xml</filterSource>
  				</configuration>
  			</plugin>
  		</plugins>
  	</build>
  </profile>
```
