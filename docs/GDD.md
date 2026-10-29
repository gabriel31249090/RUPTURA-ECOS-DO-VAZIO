# RUPTURA: ECOS DO VAZIO — documento de design

Versão de projeto: **0.1.0 / O Primeiro Eco**. Este documento distingue o recorte implementado da visão de campanha. O estado de execução e os comandos de validação ficam no README.

## Identidade e visão

RPG narrativo com exploração isométrica e combate tático por turnos, inteiramente offline e apresentado em português brasileiro. Doze anos após o Projeto Orfeu conectar ecossistemas incompatíveis, uma nova ruptura atinge um assentamento. O protagonista ouve lembranças vindas das criaturas; a paramédica Lia tenta preservar vidas enquanto os dois procuram uma estação de rádio.

Os pilares são posicionamento legível, decisões com consequências verificáveis, personagens com personalidade e atmosfera de ficção científica sombria. As referências de gênero orientam ritmo e profundidade; arte, mundo, personagens e diálogos são originais.

A campanha futura pretende abranger quatro atos e aproximadamente 20–30 horas. Essa duração é uma meta de produção, sem conteúdo equivalente implementado nesta versão.

## Recorte da primeira entrega

O marco 0.1 concentra o fluxo de **O Primeiro Eco**: criar o protagonista, atravessar a invasão com Lia, enfrentar o primeiro grupo de infectados, decidir o destino de um sobrevivente, enfrentar o Sussurrante e alcançar a transmissão de Orfeu. O encerramento é provisório e permite continuar o desenvolvimento posterior da campanha.

| Área | Implementação do recorte | Limite |
| --- | --- | --- |
| Aplicação | Java, Gradle, módulos de domínio, núcleo LibGDX e desktop LWJGL3 | JavaFX e ferramentas Swing ficam para outra fase |
| Identidade | Nome, pele, cabelo, roupa, acessório, pronomes, origem e especialização persistidos | Origens e pronomes não criam campanhas alternativas; acessórios dependem da representação visual disponível |
| Exploração | Rua autoral isométrica, colisões, Lia, suprimentos, sobrevivente e rádio | Um cenário; outros biomas ainda não existem |
| Narrativa | Prólogo e encerramento externos, respostas, condições, afinidade e histórico | Pequeno conjunto de cenas; sem editor visual |
| Combate | Protagonista e Lia contra três inimigos por encontro | Grupo fixo de dois aliados; sem recrutamento ou troca de formação |
| Progressão | Experiência, nível, recursos e especialização inicial | Sem árvore de talentos, fabricação ou equipamento intercambiável |
| Salvamento | Progresso local versionado, validação, arquivo temporário e backup | Um perfil local; sem nuvem |
| Apresentação | Geometria isométrica, silhuetas pixeladas, retratos e áudio sintetizado | Arte provisória e nenhuma trilha musical completa |
| Procedural | Gerador de salas e corredores com semente e validação de conectividade | O prólogo usa mapa autoral; masmorras ainda não integram a campanha |

## Ciclo de jogo

1. O jogador cria sua identidade e escolhe uma entre quatro dificuldades.
2. O prólogo apresenta Lia, Davi pelo rádio e a primeira manifestação da ressonância.
3. A exploração conduz a suprimentos e ao primeiro encontro tático.
4. A vitória abre o caminho para uma decisão sobre um sobrevivente.
5. A decisão altera recursos, afinidade, diálogos e o encerramento.
6. O confronto no rádio apresenta um inimigo especial com mudança de fase.
7. A transmissão revela uma pista do Projeto Orfeu e o progresso é salvo.

O diário comunica o objetivo atual. A interface mostra informações conforme o contexto, com telas separadas para identidade, exploração, diálogos e combate.

## Regras táticas implementadas

A arena lógica mede **10 × 8 casas**. A iniciativa ordena unidades por velocidade. Cada turno permite um movimento limitado seguido de ação principal; atacar, usar habilidade, defender ou usar item encerra o turno após a resolução. Também é possível encerrar voluntariamente.

O movimento respeita ocupação e obstáculos por busca de caminhos. Ataques respeitam alcance e linha de visão. Resíduo dimensional causa dano a quem termina o movimento nas casas afetadas. As fórmulas de dano ficam fora da renderização.

O protagonista recebe atributos e habilidade conforme a especialização. Lia oferece ataque à distância e cura. A afinidade positiva aumenta sua cura, com limite. Acertar uma fraqueza aumenta o dano e reduz postura; a quebra gera vulnerabilidade e consome a próxima ação do alvo para recuperação. Não há cadeia ilimitada de turnos extras.

Os encontros usam Errante, Corredor, Estilhaçado e, no encontro final, Sussurrante. O Sussurrante muda atributos, alcance e fraqueza ao cruzar metade da vida. A IA avalia distância, disponibilidade de ataque, vida dos aliados e perigos do terreno. A dificuldade altera dano recebido, agressividade e quantidade inicial de kits da campanha; não multiplica excessivamente a vida dos inimigos.

Uma máquina de estados bloqueia comandos enquanto animações e ações inimigas são resolvidas. Vitória e derrota são resultados explícitos do modelo de combate.

## Decisões e persistência

As respostas do prólogo determinam a reação de Lia e influenciam afinidade. No evento dos escombros, resgatar Ícaro acrescenta afinidade e um kit; priorizar o rádio acrescenta sucata e reduz afinidade. O encerramento consulta essas decisões e a relação com Lia.

Recompensas e escolhas são idempotentes: repetir um evento já registrado não duplica recursos. O salvamento contém identidade, posição, semente, recursos, progressão, escolhas, afinidade, histórico e estado da missão. Arquivos inválidos e versões desconhecidas recebem tratamento explícito.

## Arte, som e acessibilidade

A paleta usa azul escuro, concreto dessaturado, ferrugem e verde água para anomalias e interações. O ambiente emprega faces volumétricas, sombras estilizadas e objetos reconhecíveis. A arte inicial é desenhada por código e pode ser substituída gradualmente por atlas originais.

O áudio provisório contém dois ciclos sintetizados e três efeitos curtos. Volume geral e transições entre exploração e combate pertencem ao mixer. A ausência de dispositivo de áudio não impede jogar. Redução de movimento limita pulsações e deslocamentos; velocidade de texto e tela cheia são preferências locais.

## Critérios para considerar o marco pronto

O build deve compilar, os testes de domínio devem executar e o aplicativo desktop deve abrir. Uma verificação ponta a ponta deve cobrir novo jogo, consequências narrativas, exploração, ambos os combates, vitória, derrota, salvamento e continuidade. Ter código para uma tela não comprova que esse fluxo passou; os resultados executados devem ser registrados no README.

## Fora do recorte atual

Os demais companheiros, facções exploráveis, outros biomas, campanha de quatro atos, finais completos, árvores de talentos, equipamentos, criação de itens, editor de mapas, launcher JavaFX, chefes de campanha e distribuição comercial permanecem planejados. São extensões do projeto, sem telas fictícias para simular sua conclusão.
