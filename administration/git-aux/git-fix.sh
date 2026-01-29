#auxiliary commands to fix repo in not-up-to-date or not-clean status, don't run as script

echo "don't run as script, select one command and run it, the listing of this file follows:"
echo "====================================================================================="
cat "$(readlink -f "$0")"
exit

#sloucit rozeslou vetev, tj. byla vytvorena druha varianta vetve
git pull

#reset changes in actual branch
git reset --hard

#checkout remote branch and reset local brach to it
git checkout -B user/trojan/dev origin/user/trojan/dev
git checkout -B main origin/main

#similar to previous, but without checkout - reset local branch to the remote branch
git branch -f origin/user/trojan/dev
git branch -f origin/main

#smazat repo-main, scripty si je pripadne znovu stahnou z GitHub-u samy ciste
rm -rf .../project/xdef-main

#stahnout projekt Syntea/xdef z GitHub.com (vetve "main", do adresare "xdef-main")
git clone --branch main git@github.com:Syntea/xdef.git xdef-main

#fetch a reset vzdalenych vetvi a tagu
git fetch --prune --prune-tags --force

#vypis commit-u v podobe grafu
git log --graph --all
