package ruptura.story;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import ruptura.combat.ActionType;
import ruptura.combat.CombatSystem;
import ruptura.combat.CombatUnit;
import ruptura.combat.Difficulty;
import ruptura.save.SaveSystem;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

/** The campaign contract across narrative decisions, rewards, battle buffs and persistence. */
final class CampaignFlowTest {
    @TempDir Path directory;

    @Test void introRescueBothRewardsEndingAndSaveRoundTripPreserveConsequences() {
        GameSession session = new GameSession();
        session.heroName = "Íris";
        assertTrue(DialogueScript.scene("intro", session).stream().anyMatch(line -> line.text().contains("Íris")));
        DialogueChoice empathy = DialogueScript.choices("intro").stream()
                .filter(c -> c.value().equals("empathy")).findFirst().orElseThrow();
        DialogueScript.applyChoice(session, empathy);
        DialogueScript.applyChoice(session, empathy);
        assertEquals(2, session.affinity);
        assertEquals("empathy", session.choices.get("first_response"));
        assertEquals(1, session.dialogueHistory.size());
        assertFalse(DialogueScript.scene("intro_empathy", session).isEmpty());

        session.rewardBattle(false);
        session.rewardBattle(false);
        assertTrue(session.firstBattleWon);
        assertEquals(50, session.xp);
        assertEquals(3, session.scrap);
        assertFalse(DialogueScript.scene("survivor", session).isEmpty());
        DialogueChoice rescue = DialogueScript.choices("survivor").stream()
                .filter(c -> c.value().equals("rescue")).findFirst().orElseThrow();
        DialogueScript.applyChoice(session, rescue);
        DialogueScript.applyChoice(session, rescue);
        assertTrue(session.survivorHelped);
        assertEquals(4, session.affinity);
        assertEquals(4, session.medkits);
        assertEquals(2, session.dialogueHistory.size());
        assertTrue(DialogueScript.scene("survivorRescue", session).stream().anyMatch(line -> line.speaker().equals("ÍCARO")));

        // Integration uses public setters; the battle applies the campaign's medical bonus.
        CombatSystem battle = new CombatSystem(Difficulty.NORMAL, true, session.heroName, session.specialization);
        battle.setAffinity(session.affinity);
        battle.setPotions(session.medkits);
        assertEquals(4, battle.potions());
        battle.perform(ActionType.END_TURN, 0, 0);
        for (int i = 0; i < 10 && battle.phase() != CombatSystem.Phase.PLAYER; i++) battle.update(1);
        assertEquals("lia", battle.current().id);
        CombatUnit hero = battle.units().getFirst(); hero.hp = 20;
        assertTrue(battle.perform(ActionType.SKILL, hero.x, hero.y));
        battle.update(1);
        assertEquals(59, hero.hp); // 30 + affinity bonus capped at 9.

        session.rewardBattle(true);
        session.rewardBattle(true);
        assertTrue(session.bossWon);
        assertEquals(150, session.xp);
        assertEquals(2, session.level);
        assertEquals(9, session.scrap);
        var ending = DialogueScript.scene("ending", session);
        assertTrue(ending.stream().anyMatch(line -> line.speaker().equals("ÍCARO")));
        assertTrue(ending.stream().anyMatch(line -> line.text().contains("Eu fico")));
        assertFalse(ending.stream().anyMatch(line -> line.text().contains("Depois... depois a gente conversa")));
        assertFalse(ending.stream().anyMatch(line -> line.text().contains("Agora eu vou voltar")));
        session.completeMission();

        SaveSystem saves = new SaveSystem(directory);
        saves.save(session);
        GameSession restored = saves.load().orElseThrow();
        assertTrue(restored.completed);
        assertEquals(5, restored.questStage);
        assertEquals("Íris", restored.heroName);
        assertEquals(session.choices, restored.choices);
        assertEquals(session.dialogueHistory, restored.dialogueHistory);
        assertEquals(4, restored.affinity);
        assertEquals(4, restored.medkits);
        assertEquals(150, restored.xp);
        assertEquals(DialogueScript.scene("ending", session), DialogueScript.scene("ending", restored));
    }

    @Test void pragmaticSuppliesPathProducesDifferentEndingAndActualResourceTradeoff() {
        GameSession session = new GameSession();
        DialogueScript.applyChoice(session, DialogueScript.choices("intro").get(1));
        session.rewardBattle(false);
        DialogueScript.applyChoice(session, DialogueScript.choices("survivor").get(1));
        session.rewardBattle(true);
        session.completeMission();
        assertFalse(session.survivorHelped);
        assertEquals(-1, session.affinity);
        assertEquals(3, session.medkits);
        assertEquals(14, session.scrap);
        var ending = DialogueScript.scene("ending", session);
        assertTrue(ending.stream().anyMatch(line -> line.text().contains("Agora eu vou voltar")));
        assertTrue(ending.stream().anyMatch(line -> line.text().contains("Depois... depois a gente conversa")));
        assertFalse(ending.stream().anyMatch(line -> line.speaker().equals("ÍCARO")));
        assertFalse(ending.stream().anyMatch(line -> line.text().contains("Eu fico")));
    }
}
