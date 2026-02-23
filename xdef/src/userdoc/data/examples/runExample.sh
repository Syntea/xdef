#!/bin/sh
exName="$1"

if [ -z "${exName}" ]; then 
    echo "parameter with the name of example is missing"
    exit 1
fi

mkdir -p temp/classes temp/test

cp="-classpath temp/classes:../xdef-42.2.40-SNAPSHOT.jar:lib/derby-10.12.1.1.jar:lib/Saxon-HE-9.7.0-18-xqj.jar:lib/Saxon-HE-9.7.0-18.jar:lib/snakeyaml-1.29.jar"
copts="${cp} -encoding UTF8 -d temp/classes"

if [ "${exName}" = "task6/Town1" -o "${exName}" = "task6/Town2" ]
then
    echo "${exName}: Generating X-components ..."
    echo
    if [ "${exName}" = "task6/Town1" ]
    then
        javac ${copts} src/task6/GenComponents1.java
        java ${cp} task6.GenComponents1
        javac ${copts} src/task6/components1/*.java
    else 
        javac ${copts} src/task6/GenComponents2.java
        java ${cp} task6.GenComponents2
        javac ${copts} src/task6/components2/*.java
    fi
    echo "${exName}: Compile and run ..."
    echo
    javac ${copts} src/${exName}.java
    java ${cp} ${exName}
else 
    echo "${exName}: Compile and run ..."
    echo
    javac ${copts} src/data/MyClass.java src/GenDerby.java src/${exName}.java
    java ${cp} ${exName}
fi

if [ -e derby.log ]; then
    rm derby.log
fi
