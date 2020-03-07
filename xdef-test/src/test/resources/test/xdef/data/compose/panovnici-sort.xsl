<?xml version="1.0"  encoding="windows-1250" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="html"/>
<xsl:template match="/">
    <HTML>
        <HEAD>
            <TITLE>
                Panovn�ci �esk�ch zem�
            </TITLE>
        </HEAD>
        <BODY>
           	<xsl:apply-templates/>
        </BODY>
    </HTML>
</xsl:template>

<xsl:template match="panovnici">
<table border="border">
<tr>
<td>Panovn�k</td>
<td>vl�dl od</td>
<td>vl�dl do</td>
<td>po dobu</td>
</tr>
	<xsl:for-each select="//panovnik">
	  <xsl:sort select="jmeno"/>
      <tr>
      <td><xsl:value-of select="jmeno"/></td>
      <td><xsl:value-of select="panoval/od"/></td>
      <td><xsl:value-of select="panoval/do"/></td>
      <xsl:if test="number(panoval/do) - number(panoval/od)" >
      <td><xsl:value-of select="number(panoval/do) - number(panoval/od)"/></td>
      </xsl:if>
      </tr>
	</xsl:for-each>
</table>
</xsl:template>

</xsl:stylesheet>
