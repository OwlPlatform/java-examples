Sensor Signal Parser Solver
===========================

Author: Robert Moore

Last Updated: April 12, 2014


## Introduction ##
A very basic solver that connects to an Aggregator to retrieve Samples.  Each
Sample is parsed and the Received Signal Strength Indicator (RSSI) is
extracted and sent on to a World Model where it is made available as an
"on-demand" data type for other solvers and applications.

## Requirements ##
I will assume that you have access to a functional Aggregator, World Model,
Pipsqueak sensors (or other sensors that produce RSSI data).  Here is some
other software you need:

* Apache Maven 3

## Writing the POM ##
Maven will take care of all the heavy lifting for dependency resolution and
compilation for this tutorial.  I have to assume you have access to the
standard Maven central repositories.  Owl Platform publishes to Maven Central
through Sonatype.  Below is the pom.xml for our project:

    <project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
      <modelVersion>4.0.0</modelVersion>

      <groupId>localhost.example</groupId>
      <artifactId>owl-rssi-solver</artifactId>
      <packaging>jar</packaging>
      <version>1.0.0-SNAPSHOT</version>

      <properties>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      </properties>

      <dependencies>
        <dependency>
          <groupId>com.owlplatform</groupId>
          <artifactId>owl-worldmodel</artifactId>
          <version>1.0.5</version>
          <type>jar</type>
        </dependency>
        <dependency>
          <groupId>com.owlplatform</groupId>
          <artifactId>owl-solver</artifactId>
          <version>1.0.4</version>
          <type>jar</type>
        </dependency>
      </dependencies>

      <!-- The build section will configure the JAR output -->
      <build>
        <plugins>
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-assembly-plugin</artifactId>
            <executions>
              <execution>
                <id>build-jar-with-dependencies</id>
                <phase>package</phase>
                <goals>
                  <goal>single</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
        </plugins>
        <pluginManagement>
          <plugins>
            <!-- Required because of Apache Mina dependency 
            <plugin>
              <groupId>org.apache.felix</groupId>
              <artifactId>maven-bundle-plugin</artifactId>
              <version>2.3.7</version>
            </plugin>-->
            <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-compiler-plugin</artifactId>
              <version>3.1</version>
              <configuration>
                <source>1.7</source>
                <target>1.7</target>
              </configuration>
            </plugin>
            <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-jar-plugin</artifactId>
              <version>2.4</version>
              <configuration>
                <archive>
                  <manifest>
                    <addClasspath>true</addClasspath>
                    <mainClass>ex.owl.solver.rssi.Main</mainClass>
                  </manifest>
                </archive>
              </configuration>
            </plugin>
            <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-assembly-plugin</artifactId>
              <version>2.4</version>
              <configuration>
                <descriptorRefs>
                  <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
              </configuration>
            </plugin>
          </plugins>
        </pluginManagement>
      </build>
    </project>


