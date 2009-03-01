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


; PhotoPulse Installer for Inno Setup
; http://www.jrsoftware.org/isinfo.htm


[Setup]
SourceDir=${dist.home.path}
; Relative to SourceDir
OutputDir=${build.home.path}
OutputBaseFilename=${installer.basename}
AppName=${branding.productName}
AppVerName=${branding.installer.title}
AppCopyright=Copyright (C) 2005 Photica Inc.
AppPublisher=Photica Inc.
AppPublisherURL=http://www.photica.com/
AppMutex=PhotoPulseMutex{E0893C13-1962-45fb-8E71-3CEEFC4F6CA4}
DefaultDirName={pf}\${branding.productName}
; Set to size difference between *.pack and *.jar and classes.jsa files
ExtraDiskSpaceRequired=39426994

DefaultGroupName=${branding.productName}

UninstallDisplayIcon={app}\photopulse.exe
LicenseFile=licenses\photopulse.txt
Compression=lzma
SolidCompression=yes
${branding.installer.ppp.exclude}ChangesAssociations=yes

[Files]
Source: "*"; DestDir: "{app}"; Flags: ignoreversion
Source: "lib\*"; DestDir: "{app}\lib"; Flags: recursesubdirs
Source: "licenses\*"; DestDir: "{app}\licenses"; Flags: recursesubdirs
Source: "skins\*"; DestDir: "{app}\skins"; Flags: recursesubdirs
Source: "video\*"; DestDir: "{app}\video"
Source: "ftp\*"; DestDir: "{app}\ftp"
Source: "..\installer\jre\FontChecker.jar"; DestDir: "{tmp}"; Flags: ignoreversion 
Source: "jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs sortfilesbyextension

[InstallDelete]
; Delete so it can be regenerated when reinstalling
Type: files; Name: "{app}\jre\bin\client\classes.jsa"

[Registry]
; Set up to uninstall preferences from registry
Root: HKCU; Subkey: "Software\JavaSoft"; Flags: uninsdeletekeyifempty
Root: HKCU; Subkey: "Software\JavaSoft\Prefs"; Flags: uninsdeletekeyifempty
Root: HKCU; Subkey: "Software\JavaSoft\Prefs\com"; Flags: uninsdeletekeyifempty
Root: HKCU; Subkey: "Software\JavaSoft\Prefs\com\photica"; Flags: uninsdeletekeyifempty
Root: HKCU; Subkey: "Software\JavaSoft\Prefs\com\photica\photopulse"; Flags: uninsdeletekey
Root: HKLM; Subkey: "Software\JavaSoft"; Flags: uninsdeletekeyifempty
Root: HKLM; Subkey: "Software\JavaSoft\Prefs"; Flags: uninsdeletekeyifempty
Root: HKLM; Subkey: "Software\JavaSoft\Prefs\com"; Flags: uninsdeletekeyifempty
Root: HKLM; Subkey: "Software\JavaSoft\Prefs\com\photica"; Flags: uninsdeletekeyifempty
; Set permissions so XP limited users can store license key pref here
Root: HKLM; Subkey: "Software\JavaSoft\Prefs\com\photica\photopulse"; Flags: uninsdeletekey; Permissions: authusers-modify

; Configure JVM maxmem for launcher
Root: HKLM; Subkey: "Software\JavaSoft\Prefs\com\photica\photopulse"; ValueType: string; ValueName: "maxmem"; ValueData: "{code:GetMaxMemory|128M}"

; Put path to app in registry for add-on skin installers etc.
Root: HKLM; Subkey: "Software\Photica"; Flags: uninsdeletekeyifempty
Root: HKLM; Subkey: "Software\Photica\PhotoPulse"; Flags: uninsdeletekey
Root: HKLM; Subkey: "Software\Photica\PhotoPulse"; ValueType: string; ValueName: "Home"; ValueData: "{app}"

; Associate .ppp files with PhotoPulse
${branding.installer.ppp.exclude}Root: HKCR; Subkey: ".ppp"; ValueType: string; ValueName: ""; ValueData: "PhotoPulse.Project"; Flags: uninsdeletevalue uninsdeletekeyifempty; Tasks: associate
${branding.installer.ppp.exclude}Root: HKCR; Subkey: "PhotoPulse.Project"; ValueType: string; ValueName: ""; ValueData: "PhotoPulse Project"; Flags: uninsdeletekey; Tasks: associate
${branding.installer.ppp.exclude}Root: HKCR; Subkey: "PhotoPulse.Project\DefaultIcon"; ValueType: string; ValueName: ""; ValueData: "{app}\photopulse.exe,0"; Tasks: associate
${branding.installer.ppp.exclude}Root: HKCR; Subkey: "PhotoPulse.Project\shell\open\command"; ValueType: string; ValueName: ""; ValueData: """{app}\photopulse.exe"" -project ""%1"""; Tasks: associate

[Tasks]
Name: desktopicon; Description: "Create a &desktop icon";
${branding.installer.ppp.exclude}Name: associate; Description: "&Associate .ppp project files with ${branding.productName}";

[Icons]
Name: "{group}\${branding.productName}"; Filename: "{app}\photopulse.exe"
Name: "{group}\${branding.productName} Help"; Filename: "{app}\PhotoPulse.chm"
Name: "{group}\${branding.productName} Online"; Filename: "{app}\PhotoPulse.url"
Name: "{commondesktop}\${branding.productName}"; Filename: "{app}\photopulse.exe"; Tasks: desktopicon

