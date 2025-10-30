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

// declares the main class, extending ApplicationAdapter to get lifecycle methods
public class GameLauncher extends ApplicationAdapter {

	// declare a field to hold the Stage (the container for all UI elements)
    private Stage stage;
    
    // declares a field to hold the UI skin (visual styling)
    private Skin skin;

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
    	
    	// create a new Stage w/ a viewport of 640x480 pixels that maintains aspect ratio
        stage = new Stage(new FitViewport(640, 480));
        
        // routes all input events (touches, clicks, keys) to the stage so UI can respond
        Gdx.input.setInputProcessor(stage);
        
        // load the UI skin definition from a JSON file that defines button styles fonts etc
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        showMainMenu();
    }
    
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
    	// set the clear color to black (rgb = 0, alpha = 1)
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        
        // clear the screen with the clear color (black) before drawing the next frame
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // update the stage and all its actors based on the time elapsed since the last frame
        stage.act(Gdx.graphics.getDeltaTime());
        
        // render the stage and all its UI elements to the screen
        stage.draw();
    } // close render method

    // override the resize method, called when the window is resized
    @Override
    public void resize(int width, int height) {
    	// update viewport to the new window size while maintaining aspect ratio (true = center camera)
        stage.getViewport().update(width, height, true);
    } // close resize method

    // override the dispose method, called when the application closes
    @Override
    public void dispose() {
    	// free memory used by the stage
        stage.dispose();
        // free memory used by the skin (textures, fonts, etc.)
        skin.dispose();
    } // close the dispose method
} // end of class


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
    
    // Create a temporary input processor to capture the next key press
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
// Getter methods for your game to access settings
public int getMoveLeftKey() { return moveLeftKey; }
public int getMoveRightKey() { return moveRightKey; }
public int getJumpKey() { return jumpKey; }
public int getSlideKey() { return slideKey; }
public float getMusicVolume() { return musicVolume; }
public float getSfxVolume() { return sfxVolume; }

*/