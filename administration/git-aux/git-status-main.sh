#!/bin/bash
#in main-repo show git-status, git-log-graph
#run from dir of this script
set -e

. base/env.sh

#cd to main-repo
cd "../../../xdef-${mainBranchName}"

set -x
git fetch
git status
set +x
read -p "Press key Enter to continue ... " enter
set -x
git log --graph --all
set +x
