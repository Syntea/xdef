<?xml version="1.0"  encoding="windows-1250" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html"/>
<xsl:template match="/">
<HTML>
<HEAD></HEAD>
<BODY>
<h2>Seznam panovníků</h2>
<xsl:apply-templates select="panovnici/panovnik/jmeno"/>
<h2>Seznam jejich známých manželek</h2>
<xsl:apply-templates select="panovnici/panovnik/manzelky/manzelka"/>
</BODY>
</HTML>
</xsl:template>


<xsl:template match="jmeno">
<xsl:value-of select="."/><br/>
</xsl:template>

<xsl:template match="manzelka">
<xsl:value-of select="."/> 
<xsl:if test="@puvod"><xsl:text> rozená </xsl:text><xsl:value-of select="@puvod"/></xsl:if>
<br/>
</xsl:template>

</xsl:stylesheet> 
