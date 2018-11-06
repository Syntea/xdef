<?xml version="1.0"  encoding="windows-1250" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html"/>
<xsl:template match="/">
    <HTML>
        <HEAD>
            <TITLE>
                Panovníci českých zemí
            </TITLE>
        </HEAD>
        <BODY>
           	<xsl:apply-templates select="panovnici" mode="panovnik"/>
          	<xsl:apply-templates select="panovnici" mode="manzelka"/>
        </BODY>
    </HTML>
</xsl:template>

<xsl:template match="panovnici" mode="panovnik">
<table border="border">
<tr>
<td>Panovník</td>
<td>vládl od</td>
<td>vládl do</td>
</tr>
	<xsl:apply-templates select="panovnik" mode="panovnik"/>
</table>
</xsl:template>

<xsl:template match="panovnik" mode="panovnik">
<tr>
<td><xsl:value-of select="jmeno"/></td>
<td><xsl:value-of select="panoval/od"/></td>
<td><xsl:value-of select="panoval/do"/></td>
</tr>
</xsl:template>

<xsl:template match="panovnici" mode="manzelka">
<br/>
<table border="border">
<tr>
<td>Panovník</td>
<td>Manželky</td>
</tr>
	<xsl:apply-templates select="panovnik" mode="manzelka"/>
</table>
</xsl:template>


<xsl:template match="panovnik" mode="manzelka">
<tr>
<td><xsl:value-of select="jmeno"/></td>
<td><xsl:apply-templates select="manzelky/manzelka" mode="manzelka"/></td>
</tr>
</xsl:template>

<xsl:template match="manzelka" mode="manzelka">
<xsl:value-of select="."/><br/>
</xsl:template>

</xsl:stylesheet> 
