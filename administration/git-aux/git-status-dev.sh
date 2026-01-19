#!/bin/bash
#vypis git-status, git-log-graph
set -e

set -x
git fetch --prune --prune-tags --force
git status
set +x
read -p "Press key Enter to continue ... " enter
set -x
git log --graph --all
set +x
