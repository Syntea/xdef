#!/bin/bash
#transform html-files by template

#konstanty
saxonVersion=12.9
xmlResVer=5.3.3

#konstanty odvozene
prgDir=$(dirname $(readlink -f $0))
saxonPath=".m2/repository/net/sf/saxon/Saxon-HE/$saxonVersion/Saxon-HE-${saxonVersion}.jar"
xslFile="${prgDir}/html-transform.xsl"
cp="${HOME}/${saxonPath}:\
${HOME}/.m2/repository/org/xmlresolver/xmlresolver/${xmlResVer}/xmlresolver-${xmlResVer}.jar:\
${HOME}/.m2/repository/org/xmlresolver/xmlresolver/${xmlResVer}/xmlresolver-${xmlResVer}-data.jar\
"

#stahnout knihovnu Saxonu-HE a xmlresolver (vcetne "-data") pomoci "maven", pripadne rucne na:
# - https://central.sonatype.com/artifact/net.sf.saxon/Saxon-HE
#   - a ulozit do .m2/repository/net/sf/saxon/Saxon-HE/${saxonVersion}/
# - https://central.sonatype.com/artifact/org.xmlresolver/xmlresolver
#   - a ulozit do ${HOME}/.m2/repository/org/xmlresolver/xmlresolver/${xmlResVer}/
if [ ! -f $HOME/${saxonPath} ]; then
    get="org.apache.maven.plugins:maven-dependency-plugin:3.10.0:get"
    mvn $get -Dartifact="net.sf.saxon:Saxon-HE:${saxonVersion}:jar"         || exit 1
    mvn $get -Dartifact="org.xmlresolver:xmlresolver:${xmlResVer}:jar"      || exit 1
    mvn $get -Dartifact="org.xmlresolver:xmlresolver:${xmlResVer}:jar:data" || exit 1
fi



#hlavni krok - transformace webapp
for i in tutorial/ch??.html
do
    echo "chX: file: $i"
    xmllint --html --xmlout --nodefdtd --recover $i | \
    java -cp "${cp}" "net.sf.saxon.Transform" -xsl:"${prgDir}/html-transform-tutorial-chX.xsl" -o:../webapp/$i -s:-
done

for i in tutorial/ch??s??.html 
do
    echo "chXsY: file: $i"
    xmllint --html --xmlout --nodefdtd --recover $i | \
    java -cp "${cp}" "net.sf.saxon.Transform" -xsl:"${prgDir}/html-transform-tutorial-chXsY.xsl" -o:../webapp/$i -s:-
done

exit

for i in tutorial/ch??s??e??.html
do
    echo "example: file: $i"
    xmllint --html --xmlout --nodefdtd --recover $i | \
    java -cp "${cp}" "net.sf.saxon.Transform" -xsl:"${prgDir}/html-transform-example.xsl" -o:../webapp/$i -s:-
done
