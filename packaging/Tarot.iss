#ifndef AppVersion
  #define AppVersion "0.2.0"
#endif
#ifndef ImageDir
  #error ImageDir must point to the jpackage app-image
#endif

[Setup]
AppId={{5DB2DA46-3B15-42E3-BA24-B35CE1819427}
AppName=Tarot.exe
AppVersion={#AppVersion}
AppPublisher=Gabinete dos Arcanos
AppComments=Leituras simbolicas de tarot com interpretacao local e Ollama opcional.
DefaultDirName={localappdata}\Programs\Tarot
DefaultGroupName=Tarot.exe
DisableProgramGroupPage=yes
PrivilegesRequired=lowest
ArchitecturesAllowed=x64compatible
ArchitecturesInstallIn64BitMode=x64compatible
MinVersion=10.0
OutputDir={#OutputDir}
OutputBaseFilename=Tarot-Setup-{#AppVersion}
SetupIconFile={#IconFile}
UninstallDisplayIcon={app}\Tarot.exe
WizardStyle=classic
Compression=lzma2/normal
SolidCompression=yes
CloseApplications=yes
RestartApplications=no
VersionInfoVersion={#AppVersion}

[Languages]
Name: "brazilianportuguese"; MessagesFile: "compiler:Languages\BrazilianPortuguese.isl"

[Tasks]
Name: "desktopicon"; Description: "Criar atalho na area de trabalho"; GroupDescription: "Atalhos:"; Flags: checkedonce

[Files]
Source: "{#ImageDir}\*"; DestDir: "{app}"; Flags: ignoreversion recursesubdirs createallsubdirs

[Icons]
Name: "{group}\Tarot.exe"; Filename: "{app}\Tarot.exe"
Name: "{autodesktop}\Tarot.exe"; Filename: "{app}\Tarot.exe"; Tasks: desktopicon

[Run]
Filename: "{app}\Tarot.exe"; Description: "Abrir Tarot.exe"; Flags: nowait postinstall skipifsilent
