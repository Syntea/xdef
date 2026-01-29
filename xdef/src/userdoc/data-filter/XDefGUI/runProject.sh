#!/bin/bash
args=("$@")

if [ x${args[0]} = x ]
  then 
    echo ***********************
    echo * Missing parameters! *
    echo ***********************
    echo The first parameter is name of directory in projects, it must contain the file project.xml
    echo The second parameter is optional. It is a list jar files added to classpath separatoe is ";"
    echo .
    exit
fi  
echo Execute project: ${args[0]}
if [ x${args[1]} = x ]
  then
    java -cp "../xdef.jar" org.xdef.util.GUIEditor -p projects/${args[0]}/project.xml
  else 
    java -cp "../xdef.jar:${args[1]}" org.xdef.util.GUIEditor -p projects/${args[0]}/project.xml
fi