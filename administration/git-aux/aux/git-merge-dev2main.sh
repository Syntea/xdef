#!/bin/bash
#in main-repo do merge actual branch into main-branche 
set -e

pwd="$(pwd)"
branchCurrent="$(git branch --show-current)"

#check main-branch name
[ -n "${mainBranchName}" ] || { echo "ERROR: var 'mainBranchName' is empty"; exit; }

#enter into main-repo
cd "../xdef-${mainBranchName}"

echo '=========================='
echo 'Merge branch dev into main'
echo '=========================='
set -x
git merge --no-ff -m "Merge remote-tracking branch 'origin/${branchCurrent}' into '${mainBranchName}'" "origin/${branchCurrent}"
git push
set +x

#reenter back and pull
cd ${pwd}
echo "git-repo $(pwd): pull"
set -x
git pull
set +x
