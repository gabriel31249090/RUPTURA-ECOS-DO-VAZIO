# Combate — versão 0.1.0

## Implementado

`model/ruptura/combat` contém as regras independentes de LibGDX. O protagonista e Lia enfrentam três inimigos numa arena de 10 × 8 casas. Há um encontro comum e um encontro com o Sussurrante, que possui duas fases. Todos os nomes apresentados ao jogador estão em português brasileiro.

### Turnos e sincronização

A iniciativa ordena os participantes por velocidade, em ordem decrescente. Cada personagem pode mover-se uma vez e executar uma ação principal. As ações são ataque, habilidade, defesa e kit médico. Também é possível encerrar o turno voluntariamente.

`CombatSystem.Phase` admite `PLAYER`, `ANIMATING`, `ENEMY`, `VICTORY` e `DEFEAT`. Uma ação é validada antes de entrar em `ANIMATING`; seu resultado ocorre uma única vez depois do atraso visual. Durante esse intervalo, novas ações são rejeitadas. A movimentação devolve o controle ao mesmo personagem; as outras ações avançam a iniciativa. Personagens derrotados são ignorados.

O atraso normal é de 0,16 s para movimento e 0,42 s para outras ações. A redução de movimento usa 0,08 s. Os efeitos de apresentação são consumidos pela interface por `effectKind`, `effectTime` e coordenadas do efeito.

### Posicionamento

- Distâncias e caminhos usam vizinhos cardinais, sem movimento diagonal entre casas.
- A busca em largura considera paredes e personagens vivos. Uma casa com distância Manhattan curta ainda pode estar fora do alcance se exigir um desvio.
- Ataques e habilidades respeitam alcance e linha de visão; obstáculos também bloqueiam disparos.
- Duas casas de resíduo dimensional causam 5 PV ao final do movimento.
- Altura e cobertura visual ainda não concedem bônus de combate. Obstáculos não são destrutíveis nesta versão.

A consulta de alcance reutiliza o resultado da busca em largura até mudar a posição de um participante, a ocupação por morte ou o ator consultado. Desenhar as 80 casas não dispara 80 buscas novas. A lista pública de unidades é uma visão sem alterações estruturais, evitando cópias por casa desenhada.

### Atributos iniciais

| Personagem | PV | Energia | Ataque | Defesa | Velocidade | Movimento | Alcance básico |
|---|---:|---:|---:|---:|---:|---:|---:|
| Protagonista, antes da especialização | 110 | 40 | 25 | 9 | 19 | 3 | 1 |
| Lia Torres | 90 | 50 | 18 | 7 | 14 | 3 | 3 |

| Especialização | Efeito implementado |
|---|---|
| Ressonante | +8 energia, +6 poder dimensional; Pulso do vazio |
| Executor | +5 ataque, +4 energia; Pulso do vazio |
| Tecnomante | +8 potência tecnológica; Pulso elétrico |
| Sobrevivente | +20 PV, +3 defesa; Pulso do vazio |
| Médico de campo | +24 energia; Estabilizar |

O poder dimensional inicial é 18 e a potência tecnológica inicial é 12. As bonificações correspondentes aumentam o poder das habilidades de dano. Precisão, resistência e chance crítica já possuem campos de dados, mas ainda não participam da resolução: os ataques válidos são determinísticos, sem rolagens de acerto ou crítico.

### Fórmula central

`DamageFormula.calculate` aplica, nesta ordem:

1. `base = máximo(3, poder − defesa × 0,55)`.
2. Fraqueza: multiplicador 1,35.
3. Alvo já vulnerável: multiplicador 1,30.
4. Alvo defendendo: multiplicador 0,45.
5. Multiplicador da dificuldade, somente para dano inimigo.
6. Arredondamento ao inteiro mais próximo, com mínimo de 1 PV.

O ataque básico usa o ataque do personagem. Pulso do vazio usa poder 30 mais a bonificação dimensional acima de 18; Pulso elétrico usa poder 30 mais a bonificação tecnológica acima de 12. Defesa reduz o dano recebido até o começo do próximo turno do defensor e recupera 5 de energia.

### Habilidades, postura e itens

Os catálogos `data/abilities.json` e `data/enemies.json` ficam separados da implementação. O leitor atual aceita os registros planos usados pelo projeto, com strings e inteiros; ele não é um interpretador JSON geral. Para acrescentar efeitos arbitrários ou estruturas aninhadas, deve ser substituído ou ampliado junto com a validação dos dados.

