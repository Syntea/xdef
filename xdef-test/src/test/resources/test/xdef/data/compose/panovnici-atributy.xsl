<?xml version="1.0"  encoding="windows-1250" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="xml"/>
<xsl:template match="/">
<panovnici-ceskeho-statu>
<xsl:apply-templates select="panovnici/panovnik"/>
</panovnici-ceskeho-statu>
</xsl:template>


<xsl:template match="panovnik">
<panovnik jmeno="{jmeno}" od="{panoval/od}" do="{panoval/do}">
</panovnik>
</xsl:template>


</xsl:stylesheet> 
