package cyberrunner.game;

import com.badlogic.gdx.Game;

public class GameLauncher extends Game {
    @Override
    public void create() {
        // Start with the menu screen
        setScreen(new MenuScreen(this));
    }
}