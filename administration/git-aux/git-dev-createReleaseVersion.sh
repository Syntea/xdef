#!/bin/bash
#create release-version
#parameters:
# - 1: next development version
set -e

scriptDir="$(dirname $(readlink -f "$0"))"

#run from repo-root-dir
cd ../..

. ${scriptDir}/base/env.sh
${scriptDir}/base/git-checkDirRepo.sh main

${scriptDir}/base/git-merge-dev2main.sh
${scriptDir}/base/git-createReleaseCommits.sh $@
${scriptDir}/base/git-merge-main2dev.sh

echo '====================='
echo 'successfully finished'
echo '====================='
