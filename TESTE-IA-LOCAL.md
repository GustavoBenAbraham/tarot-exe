# Primeiro teste com Ollama — 19/10/2026

O teste inicial abaixo foi feito separadamente por `scripts/test-ollama.ps1`.
Agora o aplicativo também oferece **Aprofundar com IA**, executado em segundo plano,
com configuração de parâmetros, cancelamento e preservação da leitura local.

## Modelo utilizável no teste

- Modelo: `qwen2.5:3b`, download de aproximadamente 1,9 GB.
- Máquina: 16 GB de RAM, Intel UHD Graphics 620; inferência por CPU.
- Ollama: 0.40.4.
- Contexto: 2048 tokens; limite de saída: 420; temperatura: 0,3.
- Tempo total observado: 45,8 segundos; geração: 8,3 tokens/s.
- Finalização: `stop`, resposta completa.
- Uso observado pelo Ollama: aproximadamente 2,1 GB para o modelo carregado.

Entrada fixa: pergunta sobre organizar um projeto pessoal, O Mago normal na
situação, Dez de Paus invertido no desafio e Oito de Ouros normal no conselho.
A resposta permaneceu em português e usou as três cartas nas posições fornecidas.
O texto foi simples, com repetição dos significados e uma sugestão de delegação
e prática. Ainda precisa de ajustes de redação e testes com outras perguntas;
um exemplo não comprova qualidade geral. Não é treinamento do modelo.

O Qwen3 4B também foi baixado; as tentativas com e sem template derivado
atingiram o limite de saída com explicações internas e texto incompleto.
Não foi escolhido como padrão do script. Os modelos baixados foram preservados.

## Testar novamente

```powershell
ollama run qwen2.5:3b
```

Para repetir o teste fixo com métricas, na pasta do projeto:

```powershell
powershell.exe -NoProfile -ExecutionPolicy Bypass -File .\scripts\test-ollama.ps1
```

Resultado e resposta bruta: `target/ollama-test/resultado.md` e `response.json`.
A pasta target é gerada e pode ser removida por `mvnw clean`.
As requisições do teste vão exclusivamente a `127.0.0.1:11434`.

## Validação da integração Java

Os testes automatizados usam um servidor HTTP local simulado para verificar
UTF-8, parâmetros, orientação das cartas, modelo ausente, resposta inválida,
truncamento, cancelamento e timeout. Os testes da interface verificam execução
assíncrona, histórico com IA e descarte de respostas de tiragens antigas.

Para testar o cliente Java contra o Ollama real, execute explicitamente:

```powershell
.\mvnw.cmd '-Dtest=OllamaLiveCheck' test
```

O teste real usa os parâmetros padrão do aplicativo e salva a resposta em
`target/ollama-app-test.md`. Ele fica fora da suíte automática normal para não
exigir um modelo instalado em outras máquinas.

Resultado medido com o cliente Java nesta máquina: resposta completa em
52 segundos usando `qwen2.5:3b`. O teste real passou, além dos 48 testes
automatizados da suíte normal. Isso mede uma tiragem fixa; outras perguntas
podem ter tempos e qualidade diferentes.
