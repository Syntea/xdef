<?xml version="1.0"  encoding="windows-1250" ?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
<xsl:output method="text"/>
<xsl:strip-space elements="*"/>
<xsl:template match="panovnik">
INSERT  INTO panovnik 
VALUES ( '<xsl:value-of select="jmeno"/>', '<xsl:value-of select="panoval/od"/>', '<xsl:value-of select="panoval/do"/>');</xsl:template>
</xsl:stylesheet> 
