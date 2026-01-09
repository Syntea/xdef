#!/bin/bash
#create release-version
#parameters: [ <versionNext> ]
# - <versionNext>: next development version, optional, if not entered it's entered during script from std-input
set -e

scriptDir="$(dirname $(readlink -f "$0"))"

#run from repo-root-dir
cd ../..

. "${scriptDir}"/base/env.sh
"${scriptDir}"/base/git-checkDirRepo.sh main

"${scriptDir}"/base/git-merge-dev2main.sh
"${scriptDir}"/base/git-createReleaseCommits-inMain.sh $@
"${scriptDir}"/base/git-merge-main2dev.sh

echo '====================='
echo 'successfully finished'
echo '====================='
