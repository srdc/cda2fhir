Creating a Maven Build 
===
This project incrementally builds and releases versions of this library for use in maven projects. These files are not hosted in the Maven Central Repository, but instead Github acts as the Maven Repository. These are the directions for building and releasing a version of this library, based on the instructions provided [here](https://gist.github.com/fernandezpablo85/03cf8b0cd2e7d8527063)

Essentially, you make a new version of the CDA2FHIR repository, and build to it from the original repository on your desktop.

#1 - Increment version number in `pom.xml` file.

`<version>0.0.1-SNAPSHOT</version>` should be updated to reflect the intended version number of the release.

#3 - Commit changes directly to `fhir-stu3` branch.

`git commit -m "incremented pom to version X.Y.Z"`

#1 - Clone project to separate folder.

`git clone git@github.com:amida/cda2fhir.git cda2fhir-release`

#4 - Go into new folder and check out `release` branch.

`git checkout release`

#5 - Build the release

Be sure increment the version number in `-Dversion` to match what is in the pom file.

```mvn install:install-file -DgroupId=tr.com.srdc -DartifactId=cda2fhir -Dversion=0.0.4-SNAPSHOT -Dfile=//~/cda2fhir/target/cda2fhir-0.0.4-SNAPSHOT-jar-with-dependencies.jar -DpomFile=~/Workspace/cda2fhir/pom.xml -DlocalRepositoryPath=. -DcreateChecksum=true```

#6 - Add all generated files, commit, and push up.

`git add -A . && git commit -m "released version X.Y.Z"`

`git push origin repository`

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