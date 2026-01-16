#in current repo create commits related to release version, i.e.:
# - new release-version commit - update pom.xml, changelog.md,create git-tag
# - and commit with shifted version to the next development version (entered by user, without postfix '-SNAPSHOT')
#and also do verifying release-build
#parameters: [ <versionNext> ]
# - <versionNext>: next development version, optional, if not entered it's entered during script from std-input
set -e

#set and check new parameters
versionNext="${VERSION_NEXT}"

git config --global user.email "it@syntea.cz"
git config --global user.name "syntea-cz-builder"
git checkout main
git pull

#constants
nl='
'

echo '========================'
echo 'Check and set parameters'
echo '========================'
#get actual parameters
version="$(mvn help:evaluate -Prelease -Dexpression=project.version -q -DforceStdout)"
releaseDate="$(date +'%Y-%m-%d')"
changelog="$(awk -v RS='(\r?\n){2,}' 'NR == 1' xdef/changelog.md)"

echo "${versionNext}" | grep -E '^[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+$' > /dev/null || \
    { echo "ERROR: required version format 'Major.Minor.Revision', entered: ${versionNext}"; exit 1; }

echo "========================================="
echo "actual version to release: ${version}"
echo "release-date:              ${releaseDate}"
echo "next development version:  ${versionNext}-SNAPSHOT"
echo "changelog:${nl}------------------${nl}${changelog}${nl}------------------"
echo "========================================="

sleep 10


echo '====================='
echo 'Create release commit'
echo '====================='
#set release-date
mvn versions:set-property -Dproperty=release.date -DnewVersion="${releaseDate}" > /dev/null
echo "INFO: pom.xml: set release.date: $(mvn help:evaluate -Dexpression=release.date -q -DforceStdout)"

#update xdef/changelog.md - replace variables
sed -i 's/\${version}/'"${version}"'/;s/\${release.date}/'"${releaseDate}"'/' xdef/changelog.md
tag="version/${version}"
tagDesc="Version ${version}, release-date ${releaseDate}${nl}${nl}${changelog}"

#commit and create release-tag
git add pom.xml xdef/changelog.md
git commit -m "update pom.xml:release.date and xdef/changelog.md as for release-version ${version}"
git tag "${tag}" -m "${tagDesc}"


echo '=============================='
echo 'Create next development commit'
echo '=============================='
#set next development version
mvn versions:set-property -Dproperty=revision -DnewVersion="${versionNext}" > /dev/null
versionNextSet="$(mvn help:evaluate -Prelease -Dexpression=project.version -q -DforceStdout)"
[ "${versionNextSet}" = "${versionNext}" ] || \
    { echo "ERROR: next development version set incorrectly: ${versionNextSet}"; exit; }

#update xdef/changelog.md - add section for new development version
sed -i '1i # Version ${version}, release-date ${release.date}\n' xdef/changelog.md

#commit
git add pom.xml xdef/changelog.md
git commit -m "shift version to the next development version ${versionNext}"


echo '====================='
echo 'Push commits and tags'
echo '====================='
#push created two commits and release-tag
git push
git push origin "${tag}"
