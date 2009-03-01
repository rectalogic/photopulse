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

<!--
Generate HTML table fragment containing cells with skin thumbnail and link to each extra skin installer EXE.
-->

<xsl:stylesheet
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xalan="http://xml.apache.org/xalan"
	exclude-result-prefixes="xalan" version="1.0">

	<xsl:output indent="yes"/>

	<!-- Number of table columns -->
	<xsl:variable name="columns" select="4"/>

	<!-- Sort skin elements by title into result-tree-fragment, then use xalan:nodeset to convert that to a nodeset -->
	<xsl:variable name="sorted-skins-rtf">
		<xsl:for-each select="//skin">
			<xsl:sort select="@title"/>
			<xsl:copy-of select="."/>
		</xsl:for-each>
	</xsl:variable>
	<xsl:variable name="sorted-skins-nodeset" select="xalan:nodeset($sorted-skins-rtf)/*"/>

	<!--
		See http://blogs.msdn.com/kaevans/archive/2003/04/03/4754.aspx
		The key is that the position() function is relative to the current node set.
		The pointer to the node (the context) is defined in the for-each select statement,
		so the first node selected is that with position = 1.
		The first TR is added to the result tree, and the current node (".") and its following 3
		sibling nodes are selected and applied to the skin template.
		This means that nodes 1-4 are selected in the first loop iteration.
		The mod() operation then selects node 5, because it is the next one where mod 4 = 1.
		The loop selects the current node ("."), which is node 5, and its following 3 sibling nodes,
		selecting 5-8 and applying the template.
 	-->
	<xsl:template match="/">
		<TABLE BORDER="1" ALIGN="CENTER">
		<COL SPAN="{$columns}" WIDTH="150"/>
			<!-- Loop over sorted nodeset variable, selecting the start of each row -->
			<xsl:for-each select="$sorted-skins-nodeset[position() mod $columns = 1]">
				<TR ALIGN="CENTER">
					<!-- Select the current skin and the following N skins for this row.
 						This is selecting from sorted nodeset, position() is relative to current selected nodeset. -->
					<xsl:apply-templates select=". | following-sibling::skin[position() &lt; $columns]"/>
				</TR>
			</xsl:for-each>
		</TABLE>
	</xsl:template>

	<!-- Generate table cell with thumbnail and title linking to skin installer -->
	<xsl:template match="skin">
		<TD>
			<A HREF="{@name}-photopulse-theme.exe">
				<IMG TITLE="Download {@name}-photopulse-theme.exe" WIDTH="100" HEIGHT="100" BORDER="0" SRC="{@name}.png"/>
				<BR/>
				<xsl:value-of select="@title"/>
			</A>
		</TD>
	</xsl:template>

</xsl:stylesheet>