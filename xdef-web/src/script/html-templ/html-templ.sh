#!/bin/bash
#translate html-files by template

#konstanty
saxonVersion=12.9
saxonArtifact=net.sf.saxon:Saxon-HE:$saxonVersion:jar
saxonPath=.m2/repository/net/sf/saxon/Saxon-HE/$saxonVersion/Saxon-HE-$saxonVersion.jar
prgDir=$(dirname $(readlink -f $0))
xslFile="$prgDir"/html-templ.xsl
cp="${HOME}/${saxonPath}:\
${HOME}/.m2/repository/org/xmlresolver/xmlresolver/5.3.3/xmlresolver-5.3.3.jar:\
${HOME}/.m2/repository/org/xmlresolver/xmlresolver/5.3.3/xmlresolver-5.3.3-data.jar\
"

#zajistit knihovnu Saxonu-HE - pripadne stahnout pomoci "maven"
if [ ! -f $HOME/$saxonPath ]
then
    { which mvn > /dev/null; } || {
        echo "chyba: nenalezen program 'mvn'. Nelze automaticky stahnout Saxon-HE."                                              >&2
        echo "       Pripadne si rucne stahnete z https://search.maven.org/artifact/net.sf.saxon/Saxon-HE verzi $saxonVersion a" >&2
        echo "       ulozte Saxon-HE zde: $HOME/$saxonPath"                                                                      >&2
        exit 1
    }
    mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get -Dartifact=$saxonArtifact || { exit 1; }
fi



#hlavni krok - vygenerovat dokumentaci z JIRA-xml-exportu tasku
for i in tutorial/ch00s??.html
do
    java -cp "${cp}" "net.sf.saxon.Transform" -xsl:${xslFile} -s:$i -o:new/$i
done
