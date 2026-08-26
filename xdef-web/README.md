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



# Ideas To Be Implemented

Ideas already discussed and agreed on, but postponed. Roughly ordered by usefulness.


## Playground: protect the memory by refusing database-requests when the heap is nearly full

A small admission-control guard in [Playground.java](src/main/java/org/xdef/web/servlet/Playground.java):
when the heap is nearly full (e.g. over 90%), do not process a request using a database at all.

  * measure the heap **as it is after the last garbage-collection**, not as it is at the moment:
    ``ManagementFactory.getMemoryPoolMXBeans()``, find the old-generation pool and read its
    ``getCollectionUsage()`` (not ``getUsage()``). The usual ``(totalMemory - freeMemory)`` of ``Runtime``
    counts the not yet collected garbage too, so it reports "nearly full" regularly just before a
    garbage-collection, when in fact nearly everything is collectible.
    Note that ``getCollectionUsage()`` returns ``null`` when the running collector does not support it,
    so a fallback is needed.
  * refuse **every** request having the parameter ``databaseName``, no matter whether the database already
    exists or not: it cannot be told in advance whether an X-definition will write into the database,
    that is decided at run-time inside ``dbservice.execute(...)``. Requests without a database are not
    affected, they do not touch the databases at all.
  * report the refusal through the usual error-path (``ProcessParams.status``/``title``/``message``), so
    that it is rendered as any other error and the filled form is kept for a re-submit. An HTTP-503 would
    be formally more correct, but it would break the playground's user experience.
  * on exceeding the limit run the db-cleanup at once (out of its schedule), so that the situation is
    resolved in seconds instead of waiting until some database reaches its 30 minutes of inactivity.

What this guard does and does not cover: it stops one source of the growth - the databases. It does not
stop an already running X-definition from filling an existing database, nor the memory taken by compiling
and processing the X-definition itself. It narrows the risk, it is not a limit.


## Turn off the external entities and the xinclude of the processed XML-data (XXE)

The Playground runs X-definitions on data sent by anyone, so an input like
``<!DOCTYPE r [<!ENTITY xxe SYSTEM "file:///etc/passwd">]><r>&xxe;</r>`` may read server-side files, the
same by ``<xi:include href="file:///..."/>``. The properties intended for it are prepared, but commented
out in ``getXdPropsDefault()`` in
[XdefServletAbs.java](src/main/java/org/xdef/web/servlet/XdefServletAbs.java), marked "FIXME: not
functional": ``XDPROPERTY_DOCTYPE`` and ``XDPROPERTY_XINCLUDE`` do not reach the parser that validation
really uses.

The fix belongs to the core module ``xdef``, it cannot be done here:
  * ``KDOMBuilder`` (used e.g. when composing) has been fixed already - the features
    ``external-general-entities``, ``external-parameter-entities`` and ``load-external-dtd`` are set
    **before** ``newDocumentBuilder()``, where they take effect. Its ``_resolveIncludes`` still defaults
    to ``true`` though, in contradiction with its own field-comment, so an xinclude is still resolved.
  * ``ChkParser`` (the parser actually used by ``xd.xparse()``, i.e. by the validation) bypasses
    ``KDOMBuilder`` completely - it has its own ``SAXParserFactory`` and its own ``EntityResolver``, which
    fetches an external general entity regardless of any per-X-definition setting.

A change-request for the maintainer of the ``xdef`` library was written for it (a new property
``XDConstants.XDPROPERTY_EXTERNAL_ENTITIES``). Once the library supports it, only uncomment the two
properties in ``getXdPropsDefault()`` here.


## HeaderFooterFilter: locate the placeholder-divs by the html-parser instead of by searching the text

Rewrite [HeaderFooterFilter.java](src/main/java/org/xdef/web/filter/HeaderFooterFilter.java) the way
[LatestVersionFilter.java](src/main/java/org/xdef/web/filter/LatestVersionFilter.java) already works: parse
the page only to **locate** the places (``elem.sourceRange()``, ``elem.endSourceRange()``) and replace them
in the original page-text, so that the page stays byte-identical everywhere else.

The reason is correctness, not speed. The current ``replaceDiv()`` searches for the exact text
``<div id="header">`` and then for the **first** following ``</div>``, which means:
  * **it is not idempotent** - a second pass over an already processed page corrupts it (measured: a page
    grew from 2485 to 4119 characters), because the inserted header contains nested divs and the search
    ends at the first inner ``</div>``. It cannot happen with the current filter-mappings, but it is a trap:
    it is enough to map one of the servlets to an url ending with ".html" too.
  * it is fragile about how the placeholder is written - ``<div id='header'>`` with apostrophes, a different
    order of the attributes or an extra space and the replacement is silently skipped.

