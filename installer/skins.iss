; ***** BEGIN LICENSE BLOCK *****
; Version: MPL 1.1
;
; The contents of this file are subject to the Mozilla Public License Version
; 1.1 (the "License"); you may not use this file except in compliance with
; the License. You may obtain a copy of the License at
; http://www.mozilla.org/MPL/
;
; Software distributed under the License is distributed on an "AS IS" basis,
; WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
; for the specific language governing rights and limitations under the
; License.
;
; The Original Code is Photica Photopulse.
;
; The Initial Developer of the Original Code is
; Photica Inc.
; Portions created by the Initial Developer are Copyright (C) 2009
; the Initial Developer. All Rights Reserved.
;
; Contributor(s):
; Andrew Wason, Mike Mills
; info@photica.com
;
; ***** END LICENSE BLOCK *****

; PhotoPulse Skin Installer for Inno Setup
; http://www.jrsoftware.org/isinfo.htm


[Setup]
SourceDir=${iss.skin.sourcepath}
; Relative to SourceDir
OutputDir=${iss.skin.outputpath}
OutputBaseFilename=@SKIN@-${branding.installer.basename}-theme
AppName=${branding.productName} @SKIN@ Theme
AppVerName=${branding.productName} @SKIN@ Theme ${build.version}
; This shouldn't be used since previous install directory will be used
DefaultDirName={pf}\${branding.productName}\skins
; Set AppId to match AppName (which is AppId) of main installer, so we are an update installer
AppId=${branding.productName}
UpdateUninstallLogAppName=no
Uninstallable=no
CreateUninstallRegKey=no
AppCopyright=Copyright (C) 2005 Photica Inc.
DisableReadyPage=yes
DisableDirPage=yes
DisableProgramGroupPage=yes
LicenseFile=${iss.skin.licensefile}
Compression=zip/9
SolidCompression=yes

[Files]
Source: "@SKIN@\*"; DestDir: "{app}\skins\@SKIN@"; Flags: recursesubdirs

[Code]
function InitializeSetup(): Boolean;
begin
	if not RegValueExists(HKLM, 'Software\Photica\PhotoPulse', 'Home') then
	begin
		MsgBox('${branding.productName} does not appear to be installed.' #13 'Please install the ${branding.productName} application before attempting to install the Theme.', mbError, MB_OK);
		Result := false;
	end
	else
	begin
		Result := true;
	end;
end;
