package cyberrunner.Enemys.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import cyberrunner.Enemys.EnemySandboxApp;

public class EnemyDesktopLauncher {
    public static void main(String[] args) {
        Lwjgl3ApplicationConfiguration cfg = new Lwjgl3ApplicationConfiguration();
        cfg.setTitle("Enemy Sandbox");
        cfg.setWindowedMode(1280, 720);
        cfg.useVsync(true);
        cfg.setForegroundFPS(60);

        new Lwjgl3Application(new EnemySandboxApp(), cfg);
    }
}
