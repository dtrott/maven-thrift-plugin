***************************
*** ONGOING MAINTENANCE ***
***************************

PLEASE NOTE: THIS CODE HAS BEEN CONTRIBUTED BACK TO ASF.

https://issues.apache.org/jira/browse/THRIFT-1536

Any future work I do on this plugin, will be as patches submitted to their version.
I am not planning to perform any further maintenance on this fork (or accept any patches / pull requests).

***********************
*** VERSION WARNING ***
***********************

Drop to the command line and type:

 thrift -version

To find out what version of the compiler you are using.


Older Compiler Versions (Less than 0.7.0)
===
You must use version 0.1.10 of this plugin.
+ Check out the source.
+ Switch to the old branch: git checkout -b old maven-thrift-plugin-0.1.10

And build/use that version.



Newer Compiler Versions (0.7.0 or newer)
===
0.7.0 should work fine with the latest version of this plugin.
However I have seen incompatibilities between the compiler and libthrift.

To resolve these check out the source for the compiler from the 0.7.0 branch (Do not trust the TAR BALLS)

svn co http://svn.apache.org/repos/asf/thrift/tags/thrift-0.7.0

That version should work fine with the maven version of lib thrift:

        <dependency>
            <groupId>org.apache.thrift</groupId>
            <artifactId>libthrift</artifactId>
            <version>0.7.0</version>
        </dependency>


Note: If you have problems building on OSX you might want to look at this article:

http://lueb.be/2009/02/23/installing-apache-thrift-on-mac-os-x-105-leopard/

The fastest way to build the compiler is:

$ svn co http://svn.apache.org/repos/asf/thrift/tags/thrift-0.7.0
$ cd thrift-0.7.0
$ cp /usr/X11/share/aclocal/pkg.m4 aclocal/
$ ./bootstrap.sh
$ ./configure
$ cd compiler/cpp/
$ make



***************************
*** Maven Thrift Plugin ***
***************************

A minimal configuration to invoke this plugin would be:

<project xmlns="http://maven.apache.org/POM/4.0.0"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/maven-v4_0_0.xsd">

    <modelVersion>4.0.0</modelVersion>
    <groupId>com.mycompany.example</groupId>
    <artifactId>example-thrift</artifactId>
    <packaging>jar</packaging>
    <version>1.0-SNAPSHOT</version>
    <name>thrift-example</name>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <configuration>
                    <source>1.5</source>
                    <target>1.5</target>
                </configuration>
            </plugin>
            <plugin>
                <groupId>org.apache.thrift.tools</groupId>
                <artifactId>maven-thrift-plugin</artifactId>
                <version>0.1.10</version>
                <configuration>
                    <thriftExecutable>/usr/local/bin/thrift</thriftExecutable>
                </configuration>
                <executions>
                    <execution>
                        <id>thrift-sources</id>
                        <phase>generate-sources</phase>
                        <goals>
                            <goal>compile</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>thrift-test-sources</id>
                        <phase>generate-test-sources</phase>
                        <goals>
                            <goal>testCompile</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencies>
        <dependency>
            <groupId>org.apache.hadoop</groupId>
            <artifactId>libthrift</artifactId>
            <version>0.5.0.0</version>
        </dependency>

        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-api</artifactId>
            <version>1.5.8</version>
        </dependency>

        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>1.2.14</version>
        </dependency>
    </dependencies>
</project>


You must:
+ Use Java 1.5 or newer due to the usage of Generics
+ Either ensure the "thrift" executable is in your PATH or set the
  <thriftExecutable> parameter to the correct location.
+ Define the executions you want (you probably don't need the testCompile
  unless you have custom thrift objects in your tests.
+ Include the dependencies on libthrift and slf4j or your compile will fail.


Once this is all done add your *.thrift files to the directory: src/main/thrift

Everything should then build with a: mvn clean install




You may also need to add the following to your settings.xml to download the
plugin:

            <pluginRepositories>
                <pluginRepository>
                    <id>dtrott</id>
                    <url>http://maven.davidtrott.com/repository</url>
                </pluginRepository>
            </pluginRepositories>
