#!/bin/bash
#in current repo create commits related to release version, i.e.:
# - new release-version commit - update pom.xml, changelog.md,create git-tag
# - and commit with shifted version to the next development version (entered by user, without postfix '-SNAPSHOT')
#and also do verifying release-build
#parameters: [ <versionNext> ]
# - <versionNext>: next development version, optional, if not entered it's entered during script from std-input
#run from root-repo-dir
set -e

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

#set a check new parameters
if [ $# -eq 0 ]
then
    echo "actual version to release: ${version} (Major.Minor.Revision)"
    echo "release-date:              ${releaseDate} (YYYY-MM-DD)"
    echo "changelog:${nl}------------------${nl}${changelog}${nl}------------------"

    read -p "enter next development version (Major.Minor.Revision): " versionNext
else
    versionNext="$1"
fi

echo "${versionNext}" | grep -E '^[[:digit:]]+\.[[:digit:]]+\.[[:digit:]]+$' > /dev/null || \
    { echo "ERROR: required version format 'Major.Minor.Revision', entered: ${versionNext}" >&2; exit 1; }

echo "========================================="
echo "actual version to release: ${version}"
echo "release-date:              ${releaseDate}"
echo "next development version:  ${versionNext}-SNAPSHOT"
echo "changelog:${nl}------------------${nl}${changelog}${nl}------------------"
echo "========================================="

echo "check previous settings, press Ctrl+C to interrupt this, after 10s it will create commits related to release version ..."
for i in $(seq 10 -1 1); do echo -n "$i . "; sleep 1; done; echo "0"


set +e
(   set -e
    echo '====================='
    echo 'Create release commit'
    echo '====================='
    set -x
    #set release-date
    mvn versions:set-property -Dproperty=release.date -DnewVersion="${releaseDate}" > /dev/null
    set +x
    echo "INFO: pom.xml: set release.date: $(mvn help:evaluate -Dexpression=release.date -q -DforceStdout)"
    
    set -x
    #update xdef/changelog.md - replace variables
    sed -i 's/\${version}/'"${version}"'/;s/\${release.date}/'"${releaseDate}"'/' xdef/changelog.md
    tag="version/${version}"
    tagDesc="Version ${version}, release-date ${releaseDate}${nl}${nl}${changelog}"
    
    #commit and create release-tag
    git add pom.xml xdef/changelog.md
    git commit -m "update pom.xml:release.date and xdef/changelog.md for release-version ${version}"
    set +x
    
    
    echo '=============================='
    echo 'Verifying release-build: start'
    echo '=============================='
    set -x
    mvn clean package -Prelease,doc
    set +x
    echo '==================================='
    echo 'Verifying release-build: successful'
    echo '==================================='
    
    set -x
    git tag "${tag}" -m "${tagDesc}"
    set +x
    
    
    echo '=============================='
    echo 'Create next development commit'
    echo '=============================='
    set -x
    #set next development version
    mvn versions:set-property -Dproperty=revision -DnewVersion="${versionNext}" > /dev/null
    versionNextSet="$(mvn help:evaluate -Prelease -Dexpression=project.version -q -DforceStdout)"
    set +x
    [ "${versionNextSet}" = "${versionNext}" ] || \
        { echo "ERROR: next development version set incorrectly: ${versionNextSet}" >&2; exit 1; }
    
    #update xdef/changelog.md - add section for new development version
    set -x
    sed -i '1i # Version ${version}, release-date ${release.date}\n' xdef/changelog.md
    
    #commit
    git add pom.xml xdef/changelog.md
    git commit -m "shift version to the next development version ${versionNext}"
    set +x
    
    
    echo '====================='
    echo 'Push commits and tags'
    echo '====================='
    set -x
    #push created two commits and release-tag
    git push
    git push origin "${tag}"
    set +x
)
[ $? -eq 0 ] || {
    set -e +x
    echo "ERROR: any previous failure, I will reset current branch" >&2
    branchCurrentName="$(git branch --show-current)"
    set -x
    git checkout -B "${branchCurrentName}" "origin/${branchCurrentName}"
    set +x
    exit 1
}
set -e
