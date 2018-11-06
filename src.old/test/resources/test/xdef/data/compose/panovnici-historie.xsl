<?xml version="1.0"  encoding="windows-1250" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html"/>
<xsl:template match="/">
    <HTML>
        <HEAD>
            <TITLE>Politické dějiny zemí koruny české</TITLE>
        </HEAD>
        <BODY>
               <h1>Politické dějiny zemí koruny české</h1>
            	<xsl:apply-templates/>
        </BODY>
    </HTML>
</xsl:template>

<xsl:template match="panovnici">
<table>
<xsl:apply-templates/>
</table>
</xsl:template>

<xsl:template match="panovnik">
<tr>
<td bgcolor="#FF0A0A">Období <xsl:value-of select="panoval/od"/>-<xsl:value-of select="panoval/do"/></td>
<td bgcolor="#FFFF08">
<xsl:text>Státní zřízení: </xsl:text>
<xsl:if test="panoval/od &lt; 1918">království</xsl:if>
<xsl:if test="panoval/od &gt; 1917">republika</xsl:if>
</td>
</tr>
<tr>
<td></td>
<td><xsl:call-template name="detail"/></td>
</tr>
</xsl:template>

<xsl:template name="detail">
<xsl:text>Představitel: </xsl:text><xsl:value-of select="jmeno"/><xsl:text>
 z rodu: </xsl:text><xsl:value-of select="@rod"/>
<br/>
<xsl:text>Titul: </xsl:text> <xsl:value-of select="panoval/@titul"/>
</xsl:template>

</xsl:stylesheet> 
