#!/bin/sh
#download project's artifacts
#parameters: [ {version} ]
# - {version} - optional release or snapshot version to download, e.g. "42.2.39", default actual snapshot version
set -x

[ -n "$1" ] || verDefault="$(mvn help:evaluate -f ../../pom.xml -Dexpression=project.version -q -DforceStdout)"
version=${1:-${verDefault}}
dir=target

mvn dependency:copy -Dartifact=org.xdef:xdef:${version}                -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:zip:userdoc-en -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:jar:javadoc    -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:jar:sources    -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:zip:src        -DoutputDirectory=${dir}
