#auxiliary commands to fix repo in not-up-to-date or not-clean status
#=====

#reset changes in actual branch
git reset --hard

#checkout remote branch and create local brach, if already exists replaces it
git checkout -B main origin/main
git checkout -B user/trojan/dev origin/user/trojan/dev
#similar to previous, create local branch to the start-point, if already exists replaces it
git branch -f origin/main
git branch -f origin/user/trojan/dev

#smazat main-repo, scripty si je pripadne opet stahnou z GitHub-u samy
rm -rf .../project/xdef-main
