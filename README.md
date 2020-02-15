# About X-definition

X-definition is registered technology of the firm Syntea software group a.s.,
for professional work with XML documents.

For the purpose of describing the structure of XML documents, their validation,
processing and creation, this tool was developed by Syntea Software Group Inc.
It allows the user to not only define the structure of XML documents,
but also to describe specifically their processing and construction.

Homepage: <http://www.xdef.org>

# License
The source code for this project is licensed under
[Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).



# Examples

Ex1: simple sequence of elements
...

Ex2: choice
...

Ex3: repeating
...

Ex4: xd-script
...

<table border="0"><tr><td>

Let´s have the following example of XML data:
```xml
<Employee
    FirstName = "Andrew"
    LastName = "Aardvark"
    EnterDate = "1996-3-12"
    Salary = "21700"
/>
    <Address
        Street = "Broadway"
        Number = "255"
        Town = "Beverly Hills"
        State = "CA"
        Zip = "90210"
    />
    <Competence>electrician</Competence>
    <Competence>carpenter</Competence>
</Employee>
```

</td><td>

```xml
<xd:def xmlns:xd="http://www.xdef.org/xdef/3.2">
    <Employee
        FirstName = "required string()"
        LastName = "required string()"
        EnterDate = "required date()"
        Salary = "optional decimal()"
    />
        <Address
            Street = "required string()"
            Number = "required int()"
            Town = "required string()"
            State = "required string()"
            Zip = "required int()" />
        <Competence xd:script = "occurs 1..5">
            required string()
        </Competence>
    </Employee>
</xd:def>
```

</td></tr></table>



# Usage

## Check and download available versions
Links:
* release versions from central maven repository: <https://search.maven.org/search?q=g:org.xdef>
* release and snapshot versions from oss.sonatype.org: <https://oss.sonatype.org/#nexus-search;gav~org.xdef>

## Maven
Configuration file pom.xml:
* dependency on release version in central maven repository:
  ```xml
  <dependencies>
      <dependency>
          <groupId>org.xdef</groupId>
          <artifactId>xdef</artifactId>
          <version>[release version]</version>
      </dependency>
  <dependencies>
  ```
* dependency on release or snapshot version in oss.sonatype.org:
  ```xml
  <dependencies>
      <dependency>
          <groupId>org.xdef</groupId>
          <artifactId>xdef</artifactId>
          <version>[release or snapshot version]</version>
      </dependency>
  <dependencies>
  <distributionManagement>
      <snapshotRepository>
          <id>ossrh</id>
          <url>https://oss.sonatype.org/content/repositories/snapshots</url>
      </snapshotRepository>
      <repository>
          <id>ossrh</id>
          <url>https://oss.sonatype.org/service/local/staging/deploy/maven2</url>
      </repository>
  </distributionManagement>
  ```

# Building

Prerequisities:
* download project X-definition, eg. from GitHub: <https://github.com/Syntea/xdef>
* install _java_
* install _maven_
* configure _maven_
    * configure maven-plugin _toolchain_

Frequent building operations:
* cleaning before any compiling, building, deploying, etc.:
  ```shell
  mvn clean
  ```
* compile all java-resources, respectively all compilable resources:
  ```shell
  mvn compile
  ```
* build snapshot package:
  ```shell
  mvn package
  ```
* build snapshot package avoiding junit-tests:
  ```shell
  mvn package -DskipTests=true
  ```
* build release packages including javadoc, sources, documentation:
  ```shell
  mvn package -Prelease,javadoc,sources
  ```



# Deploying to maven central repository

Prerequisities:
* satisfy prerequisities for building
* install the pgp-managing software GnuPG (<https://gnupg.org/>)
* configure _maven_:
    * access to pgp-key
    * access to maven repository manager _oss.sonatype.org_ (having id "_ossrh_")

Deploying:
* deploy snapshot packages to snapshot-repository at _oss.sonatype.org_:
  ```shell
  mvn deploy -Pjavadoc,sources,dm-ossrh
  ```
* release the version of X-definition to the maven central repository (throw _oss.sonatype.org_):
  ```shell
  mvn deploy -Prelease,javadoc,sources,dm-ossrh
  ```
