package ruptura.save;

/** Preferências externas ao progresso da campanha. */
public final class GameSettings {
    public boolean reducedMotion;
    public boolean fullscreen;
    public float volume = .4f;
    public float textSpeed = 40;
    public int difficultyIndex = 1;
    public int width = 1280, height = 800;

    public void validate() {
        if (!Float.isFinite(volume) || volume < 0 || volume > 1
                || !Float.isFinite(textSpeed) || textSpeed < 5 || textSpeed > 200
                || difficultyIndex < 0 || difficultyIndex > 3
                || width < 800 || height < 600 || width > 7680 || height > 4320)
            throw new IllegalArgumentException("Configurações fora dos limites");
    }
}
