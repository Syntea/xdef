#!/bin/bash
#vypis git-status, git-log-graph
set -e

set -x
git status
set +x
read -p "Press key Enter to continue ... " enter
set -x
git log --graph --all
set +x
