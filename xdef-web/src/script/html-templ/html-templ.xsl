<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xs ="http://www.w3.org/2001/XMLSchema"
    xmlns:h  ="http://www.w3.org/1999/xhtml"
    exclude-result-prefixes="xs"
    version="3.0"
>

<xsl:output
    method="html"
    indent="no"
    encoding="utf-8"
    omit-xml-declaration="yes"
/>



<xsl:template match="/">
<xsl:result-document xml:space="preserve"><html lang="en">
<head>
    <title>2.1. Example of complete X-definition</title>
    <meta name="description" content="Example of X-definition"/>
    <meta charset="UTF-8"/>
    <link rel="icon" type="image/x-icon" href="../style/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="../style/common.css"/>
    <script type="module" src="../style/common.js"></script>
    <link rel="stylesheet" type="text/css" href="style/common.css"/>
    <link rel="stylesheet" type="text/css" href="../style/syntaxHighlighter.css" />
    <script src="../style/highlighter/shCore.js"></script>
    <script src="../style/highlighter/shBrushXml.js"></script>
</head>
<body>

<div id="header"><span class="errorVD">ERROR: HEADER NOT LOADED</span></div>


<div class="title">X-definition tutorial</div>

<div style="text-align: center">
    <a href="ch02.html"   ><img src="image/first.gif" alt="Previous chapter"/></a>
    <a href="ch02.html"   ><img src="image/prev.gif"  alt="Back"/></a>
    <a href="ch02s02.html"><img src="image/next.gif"  alt="Next"/></a>
    <a href="ch03.html"   ><img src="image/last.gif"  alt="Next chapter"/></a>
</div>

<h2>2.1. Example of complete X-definition</h2>

<p>
Note that the X-definition in the example below has the attributes "name" and "root" (see line 1).
The attribute "root" specifies which model (or models) from the X-definition can be used as the root
elements of the input data. The attribute "name" contains the name of X-definition (it is required
if the project is composed from more X-definitions). Note also that the element "xd:declaration"
has the attribute scope="global", which specifies that the contents of the declaration are "visible"
from all X-definitions (the attribute is optional and the default value is "local"
- i.e. the declaration is visible only from this X-definition).
</p>

<pre class="xml">
&lt;xd:def xmlns:xd="http://www.xdef.org/xdef/4.2" name="Example" root="Inventory" >
  &lt;xd:declaration scope="global">
    void message(String s) {
      outln(s);
    }
    int count = 0;
    type isbn int(10000000, 999999999);
  &lt;/xd:declaration>

  &lt;Inventory xd:script="init message('Created ' + now()); finally message('Processed ' + count + ' books');">
    &lt;Book xd:script="occurs +; onAbsence error('No books!'); finally {count++; outln('ISBN code: " ' + @ISBN)};"
          ISBN="isbn;"
          published="optional gYear();" >
      &lt;Author xd:script="occurs *" >
        string()
      &lt;/Author>
      &lt;Title>
        string();
      &lt;/Title>
    &lt;/Book>
  &lt;/Inventory>

&lt;/xd:def>
</pre>

<p>
You can try it <a href="ch02s01e00.html"><b>HERE</b></a>
</p>

<script type="text/javascript">dp.SyntaxHighlighter.HighlightAll('code')</script>

<div id="footer"><span class="errorVD">ERROR: FOOTER NOT LOADED</span></div>

<script type="module">initPageBasic()</script>

</body>
</html>
</xsl:result-document>
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
