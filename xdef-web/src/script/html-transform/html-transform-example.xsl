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
    <xsl:variable name="content"         select="h:body/node()"                                     as="node()*"/>
    
    <xsl:sequence xml:space="preserve"><html lang="en">
  <head>
    <title><xsl:sequence select="h:head/h:title/text()"/></title>
    <meta name="description" content="{h:head/h:meta[@name='description']/@content}"/>
    <link rel="icon" type="image/x-icon" href="../style/favicon.ico"/>
    <link rel="stylesheet" type="text/css" href="../style/common.css"/>
    <script type="module" src="../style/common.js"></script>
  </head>
  <body>
    <div id="header"><span class="errorVD">ERROR: HEADER NOT LOADED</span></div>


    <div class="title"><xsl:sequence select="h:head/h:title/text()"/></div>

    <xsl:apply-templates mode="content" select="$content"/>

    <div id="footer"><span class="errorVD">ERROR: FOOTER NOT LOADED</span></div>

    <script type="module">initPageBasicLined()</script>
  </body>
</html>
</xsl:sequence>
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

<xsl:template mode="content" match="h:p">
    <p>
    <xsl:apply-templates mode="content" select="node()"/>
    </p>
</xsl:template>

<xsl:template mode="content" match="h:form">
    <form method="post" action="../playground/Playground">
    <xsl:apply-templates mode="content" select="node()"/>
    </form>
</xsl:template>

<xsl:template mode="content" match="h:textarea[@name=('xdef', 'data')]">
    <textarea name="{@name}" rows="{count(tokenize(string-join(text()), '\n'))}" class="lined">
    <xsl:apply-templates mode="content" select="node()"/>
    </textarea>
</xsl:template>

<xsl:template mode="content" match="h:input[@type=('submit')]">
    <button type="submit">Validate</button>
</xsl:template>

<xsl:template mode="content" match="text()[.='X-definition']">
    <xsl:text>X-definition:</xsl:text>
</xsl:template>

<xsl:template mode="content" match="text()[.='Input data']">
    <xsl:text>Input data:</xsl:text>
</xsl:template>

<xsl:template mode="content" match="h:div[@class=('A', 'B') or @id=('line-numbers', 'line-numbers_1')]">
    <xsl:apply-templates mode="content" select="node()"/>
</xsl:template>

<xsl:template mode="content" match="h:font[starts-with(@style, 'background-color: lightgrey') = true()]">
</xsl:template>




</xsl:stylesheet>
