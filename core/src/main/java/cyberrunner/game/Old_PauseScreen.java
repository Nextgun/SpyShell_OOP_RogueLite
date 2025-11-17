//************************************
//Program Name: PauseScreen.java
//Developer: Hector Marrero-Colominas
//Co-Developer: Ayesha Khan
//Date Created: 11/17/2025
//Version: 1.0
//Purpose: Displays pause menu with options to resume or return to main menu
//************************************

package cyberrunner.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Old_PauseScreen implements Screen {
    private final Game game;
    private final Screen previousScreen;
    private Stage stage;
    private Skin skin;

    public Old_PauseScreen(Game game, Screen previousScreen) {
        this.game = game;
        this.previousScreen = previousScreen;
        stage = new Stage(new FitViewport(640, 480));
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        buildUI();
    }

    private void buildUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // create a text label displaying the title using the loaded skin's style
        Label title = new Label("X-RUNNER", skin);
        
        // make the title text x2 as large as the default size 
        title.setFontScale(2f);
        
        // add the title to the table w/ 40 pixels of padding below it, then moves to the next row
        table.add(title).padBottom(40).row();
        
        TextButton resumeButton = new TextButton("RESUME", skin);
        TextButton optionsButton = new TextButton("OPTIONS", skin);
        TextButton quitButton = new TextButton("QUIT", skin);

        table.add(resumeButton).row();
        table.add(optionsButton).row();
        table.add(quitButton).row();

        resumeButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(previousScreen);
            }
        });

        quitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	// Go back to main menu
            	game.setScreen(new MainMenuScreen(game)); 
            }
        });
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }
    
    @Override
    public void resize(int width, int height) {
        // Same logic as MainMenuScreen
        stage.getViewport().update(width, height, true);
    }

    	@Override public void show() { }
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
    }
    
} // End of class PauseScreen

