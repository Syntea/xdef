#!/bin/bash
project="$1"

if [ -z "${project}" ]
then 
    echo ***********************
    echo * Missing parameters! *
    echo ***********************
    echo The first parameter is name of directory in projects, it must contain the file project.xml
    echo The second parameter is optional. It is a list jar files added to classpath (separator is ":")
    echo .
    exit 1
fi  
echo "Execute project: ${project}"
if [ -z "$2" ]
  then
    java -cp "../xdef-${version}.jar" org.xdef.util.GUIEditor -p projects/${project}/project.xml
  else 
    java -cp "../xdef-${version}.jar:$2" org.xdef.util.GUIEditor -p projects/${project}/project.xml
fi
