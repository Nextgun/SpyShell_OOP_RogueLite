// declares package namespace for this class
package cyberrunner.Enemys;

// imports the base class that provides lifecycle methods for a libGDX application
import com.badlogic.gdx.ApplicationAdapter;

// imports the main libGDX utility class that provides access to graphics, input, files, etc
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;

// imports openGL constants (i.e. GL_COLOR_BUFFER_BIT used for clearing the screen)
import com.badlogic.gdx.graphics.GL20;

import com.badlogic.gdx.scenes.scene2d.Stage;

//imports scene2d for ui elements
import com.badlogic.gdx.scenes.scene2d.ui.*;

// imports ChangeListener for handling UI events like button clicks
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

// imports FitViewport, which maintains aspect ratio while scaling to fit the screen
import com.badlogic.gdx.utils.viewport.FitViewport;

// imports Actor, the base class for all UI elements in scene2d
import com.badlogic.gdx.scenes.scene2d.Actor;

/**
* X-Runner
* text text text
* text text text
* text text text 
* text text text
* text text text 
* text text text 
* text text text
*
* @author Ayesha Khan
* @version 1.0
* @since 2025-11-09
*/

// declares the main class, extending ApplicationAdapter to get lifecycle methods
public class GameLauncher extends ApplicationAdapter {
/**
* This method is used to declare, initialize, and create user interface (UI) elements
* that will be displayed throughout the execution of X-Runner. It utilizes an
* enum MenuState to manage and display various UI screens. It includes a main menu,
* options menu, pause menu, game over menu, and "you won" menu. 
* The main menu is the initial screen that the user sees upon the execution of the program.
* From here, the user can instigate the gameplay loop through clicking the "Start" menu 
* The options menu provides the option for the user to re-bind the controls of the game. 
* @param numA This is the first paramter to addNum method
* @param numB This is the second parameter to addNum
method
* @return int This returns sum of numA and numB.
*/
	
	// declare a field to hold the Stage (the container for all UI elements)
    private Stage stage;
    
    // declares a field to hold the UI skin (visual styling)
    private Skin skin;

    // stage management
    private enum MenuState { MAIN, OPTIONS, PAUSE, GAME_OVER, YOU_WON }
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
    	
    	// create a new Stage w/ a viewport of 640x480 pixels that maintains aspect ratio
        stage = new Stage(new FitViewport(640, 480));
        
        // routes all input events (touches, clicks, keys) to the stage so UI can respond
        Gdx.input.setInputProcessor(stage);
        
