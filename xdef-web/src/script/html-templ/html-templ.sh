#!/bin/bash
#translate html-files by template

#konstanty
saxonVersion=12.9
#konstanty odvozene
prgDir=$(dirname $(readlink -f $0))
saxonPath=.m2/repository/net/sf/saxon/Saxon-HE/$saxonVersion/Saxon-HE-$saxonVersion.jar
xslFile="$prgDir"/html-templ.xsl
cp="${HOME}/${saxonPath}:\
${HOME}/.m2/repository/org/xmlresolver/xmlresolver/5.3.3/xmlresolver-5.3.3.jar:\
${HOME}/.m2/repository/org/xmlresolver/xmlresolver/5.3.3/xmlresolver-5.3.3-data.jar\
"

#stahnout knihovnu Saxonu-HE pomoci "maven", pripadne rucne na:
# - https://central.sonatype.com/artifact/net.sf.saxon/Saxon-HE
# - https://central.sonatype.com/artifact/org.xmlresolver/xmlresolver
if [ ! -f $HOME/$saxonPath ]; then
    get="org.apache.maven.plugins:maven-dependency-plugin:3.10.0:get"
    mvn $get -Dartifact="net.sf.saxon:Saxon-HE:$saxonVersion:jar"    || { exit 1; }
    mvn $get -Dartifact="org.xmlresolver:xmlresolver:5.3.3:jar"      || { exit 1; }
    mvn $get -Dartifact="org.xmlresolver:xmlresolver:5.3.3:jar:data" || { exit 1; }
    exit
fi



#hlavni krok - vygenerovat dokumentaci z JIRA-xml-exportu tasku
for i in tutorial/ch00s??.html
do
    java -cp "${cp}" "net.sf.saxon.Transform" -xsl:${xslFile} -s:$i -o:../webapp-new/$i
done
