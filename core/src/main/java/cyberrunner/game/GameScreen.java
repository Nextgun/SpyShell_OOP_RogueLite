//************************************
//Program Name: GameScreen.java
//Developer: Hector Marrero-Colominas
//Co-Developer: Ayesha Khan
//Date Created: 11/17/2025
//Version: 1.0
//Purpose: Runs the main game loop and handles gameplay rendering
//************************************

package cyberrunner.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input; // For ESC key handling

import cyberrunner.Enemys.EnemySandboxApp;

public class GameScreen implements Screen {
    private final Game game;
    private EnemySandboxApp enemyApp;

    public GameScreen(Game game) {
        this.game = game;
        enemyApp = new EnemySandboxApp();
        enemyApp.create();
    }

    @Override
    public void render(float delta) {
        enemyApp.render();
        
        // Check for ESC to pause
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            game.setScreen(new PauseScreen(game, this));
        }
        

        // Check if player HP is zero
         if (enemyApp.getPlayerHealth() <= 0) {
             game.setScreen(new MainMenuScreen(game)); // Go back to main menu
         }

    }

	@Override
	public void resize(int width, int height) {
	    // If EnemySandboxApp needs resize, delegate:
	    enemyApp.resize(width, height);
	}
	
	@Override public void show() { }
    @Override public void pause() { enemyApp.pause(); }
    @Override public void resume() { enemyApp.resume(); }
    @Override public void hide() { }
    @Override public void dispose() {
        enemyApp.dispose();
    }

} // End of class GameScreen