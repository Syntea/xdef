#!/bin/bash
#commit and push changes and then merge main-branch into current branch
#run from dir of this script
set -e

scriptDir="$(dirname $(readlink -f "$0"))"

#run from repo-root-dir
cd ../..

. "${scriptDir}"/base/env.sh

#check dir-repo, git-pull included, dirty repo is accepted
"${scriptDir}"/base/git-checkDirRepo.sh dirty

#do commit and push all changes if repo is dirty
( unset LANG; git status; ) | grep -z 'nothing to commit, working tree clean' > /dev/null || {
    echo '==============='
    echo 'Commit and push'
    echo '==============='
    set -x
    git add --all
    git commit
    git push
    set +x
}

#do merge branch-main, may be empty
"${scriptDir}"/base/git-merge-main2dev.sh

echo '====================='
echo 'successfully finished'
echo '====================='
