#!/bin/bash
#create commits releted to release version, i.e.:
#   - new release-version commit - update pom.xml, changelog.md,create git-tag
#   - and commit with shifted version to the next development version (entered by user, without postfix '-SNAPSHOT')
set -e

#constants
nl='
'


echo '========================'
echo 'Check and set parameters'
echo '========================'
#check git status up-to-date and clean
set -x
git fetch
set +x
( unset LANG; git status; ) | grep -z 'Your branch is up to date with.*nothing to commit, working tree clean' > /dev/null \
    && { echo "INFO: git-repo is up-to-date and clean"; } \
    || { echo "ERROR: git-repo is not up-to-date and clean"; set -x; git status; exit; }

#get actual parameters
version=$(mvn help:evaluate -Prelease -Dexpression=project.version -q -DforceStdout)
releaseDate=$(date +'%Y-%m-%d')
changelog=$(awk -v RS='(\r?\n){2,}' 'NR == 1' xdef/changelog.md)

#set a check new parameters
if [ $# -eq 0 ]
then
    echo "actual version to release: ${version} (Major.Minor.Revision)"
    echo "release-date:              ${releaseDate} (YYYY-MM-DD)"
    echo "changelog:${nl}------------------${nl}${changelog}${nl}------------------"

    read -p "enter next development version (Major.Minor.Revision): " versionNext
else
    versionNext=$1
fi

echo "${versionNext}" | grep -E '^[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+$' > /dev/null \
    || { echo "ERROR: required version format 'Major.Minor.Revision', entered: ${versionNext}"; exit; }

echo "========================================="
echo "actual version to release: ${version}"
echo "release-date:              ${releaseDate}"
echo "next development version:  ${versionNext}-SNAPSHOT"
echo "changelog:${nl}------------------${nl}${changelog}${nl}------------------"
echo "========================================="

read -p "Press key Enter to create commits related to release version ... " enter


echo '====================='
echo 'Create release commit'
echo '====================='
set -x
mvn versions:set-property -Dproperty=release.date -DnewVersion="${releaseDate}" > /dev/null
echo "pom.xml: set release.date: $(mvn help:evaluate -Dexpression=release.date -q -DforceStdout)"

sed -i 's/\${version}/'"${version}"'/;s/\${release.date}/'"${releaseDate}"'/' xdef/changelog.md
tag="version/${version}"
tagDesc="Version ${version}, release-date ${releaseDate}${nl}${nl}${changelog}"

git add pom.xml xdef/changelog.md
git commit -m "update pom.xml:release.date and xdef/changelog.md as for release-version ${version}"
git tag "${tag}" -m "${tagDesc}"
set +x


echo '=============================='
echo 'Create next development commit'
echo '=============================='
set -x
mvn versions:set-property -Dproperty=revision -DnewVersion="${versionNext}" > /dev/null
echo "pom.xml: set version: $(mvn help:evaluate -Prelease -Dexpression=project.version -q -DforceStdout)"
sed -i '1i # Version ${version}, release-date ${release.date}\n' xdef/changelog.md

git add pom.xml xdef/changelog.md
git commit -m "shift version to the next development version ${versionNext}"
set +x


echo '====================='
echo 'Push commits and tags'
echo '====================='
set -x
git push
git push origin "${tag}"
set +x
