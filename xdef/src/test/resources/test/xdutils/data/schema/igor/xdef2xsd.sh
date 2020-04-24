#!/bin/sh

/opt/java6/bin/java	\
	-classpath `makecp /home/igor/devel/java/libs/xdef2/jdk1.5/lib/`:/home/igor/devel/java/libs/xdef2xsd/xdef2xsd/xdeftoxsd_ns/dist/xdeftoxsd_ns.jar	\
	cz.syntea.xd.utils.XdefToXsd \
	$@
