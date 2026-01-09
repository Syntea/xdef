#!/bin/bash
#create snapshot-version
set -e

scriptDir="$(dirname $(readlink -f "$0"))"

#run from repo-root-dir
cd ../..

. ${scriptDir}/aux/env.sh
${scriptDir}/aux/git-checkDirRepo.sh main

${scriptDir}/aux/git-merge-dev2main.sh
${scriptDir}/aux/git-merge-main2dev.sh

echo '====================='
echo 'successfully finished'
echo '====================='
