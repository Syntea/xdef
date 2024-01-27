#!/bin/bash
args=("$@")

if [ x${args[0]} = x ]; then 
    echo parameter with the name of example is missing
    exit
fi  
if  !  [ -e temp ]; then
    mkdir temp
fi
if  !  [ -e temp/classes ]; then
    mkdir temp/classes 
fi
if  !  [ -e temp/test ]; then 
   mkdir temp/test
fi

if [ ${args[0]} = task6/Town1 -o ${args[0]} = task6/Town2 ]
  then
    echo ${args[0]}: Generating X-components ...
    echo
    if [ ${args[0]} = task6/Town1 ]
      then
        javac -encoding UTF8 -classpath "temp/classes:../xdef.jar" -d temp/classes src/task6/GenComponents1.java
        java -classpath "temp/classes:../xdef.jar" task6.GenComponents1
        javac -encoding UTF8 -classpath "temp/classes:../xdef.jar" -d temp/classes src/task6/components1/*.java
      else 
        javac -encoding UTF8 -classpath "temp/classes:../xdef.jar" -d temp/classes src/task6/GenComponents2.java
        java -classpath "temp/classes:../xdef.jar" task6.GenComponents2
        javac -encoding UTF8 -classpath "temp/classes:../xdef.jar" -d temp/classes src/task6/components2/*.java
    fi
    echo ${args[0]}: Compile and run  ...
    echo
    javac -encoding UTF8 -classpath "temp/classes:../xdef.jar" -d temp/classes src/${args[0]}.java
    java -classpath "temp/classes:../xdef.jar"  ${args[0]}
  else 
    echo ${args[0]}: Compile and run  ...
    echo
    javac -encoding UTF8 -classpath "temp/classes:../xdef.jar:lib/derby.jar:lib/saxon9-xqj.jar:lib/saxon9he.jar:lib/snakeyaml-1.9.jar" -d temp/classes src/data/MyClass.java src/GenDerby.java src/${args[0]}*.java
    java -classpath "temp/classes:../xdef.jar:lib/derby.jar:lib/saxon9-xqj.jar:lib/saxon9he.jar:lib/snakeyaml-1.9.jar" ${args[0]} 
fi  

if  [ -e derby.log ]; then
    rm derby.log
fi
