# Kontrola vzhledu stranek webu pri vyvoji primo ze zdrojoveho kodu pomoci file-protokolu

Pri vyvoji stranek webu lze jejich vzhled (velmi blizky cilove podobe) okamzite kontrolovat v prohlizeci
primo ze zdrojoveho kodu pres file-protokol, tj. napr. url
file:///home/sisma/projekt/xdef-parent/xdef-web/src/main/webapp/index.html.

Akorat v zakladnim nastaveni prohlizecu se nenactou hlavicka, paticka a podobne
(objevi se hlaska: ERROR: HEADER NOT LOADED). A to kvuli CORS-policy (Cross-Origin Resource Sharing).
To lze vyresit pro jednotlive prohlizece nasledujicim nastavenim:
  * Mozilla Firefox: upravit nastaveni "about:config"
    * abour:config > polozka "security.fileuri.strict_origin_policy" > nastavit na hodnotu "false"
  * Google Chrome: spustit chrome s volbou "--allow-file-access-from-files", tj. prikaz (spustte z terminalu):
    * > chrome.exe --allow-file-access-from-files
  * Microsoft IE-Edge: ... [doplnit]
