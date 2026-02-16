#!/bin/sh

mkdir -p temp/classes temp/test

cp="-classpath temp/classes:../xdef-${version}.jar:lib/derby-${derby.version}.jar:lib/Saxon-HE-${saxon-he.version}-xqj.jar:lib/Saxon-HE-${saxon-he.version}.jar:lib/snakeyaml-${snakeyaml.version}.jar"
copts="${cp} -encoding UTF8 -d temp/classes"

echo "Compile and run ..."
javac ${copts} src/data/MyClass.java src/GenDerby.java src/GenDerby.java src/RunAll_prepare.java src/data/*.java src/task6/GenComponents*.java
java ${cp} RunAll_prepare
javac ${copts} src/*.java src/task1/*.java src/task2/*.java src/task3/*.java src/task4/*.java src/task5/*.java src/task6/*.java  src/task6/components1/*.java src/task6/components2/*.java src/task7/*.java src/task8/*.java src/task9/*.java src/task10/*.java
java ${cp} RunAll

if  [ -e derby.log ]; then
    rm derby.log
fi
