<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    version  ="3.0"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs ="http://www.w3.org/2001/XMLSchema"
    xmlns:h  ="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="xsl xs h"
>

<xsl:output
    method="html"
    version="5"
    indent="no"
    encoding="UTF-8"
/>
<!--
    omit-xml-declaration="yes"
    doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
    doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"
-->


<xsl:variable name="nl" select="'
'" as="xs:string"/>



<xsl:template match="/">
    <xsl:result-document><xsl:apply-templates select="h:html"/></xsl:result-document>
</xsl:template>



<xsl:template match="h:html">
    <xsl:variable name="nav"             select="h:body/h:div[@id='footer']"                        as="element(h:div)?"/>
    <xsl:variable name="navIts"          select="$nav/*[self::h:a or self::h:img]"                  as="element()*"/>
    <xsl:variable name="content"         select="h:body/h:div[@id='body']/node()"                   as="node()*"/>
    <xsl:variable name="title"           select="$content/self::h:h2"                               as="element(h:h2)"/>
    <xsl:variable name="contAfterTitle"  select="$content/self::h:h2/following-sibling::node()"     as="node()*"/>
    
    <xsl:sequence xml:space="preserve"><html lang="en">
  <head>
    <title><xsl:sequence select="h:head/h:title/text()"/></title>
    <meta name="description" content="{h:head/h:meta[@name='description']/@content}"/>
    <link rel="icon" type="image/x-icon" href="../style/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="../style/common.css"/>
    <script type="module" src="../style/common.js"></script>
    <link rel="stylesheet" type="text/css" href="style/common.css"/>
  </head>
  <body>
      <div id="header"><span class="errorVD">ERROR: HEADER NOT LOADED</span></div>


      <div class="title">X-definition tutorial</div>

      <xsl:if test="$nav"><div class="nav">
        <xsl:apply-templates mode="nav" select="$navIts[1]"/>
        <xsl:apply-templates mode="nav" select="$navIts[2]"/>
        <xsl:apply-templates mode="nav" select="$navIts[3]"/>
        <xsl:apply-templates mode="nav" select="$navIts[4]"/>
      </div></xsl:if>

      <h2><xsl:sequence select="$title/text()"/></h2>
      <xsl:apply-templates mode="content" select="$contAfterTitle"/>

      <div id="footer"><span class="errorVD">ERROR: FOOTER NOT LOADED</span></div>

      <script type="module">initPageBasicHili()</script>
  </body>
</html>
</xsl:sequence>
</xsl:template>



<xsl:template mode="nav" match="h:img">
    <img src="{@src}" alt="{@alt}"/>
</xsl:template>

<xsl:template mode="nav" match="h:a">
    <a href="{@href}"><img src="{h:img/@src}" alt="{h:img/@alt}"/></a>
</xsl:template>


<xsl:template mode="content" match="h:*">
    <xsl:element name="{local-name()}">
        <xsl:sequence select="@*"/>
        <xsl:apply-templates mode="content" select="node()"/>
    </xsl:element>
</xsl:template>

<xsl:template mode="content" match="text()">
    <xsl:sequence select="."/>
</xsl:template>

<xsl:template mode="content" match="h:pre[@class]">
    <pre>
        <code>
            <xsl:attribute name="class" select="concat('language-', @class)"/>
            <xsl:sequence select="node()"/>
        </code>
    </pre>
</xsl:template>



