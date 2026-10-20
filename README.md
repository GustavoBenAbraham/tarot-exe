# Tarot.exe · Gabinete dos Arcanos

Aplicativo desktop Java/JavaFX com 78 cartas ilustradas, sorteio e leituras inteiramente locais. A integração com IA foi removida.

A prioridade atual é aprofundar a matemática do sorteio, depois trabalhar o visual e os textos das leituras.

## Sorteio

- Uma ou três cartas, sem repetição dentro da tiragem.
- Embaralhamento com SecureRandom; cada nova tiragem começa com as 78 cartas.
- Inversões habilitadas por padrão: 50% normal e 50% invertida, independentemente para cada carta.
- Desativar invertidas mantém todas as cartas normais.
- Pergunta, tema e histórico não interferem no sorteio.

A aba Probabilidades apresenta as chances e as contagens de resultados da tiragem exibida. As leituras continuam usando os catálogos e regras locais existentes.

## Desenvolvimento

Requer JDK 21. Execute `./mvnw.cmd clean test` para verificar e `./mvnw.cmd javafx:run` para abrir. Os testes da interface exigem uma sessão gráfica.

Imagens locais com ampliação, histórico, exportação UTF-8, copiar leitura e probabilidades, ajuste de fonte e preferências da janela continuam disponíveis. Históricos antigos permanecem legíveis; campos antigos de IA são ignorados e os arquivos originais são preservados ao abrir.

Consulte [as fórmulas](docs/PROBABILIDADES.md), [as prioridades](PENDENCIAS.md) e [a instalação](INSTALACAO.md). Os binários antigos em dist e releases anteriores precisam ser reconstruídos para incorporar as alterações.

Ilustrações: Pamela Colman Smith, Rider–Waite–Smith (1909). [Créditos](src/main/resources/cards/ATTRIBUTION.md). Leituras simbólicas para reflexão; as probabilidades descrevem o sorteio, sem medir chances de acontecimentos da vida.
