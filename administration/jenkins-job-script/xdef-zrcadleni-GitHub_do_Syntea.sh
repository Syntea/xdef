: "git pull user/trojan/dev z GitHub"
git checkout user/trojan/dev
git pull

: "zpet do main a pull"
git checkout main
git pull

: "git-push vsechny vetve a tagy do Syntea-GitLab"
git push --verbose --all syntea
git push --verbose syntea --tags
