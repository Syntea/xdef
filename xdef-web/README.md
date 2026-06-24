# The Project Architecture

This maven-module is a servlet-web project. Implements the website "xdef.org",
the introductory website to X-Definition, e.g. placed at http://xdef.org mainly.

The website have a technical feel with an emphasis on informativeness and simplicity
without unnecessary marketing and "for effect" elements, with a few exceptions.

The website is in English. The main page (only) is also  translated into Spanish, Czech and Esperanto
(Esperanto was added as a little joke because of an article from the XML-Prague conference in the "Media" chapter,
where Esperanto is mentioned).

The content of the pages Main-Page, Downloads, and Documentation closely matches the main-readme-file
**[/README.md](../README.md)**.


## Basic principles

Basic principles:
  * the website **must work everywhere**, no matter where (on which server or in which directory) it is installed
    (e.g. installations production, testing, experimental). It follows specifically that:
    * **all local links are relative** or behave as relative


## Used APIs, standards, libraries

  * target Java-platform: Java SE 21
  * target distribution package:
    * WAR-file: ``xdef-web.war``
  * servlet API: Jakarta Servlet 6.1 for Jakarta EE 11 (requires Java SE 17+)
    * see https://jakarta.ee/specifications/servlet/6.1/
    * e.g. requires web-servlet server "Tomcat 11" (download at https://tomcat.apache.org/download-11.cgi)
  * html-pages version: HTML 5
    * XHTML has been rejected due to lack of support, XHTML is practically processed as HTML
      (mainly by major web browsers)
    * if you want to process HTML using XML tools like XSLT, you can first convert the HTML to XHTML,
      e.g. using ``xmllint``.
      See for example [html-transform.sh](src/script/html-transform/html-transform.sh)
    * all html-pages should also be **valid XML-files**, respective XHTML-files,
      with the **exception of headers**

## Used libraries in webpages

Javascript libraries and plugins:
  * jQuery 4.0.0
    * https://jquery.com/
  * jQuery - Customizable Line Numbers For Textareas
    * https://www.jqueryscript.net/form/customizable-line-numbers-textarea.html
  * highlight.js - javaScript syntax highlighter supporting Node.js and the web, version 11.11.1
    * https://highlightjs.org
  * the implemetation of the header and the footer in webpages:
    * are loaded by javascript function ``fetch``, respective jQuery function ``$.load()``.
      See function ``loadHeaderFooter()`` in [common.js](src/main/webapp/style/common.js)
    * if necessary, the root directory is derived from the location of the page icon
      (given in ``/html/head/link[rel="icon"]``, i.e. from the location of the ``favicon.ico`` file).
      See const ``rootPathRes`` in the ``loadHeaderFooter()`` function
      in [common.js](src/main/webapp/style/common.js).
      For this reason, it is **essential that every page** has a **page icon specified**.



# Development Notes


## Checking the appearance of web-pages when developing directly from the source code using the file protocol

When developing web-pages of a static nature (which is the majority in this project), their appearance
(very close to the target form) can be checked immediately in a browser directly from the source code
via the **file-protocol**, i.e. e.g. url file:///home/user/project/xdef-parent/xdef-web/src/main/webapp/index.html.

However, in the basic browser settings, ajax elements, i.e. e.g. header, footer and the like, will not be loaded
(their original form will remain, usually with the message: ``"ERROR: ... NOT LOADED"``). This is due to the CORS-policy
(Cross-Origin Resource Sharing).
This can be solved for individual browsers by changing the following settings:
  * Mozilla Firefox: edit the "about:config" setting
    * about:config > item "security.fileuri.strict_origin_policy" > set to "false"
  * Google Chrome: start chrome with the "--allow-file-access-from-files" option, i.e. the command (run from the terminal):
    * ``> chrome --allow-file-access-from-files``
  * Microsoft Edge: start Edge with the "--disable-web-security" option, i.e. the command (run from the terminal):
    * ``> msedge --disable-web-security --user-data-dir="/home/user/projekt"``
