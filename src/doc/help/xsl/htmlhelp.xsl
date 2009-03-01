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

<xsl:import href="docbook/htmlhelp/profile-htmlhelp.xsl"/>

<xsl:include href="common.xsl"/>

<!-- If 1, puts first section on separate page from Chapter page -->
<xsl:param name="chunk.first.sections" select="'1'"/>

<!-- At what depth do we stop chunking sections and include with parent?
	Sections need explicit IDs if this is set low. -->
<xsl:param name="chunk.section.depth" select="'10'"/>

<!-- To what depth (in sections) should the TOC go? -->
<xsl:param name="toc.section.depth" select="'5'"/>
<!-- Generate a partial TOC in each section -->
<xsl:param name="generate.section.toc.level" select="'3'"/>

<!-- Set initial size of window, 700x500 [top,left,bottom,right]
	When changing, delete cached previous settings in:
	C:\Documents and Settings\(user)\Application Data\Microsoft\HTML Help\hh.dat
 -->
<xsl:param name="htmlhelp.window.geometry" select="'[17,22,717,522]'"/>
<!-- Remember users window pos between restarts -->
<xsl:param name="htmlhelp.remember.window.position" select="'1'"/>

<xsl:param name="generate.index" select="'1'"/>

<!-- Use HHK file for index -->
<xsl:param name="htmlhelp.use.hhk" select="'1'"/>

<!-- Add Home button -->
<xsl:param name="htmlhelp.button.home" select="'1'"/>
<!-- Add Locate button - usually disabled -->
<!--<xsl:param name="htmlhelp.button.locate" select="'1'"/>-->

<!-- When chunking, use id attribute as filename? 0 or 1 -->
<xsl:param name="use.id.as.filename" select="'1'"/>

<!-- Expand TOC -->
<xsl:param name="htmlhelp.hhc.show.root" select="'0'"/>

<!-- Launch links in new window -->
<xsl:param name="ulink.target" select="'_blank'"/>

<!-- Include help specific stuff, exclude website specific stuff -->
<xsl:param name="profile.condition" select="'help;version'"/>

</xsl:stylesheet>
