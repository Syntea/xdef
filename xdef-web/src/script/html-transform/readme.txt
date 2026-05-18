Postup použití - instalace a spuštění:
======================================

- zvolit nějaký pracovní adresář

- zkopírovat nástroj "balik-jira2dok" z SVN http://treska/svn/cz/syntea/isdn/support/trunk/src/skript/balik-jira2dok
  do pracovního adresáře:
    - readme.txt (tento soubor, návod k použití)
    - balik-jira2dok.bat
    - balik-jira2dok.xsl
    - stáhnout příslušnou verzi (viz balik-jira2dok.sh, proměnná "saxonVersion") knihovny Saxon z:
        - https://search.maven.org/artifact/net.sf.saxon/Saxon-HE

- provést xml-export úkolů z JIRA-y (viz například iDCM:ISDN (TD) Build 1.XX.YY - Zmeny(!92427)(1).DOC):
    - "JIRA > Issues > Search for Issues“ pro nějaký JQL-dotaz
    - proveďte xml-export tlačítkem “Export > XML” do lokálního souboru, např. SearchResult.xml

- spustit:
    > ./balik-jira2dok.sh SearchRequest.xml

- výsledek je v:
    - output.html

Pozn.:
    - pro OS Windows místo balik-jira2dok.sh použijte balik-jira2dok.bat
    - pro OS Linux si nástroj knihovnu Saxon stáhne automaticky sám, pokud máte nainstalovaný Maven
