#!/bin/bash
#do merge actual branch into main-branche 
set -e

pwd="$(pwd)"
branchCurrent="$(git branch --show-current)"

#default main-branch name
[ -n "${mainBranchName}" ] || { mainBranchName="main"; }

cd "../xdef-${mainBranchName}"
set -x
git merge --no-ff -m "Merge remote-tracking branch 'origin/${branchCurrent}'" "origin/${branchCurrent}"
git push
set +x
cd ${pwd}
