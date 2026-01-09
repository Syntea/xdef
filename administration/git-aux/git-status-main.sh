#!/bin/bash
#in main-repo show git-status, git-log-graph
set -e

. base/env.sh

#cd to main-repo
cd "../../../xdef-${mainBranchName}"

set -x
git status
set +x
read -p "Press key Enter to continue ... " enter
git log --graph --all
