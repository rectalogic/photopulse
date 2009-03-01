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

<xsl:import href="docbook/html/profile-docbook.xsl"/>

<xsl:include href="common.xsl"/>

<!-- To what depth (in sections) should the TOC go? -->
<xsl:param name="toc.section.depth" select="'4'"/>

<xsl:param name="generate.index" select="'1'"/>

<!-- Include website specific stuff, exclude help specific stuff -->
<xsl:param name="profile.condition" select="'website'"/>

</xsl:stylesheet>
