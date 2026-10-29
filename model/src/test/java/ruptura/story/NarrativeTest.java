package ruptura.story;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class NarrativeTest {
    @Test void choiceChangesRelationshipOnceAndSurvivorChangesTheEnding() {
        GameSession session = new GameSession();
        session.heroName = "Íris";
        var intro = DialogueScript.choices("intro").get(0);
        DialogueScript.applyChoice(session,intro);
        DialogueScript.applyChoice(session,intro);
        assertEquals(2,session.affinity);
        assertEquals(1,session.dialogueHistory.size());
        assertEquals("empathy",session.choices.get("first_response"));
        DialogueScript.applyChoice(session,DialogueScript.choices("survivor").get(0));
        assertTrue(session.survivorHelped);
        assertEquals(4,session.affinity);
        assertEquals(4,session.medkits);
        var ending = DialogueScript.scene("ending",session);
        assertTrue(ending.stream().anyMatch(line->line.speaker().equals("ÍCARO")));
        assertTrue(ending.stream().anyMatch(line->line.text().contains("Eu fico")));
        assertFalse(ending.stream().anyMatch(line->line.text().contains("voltar para aquela rua")));
        assertTrue(ending.stream().anyMatch(line->line.text().contains("Íris")));
    }

    @Test void pragmaticDecisionHasDifferentResourcesAndDialogue() {
        GameSession session = new GameSession();
        DialogueScript.applyChoice(session,DialogueScript.choices("survivor").get(1));
        assertFalse(session.survivorHelped);
        assertEquals(-1,session.affinity);
        assertEquals(5,session.scrap);
        assertEquals(3,session.medkits);
        var ending = DialogueScript.scene("ending",session);
        assertFalse(ending.stream().anyMatch(line->line.speaker().equals("ÍCARO")));
        assertTrue(ending.stream().anyMatch(line->line.text().contains("voltar para aquela rua")));
    }

    @Test void battleRewardsAreIdempotentAndMissionCannotFinishEarly() {
        GameSession session = new GameSession();
        assertThrows(IllegalStateException.class,session::completeMission);
        session.rewardBattle(false);
        session.rewardBattle(false);
        assertEquals(50,session.xp);
        session.rewardBattle(true);
        session.rewardBattle(true);
        assertEquals(150,session.xp);
        assertEquals(2,session.level);
        session.completeMission();
        assertTrue(session.completed);
        assertEquals(5,session.questStage);
    }

    @Test void allScenesAndChoiceConsequencesHaveExternalData() {
        for (String id : new String[]{"intro","lia","crate","survivor","ending","firstVictory",
                "intro_empathy","intro_pragmatic","intro_curious","survivorRescue","survivorSupplies","radio","combatIntro"})
            assertFalse(DialogueScript.scene(id,"Alex").isEmpty(),id);
        assertEquals(3,DialogueScript.choices("intro").size());
        assertEquals(2,DialogueScript.choices("survivor").size());
    }
}
