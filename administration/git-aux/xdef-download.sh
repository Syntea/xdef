#!/bin/sh
set -x
version=${1:-42.2.39-SNAPSHOT}
dir=target
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}                -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:zip:userdoc-en -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:jar:javadoc    -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:jar:sources    -DoutputDirectory=${dir}
mvn dependency:copy -Dartifact=org.xdef:xdef:${version}:zip:src        -DoutputDirectory=${dir}
