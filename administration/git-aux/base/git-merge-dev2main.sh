#!/bin/bash
#in repo-main merge actual branch into branche-main and also do verifying snapshot-build
#run from root-repo-dir
set -e

pwd="$(pwd)"
branchCurrent="$(git branch --show-current)"

#check variable main-branch name
[ -n "${mainBranchName}" ] || { echo "ERROR: var 'mainBranchName' is empty"; exit 1; }

#enter into main-repo
cd "../xdef-${mainBranchName}"

set +e
(   set -e
    echo '=========================='
    echo 'Merge branch dev into main'
    echo '=========================='
    set -x
    git merge --no-ff -m "Merge remote-tracking branch 'origin/${branchCurrent}' into '${mainBranchName}'" "origin/${branchCurrent}"
    set +x
    
    echo '==============================='
    echo 'Verifying snapshot-build: start'
    echo '==============================='
    set -x
    mvn clean package -Pdoc
    set +x
    echo '===================================='
    echo 'Verifying snapshot-build: successful'
    echo '===================================='
    
    set -x
    git push
    set +x
)
[ $? -eq 0 ] || {
    set -e +x
    echo "ERROR: any previous failure, I will reset branch-main" >&2
    branchCurrentName="$(git branch --show-current)"
    set -x
    git checkout -B "${branchCurrentName}" "origin/${branchCurrentName}"
    set +x
    exit 1
}
set -e

#reenter back and fetch
cd ${pwd}
echo "git-repo $(pwd): fetch"
set -x
git fetch
set +x
