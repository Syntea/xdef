#!/bin/sh

./xdef2xsd.sh -i D5/D5_common.def -o tmp/D5_common_.def.xsd -r `cat D5/roots.txt`		\
	&& xmllint tmp/D5_common_.def.xsd > D5_common.def.xsd					\
	#&& rm -f tmp/D5_common_.def.xsd
