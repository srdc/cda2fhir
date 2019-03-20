Creating a Maven Build 
===
This project incrementally builds and releases versions of this library for use in maven projects. These files are not hosted in the Maven Central Repository, but instead Github acts as the Maven Repository. These are the directions for building and releasing a version of this library, based on the instructions provided [here](https://gist.github.com/fernandezpablo85/03cf8b0cd2e7d8527063)

Essentially, you make a new version of the CDA2FHIR repository, and build to it from the original repository on your desktop.

#1 - Increment version number in `pom.xml` file in main project.

`git checkout fhir-stu3`

`<version>X.Y.Z-SNAPSHOT</version>` should be updated to reflect the intended version number of the release.

#2 - Commit changes directly to `fhir-stu3` branch.

`git commit -m "incremented pom to version X.Y.Z"`

#3 - Run mvn install

`mvn install`

This will build the .jar files in this repository that will be published to Github. You may optionally use the `-DskipTests` parameter to speed up the build.

#3 - Clone main project to separate folder.

`git clone git@github.com:amida/cda2fhir.git cda2fhir-release`

#4 - Go into new folder and check out `release` branch.

`git checkout release`

#5 - Build the release

Be sure increment the version number in `-Dversion` to match what is in the pom file, and reference the correct jar file in the `-Dfile` parameter, and update the absolute path(s) to reflect your directory structure.

```mvn install:install-file -DgroupId=tr.com.srdc -DartifactId=cda2fhir -Dversion=X.Y.Z-SNAPSHOT -Dfile=/Users/matthew/Workspace/cda2fhir/target/cda2fhir-X.Y.Z-SNAPSHOT-jar-with-dependencies.jar -DpomFile=/Users/matthew/Workspace/cda2fhir/pom.xml -DlocalRepositoryPath=. -DcreateChecksum=true```

#6 - Add all generated files, commit, and push up.

`git add -A . && git commit -m "released version X.Y.Z"`

`git push origin release`

The newly commited maven file(s) may be accessed by adding the following to the pom file:

```
<repository>
  <id>amida-github</id>
  <name>github</name>
  <url>https://github.com/amida-tech/cda2fhir/raw/release</url>
</repository>
...
<dependency> 
  <artifactId>cda2fhir</artifactId>
  <groupId>tr.com.srdc</groupId>
  <version>X.Y.Z</version>	        
</dependency>
```