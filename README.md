# About X-definition

X-definition is registered technology of the Syntea software group a.s.,
for processing of the XML documents or JSON data.

For the purpose of describing the structure of XML documents or JSON data,
their validation, processing and creation, this tool was developed
by Syntea Software Group Inc. It allows the user to not only define
the structure of XML documents or JSON data, but also to describe specifically
their processing and construction.

Homepage: <http://www.xdef.org>

This project is an implementation for the platform Java 1.8+
(additional note: up to the version 41.0.4 for the platform Java 1.6+).


# License

The source code for this project is licensed under
[Apache 2.0 license](http://www.apache.org/licenses/LICENSE-2.0).


# Examples

You can try the following examples online at: <http://xdef.syntea.cz/tutorial/examples/validate.html>

Example 1: **Essential concepts**
<table><tr style="vertical-align: top;"><td>
Let´s have the following XML data:

```xml
<Employee
    FirstName = "Andrew"
    LastName  = "Aardvark"
    EnterDate = "1996-03-12"
    Salary    = "21700"
>
    <Address
        Street = "Broadway"
        Number = "255"
        Town   = "Beverly Hills"
        State  = "CA"
        Zip    = "90210"
    />
    <Competence>electrician</Competence>
    <Competence>carpenter</Competence>
</Employee>
```

</td><td>
This is the complete X-definition file with the model of the XML data on the left:

```xml
<xd:def xmlns:xd="http://www.xdef.org/xdef/4.2" xd:root="Employee">
<Employee
    FirstName = "required string()"
    LastName  = "required string()"
    EnterDate = "required date()"
    Salary    = "optional decimal()"
>
    <Address
        Street = "required string()"
        Number = "required int()"
        Town   = "required string()"
        State  = "required string()"
        Zip    = "required int()"
    />
    <Competence xd:script = "occurs 1..5">
        required string()
    </Competence>
</Employee>
</xd:def>
```

</td></tr></table>

Example 2: **References**
<table><tr style="vertical-align: top;"><td>
XML data:

```xml
<Family>
  <Father    GivenName  = "John"
             FamilyName = "Smith"
             PersonalID = "7107130345"
             Salary     = "18800" />
  <Mother    GivenName  = "Jane"
             FamilyName = "Smith"
             PersonalID = "7653220029"
             Salary     = "19400" />
  <Son       GivenName  = "John"
             FamilyName = "Smith"
             PersonalID = "9211090121" />
  <Daughter  GivenName  = "Jane"
             FamilyName = "Smith"
             PersonalID = "9655270067" />
  <Residence Street     = "Small"
             Number     = "5"
             Town       = "Big"
             Zip        = "12300" />
</Family>
```

</td><td>
Model of the XML data:

```xml
<xd:def xmlns:xd="http://www.xdef.org/xdef/4.2" xd:root="Family">
<Family>
  <Father    xd:script = "occurs 0..1; ref Person" />
  <Mother    xd:script = "occurs 1..1; ref Person" />
  <Son       xd:script = "occurs 0..*; ref Person" />
  <Daughter  xd:script = "occurs 0..*; ref Person " />
  <Residence xd:script = "occurs 1;    ref Address" />
</Family>

<Person GivenName  = "string()" 
        FamilyName = "string()" 
        PersonalID = "long()"
        Salary     = "optional int()" />
<Address Street = "string()"
         Number = "int()"
         Town   = "string()"
         Zip    = "int()" />
</xd:def>
```

</td></tr></table>



# Annotation

This document describes the programming language and the technology called
“X‑definition“. X‑definition is designed for description and processing of
data in the form of XML or JSON.

X-definition is a tool that provides the description of both the structure and
the properties of data values in an XML document. Moreover, the X-definition
allows the description of the processing of specified XML objects.
Thus X‑definitions may replace existing technologies commonly used
for XML validation - namely the DTD (Data Type Definition) or the XML schemas and
Schematron. With X-definition it is also possible to describe the construction
of XML documents (or the transformation of XML data). X-definitions provides 
also generation of JAVA class objects similar way as JAXB technology.
 
X-definition enables the merging in one source of both the validation
of XML documents and processing of data (i.e. using actions assigned
to events when XML objects are processed). Compared to the “classical”
technologies based on DTD and XML schemas, the advantage of X-definitions is
(not only) higher readability and easier maintenance.  X‑definition has been
designed for processing of XML data files of unlimited size, up to many gigabytes.

A principal property of X-definition is maximum respect for the structure
of the described data. The form of X‑definition is an XML document with a structure
similar to the described XML data. This makes possible quickly and intuitively describe
given XML data and its processing. In many cases it requires just to replace the values
in the XML data by the description written in the X‑definition X‑script language.
You can also gradually add to your X‑script required actions providing data
processing. You can take a step-by-step approach to your work.

It is assumed that the reader already knows the elementary principles of XML.
To get the most out of this document, you should also have at least basic
knowledge of the Java programming language.

X‑definition technology enables also to generate the source code
of Java classes representing XML elements described by X‑definition.
Such class is called X‑component. You can use the instances of XML data
in the form of X‑components in Java programs (similar way as in the JAXB
technology).

The term "X‑definition" we use in the two different meanings:
either as a name of the programming language or as an XML element
containing the code of X‑definition language.

For the **complete documentation** see the directory [xdef/src/documentation](/xdef/src/documentation).

You can try your examples online at:
* validation mode: <http://xdef.syntea.cz/tutorial/examples/validate.html>
* construction mode: <http://xdef.syntea.cz/tutorial/examples/compose.html>
* BNF-grammar: <http://xdef.syntea.cz/tutorial/examples/BNF.html>
* template: <http://xdef.syntea.cz/tutorial/examples/template.html>



# Usage in other projects


## Check and download available versions

Links:
* release versions from the central maven repository: <https://search.maven.org/search?q=g:org.xdef>
* release and snapshot versions from oss.sonatype.org: <https://oss.sonatype.org/#nexus-search;gav~org.xdef>


## For maven projects

Configuration file pom.xml:
* dependency on release version in the central maven repository:

  ```xml
  <dependencies>
      <dependency>
          <groupId>org.xdef</groupId>
          <artifactId>xdef</artifactId>
          <version>[release version]</version>
      </dependency>
  <dependencies>
  ```
* dependency on release or snapshot version in central.sonatype.com:

  ```xml
  <dependencies>
      <dependency>
          <groupId>org.xdef</groupId>
          <artifactId>xdef</artifactId>
          <version>[release or snapshot version]</version>
      </dependency>
  <dependencies>
  <distributionManagement>
      <repository>
          <id>ossrh</id>
          <url>https://oss.sonatype.org/service/local/staging/deploy/maven2</url>
      </repository>
      <snapshotRepository>
          <id>ossrh</id>
          <url>https://central.sonatype.com/repository/maven-snapshots/</url>
      </snapshotRepository>
  </distributionManagement>
  ```


# Building this project

Source code at GitHub:
* link to the last stable version (to the branch "master"): <https://github.com/Syntea/xdef>

Prerequisities:
* download project X-definition, eg. from GitHub
* install _java_ (at least version 8)
* install _maven_ (at least version 3.6)
* configuration:
    * configure the maven-plugin _toolchains_
      (see <https://maven.apache.org/plugins/maven-toolchains-plugin/usage.html>):
        * configure the xml-file _~/.m2/toolchains.xml_ in the home directory
        * see the template-file [configuration/maven/toolchains.xml](configuration/maven/toolchains.xml)

Frequent building operations:
* cleaning before any compiling, building, deploying, etc.:

  ```shell
  mvn clean
  ```
* compile all java-resources, respectively all compilable resources:

  ```shell
  mvn compile
  ```
* build the snapshot package:

  ```shell
  mvn package
  ```
* build the snapshot package including javadoc, sources, documentation:

  ```shell
  mvn package -Pjavadoc,sources
  ```
* by using the "skipTests" profile, avoid junit-tests:

  ```shell
  mvn package -PskipTests
  ```
* by using the profile "testOnAllJvms", junit-tests will be run on all configured Java platforms,
  i.e. Java-8 (it is run by default in module "xdef"), Java-11 (using the module "xdef-test11"),
  Java-17 (using the module "xdef-test17"), Java-21 (using the module "xdef-test21"):

  ```shell
  mvn package -PtestOnAllJvms
  ```
* build the release package:

  ```shell
  mvn package -Prelease
  ```
* build the release package including javadoc, sources, documentation:

  ```shell
  mvn package -Prelease,javadoc,sources
  ```


## Deploying to the maven central repository

Prerequisities:
* satisfy prerequisities for building
* install the pgp-managing software GnuPG (<https://gnupg.org/>)
* configuration:
    * unlocking the appropriate pgp-key
        * insert the appropriate key to the the pgp-manager
        * enter the pgp-key-password for the pgp-key:
            * during the package build by the user when prompted by the pgp-agent
            * or beforehand to the environment variable _MAVEN_GPG_PASSPHRASE_
              (see <https://maven.apache.org/plugins/maven-gpg-plugin/sign-mojo.html#passphraseEnvName>)
    * authentication to the maven repository manager _oss.sonatype.org_
      (having id _"ossrh"_ in the file [xdef/pom.xml](xdef/pom.xml))
        * configure the maven-configuration-file in the home directory _~/.m2/settings.xml_
        * see template-file [configuration/maven/settings.xml](configuration/maven/settings.xml)

Deploying:
* build and deploy the snapshot package to the repository _oss.sonatype.org_:

  ```shell
  mvn deploy -Pjavadoc,sources,dm-ossrh
  ```
* build and deploy the X-definition release package to the central maven repository (through the repository _oss.sonatype.org_):

  ```shell
  mvn deploy -Prelease,javadoc,sources,dm-ossrh
  ```