| Habilidade | Custo | Alcance | Resultado |
|---|---:|---:|---|
| Pulso do vazio | 8 | 4 | Dano dimensional |
| Pulso elétrico | 8 | 4 | Dano elétrico |
| Estabilizar | 10 | 5 | Recupera até 30 PV de um aliado vivo |

Cada ponto positivo de afinidade com Lia aumenta a cura dela em 3 PV, limitado a três pontos (+9 PV). O bônus não se aplica à cura do protagonista médico. Afinidade negativa não penaliza a cura. `setAffinity` substitui o bônus anterior, sem acumular chamadas.

Um kit recupera até 40 PV de um aliado vivo a até cinco casas e consome a ação principal. Cura e kit não podem ser gastos em aliados com a vida cheia. O inventário da sessão pode ser conectado por `setPotions`; `potions()` informa o saldo restante.

Cada inimigo começa com duas unidades de postura. Acertar sua fraqueza remove uma. Ao chegar a zero, ele fica vulnerável, recebe mais dano e perde sua próxima ação para recuperar a postura. Não há turnos extras, evitando ciclos infinitos.

### Inimigos

| Arquétipo | PV | Identidade mecânica | Fraqueza inicial |
|---|---:|---|---|
| Errante | 46 | Lento, corpo a corpo | Elétrica |
| Corredor | 38 | Movimento 4 e velocidade 16, pouca defesa | Dimensional |
| Estilhaçado | 68 | Defesa 10, alcance 3, ataque especial consome energia | Dimensional |
| Sussurrante | 134 | Chefe com alcance e pressão crescentes | Elétrica |

Ao atingir metade da vida, o Sussurrante entra na fase II uma única vez: ganha 7 de ataque, um ponto de movimento, alcance 4 e fraqueza dimensional. Não há restauração artificial da vida do chefe.

A IA compara posições alcançáveis, distância, linha de visão, perigo e proporção de vida dos aliados. Ela tende a concentrar ataques em alvos vulneráveis conforme a dificuldade. No Normal e acima, o Estilhaçado e a segunda fase do chefe usam ataques especiais de poder adicional 6 quando possuem 8 de energia. O protótipo ainda não possui comportamentos em árvore, invocações ou interação destrutiva com cobertura.

### Dificuldade

| Dificuldade | Dano recebido | Agressividade | Kits de um combate isolado |
|---|---:|---:|---:|
| História | 0,65 | 0,25 | 4 |
| Normal | 1,00 | 0,55 | 3 |
| Difícil | 1,20 | 0,80 | 2 |
| Pesadelo | 1,40 | 1,00 | 1 |

Na aventura, o saldo persistente passado por `setPotions` substitui a quantidade inicial do combate isolado. A dificuldade não aumenta a vida inimiga. Controles individuais de dano, agressividade e frequência de recursos permanecem planejados.

## Validação executada

Comando: `gradlew.bat :model:test`.

Os 14 testes de `CombatSystemTest` passaram. Eles verificam iniciativa, caminhos e colisões, invalidação do cache de navegação, resolução única após animação, defesa, cura, vitória/derrota, IA, fórmula, quebra de postura, duas fases do chefe, especializações e ligação de recursos/afinidade. Um teste executa 20 batalhas completas pela API pública: História e Normal, cinco especializações e os dois encontros. A estratégia usa cura, kits, movimentação, ataques e prioridade aos inimigos menores antes do chefe. Isso demonstra caminhos de vitória; não substitui testes com jogadores nem garante equilíbrio final de Difícil/Pesadelo.

`CampaignFlowTest` acrescenta dois testes de integração: introdução, escolha, resgate, recompensas dos dois encontros, aplicação do vínculo à cura, encerramento e round trip do salvamento; a rota pragmática também é verificada para produzir recursos e falas diferentes. Esses testes conectam os contratos do modelo e não dirigem a janela gráfica.

## Próximos passos

Ataques em área, estados elementais, animações específicas por habilidade, terreno elevado com efeito mecânico, cobertura destrutível, equipamentos, progressão de atributos e mais arquétipos. XP e nível são registrados pela missão, mas ainda não escalam os atributos de batalha.
