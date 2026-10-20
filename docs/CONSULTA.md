# Estrutura da consulta

## Primeira etapa implementada

1. Apresentação opcional: nome ou apelido, data, horário, local de nascimento e signo identificado automaticamente pela data.
2. Preparação: acolhimento pelo nome e convite para formular a pergunta.
3. Tiragem: sorteio local, animação e revelação, sempre com possibilidade de inversões.
4. Leitura: catálogo das cartas, contexto opcional do signo e conselho.
5. Registro: salvar explicitamente no histórico local ou exportar o texto.

A apresentação é aberta pelo botão Sua apresentação (opcional). Continuar sem dados limpa o perfil da sessão. Nova tiragem mantém o perfil para outra pergunta; ele pode ser alterado na apresentação. Cancelar o diálogo preserva o perfil anterior.

## Dados e responsabilidade de cada componente

- ConsultantProfile: contexto imutável da pessoa; campos opcionais e limites de tamanho.
- ZodiacSign: perguntas editoriais para os 12 signos, identificados por intervalos convencionais de dia e mês; não calcula posições astronômicas.
- Reading: fotografia da consulta, incluindo o perfil daquele momento.
- ConsultationDialog: validação de data, horário e campos de apresentação.
- TarotService: sorteio recebe o perfil apenas para anexar ao resultado. Nome, data, horário, cidade e signo não são usados como sementes ou pesos.
- LocalInterpretationService: nome e contexto do signo no texto, sem alterar identidades ou orientações.
- HistoryStore: perfil gravado junto da consulta somente ao salvar. Arquivos anteriores sem perfil continuam válidos.

A data deve usar dd/mm/aaaa, e o horário hh:mm. Data inválida, data futura e horário sem data não são aceitos. Horário e local ficam registrados para uma futura etapa; não são usados para atribuir precisão ou calcular ascendente nesta versão.

## Próxima etapa: corte e seleção de cartas

Fluxo previsto: apresentação → pergunta → embaralhamento → corte → seleção de uma ou três cartas fechadas → revelação → leitura.

A futura sessão de seleção deve manter uma única permutação do baralho e uma orientação por carta. Cortar altera o ponto inicial dessa permutação. Cada escolha consome uma carta distinta; repetir um clique não pode sortear outra carta nem selecionar a mesma duas vezes. Cancelar descarta a sessão, e pular uma animação não pode mudar o resultado.

O histórico registra apenas consultas concluídas. A leitura é produzida depois de todas as escolhas. Esses controles de corte e escolha ainda não fazem parte da interface desta primeira etapa.

## Astrologia: limite atual e evolução

O signo aparece automaticamente durante o preenchimento da data, usando intervalos convencionais. O ano valida a data, inclusive anos bissextos; não altera os limites da tabela. O resultado é identificado como aproximado, pois não há cálculo de efemérides. Datas inválidas, incompletas e futuras não recebem um signo válido. Em uma futura implementação de mapa natal, será necessário definir efemérides, coordenadas, fuso histórico e horário de nascimento, além da política para dados desconhecidos.

Referência sobre mudança de signo e dados de nascimento: [Sun Sign Changes, Cafe Astrology](https://cafeastrology.com/sunsignbornonacusp.html). As perguntas de cada signo são adaptações editoriais locais; não são diagnósticos de personalidade nem transcrições dessa fonte.

## Verificações

Cobrir consulta anônima, dados incompletos, data inválida e futura, cancelamento da apresentação, restauração do perfil no histórico, persistência compatível e igualdade do sorteio com ou sem perfil sob a mesma fonte de aleatoriedade.


### Intervalos usados

Áries: 21/03; Touro: 20/04; Gêmeos: 21/05; Câncer: 21/06; Leão: 23/07; Virgem: 23/08; Libra: 23/09; Escorpião: 23/10; Sagitário: 22/11; Capricórnio: 22/12; Aquário: 20/01; Peixes: 19/02. Cada intervalo termina no dia anterior ao próximo. Os limites são uma convenção local, sem sobreposição; tabelas populares podem divergir em um dia.
