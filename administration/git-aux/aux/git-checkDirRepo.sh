#!/bin/bash
#check directory: git-repository (up-to-date and clean) and maven-project (to be org.xdef:xdef)
#parameter 1 values:
# - main:  check also main-repo, usually in directory "../xdef-main"
# - dirty: don't check repo is clean
set -e

pwd="$(pwd)"
scriptDir="$(dirname $(readlink -f "$0"))"

check () {
    echo "INFO: check directory: $(pwd)"

    #check actual dir is maven-project
    [ -f "pom.xml" ] || \
        { echo "ERROR: dir $(pwd) is not maven-project, file 'pom.xml' not found"; exit; }

    #check actual maven-project signature
    prjGroup=$(mvn help:evaluate -Dexpression=project.groupId -q -DforceStdout)
    prjArtifact=$(mvn help:evaluate -Dexpression=project.artifactId -q -DforceStdout)
    [ "${prjGroup}" = "org.xdef" -a "${prjArtifact}" = "xdef-parent" ] || \
        {   echo "ERROR: maven-project $(pwd) is not org.xdef:xdef-parent," \
                 "actual signature: ${prjGroup}:${prjArtifact}"
            exit
        }

    #git-pull
    set -x
    git pull
    set +x
    
    #check git status up-to-date and clean
    gitStatus="$(unset LANG; git status;)"
    echo "${gitStatus}" | grep -z 'Your branch is up to date with' > /dev/null || \
        { echo "ERROR: git-repo $(pwd) is not up-to-date"; set -x; git status; set +x; exit; }

    #check git status clean, if required
    if [ ! "$1" = "dirty" ]
    then
        echo "${gitStatus}" | grep -z 'nothing to commit, working tree clean' > /dev/null || \
            { echo "ERROR: git-repo $(pwd) is not up-to-date and clean"; set -x; git status; set +x; exit; }
    fi
}


echo '===================='
echo 'Check git-repository'
echo '===================='

#check repo/project in actual dir
check $1

#check secondary repo of main-branch if required
if [ "$1" = "main" ]
then
    #default main-branch name
    [ -n "${mainBranchName}" ] || { mainBranchName="main"; }

    if [ -d "../xdef-${mainBranchName}" ]
    then
        cd "../xdef-${mainBranchName}"
    else
        cd ..
        #clone git-repo "xdef" main-branche
        git clone git@github.com:Syntea/xdef.git "xdef-${mainBranchName}"
        cd "xdef-${mainBranchName}"
    fi

    #check repo/project
    check
    
    #check branch-name
    branchCurrentName="$(git branch --show-current)"
    [ "${branchCurrentName}" = "${mainBranchName}" ] || \
        {   echo "ERROR: git-repo $(pwd) branch name is not '${mainBranchName}'," \
                 "branch name: ${branchCurrentName}"
            exit
        }

    cd ${pwd}
fi
