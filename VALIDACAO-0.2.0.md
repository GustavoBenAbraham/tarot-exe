# Validação de entrega — 20/10/2026

Refinamentos: exportação UTF-8, cópia da leitura, ajuste e persistência de fonte,
preferências da janela, tempo de geração da IA, logs rotativos, ícone próprio,
validação estrita dos catálogos e fallback testado de imagens inválidas/ausentes.
O aplicativo mantém o layout em colunas e os dados anteriores do histórico.

## Testes

- **53 testes automatizados passaram**, zero falhas, erros ou ignorados.
- **Teste real de IA passou**: qwen2.5:3b, resposta completa em 43 segundos.
- **Executável nativo passou**: abriu sem JAVA_HOME e sem Java no PATH; usou o jvm.dll incluído.
- **Instalador passou**: instalação por usuário, ícone na área de trabalho, Menu Iniciar e desinstalador.
- **Desinstalação/reinstalação passaram**: código 0, histórico preservado por comparação de SHA-256.
- **ZIP portátil passou**: extraído em uma pasta com espaço no caminho e aberto com seu próprio runtime.

Os testes de distribuição foram feitos nesta máquina Windows. Não equivalem
a validação em um computador limpo independente ou em todas as escalas de exibição.

## Arquivos de entrega

- `dist/Tarot-Setup-0.2.0.exe` — aproximadamente 50 MB.
- `dist/Tarot-Portable-0.2.0.zip` — aproximadamente 57 MB.
- `dist/SHA256.json` — checksums dos dois arquivos.

O instalador está sem assinatura digital. Ollama e o modelo são opcionais,
separados do instalador. O runtime Java, as 78 imagens e o catálogo já estão incluídos.

Instalação validada em `%LOCALAPPDATA%\Programs\Tarot`; atalho em
`Desktop\Tarot.exe.lnk`. O aplicativo pode ser aberto pelo atalho, sem terminal.

## Próximos testes de aceitação do usuário

Abra pelo atalho e confira sua pergunta, os textos em ambos os modos, tamanho
das cartas, histórico e exportação. A revisão editorial das respostas continua
importante: testes verificam funcionamento, não a qualidade simbólica das leituras.
