# Modelo probabilístico local

O sorteio usa embaralhamento uniforme com SecureRandom e seleção das primeiras n cartas, sem reposição. O baralho completo é usado novamente a cada tiragem. Pergunta e tema não influenciam as chances.

A orientação é decidida separadamente para cada carta. A chance de 50% para cada orientação é uma escolha do aplicativo, e pode diferir de um embaralhamento físico.

## Identidades e posições

Para N=78 e n cartas:

- Carta específica em uma posição: 1/N.
- Carta específica em qualquer posição: n/N.
- Após i cartas conhecidas, cada restante tem chance 1/(N−i) de ser a próxima.
- Conjuntos sem ordem: C(N,n) = N! / (n! (N−n)!).
- Sequências com posições: A(N,n) = N! / (N−n)!.
- Com orientações habilitadas: A(N,n) × 2^n.

| Tiragem | Conjuntos | Sequências | Com orientações |
| --- | ---: | ---: | ---: |
| Uma carta | 78 | 78 | 156 |
| Três cartas | 76.076 | 456.456 | 3.651.648 |

A chance de cada sequência exata com suas orientações é o inverso da última coluna. Sem invertidas, use a coluna de sequências.

## Invertidas: binomial

P(K=k) = C(n,k) / 2^n.

Para três cartas, as chances de 0, 1, 2 e 3 invertidas são 12,5%, 37,5%, 37,5% e 12,5%. Com inversões desativadas, P(K=0)=1.

## Categorias: hipergeométrica

P(X=k) = C(M,k) × C(N−M,n−k) / C(N,n), para uma categoria com M cartas.

A interface apresenta os Arcanos Maiores (M=22). A função também permite estudar um naipe (M=14). Identidades na mesma tiragem são dependentes pela ausência de reposição; orientações são independentes.

Contagens usam BigInteger; percentuais são aproximações com seis casas decimais. Os testes verificam contagens, soma das probabilidades e E[X]=nM/N.

## Próximos estudos

Comparar frequências observadas em simulações com a teoria, quantificar erro amostral e estudar intervalos de confiança. Não compensar resultados anteriores alterando os pesos das cartas.
