# RUPTURA: ECOS DO VAZIO — plano de desenvolvimento

O primeiro objetivo é um recorte curto de **O Primeiro Eco** com começo, desenvolvimento, encerramento provisório e progresso persistente. A campanha completa será construída depois de validar esse ciclo.

## Situação do marco 0.1

Os modelos de combate, mapa, narrativa, progressão e salvamento estão implementados, assim como os renderizadores, recursos provisórios, menu e criação de personagem. Exploração, diálogos, dois encontros e encerramento estão integrados. O build, 33 testes JUnit e a execução da missão na janela OpenGL foram verificados neste computador. Comandos, resultados e limites de validação ficam no README; a avaliação de usabilidade por jogadores ainda está pendente.

| Fase | Entrega prevista | Situação nesta versão |
| --- | --- | --- |
| 0 — Fundação | Gradle Wrapper, Java 21, LibGDX, desktop, módulos, recursos e documentos | Implementado; build e janela verificados |
| 1 — Combate | Arena, iniciativa, ações, IA, habilidades, fraquezas, estados e desfechos | Modelo e apresentação integrados e testados |
| 2 — Narrativa | Cenas externas, escolhas, condições, afinidade, retratos e histórico | Modelo e roteiro do prólogo implementados; apresentação integrada no marco |
| 3 — Exploração | Rua, colisão, interação, progressão de eventos e mapa reproduzível | Mapa e sistemas implementados; fluxo visual integrado no marco |
| 4 — Vertical slice | Completar O Primeiro Eco e validar novo jogo → final → continuar | Fluxo integrado e piloto ponta a ponta executado; revisão com jogadores pendente |
| 5 — Progressão | Equipamentos, inventário completo, talentos e combinações de habilidades | Apenas XP, níveis, recursos e especializações iniciais implementados |
| 6 — Ferramentas | Launcher JavaFX e editores Swing em processos próprios | Planejado |
| 7 — Campanha | Quatro atos, companheiros, facções e finais com consequências | Planejado; universo inicial documentado |
| 8 — Polimento | Arte definitiva, áudio, desempenho, acessibilidade e distribuição | Recursos originais provisórios já presentes; produção definitiva planejada |

## Gate do primeiro marco

1. Compilar todos os módulos com Gradle e executar testes JUnit do domínio.
2. Abrir a janela LibGDX em ambiente com OpenGL disponível e verificar menu, redimensionamento e entrada.
3. Concluir novo jogo com as duas decisões do sobrevivente e observar diferenças de recursos e encerramento.
4. Verificar alcance, colisões, movimento, habilidades, cura, defesa, fraquezas, ordem dos turnos e mudança de fase do chefe.
5. Cobrir vitória e derrota, retorno ao menu, salvamento, continuidade e recuperação de arquivo inválido.
6. Verificar texto em português, volume e redução de movimento.
7. Registrar evidências executadas e limitações reais no README.

## Próximo marco após o recorte jogável

Consolidar a experiência de O Primeiro Eco com ajuste de dificuldade, clareza de alcance e efeitos de ação, restauração consistente de recursos e feedback de escolhas. Acrescentar testes apenas para regras com risco real e problemas encontrados na integração. Medir desempenho antes de expandir o cenário.

Em seguida, introduzir um segundo cenário e um companheiro com habilidade diferente; o novo conteúdo deve usar os mesmos contratos de combate, diálogo e salvamento. Integrar a geração procedural à exploração somente após definir encontros, entradas e saídas narrativas.

## Expansão de campanha

Ato I estabelece a queda do assentamento e o sinal de Orfeu. Ato II apresenta infectados com lembranças e conflitos entre facções. Ato III leva o grupo ao outro lado e cobra consequências de relacionamento. Ato IV depende das decisões acumuladas para oferecer sacrifício, reconstrução, dominação, tragédia e um caminho secreto.

Cada ato precisará de roteiro, mapas, encontros, recursos e testes próprios. Uma meta de duração não equivale a conteúdo produzido. Finais futuros devem alterar destinos de pessoas e lugares e consultar condições persistidas, além de apresentar textos diferentes.

## Princípios para as próximas mudanças

Preservar a separação entre regras e renderização. Preferir sistemas pequenos completos a classes vazias. Usar dados externos para conteúdo. Não integrar Swing, JavaFX e LibGDX à mesma janela. Versionar qualquer alteração do formato de salvamento. Substituir recursos provisórios por produção original ou devidamente licenciada. Atualizar documentação e evidências junto com o código.
