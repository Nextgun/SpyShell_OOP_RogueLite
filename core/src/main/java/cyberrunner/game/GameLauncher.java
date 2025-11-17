
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

    // stage management
    private enum MenuState { MAIN, OPTIONS }
    private MenuState currentState = MenuState.MAIN;
    
    // keybind settings
    private int moveLeftKey = Input.Keys.A; 
    private int moveRightKey = Input.Keys.D; 
    private int moveUpKey = Input.Keys.W; 
    private int moveDownKey = Input.Keys.S; 
    
    // overrides the create method, called once when the application starts
    // overriding is when a subclass provides a specific implementation for a method that
    // is already defined in its parent class
    @Override
    public void create() {
        // Create and initialize the actual game/sandbox
        enemySandboxApp = new EnemySandboxApp();
        enemySandboxApp.create();
    }

     // add a click listener to the options button
        optionsButton.addListener(new ChangeListener() {
        	// override the changed method, called when the button is clicked
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	// print a message to the console when clicked (used for debugging)
                System.out.println("Options clicked!");
                showOptionsMenu();
                // you could swap some internal state here to go to gameplay
            }
        }); // close the listener
        
        // add a click listener to the exit button
        exitButton.addListener(new ChangeListener() {
        	// override the changed method for the exit button
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	// close the application when the exit button is clicked
                Gdx.app.exit();
            }
        }); // close the listener
    } // end of showMainMenu
    
    private void showOptionsMenu() {
    	currentState = MenuState.OPTIONS;
    	stage.clear();
    	
        // create a new Table to organize UI elements in a grid layout
    	Table table = new Table();
    	
        // make the table fill the entire stage (centered layout)
    	table.setFillParent(true);
    	
    	stage.addActor(table);
    	
    	// create a text label displaying the title using the loaded skin's style
        Label title = new Label("OPTIONS", skin);
        title.setFontScale(2f);
        table.add(title).padBottom(40).row();
    	
        // change keybind settings
        Label controlsHeader = new Label("CONTROLS", skin);
        controlsHeader.setFontScale(1.5f);
        table.add(controlsHeader).colspan(2).padTop(20).padBottom(10).row();
    } // end of options
    
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