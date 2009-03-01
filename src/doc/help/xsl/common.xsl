<?xml version="1.0"?>

<!-- ***** BEGIN LICENSE BLOCK *****
   - Version: MPL 1.1
   -
   - The contents of this file are subject to the Mozilla Public License Version
   - 1.1 (the "License"); you may not use this file except in compliance with
   - the License. You may obtain a copy of the License at
   - http://www.mozilla.org/MPL/
   -
   - Software distributed under the License is distributed on an "AS IS" basis,
   - WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
   - for the specific language governing rights and limitations under the
   - License.
   -
   - The Original Code is Photica Photopulse.
   -
   - The Initial Developer of the Original Code is
   - Photica Inc.
   - Portions created by the Initial Developer are Copyright (C) 2003
   - the Initial Developer. All Rights Reserved.
   -
   - Contributor(s):
   - Andrew Wason, Mike Mills
   - info@photica.com
   -
   - ***** END LICENSE BLOCK ***** -->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

<!-- Use css stylesheet for html -->
<xsl:param name="html.stylesheet" select="'help.css'"/>
<!-- Use bullet list for TOC -->
<xsl:param name="toc.list.type" select="'ul'"/>

<!-- Set profile.userlevel so content can be conditionally excluded -->
<xsl:param name="profile.userlevel">
	<xsl:choose>
		<xsl:when test="'${branding.enableExpertMode}' = 'true'"><xsl:text>expert</xsl:text></xsl:when>
		<xsl:otherwise><xsl:text>noexpert</xsl:text></xsl:otherwise>
	</xsl:choose>
</xsl:param>

<!-- Set profile.vendor in support of vendor specific content -->
<xsl:param name="profile.vendor">
	<xsl:choose>

		<!-- an xsl:when should be constructed for each vendor where help content is vendor dependent. -->
		<xsl:when test="'${branding}' = 'XXX'"><xsl:text>XXX</xsl:text></xsl:when>

		<!-- If there was no vendor dependent data then we default to photica. -->
		<xsl:otherwise>
			<xsl:text>photica</xsl:text>
		</xsl:otherwise>

	</xsl:choose>
</xsl:param>

<!-- Replace element with Ant version property. -->
<xsl:template match="phrase[@condition='version']">
	<xsl:text>${build.version}</xsl:text>
</xsl:template>

<!-- Replace application element with PhotoPulse branding product name. -->
<xsl:template match="application[contains(text(), 'PhotoPulse')]">
	<xsl:text>${branding.productName}</xsl:text>
</xsl:template>

<xsl:template name="user.head.content">
	<xsl:param name="node" select="."/>
</xsl:template>

<!-- Customization layer to replace default xref generated text
	http://www.sagehill.net/docbookxsl/CustomGentext.html#CustomGenText -->
<xsl:param name="local.l10n.xml" select="document('')"/> 
<l:i18n xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0"> 
	<l:l10n language="en"> 
		<l:context name="xref"> 
			<l:template name="bridgehead" text="%t"/>
			<l:template name="refsection" text="%t"/>
			<l:template name="refsect1" text="%t"/>
			<l:template name="refsect2" text="%t"/>
			<l:template name="refsect3" text="%t"/>
			<l:template name="sect1" text="%t"/>
			<l:template name="sect2" text="%t"/>
			<l:template name="sect3" text="%t"/>
			<l:template name="sect4" text="%t"/>
			<l:template name="sect5" text="%t"/>
			<l:template name="section" text="%t"/>
			<l:template name="simplesect" text="%t"/>
		</l:context>    
	</l:l10n>
</l:i18n>


</xsl:stylesheet>
