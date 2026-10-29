# Personagens e relacionamentos

## Implementado no primeiro marco

### Protagonista

Nome padrão: Alex, substituído pelo nome escolhido pelo jogador. O perfil guarda nome, origem, especialização, pronomes e índices de pele, cabelo, roupa e acessório. As opções visuais e os efeitos disponíveis na interface devem ser conferidos no código do criador; a presença de um campo no salvamento não implica uma campanha de origem exclusiva.

A voz além das fendas faz parte da história central. A primeira resposta à situação pode demonstrar empatia, pragmatismo ou curiosidade. Essa decisão é persistida em `choices.first_response`, altera a afinidade com Lia e possui respostas autorais próprias no roteiro externo.

### Lia Torres

Ex-paramédica de resgate; companheira controlável no combate. Determinada, empática e pragmática. Usa humor breve quando precisa conter o medo. Acredita que alguns infectados possam ser recuperados, mas sabe que reconhecer uma voz não garante reconhecer a pessoa.

Lia guia a fuga de Aurora, no Vale do Cinzel. A decisão sobre a pessoa nos escombros afeta sua confiança. A relação não é apenas um número: falas do encerramento mudam conforme o que o jogador fez e a afinidade acumulada.

| Evento | Efeito atual |
| --- | --- |
| Resposta empática na abertura | Afinidade +2; reação em `intro_empathy`. |
| Resposta pragmática | Afinidade inalterada; reação em `intro_pragmatic`. |
| Resposta curiosa | Afinidade +1; reação em `intro_curious`. |
| Resgatar Ícaro | Afinidade +2, um estojo adicional e presença de Ícaro no encerramento. |
| Priorizar o rádio | Afinidade −1, cinco peças de sucata; Lia pede para voltar à rua no encerramento. |
| Afinidade final ≥2 | Lia afirma que permanecerá com o protagonista. |
| Afinidade final <2 | Lia aceita continuar até entender o sinal, deixando uma conversa pendente. |

As consequências são aplicadas uma única vez por chave de escolha. A afinidade persiste no salvamento. Desbloqueios de habilidades, missões pessoais e deserção ainda não foram implementados.

### Davi Kaito

Engenheiro de sistemas e especialista em tecnologia antiga. Participa por rádio no prólogo; seu sarcasmo contrasta com o perigo. Trabalhou indiretamente para uma organização ligada a Orfeu, mas esse passado não é revelado como resposta definitiva na abertura.

Recrutamento, drones, torretas, hacking e progressão própria estão planejados. Sua participação narrativa atual não deve ser confundida com um terceiro combatente controlável.

### Ícaro

Técnico da estação de rádio preso nos escombros. A suspeita de mordida dimensional é desmentida ao ser resgatado: o ferimento veio de um cachorro assustado. Entrega um estojo e informa sobre a porta lateral. Seu resgate modifica o diálogo de encerramento. Não integra a equipe.

### O Sussurrante

Antagonista do encontro final do prólogo. Imita uma voz familiar a Lia, mas falha numa forma de tratamento pessoal. A cena demonstra que copiar uma voz não implica conhecer toda a pessoa.

## Companheiros planejados

| Personagem | Identidade | Papel planejado | Conflito |
| --- | --- | --- | --- |
| Eva Moraes | Pesquisadora de fenômenos dimensionais | Energia, análise e controle de campo | Sabe mais sobre a invasão do que admite. |
| Rook | Mercenário e antigo soldado | Defesa, força física e armas pesadas | Participou de operações contra assentamentos considerados contaminados. |
| Noa | Sobrevivente de uma instalação dimensional | Distorções espaciais | Sua origem contradiz sua aparência. |

A equipe futura poderá ter até três integrantes ativos, incluindo o protagonista. O marco atual usa protagonista e Lia. Retratos com expressões, habilidades exclusivas avançadas, conflitos entre integrantes, progressão individual e missões pessoais serão ampliados em fases posteriores.
