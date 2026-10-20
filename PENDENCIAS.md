# Pendências e validação — versão 0.2.0

Atualizado em 20/10/2026. Projeto atual: Desktop/Tarot-exe-MVP/Tarot-exe-MVP.

## Concluído e validado

- [x] Baralho de 78 cartas, sorteio de uma/três e orientações sem repetição.
- [x] Textos normais/invertidos e práticas para as 78 cartas; quatro temas locais.
- [x] Relações entre posições, naipes recorrentes e síntese por regras.
- [x] 78 imagens locais, cache, fallback e ampliação por clique ou Enter.
- [x] Rejeitar catálogo incompleto, campos extras, vazios e chaves duplicadas.
- [x] Layout vintage Win95/98 em colunas redimensionáveis, com cartas legíveis.
- [x] Minimizar, maximizar, redimensionar, F11 e saída da tela cheia por Esc.
- [x] Copiar leitura, exportar UTF-8 e ajustar fonte com A−/A+.
- [x] Lembrar tamanho da janela, estado maximizado, fonte e divisão das colunas.
- [x] Histórico local: salvar, listar, reabrir, excluir e preservar textos originais.
- [x] Recuperar arquivos válidos quando houver uma entrada de histórico corrompida.
- [x] Ollama opcional em segundo plano, parâmetros persistidos, timeout e cancelamento.
- [x] Impedir respostas antigas de alterar uma nova tiragem; manter leitura local em falhas.
- [x] Mostrar tempo decorrido da IA e registrar falhas com logs de tamanho limitado.
- [x] Ícone próprio no aplicativo, executável e atalhos.
- [x] Build reproduzível, instalador Windows em português e ZIP portátil com Java incluído.
- [x] Atalhos na área de trabalho e no Menu Iniciar; desinstalador do Windows.

## Evidências desta versão

- Suíte automática: 53 testes, zero falhas/erros/ignorados; repetida durante o build de distribuição.
- Teste real com Ollama: cliente Java completou resposta em 43 segundos com qwen2.5:3b.
- Capturas conferidas em 1360×900, 960×800 e 720×600, com rolagem em janelas pequenas.
- Aplicativo nativo, instalado e extraído do ZIP abriram e responderam sem JAVA_HOME e sem Java no PATH.
- O módulo jvm.dll carregado veio da pasta runtime incluída no produto.
- Instalação, desinstalação e reinstalação concluíram com código 0.
- Histórico existente teve nomes e SHA-256 comparados antes/depois da desinstalação: preservado.
- Atalhos da área de trabalho e Menu Iniciar e desinstalador foram verificados.
- Logs gerados durante a abertura nativa não registraram erros.

Relatórios gerados: target/build-windows.log, native-smoke.json, install-smoke.json,
reinstall-smoke.json e ollama-app-test.md. Artefatos: dist. Guia: INSTALACAO.md.

## Pendências para distribuição pública e evolução

- [ ] Validar em outro computador Windows e em diferentes escalas de exibição.
- [ ] Obter certificado e assinar digitalmente instalador/executável.
- [ ] Ampliar conteúdo específico por carta e tema a partir da revisão editorial do usuário.
- [ ] Avaliar qualidade da IA em mais perguntas; um teste não garante consistência geral.
- [ ] Definir processo de atualização/versionamento para próximas versões.

Ollama e modelos são opcionais e não são incluídos no instalador. O modo por
catálogo funciona sem rede, sem Ollama e sem Java previamente instalado.
