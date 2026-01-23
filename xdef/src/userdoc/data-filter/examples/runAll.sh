#!/bin/bash
args=("$@")

if  !  [ -e temp ]; then 
    mkdir temp
fi  
if  !  [ -e temp/classes ]; then
    mkdir temp/classes
fi
if  !  [ -e temp/test ]; then
   mkdir temp/test 
fi

echo Compile and run  ...
javac -encoding UTF8 -classpath "temp/classes:../xdef.jar:lib/derby.jar:lib/saxon9-xqj.jar:lib/saxon9he.jar:lib/snakeyaml-1.9.jar" -d temp/classes src/data/MyClass.java src/GenDerby.java src/GenDerby.java src/RunAll_prepare.java src/data/*.java src/task6/GenComponents*.java
java -classpath "temp/classes:../xdef.jar:lib/derby.jar:lib/saxon9-xqj.jar:lib/saxon9he.jar:lib/snakeyaml-1.9.jar" RunAll_prepare
javac -encoding UTF8 -classpath "temp/classes:../xdef.jar:lib/derby.jar:lib/saxon9-xqj.jar:lib/saxon9he.jar:lib/snakeyaml-1.9.jar" -d temp/classes src/*.java src/task1/*.java src/task2/*.java src/task3/*.java src/task4/*.java src/task5/*.java src/task6/*.java  src/task6/components1/*.java src/task6/components2/*.java src/task7/*.java src/task8/*.java src/task9/*.java src/task10/*.java
java -classpath "temp/classes:../xdef.jar:lib/derby.jar:lib/saxon9-xqj.jar:lib/saxon9he.jar:lib/snakeyaml-1.9.jar" RunAll

if  [ -e derby.log ]; then
    rm derby.log
fi
