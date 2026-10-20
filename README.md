# Tarot.exe · Tarot.exe

Aplicativo desktop Java/JavaFX com 78 cartas ilustradas, sorteio e leituras inteiramente locais. A integração com IA foi removida.

A prioridade atual é aprofundar a matemática do sorteio, depois trabalhar o visual e os textos das leituras.

## Sorteio

- Uma ou três cartas, sem repetição dentro da tiragem.
- Embaralhamento com SecureRandom; cada nova tiragem começa com as 78 cartas.
- Inversões sempre habilitadas: 50% normal e 50% invertida, independentemente para cada carta.
- Pergunta, tema e histórico não interferem no sorteio.

Os estudos de probabilidade permanecem no codigo e na documentacao. As leituras continuam usando os catálogos e regras locais existentes.

## Desenvolvimento

Requer JDK 21. Execute `./mvnw.cmd clean test` para verificar e `./mvnw.cmd javafx:run` para abrir. Os testes da interface exigem uma sessão gráfica.

Imagens locais com ampliação, histórico, exportação UTF-8, copiar leitura e probabilidades, ajuste de fonte e preferências da janela continuam disponíveis. Históricos antigos permanecem legíveis; campos antigos de IA são ignorados e os arquivos originais são preservados ao abrir.

Consulte [as fórmulas](docs/PROBABILIDADES.md), [as prioridades](PENDENCIAS.md) e [a instalação](INSTALACAO.md). Os binários antigos em dist e releases anteriores precisam ser reconstruídos para incorporar as alterações.

Ilustrações: Pamela Colman Smith, Rider–Waite–Smith (1909). [Créditos](src/main/resources/cards/ATTRIBUTION.md).

## Referência de estudo atual

As interpretações foram revisadas com base no PDF de *78 Graus de Sabedoria*, de Rachel Pollack, fornecido pelo usuário. O [mapa de referência](docs/REFERENCIA-LEITURAS.md) documenta páginas, critérios de adaptação e limites. As respostas continuam sendo compostas por textos e regras locais.

Todas as novas tiragens usam leitura geral, sem seletor de tema ou controle para desativar invertidas.

## Animação do baralho

Ao sortear, um monte de cartas com verso azul pontilhado é embaralhado, distribuído e revelado uma carta por vez. A animação usa desenho e movimentos locais no estilo de utilitários do fim dos anos 90. A sequência dura cerca de 2,6 segundos para uma carta e 4,5 segundos para três. Mostrar cartas pula a sequência; Nova tiragem cancela. O resultado é sorteado uma única vez antes da apresentação e não muda ao pular a animação.
