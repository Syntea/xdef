: 'mark existing git-tag ${GIT_TAG} as a release-tag on GitHub.com on repository "Syntea/xdef":'
#  - viz https://wiki.syntea.cz/doku.php?id=syntea:navody:github#github-api operation "Create a release"
#  - it's needed authentificating github-token ${CREATERELEASE_GITHUB_TOKEN} for this operation
result=$(curl \
    -i -L \
    -X POST \
    -H "Accept: application/vnd.github+json" \
    -H "Authorization: Bearer ${CREATERELEASE_GITHUB_TOKEN}" \
    -H "X-GitHub-Api-Version: 2026-03-10" \
    -d "{\"tag_name\":\"${GIT_TAG}\"" \
    https://api.github.com/repos/Syntea/xdef/releases 
)
echo "${result}" | head --lines=1 | grep '^HTTP/2 201\s*$' > /dev/null || {
    echo "ERROR: failed: create release-tag on GitHub.com on the repository \"Syntea/xdef\", error-response:"
    echo "${result}"
    exit 1
}
