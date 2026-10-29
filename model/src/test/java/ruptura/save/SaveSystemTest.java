package ruptura.save;

import ruptura.story.DialogueScript;
import ruptura.story.GameSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

class SaveSystemTest {
    @TempDir Path temporary;

    @Test void roundTripPreservesProfileQuestInventoryRelationshipsAndHistory() {
        SaveSystem saves = new SaveSystem(temporary);
        GameSession original = new GameSession();
        original.heroName = "João Çâo";
        original.skin = 2;
        original.playerX = 11.25f;
        original.playerY = 8.5f;
        original.seed = 987654321;
        original.crateOpened = true;
        original.specialization = "Médico de campo";
        DialogueScript.applyChoice(original,DialogueScript.choices("intro").get(0));
        DialogueScript.applyChoice(original,DialogueScript.choices("survivor").get(0));
        original.rewardBattle(false);
        original.rewardBattle(true);
        original.completeMission();
        saves.save(original);
        GameSession loaded = saves.load().orElseThrow();
        assertEquals(original.heroName,loaded.heroName);
        assertEquals(original.specialization,loaded.specialization);
        assertEquals(original.seed,loaded.seed);
        assertEquals(original.playerX,loaded.playerX);
        assertEquals(original.medkits,loaded.medkits);
        assertEquals(original.affinity,loaded.affinity);
        assertEquals(original.choices,loaded.choices);
        assertEquals(original.dialogueHistory,loaded.dialogueHistory);
        assertEquals(original.level,loaded.level);
        assertEquals(original.questStage,loaded.questStage);
        assertTrue(loaded.completed);
    }

    @Test void corruptPrimaryRecoversPreviousValidSave() throws Exception {
        SaveSystem saves = new SaveSystem(temporary);
        GameSession session = new GameSession();
        saves.save(session);
        session.heroName = "Depois";
        saves.save(session);
        Files.writeString(temporary.resolve("slot-1.properties"),"isto não é um salvamento",StandardCharsets.UTF_8);
        assertEquals("Alex",saves.load().orElseThrow().heroName);
        assertTrue(saves.lastError().contains("cópia anterior"));
    }

    @Test void futureOrMalformedFilesFailWithoutCrashing() throws Exception {
        SaveSystem saves = new SaveSystem(temporary);
        Files.writeString(temporary.resolve("slot-1.properties"),"version=99\nheroName=Futuro");
        assertTrue(saves.load().isEmpty());
        assertTrue(saves.lastError().contains("incompatível"));
        Files.writeString(temporary.resolve("slot-1.properties"),"version=abc");
        assertTrue(saves.load().isEmpty());
    }

    @Test void invalidSaveNeverOverwritesAValidOne() {
        SaveSystem saves = new SaveSystem(temporary);
        GameSession session = new GameSession();
        saves.save(session);
        session.playerX = Float.NaN;
        assertThrows(IllegalStateException.class,()->saves.save(session));
        assertEquals(2,saves.load().orElseThrow().playerX);
    }

    @Test void interruptedTemporaryFileIsIgnored() throws Exception {
        SaveSystem saves = new SaveSystem(temporary);
        saves.save(new GameSession());
        Files.writeString(temporary.resolve("slot-1.properties.tmp"),"version=");
        assertEquals("Alex",saves.load().orElseThrow().heroName);
    }

    @Test void preferencesRoundTripAndOutOfRangeValuesRestoreDefaults() throws Exception {
        SaveSystem saves = new SaveSystem(temporary);
        GameSettings settings = new GameSettings();
        settings.reducedMotion = true;
        settings.textSpeed = 70;
        settings.volume = .2f;
        settings.difficultyIndex = 3;
        saves.saveSettings(settings);
        var loaded = saves.loadSettings();
        assertTrue(loaded.reducedMotion);
        assertEquals(70,loaded.textSpeed);
        assertEquals(.2f,loaded.volume);
        assertEquals(3,loaded.difficultyIndex);
        Files.writeString(temporary.resolve("settings.properties"),"version=1\nvolume=NaN");
        assertEquals(.4f,saves.loadSettings().volume);
        assertFalse(saves.lastError().isBlank());
    }
}
