version=grep version pom.xml
description=grep desc xdef/changelog.md
git tag "version/${version}" --description "${description}"
mvn versions:set-property -Dproperty=revision -DnewVersion=${VERSION_NEXT}
git commit
git push
git push tags
