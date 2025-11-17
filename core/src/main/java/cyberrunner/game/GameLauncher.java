
// Author: Martin Taylor
// File: GameLauncher.java
// Date: 2025-11-04
// Description: Core application entry point for all platforms.
//              Delegates lifecycle calls to EnemySandboxApp so launchers
//              only need to construct GameLauncher.

package cyberrunner.game;


// imports the base class that provides lifecycle methods for a libGDX application
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

 // override the render method, called every frame (~60 fps)
    @Override
    public void render() {
        // Delegate rendering & game loop to EnemySandboxApp
        if (enemySandboxApp != null) {
            enemySandboxApp.render();
        }
    }

    // override the resize method, called when the window is resized
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

    // override the dispose method, called when the application closes
    @Override
    public void dispose() {
        if (enemySandboxApp != null) {
            enemySandboxApp.dispose();
        }
    }
}