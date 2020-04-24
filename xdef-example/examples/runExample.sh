#!/bin/bash
args=("$@")

if [ x${args[0]} = x ]
  then 
    echo parameter with the name of example is missing
    exit
fi  

if  !  [ -e temp ]
  then
    mkdir temp
fi
if  !  [ -e temp/classes ]
  then
    mkdir temp/classes 
fi
if  !  [ -e temp/test ]
  then 
   mkdir temp/test
fi

if  [ -e temp/components ] 
  then
    rm -rf temp/components
fi

if [ ${args[0]} = Example_XC1 -o ${args[0]} = Example_XC2 ]
  then
    echo Generate X-components to temp/components and run ${args[0]} ...
    echo
    javac -encoding UTF8 -classpath "temp/classes:../xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar" -d temp/classes src/GenComponents.java src/data/MyClass.java
    java -classpath "temp/classes:../xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar"  GenComponents
    javac -encoding UTF8 -classpath "temp/classes:../xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar" -d temp/classes temp/components/*.java src/${args[0]}.java
    java -classpath "temp/classes:../xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar"  ${args[0]}
  else 
    echo ${args[0]}: Compile and run  ...
    echo
   javac -encoding UTF8 -classpath "temp/classes:../xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar:derby/derby.jar" -d temp/classes src/data/MyClass.java src/GenDerby.java src/${args[0]}.java
   java -classpath "temp/classes:../xdef-32.5.6 (2020-02-14) (2019-12-17) (2019-12-17).jar:derby/derby.jar" ${args[0]} 
fi  

if  [ -e derby.log ] 
  then
    rm derby.log
fi
