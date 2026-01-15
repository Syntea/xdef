#auxiliary commands to fix repo in not-up-to-date or not-clean status
#=====

#sloucit rozhozenou vetev, tj. byla vytvorena druha varianta vetve
git pull

#reset changes in actual branch
git reset --hard

#checkout remote branch and reset local brach to it
git checkout -B user/trojan/dev origin/user/trojan/dev
git checkout -B main origin/main

#similar to previous, but without checkout - reset local branch to the remote branch
git branch -f origin/user/trojan/dev
git branch -f origin/main

#smazat main-repo, scripty si je pripadne opet stahnou z GitHub-u samy
rm -rf .../project/xdef-main