        // load the UI skin definition from a JSON file that defines button styles fonts etc
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        showMainMenu();
    } // end of create()
    
    /**
    * This is the showMainMenu() method. 
    * It sets the state of the game to the MAIN enum, which will thus display the main menu. 
    * The main menu is the initial screen that the user sees upon the execution of the program.
    * Clicking "START" will begin the gameplay by calling the gameplay loop function.
    * Clicking "OPTIONS" will switch the state to the OPTIONS menu, where the user can adjust settings. 
    * Clicking "EXIT" will terminate the execution of the program..
    * @param args Unused.
    * @return Nothing.
    * @exception IOException On input error.
    * @see IOException
    */
    private void showMainMenu() {
    	
    	currentState = MenuState.MAIN;
    	stage.clear();
    	
        // create a new Table to organize UI elements in a grid layout
        Table table = new Table();
    	
        // make the table fill the entire stage (centered layout)
        table.setFillParent(true);
    	
        
        // add the table to the stage so it will be rendered and receive input
        stage.addActor(table);

        // create a text label displaying the title using the loaded skin's style
        Label title = new Label("X-RUNNER", skin);
        
        // make the title text x2 as large as the default size 
        title.setFontScale(2f);
        
        // add the title to the table w/ 40 pixels of padding below it, then moves to the next row
        table.add(title).padBottom(40).row();

        // create a button displaying "START GAME", "OPTIONS", and "EXIT" text 
        TextButton startButton = new TextButton("START GAME", skin);
        TextButton optionsButton = new TextButton("OPTIONS", skin);
        TextButton exitButton = new TextButton("EXIT", skin);

        // make each button's text 1.5x larger than default
        startButton.getLabel().setFontScale(1.5f);
        optionsButton.getLabel().setFontScale(1.5f);
        exitButton.getLabel().setFontScale(1.5f);

        // add each button to table with width of 250 pixels, 10 pixels padding on all sides
        table.add(startButton).width(250).pad(10).row();
        table.add(optionsButton).width(250).pad(10).row();
        table.add(exitButton).width(250).pad(10).row();

        // add a click listener to the start button
        startButton.addListener(new ChangeListener() {
        	// override the changed method, called when the button is clicked
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	// print a message to the console when clicked (used for debugging)
                System.out.println("Start game clicked!");
                EnemySandboxApp gameplay = new EnemySandboxApp();
                gameplay.create();
                Gdx.input.setInputProcessor(null);
                // you could swap some internal state here to go to gameplay
            }
        }); // close the listener

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
    } // end of showMainMenu()
    
    /**
    * This is the showOptionsMenu method.
    * @param args Unused.
    * @return Nothing.
    * @exception IOException On input error.
    * @see IOException
    */
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
    } // end of showOptionsMenu()
    
    /**
    * This is the showPauseMenu method.
    * @param args Unused.
    * @return Nothing.
    * @exception IOException On input error.
    * @see IOException
    */
    private void showPauseMenu() {
        currentState = MenuState.PAUSE;
        stage.clear();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("PAUSED", skin);
        title.setFontScale(2f);
        table.add(title).padBottom(40).row();

        TextButton resumeButton = new TextButton("RESUME", skin);
        TextButton restartButton = new TextButton("RESTART GAME", skin);
        TextButton mainMenuButton = new TextButton("MAIN MENU", skin);

        table.add(resumeButton).width(250).pad(10).row();
        table.add(restartButton).width(250).pad(10).row();
        table.add(mainMenuButton).width(250).pad(10).row();

        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Resuming game...");
                // UNPAUSE GAME LOGIC CODE GOES HERE
                // NEED TO CHECK WITH MARTIN'S CODE TO INTEGRATE
                // THE RETURN TO THE GAMEPLAY LOOP
            }
        });

        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Restart clicked!");
                // CALL THE ENEMYSANDBOXAPP TO RESTART THE GAMELOOP VARIABLES ETC ALL BACK TO THE BEGINNING
            }
        });

        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showMainMenu();
            }
        });
    } // end of showPauseMenu()

    /**
    * This is the showGameOverMenu method.
    * @param args Unused.
    * @return Nothing.
    * @exception IOException On input error.
    * @see IOException
    */
    private void showGameOverMenu() {
        currentState = MenuState.GAME_OVER;
        stage.clear();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("GAME OVER", skin);
        title.setFontScale(2f);
        table.add(title).padBottom(40).row();

        TextButton restartButton = new TextButton("RESTART", skin);
        TextButton mainMenuButton = new TextButton("MAIN MENU", skin);
        TextButton exitButton = new TextButton("EXIT", skin);

        table.add(restartButton).width(250).pad(10).row();
        table.add(mainMenuButton).width(250).pad(10).row();
        table.add(exitButton).width(250).pad(10).row();

        restartButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Restarting game...");
                // RESTART HERE
            }
        });

        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showMainMenu();
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    } // end of showGameOverMenu
    
    /**
    * This is the main method which makes use of addNum method.
    * @param args Unused.
    * @return Nothing.
    * @exception IOException On input error.
    * @see IOException
    */
    private void showYouWonMenu() {
        currentState = MenuState.YOU_WON;
        stage.clear();

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        Label title = new Label("YOU WON!", skin);
        title.setFontScale(2f);
        table.add(title).padBottom(40).row();

        TextButton playAgainButton = new TextButton("PLAY AGAIN", skin);
        TextButton mainMenuButton = new TextButton("MAIN MENU", skin);
        TextButton exitButton = new TextButton("EXIT", skin);

        table.add(playAgainButton).width(250).pad(10).row();
        table.add(mainMenuButton).width(250).pad(10).row();
        table.add(exitButton).width(250).pad(10).row();

        playAgainButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Play again clicked!");
                // RESTART BACK TO GAMEPLAY LOOP 
                // MAYBE VIEW SCORES OPTION SHOULD BE INCLUDED IN THIS MENU?
            }
        });

        mainMenuButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                showMainMenu();
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
            }
        });
    } // end of showYouWonMenu()
  
    /**
    * This is the main method which makes use of addNum method.
    * @param args Unused.
    * @return Nothing.
    * @exception IOException On input error.
    * @see IOException
    */
    // override the render method, called every frame (~60 fps)
    @Override
    public void render() {
    	// set the clear color to black (rgb = 0, alpha = 1)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        
        // clear the screen with the clear color (black) before drawing the next frame
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // update the stage and all its actors based on the time elapsed since the last frame
        stage.act(Gdx.graphics.getDeltaTime());
        
        // render the stage and all its UI elements to the screen
        stage.draw();
    } // end of render()

    /**
    * This is the main method which makes use of addNum method.
    * @param args Unused.
    * @return Nothing.
    * @exception IOException On input error.
    * @see IOException
    */
    // override the resize method, called when the window is resized
    @Override
    public void resize(int width, int height) {
    	// update viewport to the new window size while maintaining aspect ratio (true = center camera)
        stage.getViewport().update(width, height, true);
    } // end of resize()

    /**
    * This is the main method which makes use of addNum method.
    * @param args Unused.
    * @return Nothing.
    * @exception IOException On input error.
    * @see IOException
    */
    // override the dispose method, called when the application closes
    @Override
    public void dispose() {
    	// free memory used by the stage
        stage.dispose();
        // free memory used by the skin (textures, fonts, etc.)
        skin.dispose();
    } // end of dispose()
} // end of class



