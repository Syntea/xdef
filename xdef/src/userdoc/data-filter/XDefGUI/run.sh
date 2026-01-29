#!/bin/bash
args=("$@")

java -cp "../xdef.jar" org.xdef.util.GUIEditor ${args[0]} ${args[1]} ${args[2]} ${args[3]} ${args[4]} ${args[5]} 
