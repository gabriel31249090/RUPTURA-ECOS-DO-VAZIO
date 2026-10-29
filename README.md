# RUPTURA: ECOS DO VAZIO

RPG tático isométrico original em Java e LibGDX. **Versão 0.1.0: O Primeiro Eco**, um capítulo curto com começo, dois combates, escolha narrativa, encerramento e progresso local. Interface e diálogos em português brasileiro.

## Jogar no Windows

Abra **`iniciar.bat`** na raiz. O pacote local já compilado fica em `lwjgl3/build/install/Ruptura/`; seu executável é `bin/Ruptura.bat`. É necessário Java 21 ou superior. Neste computador, o projeto foi compilado e executado com o JDK 23.0.2 disponível, gerando bytecode Java 21.

O jogo e o pacote compilado funcionam offline. O primeiro build em outro computador baixa Gradle e dependências da internet. O arquivo `index.java` original estava vazio e foi preservado; o ponto de entrada real é `ruptura.desktop.DesktopLauncher`.

## Compilar, testar e distribuir

No PowerShell, dentro desta pasta:

```powershell
.\gradlew.bat build
.\gradlew.bat lwjgl3:run
.\gradlew.bat model:test
.\gradlew.bat lwjgl3:installDist
.\gradlew.bat lwjgl3:distZip
```

O Gradle Wrapper dispensa Gradle instalado no PATH. Requer JDK 21+; JDK 21 e 23 são o alvo inicial. Gradle 9.2.0 e LibGDX 1.14.2 estão fixados. A distribuição ZIP aparece em `lwjgl3/build/distributions/` e inclui bibliotecas, recursos e scripts. Ela requer Java instalado; um runtime embutido e instalador nativo ficam para a distribuição futura. No Linux/macOS, use `sh gradlew`; no macOS inicie com `-XstartOnFirstThread` (a tarefa Gradle já faz isso).

## Controles

| Contexto | Controle | Ação |
|---|---|---|
| Exploração | WASD / setas | Movimento relativo à tela |
| Exploração | Clique no chão | Caminhar por uma rota válida |
| Exploração | E / botão contextual | Conversar, abrir suprimentos, investigar, entrar na estação |
| Exploração | J / I | Diário / inventário |
| Exploração | F5 | Salvar |
| Diálogo | Espaço / Enter | Revelar fala; pressione novamente para avançar |
| Diálogo | Tab / H | Revelar texto / histórico |
| Escolha | 1–3 / clique | Selecionar resposta |
| Combate | 1 / 2 / 3 | Mover / atacar / habilidade; depois clique na casa ou alvo |
| Combate | 4 / 5 / 6 | Defender / estojo em aliado / encerrar turno |
| Geral | Esc | Pausar, retomar ou fechar arquivo |

No criador, clique nas opções para alternar; clique no nome para digitar. Há cinco especializações com diferenças reais. A aparência, a origem e os pronomes são persistidos; o roteiro inicial ainda não possui ramificações exclusivas para cada origem/pronome.

## O Primeiro Eco

Ouça Lia, siga o marcador na avenida e atravesse o primeiro bloqueio. O estojo próximo da ambulância é opcional. Depois do primeiro combate, investigue a pessoa entre os escombros e alcance a estação de rádio. O Sussurrante muda de fase e fraqueza. A escolha de resgate e o vínculo com Lia alteram as falas finais; afinidade positiva também melhora a cura dela.

Cada turno permite um movimento e uma ação. Casas iluminadas mostram destinos/alvos válidos. Habilidades gastam energia. Lia cura aliados feridos; Médico de campo também pode curar. Acertar fraquezas quebra postura. Casas de resíduo dimensional causam dano ao entrar. Os inimigos respeitam obstáculos, alcance e linha de visão. Em derrota, é possível repetir o encontro. O diário abre o bestiário e o inventário.

## Salvamento e configurações

