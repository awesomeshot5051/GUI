; Inno Setup Script for ETVaultGuard Installer

[Setup]
AppName=ETVaultGuard
AppVersion=4.0
DefaultDirName={pf64}\EnglandTechnologies\ETVaultGuard
DefaultGroupName=England Technologies
OutputBaseFilename=ETVaultGuardSetup
Compression=lzma
SolidCompression=yes
PrivilegesRequired=admin

[Files]
; Your EXE file to be installed
Source: "ETVaultGuard.exe"; DestDir: "{app}"; Flags: ignoreversion

[Icons]
; Create a Start Menu shortcut
Name: "{group}\ETVaultGuard"; Filename: "{app}\ETVaultGuard.exe"

[Run]
; Optional: run the app after installation
Filename: "{app}\ETVaultGuard.exe"; Description: "Launch ETVaultGuard"; Flags: nowait postinstall skipifsilent
