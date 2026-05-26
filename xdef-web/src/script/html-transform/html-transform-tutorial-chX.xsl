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
    <xsl:variable name="nav"            select="h:body/h:div[@id='footer']"                         as="element(h:div)?"/>
    <xsl:variable name="navIts"         select="$nav/*[self::h:a or self::h:img]"                   as="element()*"/>
    <xsl:variable name="content"        select="h:body/h:div[@id='body']/node()"                    as="node()*"/>
    <xsl:variable name="title"          select="$content/self::h:h2"                                as="element(h:h2)"/>
    <xsl:variable name="contAfterTitle" select="$content/self::h:h2/following-sibling::node()"      as="node()*"/>
    <xsl:variable name="toc"            select="$contAfterTitle/self::h:dl/h:dt/h:a"                as="element(h:a)*"/>
    
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

      <h1><xsl:sequence select="$title/text()"/></h1>

      <div class="toc">
      Table of Contents:<ul><!--
        --><xsl:apply-templates mode="toc" select="$toc"/>
      </ul>
      </div>

      <div id="footer"><span class="errorVD">ERROR: FOOTER NOT LOADED</span></div>

      <script type="module">initPageBasic()</script>
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


<xsl:template mode="toc" match="h:a">
    <xsl:text>
        </xsl:text>
    <li>
    <a href="{@href}"><xsl:sequence select="text()"/></a>
    </li>
</xsl:template>



</xsl:stylesheet>
