#!/bin/bash
args=("$@")

java -cp "../xdef.jar" org.xdef.util.GUIEditor -g ${args[0]} ${args[1]} -workDir temp
