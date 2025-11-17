// Author: Hector Marrero-Colominas
// Co-Developer: Martin Taylor
// File: Lwjgl3Launcher.java
// Description: Desktop launcher. Starts GameLauncher, which delegates to EnemySandboxApp.

package cyberrunner.game.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;

import cyberrunner.game.GameLauncher;

public class Lwjgl3Launcher {

    public static void main(String[] args) {
        createApplication();
    }

    private static Lwjgl3Application createApplication() {
        Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();

        config.setTitle("SpyShell OOP RogueLite");
        config.setWindowedMode(1280, 720);
        config.useVsync(true);
        config.setForegroundFPS(60);

        // Entry: GameLauncher -> EnemySandboxApp
        return new Lwjgl3Application(new GameLauncher(), config);
    }
} // End of class Lwjgl3Launcher
