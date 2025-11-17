//************************************
//Program Name: MainMenuScreen.java
//Developer: Hector Marrero-Colominas
//Co-Developer: Ayesha Khans
//Date Created: 11/17/2025
//Version: 1.0
//Purpose: Displays the main menu UI with options to start the game or quit

package cyberrunner.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class MainMenuScreen implements Screen {
    private final Game game;
    private Stage stage;
    private Skin skin;

    public MainMenuScreen(Game game) {
        this.game = game;
        stage = new Stage(new FitViewport(640, 480));
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
        buildUI();
    }

    private void buildUI() {
        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        TextButton startButton = new TextButton("START GAME", skin);
        TextButton exitButton = new TextButton("EXIT", skin);

        table.add(startButton).row();
        table.add(exitButton).row();

        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
            	// Switch to game
                game.setScreen(new GameScreen(game)); 
            }
        });

        exitButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                Gdx.app.exit();
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
        // Update the viewport to match the new window size
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
    
} // End of class MainMenuScreen