<!-- zpracuje jeden JIRA-task podle danych formatovacich pravidel do html-odstavce, chyby se vypisi jako komentar -->
<xsl:template match="item">
    <!-- cely clanek -->
    <xsl:variable name="desc"       select="parse-xml-fragment(description/text())"/>
    <!-- separace prvnich tri odstavcu Popis, Analyza, Reseni -->
    <xsl:variable name="popisTit"   select="($desc/h1)[1]"                          as="element(h1)?"/>
    <xsl:variable name="popisTitZa">
        <xsl:copy-of                select="$popisTit/following-sibling::node()"/>
    </xsl:variable>
    <xsl:variable name="analyzaTit" select="($popisTitZa/h1)[1]"                    as="element(h1)?"/>
    <xsl:variable name="popis"      select="$analyzaTit/preceding-sibling::node()"  as="node()*"/>
    <xsl:variable name="analyzaTitZa">
        <xsl:copy-of                select="$analyzaTit/following-sibling::node()"/>
    </xsl:variable>
    <xsl:variable name="reseniTit"  select="($analyzaTitZa/h1)[1]"                  as="element(h1)?"/>
    <xsl:variable name="analyza"    select="$reseniTit/preceding-sibling::node()"   as="node()*"/>
    <xsl:variable name="reseniTitZa">
        <xsl:copy-of                select="$reseniTit/following-sibling::node()"/>
    </xsl:variable>
    <xsl:variable name="dalsiTit"   select="($reseniTitZa/h1)[1]"                   as="element(h1)?"/>
    <xsl:variable name="reseni"     select="$dalsiTit/preceding-sibling::node()"    as="node()*"/>

    <!-- pole ID - ID u zakaznika -->
    <xsl:variable name="ID"         select="
        customfields/customfield[customfieldname/text() = 'ID']
    "                                                                               as="element(customfield)?"/>


    <xsl:variable name="testPopis"   select="$popisTit  /text() = 'Popis'"          as="xs:boolean"/>
    <xsl:variable name="testAnalyza" select="$analyzaTit/text() = 'Analýza'"        as="xs:boolean"/>
    <xsl:variable name="testReseni"  select="$reseniTit /text() = 'Řešení'"         as="xs:boolean"/>

    <xsl:variable name="testPopHead" select="exists(($popis/self::*)[1]/self::ul)"  as="xs:boolean"/>

    <xsl:variable name="test"        select="
        $testPopis and $testAnalyza and $testReseni and $testPopHead
    "                                                                               as="xs:boolean"/>

    <!-- chyby se vypisi s cervenym podbarvenim -->
    <div>
        <!-- nadpis tasku s JIRA-ID a s moznosti byt odkazovan pres a/@name-->
        <h1>
            <a name="{key/text()}"/>
            <xsl:sequence select="summary/text()"/>
            <xsl:text> (</xsl:text>
            <xsl:sequence select="key/text()"/>
            <xsl:text>)</xsl:text>
        </h1>

        <!-- odstavec s popisem -->
        <h2>
            <xsl:text>Popis</xsl:text>
            <xsl:if test="not($testPopis)">
                <span xml:space="preserve"> </span>
                <span class="j2dChyba">První odstavec není "Popis"</span>
            </xsl:if>
            <xsl:if test="not($testPopHead)">
                <span xml:space="preserve"> </span>
                <span class="j2dChyba">Popis nemá hlavičku</span>
            </xsl:if>
        </h2>
        <xsl:apply-templates mode="trans" select="$popis">
            <xsl:with-param name="ID" select="if ($ID) then parse-xml-fragment(string($ID/customfieldvalues/customfieldvalue)) else ()"/>
        </xsl:apply-templates>

        <!-- odstavec s analyzou -->
        <h2>
            <xsl:text>Analýza</xsl:text>
            <xsl:if test="not($testAnalyza)">
                <span xml:space="preserve"> </span>
                <span class="j2dChyba">Druhý odstavec není "Analýza"</span>
            </xsl:if>
        </h2>
        <xsl:apply-templates mode="trans" select="$analyza"/>

        <!-- odstavec s resenim -->
        <h2>
            <xsl:text>Řešení</xsl:text>
            <xsl:if test="not($testReseni)">
                <span xml:space="preserve"> </span>
                <span class="j2dChyba">Třetí odstavec není "Řešení"</span>
            </xsl:if>
        </h2>
        <xsl:apply-templates mode="trans" select="$reseni"/>

        <!-- pokud clanek obsahuje nejakou formatovaci chybu umisti se cely nakonec s oranzovym podbarvenim -->
        <xsl:if test="not($test)">
            <div class="j2dOriginal">
                <xsl:sequence select="$desc"/>
            </div>
        </xsl:if>
    </div>
</xsl:template>



<xsl:template mode="trans" match="@* | node()">
    <xsl:param name="ID" as="xs:string?"/>

    <xsl:variable name="sub" as="node()*">
        <xsl:apply-templates mode="#current" select="node()"/>
    </xsl:variable>

    <xsl:choose>
        <!-- generovani nadpisu h1-6 jako h2-7 -->
        <xsl:when test="self::h1">
            <h2><xsl:sequence select="$sub"/></h2>
        </xsl:when>
        <xsl:when test="self::h2">
            <h3><xsl:sequence select="$sub"/></h3>
        </xsl:when>
        <xsl:when test="self::h3">
            <h4><xsl:sequence select="$sub"/></h4>
        </xsl:when>
        <xsl:when test="self::h4">
            <h5><xsl:sequence select="$sub"/></h5>
        </xsl:when>
        <xsl:when test="self::h5">
            <h6><xsl:sequence select="$sub"/></h6>
        </xsl:when>
        <xsl:when test="self::h6">
            <h7><xsl:sequence select="$sub"/></h7>
        </xsl:when>
        <!-- externi link -->
        <xsl:when test="self::a[@class = 'external-link']">
            <a title="{string($sub)}">
                <xsl:sequence select="@href, $sub"/>
            </a>
        </xsl:when>
        <!-- generovani odkazu na jiny JIRA-task, pripadne preskrtnuti (odkaz na hotovy JIRA-task) se odstrani -->
        <xsl:when test="self::a[@class = 'issue-link']">
            <a title="{@title}" href="#{@data-issue-key}">
                <xsl:sequence select="
                    for $n in $sub return
                        if ($n/self::del) then $n/node() else $n
                "/>
                <xsl:text> (</xsl:text><xsl:value-of select="@title"/><xsl:text>)</xsl:text>
            </a>
        </xsl:when>
        <!-- generovani obrazku - ruzne typy umisteni v clanku -->
        <xsl:when test="self::span[@class = 'image-wrap' and a[@href and img[@src]]]">
            <img src="{a/@href}">
                <xsl:sequence select="a/img/@width | a/img/@height | a/img/@style"/>
            </img>
        </xsl:when>
        <xsl:when test="self::span[@class = 'image-wrap' and img[@src]]">
            <img src="{img/@src}">
                <xsl:sequence select="img/@width | img/@height | img/@style"/>
            </img>
        </xsl:when>
        <!-- pridani externiho ID na konec hlavicky (coz je prvni <ul>), je-li ID zadano -->
        <xsl:when test="$ID and self::ul and empty(preceding-sibling::*)">
            <xsl:copy>
                <xsl:sequence select="$sub"/>
                <li>ID: <xsl:sequence select="$ID"/></li>
            </xsl:copy>
        </xsl:when>
        <xsl:otherwise>
            <xsl:copy>
                <xsl:sequence select="@*, $sub"/>
            </xsl:copy>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>



</xsl:stylesheet>
