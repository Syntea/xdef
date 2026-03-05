#!/bin/sh

java -cp "../xdef-${project.version}.jar" org.xdef.util.GUIEditor -g $1 $2 -workDir temp
