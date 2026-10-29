package ruptura.desktop;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import ruptura.RupturaGame;

/** The desktop owns its window; tools and launchers will be separate processes. */
public final class DesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("RUPTURA: ECOS DO VAZIO");
        config.setWindowedMode(1280, 800);
        config.setWindowSizeLimits(960, 600, -1, -1);
        config.useVsync(true);
        config.setForegroundFPS(60);
        config.setIdleFPS(30);
        new Lwjgl3Application(new RupturaGame(args), config);
    }
}
