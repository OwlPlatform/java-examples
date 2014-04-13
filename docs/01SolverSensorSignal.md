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
          <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-jar-plugin</artifactId>
            <configuration>
              <archive>
                <manifest>
                  <addClasspath>true</addClasspath>
                  <mainClass>ex.owl.solver.rssi.Main</mainClass>
                </manifest>
              </archive>
            </configuration>
          </plugin>
        </plugins>
        <pluginManagement>
          <plugins>
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
            </plugin>
            <plugin>
              <groupId>org.apache.maven.plugins</groupId>
              <artifactId>maven-assembly-plugin</artifactId>
              <version>2.4</version>
              <configuration>
                <descriptorRefs>
                  <descriptorRef>jar-with-dependencies</descriptorRef>
                </descriptorRefs>
                <archive>
                  <manifest>
                    <mainClass>ex.owl.solver.rssi.Main</mainClass>
                  </manifest>
                </archive>
              </configuration>
            </plugin>
          </plugins>
        </pluginManagement>
      </build>
    </project>

In the pom.xml, we can see that our "main" class will be
"ex.owl.solver.rssi.Main", so let's create that now.

    mkdir -p src/main/java/ex/owl/solver/rssi/
    touch src/main/java/ex/owl/solver/rssi/Main.java

And we can test the syntax of the POM by running Maven with the "package"
directive to produce the JAR.

    mvn clean package

Near the end should be the line `[INFO] BUILD SUCCESS`.  Aside from defining
this main class, the POM will import the required libraries and dependencies,
will build a .jar file, and will make it executable.  This is a nice
convenient way of wrapping-up the code into the closest thing Java gives to an
executable object.

## Writing the Solver ##

Here's the basic outline of the solver's operation:

1. Read all Samples from the Aggregator.
2. For each Transmitter/Receiver pair, extract the RSSI from the Sample.
3. Use the extracted RSSI values to compute an average RSSI and a variance of
   the RSSI values.
4. Push these average/variance RSSI values into the World Model with the
   Attribute name "rssi.average" and "rssi.variance". 
5. Push these Attributes using the Origin value "ex-rssi-solver".

So let's jump right in to coding.  We'll start with some "boilerplate" code
that's necessary either for Java or for Owl Platform solvers.  Open up
Main.java in your favorite editor and type this lovely bit of code:

    package ex.owl.solver.rssi;

    import com.owlplatform.solver.*;
    import com.owlplatform.worldmodel.solver.*;

    public class Main {
      public static void main(String[]args){
        if(args.length != 4){
          System.err.println("Usage: <Aggregator Host> <Aggregator Port> <WM Solver Host> <WM Solver Port>");
          System.exit(1);
        }
        final SolverAggregatorConnection pull = new SolverAggregatorConnection();
        pull.setHost(args[0]);
        pull.setPort(Integer.parseInt(args[1]);

        final SolverWorldConnection push = new SolverWorldConnection();
        push.setOriginString("ex-rssi-solver");
        push.setHost(args[2]);
        push.setPort(Integer.parseInt(args[3]));

        // Done configuring connections to World Model
      }
    }

At this point, we can try to rebuild the JAR file and execute it, and we
should get the error message (if we don't provide the required arguments).

    mvn clean package
    java -jar target/owl-rssi-solver-1.0.0-SNAPSHOT-jar-with-dependencies.jar



