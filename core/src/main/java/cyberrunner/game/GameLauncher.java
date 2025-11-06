// Author: Martin Taylor
// File: GameLauncher.java
// Date: 2025-11-04
// Description: Core application entry point for all platforms.
//              Delegates lifecycle calls to EnemySandboxApp so launchers
//              only need to construct GameLauncher.

package cyberrunner.game;

import com.badlogic.gdx.ApplicationAdapter;
import cyberrunner.Enemys.EnemySandboxApp;

public class GameLauncher extends ApplicationAdapter {

    private EnemySandboxApp enemySandboxApp;

    @Override
    public void create() {
        // Create and initialize the actual game/sandbox
        enemySandboxApp = new EnemySandboxApp();
        enemySandboxApp.create();
    }

    @Override
    public void render() {
        // Delegate rendering & game loop to EnemySandboxApp
        if (enemySandboxApp != null) {
            enemySandboxApp.render();
        }
    }

    @Override
    public void resize(int width, int height) {
        if (enemySandboxApp != null) {
            enemySandboxApp.resize(width, height);
        }
    }

    @Override
    public void pause() {
        if (enemySandboxApp != null) {
            enemySandboxApp.pause();
        }
    }

    @Override
    public void resume() {
        if (enemySandboxApp != null) {
            enemySandboxApp.resume();
        }
    }

    @Override
    public void dispose() {
        if (enemySandboxApp != null) {
            enemySandboxApp.dispose();
        }
    }
}