// 11 4 2025 NOTES FROM AYESHA: 
// THE BELOW COMMENTED-OUT CODE IS FOR THE REBINDING OPTION IN THE OPTIONS MENU.
// NEED TO CONFIGURE WITH MARTIN'S CODE TO INTEGRATE INTO THE REST OF THE GAME. 
// ADDITIONAL THINGS NEEDED TO DO:
// the gameplay class seems to be EnemySandboxApp. 
// need to put in function calls to these menu options within the main gameplay loop.
// i.e. pressing the key ESC --> we call showPauseMenu
// it will look something like this: 
// // Inside EnemySandboxApp when the player presses ESC:
// if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
//    gameLauncher.showPauseMenu();
//}
// WHEN PLAYER HEALTH = 0 -> CALL SHOWGAMEOVERMENU
// WHEN WIN CONDITION MET -> CALL SHOWYOUWONMENU
// 
// the critical thing to do next is to make these transitions between the menu screens and gameplay.
// need to understand martin's code to do that.
// will comment this class as per demir's standards. 
// will also properly rename this class into MenuScreens class.
// no need to have it in the gamelauncher or whatever is going on here. 
// fix this mess. 
// integrate to martin's code.
// comment. 
// refactor and clean as needed.
// END OF 11 4 2025 NOTES FROM AYESHA

// POTENTIAL KEY BINDING CLASs: 

        // move left
    
        /*table.add(new Label("Move Left:", skin)).padRight(10);
        final TextButton moveLeftButton = new TextButton(Input.Keys.toString(moveLeftKey), skin);
        moveLeftButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                moveLeftButton.setText("Press key...");
                waitForKeyPress(moveLeftButton, new KeybindCallback() {
                    @Override
                    public void onKeySet(int keycode) {
                        moveLeftKey = keycode;
                        moveLeftButton.setText(Input.Keys.toString(keycode));
                    }
                });
            }
        });
        table.add(moveLeftButton).width(150).row();
        
        // move right
        table.add(new Label("Move Right:", skin)).padRight(10);
        final TextButton moveRightButton = new TextButton(Input.Keys.toString(moveRightKey), skin);
        moveRightButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                moveRightButton.setText("Press key...");
                waitForKeyPress(moveRightButton, new KeybindCallback() {
                    @Override
                    public void onKeySet(int keycode) {
                        moveRightKey = keycode;
                        moveRightButton.setText(Input.Keys.toString(keycode));
                    }
                });
            }
        });
        table.add(moveRightButton).width(150).row();
    
        // move up
        table.add(new Label("Move Up:", skin)).padRight(10);
        final TextButton moveUpButton = new TextButton(Input.Keys.toString(moveUpKey), skin);
        moveRightButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                moveRightButton.setText("Press key...");
                waitForKeyPress(moveUpButton, new KeybindCallback() {
                    @Override
                    public void onKeySet(int keycode) {
                        moveUpKey = keycode;
                        moveUpButton.setText(Input.Keys.toString(keycode));
                    }
                });
            }
        });
        table.add(moveUpButton).width(150).row();
    	
        // move up
        table.add(new Label("Move Down:", skin)).padRight(10);
        final TextButton moveDownButton = new TextButton(Input.Keys.toString(moveDownKey), skin);
        moveDownButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                moveDownButton.setText("Press key...");
                waitForKeyPress(moveUpButton, new KeybindCallback() {
                    @Override
                    public void onKeySet(int keycode) {
                        moveDownKey = keycode;
                        moveDownButton.setText(Input.Keys.toString(keycode));
                    }
                });
            }
        });
        table.add(moveDownButton).width(150).row();
    }*/
  
    
    
    
    
    

/*
private interface KeybindCallback {
    void onKeySet(int keycode);
}
*/

/*
private TextButton currentKeybindButton = null;
private KeybindCallback currentKeybindCallback = null;

private void waitForKeyPress(TextButton button, KeybindCallback callback) {
    currentKeybindButton = button;
    currentKeybindCallback = callback;
    
    // create a temporary input processor to capture the next key press
    stage.setKeyboardFocus(null);
    Gdx.input.setInputProcessor(new com.badlogic.gdx.InputAdapter() {
        @Override
        public boolean keyDown(int keycode) {
            if (keycode != Input.Keys.ESCAPE) {
                currentKeybindCallback.onKeySet(keycode);
            } else {
                currentKeybindButton.setText(Input.Keys.toString(moveLeftKey));
            }
            currentKeybindButton = null;
            currentKeybindCallback = null;
            Gdx.input.setInputProcessor(stage);
            return true;
        }
    });
}
*/

/*
// GETTER METHDODS TO ACCESS SETTINGS ? 
public int getMoveLeftKey() { return moveLeftKey; }
public int getMoveRightKey() { return moveRightKey; }
public int getJumpKey() { return jumpKey; }
public int getSlideKey() { return slideKey; }
public float getMusicVolume() { return musicVolume; }
public float getSfxVolume() { return sfxVolume; }

*/