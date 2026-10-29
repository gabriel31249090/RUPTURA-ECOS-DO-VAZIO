# Arquitetura — versão 0.1.0

## Estrutura implementada

O projeto usa Gradle Wrapper, Java com alvo de compilação 21 e LibGDX 1.14.2. O ambiente de desenvolvimento utilizado possui JDK 23.0.2; `options.release = 21` mantém as classes e APIs do projeto compatíveis com Java 21. Não há necessidade de instalar Gradle no sistema.

```text
model     → Java puro: combate, mundo, narrativa, sessão, salvamento
core      → model + LibGDX: telas, entrada, interface e renderização
lwjgl3    → core + backend desktop: janela e ponto de entrada
data/     → catálogos de combate e roteiro externo
assets/   → fontes e recursos de apresentação
docs/     → design, limites atuais e evolução
```

As dependências apontam em uma direção: `lwjgl3 → core → model`. O modelo não importa LibGDX, Swing ou JavaFX e pode ser testado sem abrir janela ou criar contexto OpenGL. O arquivo vazio `index.java` encontrado inicialmente foi preservado e não faz parte dos source sets.

## Sistemas e responsabilidades

| Código | Responsabilidade atual |
|---|---|
| `ruptura.desktop.DesktopLauncher` | Configura a janela LWJGL3, sincronização vertical e limite de FPS |
| `ruptura.RupturaGame` | Ciclo LibGDX, troca e descarte das telas, recursos compartilhados e preferências |
| `ruptura.screens` | Fluxo de apresentação, controles e transições entre menu, criação e aventura |
| `ruptura.ui.Ui` | Interface customizada, fontes, texto e botões; executa o clique após desenhar |
| `ruptura.render.WorldRenderer` | Projeção isométrica, geometria do cenário, objetos e personagens |
| `ruptura.render.PortraitRenderer` | Retratos procedurais dos personagens |
| `ruptura.render.Atmosphere` | Composição atmosférica e movimentos decorativos |
| `ruptura.combat.CombatSystem` | Arena, iniciativa, ações, IA, resolução atrasada e resultados |
| `ruptura.combat.DamageFormula` | Fórmula de dano central e determinística |
| `ruptura.story.GameSession` | Perfil, missão, inventário simples, escolhas, afinidade e histórico |
| `ruptura.story.DialogueScript` | Roteiro externo, falas condicionais, escolhas e substituições de texto |
| `ruptura.world.WorldMap` | Rua autoral com variação por semente e navegação cardinal |
| `ruptura.world.DungeonGenerator` | Geração reproduzível de salas e corredores; ainda não usada na missão |
| `ruptura.save.SaveSystem` | Slot versionado, validação, backup, recuperação e gravação temporária |
| `ruptura.save.GameSettings` | Preferências e validação de intervalos |

As telas traduzem comandos do jogador em chamadas ao modelo e desenham os resultados. O combate não decide cores, posições de interface ou comandos de teclado. Não existe uma classe vazia para cada sistema do planejamento: as responsabilidades presentes têm implementações pequenas e executáveis.

## Estado de campanha e combate

`GameSession` armazena o progresso da missão, perfil e decisões. As recompensas e escolhas têm proteção contra aplicação duplicada. O estado temporário de batalha está nos `CombatUnit` e em `CombatSystem`; ele não é gravado no meio de uma ação. Pontos de retomada usam o progresso da aventura.

A máquina de estados do combate bloqueia entrada durante a animação e executa o efeito pendente uma única vez. A passagem de tempo ocorre por `update(delta)`. `phase()` identifica vitória e derrota, e a tela faz a transição correspondente. Afinidade e recursos podem ser enviados por `setAffinity` e `setPotions`; o saldo de kits é consultável ao encerrar o encontro.

O estado visual geral usa as telas e o fluxo local da aventura. A arquitetura não pressupõe que já exista um `GameState` global com todos os estados da campanha final.

## Dados externos

- `data/enemies.json`: atributos e fraquezas dos quatro inimigos implementados.
- `data/abilities.json`: nome, descrição, poder, custo, tipo e alcance das habilidades.
- `data/dialogues/prologue.properties`: roteiro UTF-8, expressões, condições e escolhas.

Os arquivos são incluídos nos recursos do módulo `model` preservando o prefixo `data/`. O catálogo de combate também aceita o arquivo relativo ao diretório de trabalho durante o desenvolvimento. O interpretador de catálogo atende apenas o esquema plano atual; novos esquemas exigem ampliar o leitor e seus testes.

`DialogueScript` aceita as condições de afinidade `affinity>=2`, `affinity<2` e comparação simples `chave=valor` com uma escolha registrada. Nome, origem e especialização são interpolados nas falas. Um grafo narrativo geral, scripts de eventos arbitrários e validação editorial completa são trabalhos futuros.

## Persistência

O diretório padrão é `~/.ruptura`, definido pelo `user.home` da JVM. Há um slot principal, uma cópia anterior e um arquivo separado de preferências. Todos usam `Properties` em UTF-8 com versão explícita do formato.

O salvamento valida limites e consistência narrativa antes de escrever. Grava um arquivo temporário, força sua escrita e tenta movê-lo atomicamente. Sistemas de arquivos sem suporte a movimento atômico usam substituição convencional. Um slot anterior só vira backup depois de ser validado; um arquivo corrompido não substitui uma cópia válida. A leitura tenta recuperar o backup e informa a recuperação ao chamador.

O formato atual não serializa referências gráficas, recursos OpenGL, instâncias completas de tela nem uma batalha em andamento. Migrações entre versões ainda precisam ser implementadas antes de alterar o esquema de salvamento.

## Renderização e recursos

A projeção usa losangos de 64 × 32 unidades e geometria 2D desenhada por `ShapeRenderer`. Fachadas, veículos, cristais, postes, rádios, personagens e retratos são construídos por código. A aparência é provisória e original. Esse sistema produz profundidade isométrica, mas não é um motor voxel 3D.

A interface é customizada sobre LibGDX, com `SpriteBatch`, fontes FreeType e viewport virtual. Os recursos compartilhados são descartados no encerramento e as telas anteriores são descartadas na troca. A opção de redução de movimento é passada aos efeitos visuais e ao combate.

Áudio dinâmico, carregamento assíncrono e pooling de partículas ainda não possuem um sistema próprio implementado. A meta de 60 FPS está na configuração desktop; não deve ser interpretada como benchmark em todo hardware.

## Build e verificação

```powershell
.\gradlew.bat test
.\gradlew.bat :lwjgl3:run
.\gradlew.bat :lwjgl3:installDist
```

O projeto separa os testes de lógica da verificação gráfica. Os testes de `model` executam sem janela. O aplicativo também oferece modos de captura/smoke para verificar a criação real do contexto gráfico e a renderização de menu, criação, exploração e combate. Uma execução de teste unitário não demonstra que a janela abriu; esses resultados devem ser relatados separadamente.

## Expansão planejada

Launcher JavaFX e editores Swing serão aplicativos separados, com comunicação por argumentos, arquivos ou contratos explícitos. Eles não disputarão o controle da janela LibGDX. A campanha em quatro atos, mais companheiros, árvore de habilidades, equipamentos, masmorras jogáveis e áudio devem ser acrescentados sobre os contratos existentes, com novos testes nas regras correspondentes.
