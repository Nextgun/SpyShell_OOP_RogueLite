
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
    private KeybindManager keybindManager;

    @Override
    public void create() {
        // Create the keybind manager first
        keybindManager = new KeybindManager();
        
        // Then create the main menu screen with it
        setScreen(new MainMenuScreen(this, keybindManager));
    }
    
    public KeybindManager getKeybindManager() {
        return keybindManager;
    }
}