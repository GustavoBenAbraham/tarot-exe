# Tarot.exe 0.2.0 para Windows

Abra `dist/Tarot-Setup-0.2.0.exe` e siga o assistente em português.
A opção de criar um atalho na área de trabalho vem selecionada.
A instalação é por usuário, em `%LOCALAPPDATA%\Programs\Tarot`, sem exigir
Java, Maven ou Eclipse. Também há entrada no Menu Iniciar e desinstalador
em Configurações > Aplicativos do Windows.

O instalador não tem assinatura digital de editor. Assiná-lo é uma etapa
de publicação ainda pendente para distribuição pública.

## Versão portátil

Extraia todo o conteúdo de `dist/Tarot-Portable-0.2.0.zip` e abra `Tarot.exe`.
As pastas `app` e `runtime` precisam permanecer ao lado do executável.
Não execute de dentro do ZIP.

## Dados e atualizações

- Histórico, parâmetros, fonte e preferências ficam em `%USERPROFILE%\.tarot-exe`.
- Desinstalar ou reinstalar preserva essa pasta.
- Faça backup dessa pasta com o aplicativo fechado.
- Logs de diagnóstico ficam em `.tarot-exe/logs`, com rotação para limitar o tamanho.
- Atualizações devem manter o AppId do instalador e a compatibilidade dos dados.

Leituras do catálogo e imagens estão incluídas e funcionam offline.
O aplicativo atual funciona inteiramente de forma local, sem modelos externos.
Binários antigos em dist precisam ser reconstruídos.

## Construir outra versão

Na máquina de desenvolvimento: Windows x64, JDK 21, Maven Wrapper do projeto
e compilador Inno Setup. O ícone é gerado pelo código de `packaging/create-icon.ps1`.

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\packaging\build-windows.ps1 -IsccPath "C:\caminho\para\ISCC.exe"
```

O script executa os testes e o build Maven, cria o aplicativo com jpackage,
gera instalador e ZIP em `dist` e grava seus SHA-256 em `SHA256.json`.
O runtime é gerado a partir do JDK de JAVA_HOME. `.build-tools` contém apenas
ferramentas de desenvolvimento e não deve ser distribuída.
`-SkipTests` permite reempacotamento depois de uma validação já feita.
