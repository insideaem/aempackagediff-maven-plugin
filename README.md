# aempackagediff-maven-plugin
Maven plugin to generate an AEM Package only containing the changes between two commits, thus enabling to build a diff package

##How it works?
When executed the plugin calls an external process that will generate the list of changes between the two specified commits.
Then the configured filter.xml file is changed so that it contains the filter definition only for the changed files

##Configuration
Here is a sample configuration of the plugin:

  <profile>
  	<id>aempackagediff</id>
  	<build>
  		<plugins>
  			<plugin>
  				<groupId>com.insideaem.maven.plugin</groupId>
  				<artifactId>aempackagediff</artifactId>
  				<executions>
  					<execution>
  						<id>aempackagediff</id>
  						<phase>prepare-package</phase>
  						<goals>
  							<goal>aempackagediff</goal>
  						</goals>
  					</execution>
  				</executions>
  				<configuration>
  					<outputDirectory>${project.build.directory}/aempackagediff</outputDirectory>
  				</configuration>
  			</plugin>
  			
  			<plugin>
  				<groupId>com.day.jcr.vault</groupId>
  				<artifactId>content-package-maven-plugin</artifactId>
  				<extensions>true</extensions>
  				<configuration>
  					<filterSource>target/aempackagediff/filter.xml</filterSource>
  				</configuration>
  			</plugin>
  		</plugins>
  	</build>
  </profile>