[UninstallDelete]
; Delete thumbnail cache - keep in sync with Java code.
; This should be the same as Java user.home property - but may not always be.
Type: files; Name: "{userdesktop}\..\PhotoPulseCache\*.jpg"
Type: dirifempty; Name: "{userdesktop}\..\PhotoPulseCache"
; Delete the jre - installation creates various files inside it that prevent full uninstall
Type: filesandordirs; Name: "{app}\jre\*"
; Delete unpacked jars
Type: files; Name: "{app}\lib\*.jar"

[Run]
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\jre\lib\ext\sunpkcs11.pack"" ""{app}\jre\lib\ext\sunpkcs11.jar"""; StatusMsg: "Unpacking sunpkcs11.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\jre\lib\im\indicim.pack"" ""{app}\jre\lib\im\indicim.jar"""; StatusMsg: "Unpacking indicim.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\jre\lib\im\thaiim.pack"" ""{app}\jre\lib\im\thaiim.jar"""; StatusMsg: "Unpacking thaiim.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\jre\lib\jce.pack"" ""{app}\jre\lib\jce.jar"""; StatusMsg: "Unpacking jce.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\jre\lib\jsse.pack"" ""{app}\jre\lib\jsse.jar"""; StatusMsg: "Unpacking jsse.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\jre\lib\rt.pack"" ""{app}\jre\lib\rt.jar"""; StatusMsg: "Unpacking rt.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\lib\clibwrapper_jiio.pack"" ""{app}\lib\clibwrapper_jiio.jar"""; StatusMsg: "Unpacking clibwrapper_jiio.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\lib\concurrent.pack"" ""{app}\lib\concurrent.jar"""; StatusMsg: "Unpacking concurrent.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\lib\jai_imageio.pack"" ""{app}\lib\jai_imageio.jar"""; StatusMsg: "Unpacking jai_imageio.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\lib\jaxen-core.pack"" ""{app}\lib\jaxen-core.jar"""; StatusMsg: "Unpacking jaxen-core.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\lib\jaxen-dom.pack"" ""{app}\lib\jaxen-dom.jar"""; StatusMsg: "Unpacking jaxen-dom.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\lib\jgen.pack"" ""{app}\lib\jgen.jar"""; StatusMsg: "Unpacking jgen.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\lib\log4j.pack"" ""{app}\lib\log4j.jar"""; StatusMsg: "Unpacking log4j.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\lib\mlibwrapper_jai.pack"" ""{app}\lib\mlibwrapper_jai.jar"""; StatusMsg: "Unpacking mlibwrapper_jai.jar..."; Flags: runhidden
Filename: "{app}\jre\bin\unpack200.exe"; Parameters: "--remove-pack-file --quiet ""{app}\lib\saxpath.pack"" ""{app}\lib\saxpath.jar"""; StatusMsg: "Unpacking saxpath.jar..."; Flags: runhidden

; Build classes.jsa. Do not run -Xshare:dump on Win95/98/ME
Filename: "{app}\jre\bin\javaw.exe"; Parameters: "-Xshare:dump"; StatusMsg: "Creating runtime shared data..."; MinVersion: 0,4
; Check for bad fonts, build jre\lib\fonts\badfonts.txt
Filename: "{app}\jre\bin\javaw.exe"; Parameters: "-jar ""{tmp}\FontChecker.jar"""; StatusMsg: "Configuring fonts..."

; Offer to install Windows Media Format redist if the installer exists alongside our installer and user is not up to date
Filename: "{src}\wmf\wmfdist.exe"; Description: "Install Microsoft Windows Media 9"; Flags: postinstall skipifdoesntexist; Check: InstallWMF(ExpandConstant('{src}\wmf\wmfdist.exe'))

[Code]
function InstallWMF(InstallerFileName: String): Boolean;
var
  VerMS, VerLS: Cardinal;
begin 
  Result := False;
  // If the installer exists, then check if we need it
  if FileExists(InstallerFileName) then begin
    // Check if wmvcore.dll exists and what version it is.
    // If not there or <9, we need to run the installer.
    if GetVersionNumbers(ExpandConstant('{sys}\wmvcore.dll'), VerMS, VerLS) then begin
      if VerMS < $00090000 then
        Result := True;
    end else
      Result := True;
  end;
end; 

type
  TMemoryStatus = record
    dwLength : Longint;
    dwMemoryLoad : Longint;
    dwTotalPhys : Longint;
    dwAvailPhys : Longint;
    dwTotalPageFile : Longint;
    dwAvailPageFile : Longint;
    dwTotalVirtual : Longint;
    dwAvailVirtual : Longint;
  end;

procedure GlobalMemoryStatus(var lpBuffer: TMemoryStatus); external 'GlobalMemoryStatus@kernel32.dll';

function GetMaxMemory(Default: String): String;
var
  MemStatus: TMemoryStatus;
  MemSize: Longint;
begin
  GlobalMemoryStatus(MemStatus);

  // Compute 2/3 of physical memory in Mb
  MemSize := (((2 * MemStatus.dwTotalPhys) / 3) / (1024 * 1024));

  // Don't use less than 128M
  if (MemSize <= 128) then
    Result := Default
  else
    Result := IntToStr(MemSize) + 'M'
end;