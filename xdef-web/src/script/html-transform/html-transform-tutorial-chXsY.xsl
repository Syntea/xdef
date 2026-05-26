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

      <div class="title">X-definition Tutorial</div>

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



</xsl:stylesheet>
