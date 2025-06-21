; Script generated for ETVaultGuard setup with bundled JRE and optional install

[Setup]
AppName=ETVaultGuard
AppVersion=3.5
DefaultDirName={pf}\ETVaultGuard
DisableDirPage=no
DefaultGroupName=ETVaultGuard
UninstallDisplayIcon={app}\ETVaultGuard.exe
OutputDir=.
OutputBaseFilename=ETVaultGuard_Setup
Compression=lzma
SolidCompression=yes
ArchitecturesInstallIn64BitMode=x64
PrivilegesRequired=admin
AllowNoIcons=yes
CreateAppDir=yes

[Files]
; The actual app executable
Source: "ETVaultGuard.exe"; DestDir: "{app}"; Flags: ignoreversion

; JRE folder
Source: "jre\*"; DestDir: "{app}\jre"; Flags: recursesubdirs createallsubdirs ignoreversion

[Icons]
; Start menu shortcut
Name: "{group}\ETVaultGuard"; Filename: "{app}\ETVaultGuard.exe"

; Desktop shortcut
Name: "{commondesktop}\ETVaultGuard"; Filename: "{app}\ETVaultGuard.exe"; Tasks: desktopicon

[Tasks]
Name: "desktopicon"; Description: "Create a &desktop shortcut"; GroupDescription: "Additional icons:"

[Run]
; Optionally run after install
Filename: "{app}\ETVaultGuard.exe"; Description: "Launch ETVaultGuard"; Flags: nowait postinstall skipifsilent
