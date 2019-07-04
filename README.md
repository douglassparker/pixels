# pixels
This project analyzes the most common pixels in image files. This is a solution to a coding problem presented [here](https://gist.github.com/ehmo/e736c827ca73d84581d812b3a27bb132). The solution makes extensive use of [Project Reactor](https://projectreactor.io), an implementation of the [Reactive Streams](https://www.reactive-streams.org) specification.

## Prerequisites
1. [GIT](https://gist.github.com/ehmo/e736c827ca73d84581d812b3a27bb132) for downloading the project.
2. Java Version 8 or higher. Project was built and tested with Java 8, but it should work with higher versions.
3. [Maven](https://maven.apache.org/)(optional) for building the project. Maven 3.2.3 was used to develop the project, although other Maven 3 versions should work as well. Maven is not required. The project can be built without it.

## Downloading the Project.
```
git clone https://github.com/douglassparker/pixels.git
```

## Building the Project
Open your command shell. From the root directory of the project, enter:
```
mvn clean install
```

If Maven is not installed, enter:
```
./mvnw clean install
```
from a POSIX-compliant shell (e.g. Bash)

or
```
mvnw clean install
```
from a Windows command prompt.


## Running the Project
After building:
```
cd target
java -jar pixels*.jar
```
On my laptop, the project takes around 11 minutes to run. Copious numbers of Reactor generated log messages will appear on the console. However, logging happens asynchronously, so there is little performance degradation due to logging. After completion the log file is **target/pixels.log** and the results are in **target/pixels.txt**. Have a look at the generated Javadoc at **target/site/apidocs/index.html** and **target/site/testapidocs/index.html**.