The price: this filter is mapped to ``*.html`` and to the servlets, i.e. to all pages, so every page would
be parsed (units of ms for a 16 kB page), while today it is only a text-search. And ``other/download.html``
and ``other/documentation.html`` would be parsed twice, as they go through both filters.

It is worth extracting the "replace at the source-positions in the original text" logic into a shared
helper-class next to [BufferingResponseWrapper.java](src/main/java/org/xdef/web/filter/BufferingResponseWrapper.java) -
the filters stay separate, they only share the tool.


## Playground: smaller ideas

  * **run the servlet-init at the deployment**:
    [Playground.java](src/main/java/org/xdef/web/servlet/Playground.java) has no ``<load-on-startup>`` in
    [web.xml](src/main/webapp/WEB-INF/web.xml), so its ``init()`` - registering the db-driver and starting
    the db-cleanup timer - runs at the first request touching the servlet, and that request pays for it.
  * **take the database-settings from init-params**: ``dbUser``, ``dbPassw`` and ``dbTTL`` are hard-coded.
    Since the setup moved from a static-block into ``init()``, the ``ServletConfig`` is available there, so
    they could be configured in web.xml.
  * **shut the databases down in parallel in ``destroy()``**: dropping one in-memory Derby database takes
    about 0.5 s regardless of its size (measured, and the same on Derby 10.12 and 10.17), and ``destroy()``
    drops them one by one. With many databases alive at the shutdown it delays the shutdown of the whole
    container by N × 0.5 s.


## A single source of the supported languages

The list of the localized subsites ("cs", "es", "eo") and the way the language is resolved are written twice:
in [LangRedirectFilter.java](src/main/java/org/xdef/web/filter/LangRedirectFilter.java) +
``ServletUtil.detectLanguage()`` and in ``redirectToLangIndex()`` in
[common.js](src/main/webapp/style/common.js). The javascript one is the authoritative one - without it the
website does not work at all, the filters are only an optimization - so when they diverge, the result is
"it flashes as before" or "it does not redirect", not a broken page. Still, at least the list itself can
have a single source.

**The way to do it: distribute the list from a maven-property by the resource-filtering**, which this
project already uses for other things:
  * a property in [pom.xml](pom.xml), e.g. ``<webapp.languages>cs,es,eo</webapp.languages>``
  * **java**: add a line into
    [buildinfo.properties](src/main/resources-filters/org/xdef/web/config/buildinfo.properties) (that
    whole directory is filtered already) and read it the way
    [BuildInfo.java](src/main/java/org/xdef/web/config/BuildInfo.java) reads the properties there
  * **javascript**: put the constant into a **new small file ``style/common-consts.js``**, add only that
    file to the filtered ``webResources`` of the war-plugin (today only the four ``footer.html`` files are
    there) and import it into common.js, which is a module already:
    * common-consts.js: ``export const supported = "${webapp.languages}".split(",");``
    * common.js: ``import { supported } from "./common-consts.js"``

Keeping the filtering away from the hand-written common.js is worth it: **javascript template-literals use
the very same ``${...}`` syntax as the maven-filtering**. Today common.js contains no template-literal
(only the literal ``"${rootPath}"`` in ``replaceHtml()``, and an unknown property is left untouched by the
maven-resources-plugin, as the already filtered footer.html proves - there ``${project.version}`` gets
replaced while ``${rootPath}`` stays as it is). But it is enough to write e.g. ``` `${lang}/index.html` ```
one day and, if a property of that name exists, the build would silently rewrite the code. A tiny generated
file cannot get into such a conflict, and it is also the natural place for any other build-time value
needed in the javascript.

What to expect when the filtering did not run, i.e. when developing directly from the source-code over
the file-protocol: the javascript gets the list as one item ``"${webapp.languages}"``, no language matches
and nothing is redirected. It is the same kind of degradation as the already filtered footer.html has
(it shows the literal ``${project.version}`` in the source-code), and while developing it is rather welcome
- one wants to see the page one opened. It does not break the "plain static files" principle either: that
one is about **where the webapp is installed**, and what gets installed is the output of the build, i.e.
the already filtered file. Only the raw source-tree stays unfiltered.

The duplication of the resolving logic itself cannot be removed: it is the price for the requirement that
the pages **must work as plain static files too**, where no filter runs.


