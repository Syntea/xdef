#!/bin/bash
#in main-repo create commits releted to release version
#parameters: [ <versionNext> ]
#see ./git-createReleaseCommits.sh
#run from root-repo-dir
set -e

pwd="$(pwd)"
scriptDir="$(dirname $(readlink -f "$0"))"
#check variable main-branch name
[ -n "${mainBranchName}" ] || { echo "ERROR: var 'mainBranchName' is empty" >&2; exit 1; }

#enter into main-repo
cd "../xdef-${mainBranchName}"

"${scriptDir}"/git-createReleaseCommits.sh $@

#reenter back and pull
cd "${pwd}"
echo "git-repo $(pwd): pull"
set -x
git pull
set +x
