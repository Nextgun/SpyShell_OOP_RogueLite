
//************************************
//Program Name: GameLauncher.java
//Developer: Hector Marrero-Colominas
//Date Created: 11/17/2025
//Version: 2.0
//Purpose: Entry point for the game; manages screen transitions using LibGDX Game class
//************************************

package cyberrunner.game;

import com.badlogic.gdx.Game;

public class GameLauncher extends Game {

    @Override
    public void create() {
    	// Start at main menu
    	setScreen(new MainMenuScreen(this)); 
    }
} // End of class GameLauncher