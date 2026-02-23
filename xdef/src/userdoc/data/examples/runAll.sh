#!/bin/sh

mkdir -p temp/classes temp/test

cp="-classpath temp/classes:../xdef-42.2.40-SNAPSHOT.jar:lib/derby-10.12.1.1.jar:lib/Saxon-HE-9.7.0-18-xqj.jar:lib/Saxon-HE-9.7.0-18.jar:lib/snakeyaml-1.29.jar"
copts="${cp} -encoding UTF8 -d temp/classes"

echo "Compile and run ..."
javac ${copts} src/data/MyClass.java src/GenDerby.java src/GenDerby.java src/RunAll_prepare.java src/data/*.java src/task6/GenComponents*.java
java ${cp} RunAll_prepare
javac ${copts} src/*.java src/task1/*.java src/task2/*.java src/task3/*.java src/task4/*.java src/task5/*.java src/task6/*.java  src/task6/components1/*.java src/task6/components2/*.java src/task7/*.java src/task8/*.java src/task9/*.java src/task10/*.java
java ${cp} RunAll

if  [ -e derby.log ]; then
    rm derby.log
fi
