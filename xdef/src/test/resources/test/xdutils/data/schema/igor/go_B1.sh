#!/bin/sh

#./xdef2xsd.sh -i B1/B1_common.def B1/BM_common.MD -o tmp/B1_common_.def.xsd -r `cat B1/roots.txt`		\
./xdef2xsd.sh -i B1/x.def -o tmp/B1_common_.def.xsd -r `cat B1/roots.txt`		\
	&& xmllint tmp/B1_common_.def.xsd > B1_common.def.xsd					\
	#&& rm -f tmp/B1_common_.def.xsd
