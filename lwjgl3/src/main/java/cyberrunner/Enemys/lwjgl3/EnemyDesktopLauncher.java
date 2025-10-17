package cyberrunner.Enemys.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import cyberrunner.Enemys.EnemySandboxApp;

public class EnemyDesktopLauncher {
    public static void main (String[] args) {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
        config.setTitle("Enemy Sandbox");
        config.setWindowedMode(1280, 720);
        config.setForegroundFPS(60);
        config.useVsync(true);
        config.setResizable(true);

        // Launch the sandbox app that spawns a Goblin + Hobgoblin
        new Lwjgl3Application(new EnemySandboxApp(), config);
    }
}
