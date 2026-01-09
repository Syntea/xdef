#!/bin/bash
#create release-version
set -e

scriptDir="$(dirname $(readlink -f "$0"))"

#run from repo-root-dir
cd ../..

. ${scriptDir}/aux/env.sh

${scriptDir}/aux/git-checkDirRepo.sh main
${scriptDir}/aux/git-merge-dev2main.sh

${scriptDir}/aux/git-checkDirRepo.sh
${scriptDir}/aux/git-merge-main2dev.sh

${scriptDir}/aux/git-checkDirRepo.sh main
pwd=$(pwd)
cd "../xdef-${mainBranchName}"
${scriptDir}/aux/git-createReleaseCommits.sh
cd ${pwd}
