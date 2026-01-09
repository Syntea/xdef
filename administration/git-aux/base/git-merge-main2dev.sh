#!/bin/bash
#do merge main-branch into actual branch
set -e

branchCurrent="$(git branch --show-current)"

#check variable main-branch name
[ -n "${mainBranchName}" ] || { echo "ERROR: var 'mainBranchName' is empty"; exit 1; }

echo '=========================='
echo 'Merge branch main into dev'
echo '=========================='
set -x
git merge -m "Merge remote-tracking branch 'origin/${mainBranchName}' into '${branchCurrent}'" "origin/${mainBranchName}"
git push
set +x
