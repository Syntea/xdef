#!/bin/bash
#update actual commit as new release-version (update pom.xml, changelog.md, create git-tag) and
#shift version to the next development version (entered from std-input) as new commit

#parameters
if [ $# -eq 0 ]
then
    read -p "enter next development version: " versionNext
else 
    versionNext=$1
fi

version=$(mvn help:evaluate -Prelease -Dexpression=project.version -q -DforceStdout)
releaseDate=$(date +'%Y-%m-%d')
changelog=$(awk -v RS='(\r?\n){2,}' 'NR == 1' xdef/changelog.md)

echo "actual version to release: ${version}"
echo "release-date:              ${releaseDate}"
echo "changelog:\n------\n${changelog}\n------\n"
echo "next development version:  ${versionNext}"
read -p "Press key Enter to continue... "

mvn versions:set-property -Dproperty=release.date -DnewVersion="${releaseDate}"

sed 's/\${version}/'"${version}"'/;s/\${release.date}/'"${releaseDate}"'/' xdef/changelog.md | head -n 10
description=$(head -n1 xdef/changelog.md | tr '#')

git tag "version/${version}" --description "${description}"
git commit -m "update pom.xml/release.date and xdef/changelog.md as for release-version ${version}"

mvn versions:set-property -Dproperty=revision -DnewVersion=${VERSION_NEXT}
(   echo '# Version ${version}, release-date ${release.date}'
    echo
    cat xdef/changelog.md
) > xdef/changelog.md

git commit -m "shift version to the next development version ${versionNext}"

#git push
#git push tags
