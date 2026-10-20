# Aprendizados e evolução do projeto

Sou Gustavo, estudante de Java. Uso o Tarot.exe como projeto de portfólio para estudar a construção de um aplicativo desktop e sua distribuição.

## Direção atual

Em 20/10/2026, decidi remover a integração com IA e concentrar o aplicativo no sorteio e nas leituras inteiramente locais. A prioridade é aprofundar os cálculos estatísticos e probabilísticos, depois evoluir o visual e os textos.

## Temas em prática

| Tema | Aplicação | Aprendizado |
| --- | --- | --- |
| Java e orientação a objetos | Records, enums e serviços | Separar responsabilidades. |
| Coleções e imutabilidade | Baralho e tiragens | Evitar alterações acidentais. |
| Aleatoriedade | SecureRandom e sorteio sem reposição | Distinguir identidades dependentes de orientações independentes. |
| Combinatória | BigInteger e contagem de resultados | Distinguir conjuntos, sequências e orientações. |
| Probabilidade | Binomial e hipergeométrica | Calcular chances de invertidas e categorias. |
| JavaFX | Colunas, abas, imagens e eventos | Manter cartas e informações acessíveis. |
| Persistência | UTF-8, gravação temporária e movimento atômico | Preservar dados diante de falhas e mudanças de versão. |
| Testes | JUnit e testes da interface | Verificar regras, limites e compatibilidade. |
| Distribuição | Maven, jpackage e Inno Setup | Empacotar com runtime para outras máquinas. |

## Experimento anterior com IA

A versão anterior experimentou Ollama e modelos locais. Essa integração foi removida do código atual. Os estudos de operações assíncronas, cancelamento e respostas atrasadas fazem parte do histórico de aprendizado, mas não são funcionalidades atuais.

## Preservação dos dados

O histórico fica no perfil do usuário, fora da pasta do executável. Arquivos antigos continuam legíveis e são preservados ao abrir. Campos antigos da integração removida são ignorados.

## Próximos estudos

Simular sorteios, comparar frequências com as probabilidades teóricas, estudar erro amostral e intervalos de confiança. Depois, revisar visual e conteúdo das leituras. Consulte [as fórmulas](PROBABILIDADES.md) e [as prioridades](../PENDENCIAS.md).
