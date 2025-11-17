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
import com.badlogic.gdx.scenes.scene2d.ui.*;

public class MainMenuScreen implements Screen {
    private final Game game;
    private final KeybindManager keybindManager;
    private Stage stage;
    private Skin skin;

    public MainMenuScreen(Game game, KeybindManager keybindManager) {
        this.game = game;
        this.keybindManager = keybindManager;
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

        TextButton startButton = new TextButton("START GAME", skin);
        TextButton optionsButton = new TextButton("OPTIONS", skin);
        TextButton exitButton = new TextButton("EXIT", skin);

        table.add(startButton).row();
        table.add(optionsButton).row();
        table.add(exitButton).row();

        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Switch to game
                game.setScreen(new GameScreen(game, keybindManager));
            }
        });

        // options listener
        optionsButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                // Pass this MainMenuScreen so we can come back to it
                game.setScreen(new OptionsScreen(game, MainMenuScreen.this, keybindManager));
            }
        });

        // exit button
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

    @Override 
    public void show() {
        // CRITICAL: Set input processor when screen is shown
        Gdx.input.setInputProcessor(stage);
    }
    
    @Override public void pause() { }
    @Override public void resume() { }
    @Override public void hide() { }
    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
    }

} // End of class MainMenuScreen