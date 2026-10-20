# Como ampliar o conteúdo local

Você pode escrever textos, exemplos e perguntas de reflexão sem usar IA.
Faça uma cópia do arquivo antes de editar e use UTF-8 (por exemplo, no VS Code).
As alterações entram ao reiniciar pelo Maven, que copia os recursos para o aplicativo.
Uma distribuição já empacotada precisa ser gerada novamente.

## 1. Textos das cartas

Edite `src/main/resources/interpretations.properties`.
Cada linha tem o ID da carta e três partes separadas por `|`:

```properties
0=Um começo pede abertura para aprender e atenção aos recursos disponíveis.|O desejo de começar pode estar bloqueado pelo medo ou acelerado pela pressa.|Escolha uma experiência pequena e reversível para testar sua ideia.
```

As partes são: significado normal, significado invertido e prática sugerida.
O ID 0 é O Louco; 1 é O Mago; 8 é A Força; 11 é A Justiça; 21 é O Mundo.
Os menores seguem Paus (22–35), Copas (36–49), Espadas (50–63), Ouros (64–77).
Em cada naipe: Ás a Dez, Pajem, Cavaleiro, Rainha e Rei.
A lista completa está em `src/main/java/com/tarotexe/domain/TarotDeck.java`.

Mantenha uma única linha por ID, todos os IDs de 0 a 77 e três campos não vazios.
Não use `|` dentro dos textos; não crie uma segunda linha com o mesmo ID.
Não adicione campos extras: o leitor atual aceita exatamente esses três.
Use duas ou três frases curtas para explicar a carta, um exemplo possível e
uma ação que a pessoa consiga experimentar. Você pode ampliar o texto dentro
de cada campo. Prefira palavras acessíveis e mantenha a interpretação invertida
diferente da normal, sem tratá-la automaticamente como algo ruim.

## 2. Contextos escolhidos pelo usuário

Edite `src/main/resources/contexts.properties`.
O formato é `TEMA.GRUPO=aplicação|pergunta de reflexão`:

```properties
WORK.2=Considere como escuta e cooperação participam desta situação de trabalho.|Que expectativa precisa ser conversada com a equipe?
```

Temas: GENERAL (geral), RELATIONSHIPS (relacionamentos), WORK (trabalho e projetos),
SELF_KNOWLEDGE (autoconhecimento).
Grupos: 0 Arcanos Maiores, 1 Paus, 2 Copas, 3 Espadas, 4 Ouros.
Mantenha as 20 combinações existentes, uma linha por chave e dois campos não vazios.
A aplicação usa o grupo da carta de conselho, ou da carta única. Essa regra
é deliberadamente simples; não faz análise automática da pergunta.
Adicionar outro tema ao seletor ou um contexto específico por carta exige
uma mudança no código e nos testes, além dos textos.

## 3. O que você pode preparar para a próxima ampliação

Para cada carta, escreva em um documento separado:

- Um exemplo em relacionamentos, um em trabalho e um em autoconhecimento.
- Uma pergunta de reflexão específica para cada exemplo.
- Uma interpretação invertida com foco em excesso, bloqueio ou integração.
- Uma ação pequena, observável e possível, sem prometer um resultado.
- Sua referência de estudo, quando houver; use sua própria redação.

Esses campos ainda não são importados automaticamente: servem como material
para ampliar o esquema do catálogo depois. Você pode começar por cinco cartas
que conhece bem; não precisa preencher as 78 de uma vez.

## 4. Validar as alterações

Na pasta do `pom.xml`, execute:

```powershell
.\mvnw.cmd clean test
.\mvnw.cmd javafx:run
```

Os testes verificam cobertura das 78 cartas, campos presentes, textos normais
distintos e contextos dos quatro temas. Eles não avaliam a qualidade editorial:
revise se a leitura ficou clara, útil e coerente. IDs duplicados são rejeitados
pelo validador, assim como campos vazios, ausentes ou extras.
