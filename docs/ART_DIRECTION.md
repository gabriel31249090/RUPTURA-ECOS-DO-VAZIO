# Direção artística — RUPTURA: ECOS DO VAZIO

## Implementado no protótipo

A identidade combina asfalto azul escuro, fachadas de concreto dessaturado, ferrugem em cobre e ressonância em verde água. A hierarquia usa luz e saturação: o ambiente permanece escuro, interações recebem marcadores luminosos e ameaças usam acentos quentes.

`WorldRenderer` desenha geometrias isométricas originais com losangos de 64 × 32 unidades, faces laterais, alturas e sombras. Há fachadas quebradas, veículos abandonados, postes, caixas médicas, barricadas, cristais e uma estação de rádio com torre. As formas e detalhes são produzidos pelo código; não são recursos extraídos de outros jogos.

Os personagens são silhuetas pixeladas, construídas em camadas. O protagonista possui variações de pele, cabelo e roupa; Lia usa um casaco claro com marca médica. Errantes, Corredores, Estilhaçados e o Sussurrante têm proporções e acentos próprios. Respiração discreta, pulso dos marcadores, luzes e barras contextuais complementam a leitura tática.

`PortraitRenderer` cria retratos facetados de Lia, Davi e do protagonista e uma representação abstrata para narração. Expressões tensas alteram as sobrancelhas. São retratos provisórios com composição original.

A opção de reduzir movimento mantém os marcadores e silhuetas estáveis. O áudio inicial é síntese própria: um drone ambiente, pulsação de combate e três efeitos curtos. Não existem faixas comerciais incorporadas.

## Limitações atuais

O cenário é geometria 2D em projeção isométrica, sem engine voxel 3D, iluminação por física, sombras calculadas ou sprites desenhados à mão. Os retratos e a animação são provisórios. A sobreposição depende da ordem de profundidade definida pela tela. Os sons não constituem uma trilha musical completa.

## Próximos passos

Substituir gradualmente as silhuetas por uma atlas original, preservando escala, paleta e leitura. Produzir retratos com mais expressões, animações completas de combate e efeitos próprios para cada habilidade. Acrescentar névoa por camadas, composição de iluminação, ambientes inéditos e transições musicais narrativas. Validar contraste, legibilidade da grade e desempenho antes de aumentar a densidade visual.
