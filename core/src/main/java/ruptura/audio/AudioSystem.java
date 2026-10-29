package ruptura.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.utils.Disposable;
import java.util.HashMap;
import java.util.Map;

/** Small offline audio mixer. Missing files or an unavailable device never stop gameplay. */
public final class AudioSystem implements Disposable {
    private final Map<String, Sound> sounds = new HashMap<>();
    private long ambientHandle = -1, combatHandle = -1;
    private float combatMix;
    private boolean started, disabled;

    public AudioSystem() {
        for (String id : new String[]{"ambient", "combat", "click", "impact", "weakness"}) {
            try {
                if (Gdx.audio != null && Gdx.files.internal("audio/" + id + ".wav").exists())
                    sounds.put(id, Gdx.audio.newSound(Gdx.files.internal("audio/" + id + ".wav")));
            } catch (RuntimeException unavailable) {
                if (Gdx.app != null) Gdx.app.log("Áudio", "Recurso indisponível: " + id);
            }
        }
    }

    /** Smoothly crossfades two synthesized loops; master volume is read every frame. */
    public void update(float delta, float volume, boolean combat) {
        if (disabled) return;
        float master = clamp(volume);
        try {
            if (!started) {
                started = true;
                if (sounds.containsKey("ambient")) ambientHandle = sounds.get("ambient").loop(0);
                if (sounds.containsKey("combat")) combatHandle = sounds.get("combat").loop(0);
            }
            float dt = Math.max(0, Math.min(.2f, delta));
            combatMix += ((combat ? 1f : 0f) - combatMix) * (1f - (float)Math.exp(-dt * 2.2f));
            if (ambientHandle != -1) sounds.get("ambient").setVolume(ambientHandle, master * .36f * (1-combatMix*.8f));
            if (combatHandle != -1) sounds.get("combat").setVolume(combatHandle, master * .32f * combatMix);
        } catch (RuntimeException unavailable) {
            disabled = true;
            if (Gdx.app != null) Gdx.app.log("Áudio", "Dispositivo indisponível; o jogo continuará sem áudio.");
        }
    }

    public void play(String id, float volume) {
        if (disabled || id == null) return;
        String key = switch (id) {
            case "attack", "hit", "damage" -> "impact";
            case "skill", "break", "victory" -> "weakness";
            case "confirm", "select", "step" -> "click";
            default -> id;
        };
        Sound sound = sounds.get(key);
        if (sound != null) {
            try { sound.play(clamp(volume) * .7f); }
            catch (RuntimeException unavailable) { /* Keep input and combat responsive if audio fails. */ }
        }
    }

    private static float clamp(float value) { return Float.isFinite(value) ? Math.max(0, Math.min(1,value)) : 0; }

    @Override public void dispose() {
        for (Sound sound : sounds.values()) {
            try { sound.dispose(); } catch (RuntimeException ignored) { }
        }
        sounds.clear();
        disabled = true;
    }
}
