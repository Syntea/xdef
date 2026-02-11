#!/bin/bash
#do merge main-branch into actual branch
#run from root-repo-dir
set -e

branchCurrent="$(git branch --show-current)"

#check variable main-branch name
[ -n "${mainBranchName}" ] || { echo "ERROR: var 'mainBranchName' is empty"; exit 1; }

set +e
(   set -e
    echo '=========================='
    echo 'Merge branch main into dev'
    echo '=========================='
    set -x
    git merge -m "Merge remote-tracking branch 'origin/${mainBranchName}' into '${branchCurrent}'" "origin/${mainBranchName}"
    git push
    set +x
)
[ $? -eq 0 ] || {
    set -e +x
    echo "ERROR: any previous failure, I will reset current branch" >&2
    branchCurrentName="$(git branch --show-current)"
    set -x
    git checkout -B "${branchCurrentName}" "origin/${branchCurrentName}"
    set +x
    exit 1
}
set -e
