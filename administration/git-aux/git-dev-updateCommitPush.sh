#!/bin/bash
#commit and push changes and then merge main-branch into current branch
set -e

scriptDir="$(dirname $(readlink -f "$0"))"

#run from repo-root-dir
cd ../..

. ${scriptDir}/aux/env.sh

#check dir-repo, git-pull included, dirty repo is accepted
${scriptDir}/aux/git-checkDirRepo.sh dirty

#do commit and push all changes if repo is dirty
( unset LANG; git status; ) | grep -z 'nothing to commit, working tree clean' > /dev/null || {
    set -x
    git add --all
    git commit
    git push
    set +x
}

#do merge main-branch, may be empty
${scriptDir}/aux/git-merge-main2dev.sh
