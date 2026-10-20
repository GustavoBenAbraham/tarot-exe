# Contribuindo

Este é um projeto de estudo em evolução. Sugestões, relatos de problemas e
revisões dos textos são bem-vindos nas Issues.

Para um problema, informe a versão, o Windows utilizado, os passos para
reproduzir e o resultado esperado. Evite publicar perguntas pessoais, histórico
completo ou configurações privadas. Use uma pergunta de demonstração.

## Desenvolvimento

1. Configure um JDK 21 em JAVA_HOME.
2. Use o Maven Wrapper incluído.
3. Execute `mvnw.cmd clean test` em uma sessão gráfica Windows.
4. Para alterações sem interface, use a seleção de testes do workflow de CI.
5. Consulte [CONTEUDO.md](CONTEUDO.md) antes de modificar os catálogos.

Mantenha o modo por regras independente do Ollama, a interface responsiva e
o histórico compatível com leituras antigas. Não inclua modelos de IA, tokens,
arquivos pessoais ou ferramentas de build no código-fonte.

## Capturas da documentação

```powershell
.\mvnw.cmd '-Dtest=DocumentationScreenshots' test
```

Esse teste explícito usa um histórico temporário com uma pergunta de
demonstração e a resposta de exemplo registrada em `docs/examples`.
Captura a interface real em `docs/screenshots`, sem ler o histórico pessoal
e sem chamar o Ollama. Não faz parte da suíte automática comum.
