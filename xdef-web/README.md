# The Project Architecture

This maven-module is a servlet-web project. Implements web "xdef.org", the introduction web to X-definition,
e.g. placed at http://xdef.org mainly.

## Used APIs, standards, libraries

  * target Java-platform: Java SE 21
  * target distribution package:
    * WAR-file: ``xdef-web.war``
  * servlet API: Jakarta Servlet 6.1 for Jakarta EE 11 (requires Java SE 17+)
    * see https://jakarta.ee/specifications/servlet/6.1/
    * e.g. requires web-servlet server "Tomcat 11" (download at https://tomcat.apache.org/download-11.cgi)
  * html-pages version: HTML 5

## Used libraries

Javascript libraries and plugins:
  * jQuery 4.0.0
    * https://jquery.com/
  * jQuery - Customizable Line Numbers For Textareas
    * https://www.jqueryscript.net/form/customizable-line-numbers-textarea.html

# Development Notes

## Kontrola vzhledu stranek webu pri vyvoji primo ze zdrojoveho kodu pomoci file-protokolu

Pri vyvoji stranek webu majici staticky charakter (coz je v tomto projektu vetsina) lze jejich vzhled (velmi blizky
cilove podobe) okamzite kontrolovat v prohlizeci primo ze zdrojoveho kodu pres file-protokol, tj. napr. url
file:///home/user/projekt/xdef-parent/xdef-web/src/main/webapp/index.html.

Akorat v zakladnim nastaveni prohlizecu se nenactou ajax-prvky, tj. napr. hlavicka, paticka a podobne
(zustane jejich puvodni podoba, obvykle s hlaskou: ``"ERROR: ... NOT LOADED"``). A to kvuli CORS-policy
(Cross-Origin Resource Sharing).
To lze vyresit pro jednotlive prohlizece nasledujici zmenou nastaveni:
  * Mozilla Firefox: upravit nastaveni "about:config"
    * abour:config > polozka "security.fileuri.strict_origin_policy" > nastavit na hodnotu "false"
  * Google Chrome: spustit chrome s volbou "--allow-file-access-from-files", tj. prikaz (spustte z terminalu):
    * ``> chrome --allow-file-access-from-files``
  * Microsoft Edge: spustit Edge s volbou "--disable-web-security", tj. prikaz (spustte z terminalu):
    * ``> msedge --disable-web-security --user-data-dir="/home/user/projekt"``
