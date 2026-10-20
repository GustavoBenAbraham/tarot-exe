# Aprendizados e evolução do projeto

Sou Gustavo, estudante de Java. Estou usando o Tarot.exe como projeto de
portfólio para acompanhar o caminho entre uma ideia, um aplicativo funcional
e uma distribuição que outra pessoa consiga instalar.

O projeto foi construído de forma incremental, com apoio de ferramentas de IA
na implementação, revisão e testes. Meu foco de estudo é compreender as decisões,
reproduzir os testes e continuar evoluindo o código. Ter uma funcionalidade no
repositório não significa que eu já domine todos os assuntos envolvidos.

## O que pratiquei nesta aplicação

| Tema | Aplicação no projeto | Aprendizado trabalhado |
| --- | --- | --- |
| Java e orientação a objetos | Records, enums, serviços e interfaces | Dar nomes claros aos conceitos e separar responsabilidades. |
| Coleções e imutabilidade | Baralho e listas de cartas sem alteração externa | Evitar que uma leitura mude por efeito de outra operação. |
| JavaFX | Colunas, abas, propriedades, eventos e imagens | Fazer a interface acompanhar o estado da aplicação. |
| Programação assíncrona | HttpClient, CompletableFuture e thread do JavaFX | Uma operação lenta precisa acontecer sem bloquear a janela. |
| Cancelamento e concorrência | Identificação da geração e descarte de respostas antigas | Uma resposta da IA não pode aparecer na tiragem errada. |
| Arquivos e persistência | UTF-8, arquivos temporários e movimento atômico | Salvar dados envolve tratar falhas, corrupção e compatibilidade. |
| Testes | JUnit 5 e servidor HTTP local simulado | Exercitar erros e limites, além do caminho que funciona. |
| Integração com IA | Ollama opcional e parâmetros configuráveis | O modelo complementa o catálogo; disponibilidade e qualidade precisam de avaliação. |
| Distribuição | Maven, jpackage e Inno Setup | Rodar no ambiente de desenvolvimento é diferente de instalar em outra máquina. |
| Documentação | README, capturas, instruções e Releases | Facilitar que outra pessoa entenda, execute e avalie o projeto. |

## Decisões que mudaram minha forma de pensar o projeto

### A interface precisa respeitar o uso real

Uma primeira tentativa de ajustar as cartas à altura disponível deixou as
imagens pequenas demais. A solução foi manter um tamanho legível, oferecer
rolagem e abrir a imagem original em uma janela ampliada. Depois, a área de
trabalho passou para colunas, separando cartas e leitura.

### A IA é uma integração, não um requisito para o aplicativo funcionar

O catálogo continua produzindo a leitura imediatamente. O Ollama é acionado
por um botão separado e pode falhar, demorar ou ser cancelado sem apagar essa
leitura. Os testes locais compararam modelos; o Qwen2.5 3B foi mais adequado
ao teste curto nessa máquina. Isso não estabelece que ele seja o melhor modelo
para todo equipamento ou toda pergunta.

### Cancelar não é apenas esconder um indicador

O pedido HTTP deve ser cancelado, e uma resposta atrasada precisa ser ignorada
se o usuário já iniciou outra tiragem. Esse comportamento é exercitado com
futuros controlados nos testes da interface.

### Os dados do usuário precisam sobreviver à evolução do aplicativo

O histórico guarda os textos já apresentados e fica no perfil do usuário,
fora da pasta do executável. A desinstalação foi testada comparando os arquivos
do histórico antes e depois; eles foram preservados.

### Teste automatizado não resolve toda a avaliação

Os testes ajudam a verificar regras, arquivos, parâmetros e fluxos. A clareza
dos textos, a interpretação simbólica e o conforto da interface ainda exigem
leitura e teste humano. Por isso, o projeto mantém limitações e próximos passos
documentados.

## O que estou aprendendo agora

- Melhorar a organização do código conforme novas funcionalidades aparecem.
- Tornar mensagens e configurações mais claras para quem usa o programa.
- Revisar o conteúdo e avaliar respostas de IA sem depender apenas de um exemplo.
- Automatizar verificações com GitHub Actions e publicar versões rastreáveis.
- Validar instalação, atualização e layout em outras máquinas e escalas de tela.

## Próximos passos

O plano atual está em [PENDENCIAS.md](../PENDENCIAS.md). Entre os próximos
passos estão ampliar os contextos por carta, testar outras configurações de
exibição e preparar assinatura digital para distribuição pública.