Os arquivos ficam em **`%USERPROFILE%\.ruptura\`**: `slot-1.properties`, `slot-1.backup.properties` e `settings.properties`. Um slot, formato versionado, validação e troca por arquivo temporário. Havendo um backup válido, ele pode recuperar um slot corrompido. Salva após escolhas e encontros e ao terminar o capítulo. A batalha em andamento não é serializada: retoma-se o progresso anterior ao encontro. Novo jogo utiliza o mesmo slot quando começa a salvar.

Configurações disponíveis: janela/tela cheia, volume, velocidade de texto e redução de movimento/efeitos. A janela pode ser redimensionada com proporção preservada. Não há rede, telemetria ou contas no jogo.

## Arquitetura e dados

- `model/`: combate, fórmulas, mapa, geração procedural, narrativa e persistência; Java puro.
- `core/`: telas, coordenação da missão, interface, arte isométrica, retratos e áudio LibGDX.
- `lwjgl3/`: entrada desktop e distribuição.
- `data/`: inimigos/habilidades em JSON e diálogos em Properties UTF-8, incluídos no classpath.
- `assets/`: fontes licenciadas e áudio original. Cenários/sprites/retratos são desenhados por código.
- `tools/GenerateAudio.java`: geração reproduzível dos cinco sons originais.
- `docs/`: [design](docs/GDD.md), [mundo](docs/LORE.md), [personagens](docs/CHARACTERS.md), [combate](docs/COMBAT.md), [narrativa](docs/NARRATIVE.md), [arte](docs/ART_DIRECTION.md), [arquitetura](docs/ARCHITECTURE.md) e [plano](docs/ROADMAP.md).

## Verificação

JUnit verifica turnos, execução única após animação, dano, navegação, defesa, cura, fraquezas, duas fases do chefe, especializações, escolhas, consequências, persistência e corrupção. Os testes percorrem 200 sementes de ruas e 200 de masmorras; simulações vencem os dois encontros com as cinco especializações em História/Normal.

Verificação executada neste computador: **33 testes JUnit aprovados**, build e distribuição concluídos, janela OpenGL aberta e capturas inspecionadas. Um piloto de integração percorreu as duas rotas da missão na janela real e validou o salvamento final: resgate (150 XP, afinidade 4) e prioridade ao rádio (150 XP, afinidade −1). Ele usa os comandos de diálogo, caminhada e combate do jogo; não mede a usabilidade de cliques humanos.

Para repetir as duas rotas com renderização real e salvamentos isolados em `build/verification/`, sem alterar o slot do jogador:

```powershell
.\gradlew.bat lwjgl3:run '-PgameArgs=--verify'
.\gradlew.bat lwjgl3:run '-PgameArgs=--verify --supplies'
```

Para testar a janela real e gravar uma captura sem salvar o progresso do jogador:

```powershell
.\gradlew.bat lwjgl3:run '-PgameArgs=--smoke --capture'
.\gradlew.bat lwjgl3:run '-PgameArgs=--combat --smoke --capture'
.\gradlew.bat lwjgl3:run '-PgameArgs=--explore --smoke --capture'
.\gradlew.bat lwjgl3:run '-PgameArgs=--creator --smoke --capture'
.\gradlew.bat lwjgl3:run '-PgameArgs=--intro --smoke --capture'
```

Capturas ficam em `captures/`. Os modos smoke encerram após 90 quadros; verificam criação da janela, recursos e renderização, sem avaliar manualmente todos os comandos. Modos de diagnóstico usam arquivos em `build/diagnostics/`, separados do perfil do jogador. O relatório JUnit fica em `model/build/reports/tests/test/index.html`.

## Limites deste marco

Este é um protótipo jogável, não a campanha de 20–30 horas. O gerador de masmorras tem testes, mas a missão usa a rua autoral e não entra em masmorras. Equipamentos, fabricação, árvore de habilidades, demais companheiros, todos os arquétipos, editores Swing e launcher JavaFX pertencem às próximas fases. Não há controle/gamepad nem remapeamento, dublagem ou animações finais. Não foi medido desempenho em outras máquinas. Difícil/Pesadelo precisam de balanceamento com jogadores. O áudio é uma base sintetizada original e provisória.

Próximo marco: refinar a experiência de combate, levar a geração procedural para uma masmorra jogável e implementar equipamento/progressão com conteúdo adicional do Ato I.

## Referências técnicas e licenças

Estrutura conferida com a [documentação oficial LibGDX](https://libgdx.com/wiki/start/project-generation), [backend LWJGL3](https://libgdx.com/wiki/app/starter-classes-and-configuration) e [compatibilidade Java/Gradle](https://docs.gradle.org/current/userguide/compatibility.html). Veja `THIRD_PARTY_NOTICES.md` e as licenças das fontes. As referências artísticas do briefing orientam o gênero; personagens, roteiro, arte e sons deste projeto são originais.
