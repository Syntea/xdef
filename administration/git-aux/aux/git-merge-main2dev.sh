#!/bin/bash
#do merge main-branch into actual branch
set -e

branchCurrent="$(git branch --show-current)"

#default main-branch name
[ -n "${mainBranchName}" ] || { mainBranchName="main"; }

set -x
git merge -m "Merge remote-tracking branch 'origin/${mainBranchName}' into '${branchCurrent}'" "origin/${mainBranchName}"
git push
set +x
