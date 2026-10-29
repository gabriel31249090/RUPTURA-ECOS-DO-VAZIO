# Estrutura narrativa

## Missão implementada: O Primeiro Eco

A missão é uma fuga com começo, decisão moral e encerramento provisório. O protagonista e Lia tentam alcançar uma estação de rádio durante a invasão de Aurora, assentamento no Vale do Cinzel. A transmissão final conecta o incidente local ao Projeto Orfeu sem solucionar o mistério central.

O conteúdo autoral está em `data/dialogues/prologue.properties`, lido como UTF-8 por `DialogueScript`. O formato equivalente a um roteiro estruturado oferece contagem de falas, nome, texto, expressão, escolhas, chaves persistentes e condições simples. O texto aceita `{hero}`, `{origin}` e `{specialization}`. Não é uma linguagem geral de scripts.

| Momento | Cena/dado | Consequência atual |
| --- | --- | --- |
| Céu se rompe; rádio de Davi | `intro` | Apresenta a fuga e a percepção incomum do protagonista. |
| Primeira resposta | `first_response` | Empatia, pragmatismo ou curiosidade; afinidade persistente. |
| Reação de Lia | `intro_empathy`, `intro_pragmatic`, `intro_curious` | Resposta própria à escolha inicial. |
| Orientação e suprimentos | `lia`, `crate` | Instruções contextuais e comentário sobre Davi. |
| Primeiro bloqueio | `combatIntro`, `firstVictory` | Introduz criaturas e ecos de memórias. |
| Pessoa nos escombros | `survivor` | Escolha persistente `survivor=rescue` ou `survivor=supplies`. |
| Reação à decisão | `survivorRescue`, `survivorSupplies` | Ícaro é resgatado ou Lia promete retornar. |
| A voz do irmão | `radio` | Introduz o Sussurrante e o confronto final. |
| Transmissão de 2041 | `ending` | Falas condicionais refletem resgate e afinidade. |

`GameSession` guarda escolhas, histórico, progresso de missão e relação com Lia. `rewardBattle` aplica recompensas uma única vez. `completeMission` exige a vitória no encontro final. Salvamentos são locais e versionados; o sistema narrativo não depende de serviços externos.

## Condições e consequências atuais

`DialogueScript.scene(id, session)` avalia condições de igualdade de escolhas e os limites `affinity>=2` e `affinity<2`. `applyChoice` registra a resposta no histórico e aplica a consequência uma única vez. O resgate entrega um estojo e faz Ícaro aparecer no encerramento; priorizar o rádio entrega sucata e mantém a promessa de retorno como conflito com Lia.

A rua autoral mantém rotas válidas para todas as interações. O gerador procedural é uma base testada para expansões; a missão atual não muda seus eventos narrativos aleatoriamente.

Entradas/saídas arbitrárias de personagens, comandos de câmera por roteiro, trilhas por nó, condições booleanas compostas e edição visual do diálogo ainda estão planejados.

## Campanha de quatro atos — planejada

### Ato I — O Céu se Partiu

O ataque ao assentamento revela a habilidade do protagonista e deixa uma transmissão de Orfeu. O marco implementado corresponde a uma parte inicial deste ato, não ao ato inteiro.

### Ato II — Os Mortos se Lembram

Viagens entre comunidades, ruínas e instalações científicas. Infectados preservam fragmentos de vida. Facções discordam sobre cura, contenção e responsabilidade. Missões secundárias começam a modificar rotas, alianças e o acesso a informação.

### Ato III — O Outro Lado

Uma fenda estabilizada permite a travessia. As dimensões têm geografias e formas de existência próprias. Relações anteriores determinam quais companheiros aceitam o risco. As ligações entre Orfeu e a colisão dimensional aparecem através de evidências e contradições.

### Ato IV — A Última Escolha

A separação das dimensões cobra um preço. O jogo deve avaliar decisões anteriores, pessoas salvas, compromissos, informações e relações; a escolha final não deve substituir esses fatores.

## Finais futuros — não implementados

| Final | Diretriz de consequências |
| --- | --- |
| Sacrifício | A separação exige uma perda concreta; as comunidades salvas determinam o que sobrevive. |
| Reconstrução | Uma solução compartilhada depende de alianças, pesquisa e sobreviventes capazes de sustentá-la. |
| Dominação | Controlar as conexões altera a autonomia das facções e as lealdades do grupo. |
| Trágico | Compromissos quebrados e falhas de contenção produzem perdas acumuladas. |
| Secreto | Exige descobertas e cooperação específicas; não deve depender só de uma opção escondida no último diálogo. |

As condições finais exatas serão definidas quando os sistemas correspondentes estiverem implementados. A duração pretendida de 20–30 horas pertence ao objetivo de longo prazo, sem promessa para esta versão.
