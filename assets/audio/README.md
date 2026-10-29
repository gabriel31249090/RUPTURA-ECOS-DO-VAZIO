# Áudio provisório original

Todos os arquivos WAV desta pasta são sintetizados matematicamente por `tools/GenerateAudio.java`. Não contêm amostras, gravações ou músicas de terceiros. Podem ser usados e modificados com o código deste projeto.

- `ambient.wav`: drone de exploração em ciclo de seis segundos.
- `combat.wav`: pulsação tensa em ciclo de quatro segundos.
- `click.wav`: confirmação de interface.
- `impact.wav`: impacto percussivo.
- `weakness.wav`: acorde de ressonância.

Formato: PCM mono, 16 bits, 22.050 Hz. O mixer aplica o volume geral e transições suaves entre exploração e combate. Ausência de arquivo ou dispositivo não impede a execução do jogo.

Para regenerar, na raiz do projeto com Java 21 ou posterior:

```powershell
java tools/GenerateAudio.java
```

São recursos provisórios. Uma trilha narrativa completa, variações de passos e espacialização estão planejadas para o polimento.
