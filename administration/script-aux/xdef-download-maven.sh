#!/bin/sh
#download project's artifacts by maven system
#parameters: [ {version} ]
# - {version} - optional release or snapshot version to download, e.g. "42.2.39", default actual snapshot version
set -x

[ -n "$1" ] || verDefault="$(mvn help:evaluate -f ../../pom.xml -Dexpression=project.version -q -DforceStdout)"
version=${1:-${verDefault}}
dir=target

[ -n "${version}" ] || {
    echo "actual snapshot version not found, you may not be in xdef-maven-project directory, " \
         "enter the version as parameter";
    exit 1;
}

mvn dependency:copy -Dartifact=org.xdef:xdef:${version}             -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:zip:userdoc -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:jar:javadoc -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:jar:sources -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:zip:src     -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:pom         -DoutputDirectory=${dir}
