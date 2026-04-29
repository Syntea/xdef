#!/bin/bash
#translate html-files by template

#konstanty
saxonVersion=12.9
saxonArtifact=net.sf.saxon:Saxon-HE:$saxonVersion:jar
saxonPath=.m2/repository/net/sf/saxon/Saxon-HE/$saxonVersion/Saxon-HE-$saxonVersion.jar
prgDir=$(dirname $(readlink -f $0))
xslFile="$prgDir"/html-template.xsl

#zajistit knihovnu Saxonu-HE - pripadne stahnout pomoci "maven"
if [ ! -f $HOME/$saxonPath ]
then
    { which mvn > /dev/null; } || {
        echo "chyba: nenalezen program 'mvn'. Nelze automaticky stahnout Saxon-HE."                                              >&2
        echo "       Pripadne si rucne stahnete z https://search.maven.org/artifact/net.sf.saxon/Saxon-HE verzi $saxonVersion a" >&2
        echo "       ulozte Saxon-HE zde: $HOME/$saxonPath"                                                                      >&2
        exit 1
    }
    mvn org.apache.maven.plugins:maven-dependency-plugin:3.1.1:get -Dartifact=$saxonArtifact >&2 || { exit 1; }
fi



#hlavni krok - vygenerovat dokumentaci z JIRA-xml-exportu tasku
for i in tutorual/*.html
do
    java -jar ${HOME}/${saxonPath} -xsl:${xslFile} -s:$i -o:tutorial-new/$i.html
done
