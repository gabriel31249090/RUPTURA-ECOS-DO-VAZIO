package ruptura.story;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Progressão da missão e decisões; independente da janela e da renderização. */
public final class GameSession {
    public String heroName = "Alex";
    public String specialization = "Ressonante";
    public String pronouns = "elu/delu";
    public String origin = "Mensageiro";
    public int skin, hair, outfit, accessory;
    public int difficultyIndex = 1, questStage, affinity, medkits = 3, scrap, level = 1, xp;
    public long seed = 20410517L;
    public float playerX = 2, playerY = 3;
    public boolean crateOpened, firstBattleWon, bossWon, survivorHelped, completed;
    public final Map<String, String> choices = new LinkedHashMap<>();
    public final List<String> dialogueHistory = new ArrayList<>();

    public String objective() {
        if (completed) return "O Primeiro Eco concluído · a transmissão continua";
        if (bossWon) return "Ative o rádio e ouça a transmissão de Orfeu";
        if (!firstBattleWon) return "Siga Lia pela rua e atravesse o primeiro bloqueio";
        if (!choices.containsKey("survivor")) return "Investigue a voz entre os escombros";
        return "Alcance a estação de rádio e enfrente o Sussurrante";
    }

    /** Recompensas são idempotentes: carregar/repetir eventos não duplica recursos. */
    public void rewardBattle(boolean boss) {
        if (boss ? bossWon : firstBattleWon) return;
        if (boss) {
            bossWon = true;
            questStage = Math.max(questStage, 4);
            xp += 100;
            scrap += 6;
        } else {
            firstBattleWon = true;
            questStage = Math.max(questStage, 2);
            xp += 50;
            scrap += 3;
        }
        level = 1 + xp / 100;
    }

    public void choose(String key, String value) {
        if (key == null || key.isBlank() || value == null || value.isBlank())
            throw new IllegalArgumentException("Escolha narrativa inválida");
        if (choices.containsKey(key)) return;
        choices.put(key, value);
        if ("survivor".equals(key)) {
            survivorHelped = "rescue".equals(value);
            questStage = Math.max(questStage, 3);
            if (survivorHelped) {
                affinity += 2;
                medkits += 1; // A pessoa resgatada entrega o estojo do posto de saúde.
            } else {
                scrap += 5;
                affinity -= 1;
            }
        }
    }

    public void completeMission() {
        if (!bossWon) throw new IllegalStateException("O caminho do rádio ainda está bloqueado");
        completed = true;
        questStage = 5;
    }
}