## FAQ-pages: fix the content of the answers

Found by a review of [faq.html](src/main/webapp/other/faq.html) and verified against the sources of the
core module ``xdef``. The same text is in [faq2.html](src/main/webapp/other/faq2.html) and
[faq3.html](src/main/webapp/other/faq3.html) as well, so whichever of the three variants wins, the answers
have to be fixed there.

**The java-samples do not compile at all**, they come from the times of the old package-names:

  1. **package-names that do not exist any more**: ``cz.syntea.xd.XDDocument``, ``.XDFactory``, ``.XDPool``
     are now ``org.xdef.*``, ``cz.syntea.common.xml.KXmlUtils`` is ``org.xdef.xml.KXmlUtils`` and
     ``cz.syntea.common.sys.ArrayReporter``, ``.Report`` are ``org.xdef.sys.*``. On top of that the samples
     start with ``package cz.syntea.skp.slp2.lp.epemog;``, an internal package-name of some old project,
     which makes no sense in a public example.
  2. **``XDFactory.genXDPool(...)`` does not exist**: there is no such method in the whole core module, the
     only place mentioning it is an outdated comment in its own ``package-info.java``. The method to use is
     ``XDFactory.compileXD(Properties, String...)``, i.e. ``XDFactory.compileXD(null, "C:/xdef/validation.xdef")``.
  3. **a bug in the sample itself**: ``System.out.printf(KXmlUtils.nodeToString(result, true))`` - a one
     argument ``printf`` takes its string as a format, so as soon as the printed XML contains a ``%``, it
     throws ``UnknownFormatConversionException``. The other sample correctly uses ``println``.
  4. **an obsolete namespace**: the xml-sample uses ``http://www.syntea.cz/xdef/2.0``, while the rest of the
     website uses ``http://www.xdef.org/xdef/4.2`` (20 occurrences).

**The text does not match the samples**:

  5. the text says "defined using the keyword **xd:declarations**", the sample right under it uses
     ``<xd:declaration>`` (singular) - and so does the text of another answer on the same page.
  6. "The **x-definitionParser** contains a large number of data types" - no such thing exists, it was
     probably meant to be "X-definition".

**The structure of the page**:

  7. **faq.html has no heading at all** (no ``h1``, no ``h2``, no ``h3``): the questions are the plain text
     of the summaries. So the page has no outline for the assistive technologies nor for the search engines,
     although it is six chapters by its content. A ``<summary>`` may contain one heading element, so putting
     an ``<h2>`` back inside it would fix that and the styling could stay on the summary.
     (faq2.html has 2 ``h1`` + 6 ``h2``, faq3.html has 6 ``h2``.)
  8. **the redundant "Question: "** at the beginning of each of the six questions: on a page named FAQ it
     carries no information and it is the first thing on every line.

**A detail**:

  9. the ``meta`` description is literally the same as the ``title`` - a wasted place for the search engines,
     a sentence about the content of the page would serve better.

What is fine: the pages are valid XML (with the exception of the header, as the principle above requires),
the menu links to the FAQ in all the language-versions, ``lang="en"`` fits, and the permanent links and the
anchors of the questions all resolve.


## Pre-compile the "mustache" template

``ServletUtil.mustache()`` runs a regexp over the whole template on every request, although the template
never changes. It could be split once into a list of literals and a list of keys, and the response then only
concatenated - probably one extra class in ``org.xdef.web.util``.

Expect no measurable speed-up (the template-filling takes about 1% of the request time), the gain is the
simpler code and dropping the dependency on ``Matcher.quoteReplacement()``.


## Rejected alternatives

Ideas that look obvious but were considered and rejected, with the reason:

  * **running the X-definition in a separate process limited in memory and time** (a real, OS-enforced limit,
    unlike any in-process trick): the in-memory Derby databases live inside one JVM-process only, so a child
    process would not see the database used by the other requests and the whole ``dbservice`` feature would
    break. Not worth it for the purpose of this webapp.
  * **an XML-parser instead of an html-parser** for locating the places to replace in the pages: 105 of the
    139 html-pages of this project are not well-formed XML (html-entities like ``&nbsp;``, unclosed ``<link>``
    and ``<meta>``, a bare ``&`` in an url), so an XML-parser would fail on most of them. Beside that, writing
    a parsed document back would rewrite the whole page, not only the replaced places.
  * **generating byte-code for the templates** (in the style of JSP): the loop over the template-parts is
    compiled by the JIT anyway, so there is nothing measurable to gain, and the generating itself costs more
    than it saves.
