package cyberrunner.game;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class OptionsScreen implements Screen {
    private final Game game;
    private final Screen previousScreen;
    private Stage stage;
    private Skin skin;
    private KeybindManager keybindManager;
    
    // UI elements for keybind display
    private TextButton moveLeftButton;
    private TextButton moveRightButton;
    private TextButton jumpButton;
    private TextButton dashButton;
    
    private String waitingForKey = null; // Track which key we're rebinding

    public OptionsScreen(Game game, Screen previousScreen, KeybindManager keybindManager) {
        this.game = game;
        this.previousScreen = previousScreen;
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

        Label title = new Label("OPTIONS", skin);
        title.setFontScale(2f);
        table.add(title).padBottom(40).colspan(2).row();
        
        // Keybind section
        Label keybindTitle = new Label("Key Bindings", skin);
        keybindTitle.setFontScale(1.5f);
        table.add(keybindTitle).padBottom(20).colspan(2).row();
        
        // Move Left
        Label moveLeftLabel = new Label("Move Left:", skin);
        moveLeftButton = new TextButton(getKeyName(keybindManager.getMoveLeft()), skin);
        table.add(moveLeftLabel).padRight(10);
        table.add(moveLeftButton).width(150).padBottom(10).row();
        
        // Move Right
        Label moveRightLabel = new Label("Move Right:", skin);
        moveRightButton = new TextButton(getKeyName(keybindManager.getMoveRight()), skin);
        table.add(moveRightLabel).padRight(10);
        table.add(moveRightButton).width(150).padBottom(10).row();
        
        // Jump
        Label jumpLabel = new Label("Jump:", skin);
        jumpButton = new TextButton(getKeyName(keybindManager.getJump()), skin);
        table.add(jumpLabel).padRight(10);
        table.add(jumpButton).width(150).padBottom(10).row();
        
        // Dash
        Label dashLabel = new Label("Dash:", skin);
        dashButton = new TextButton(getKeyName(keybindManager.getDash()), skin);
        table.add(dashLabel).padRight(10);
        table.add(dashButton).width(150).padBottom(30).row();
        
        // Back button
        TextButton backButton = new TextButton("BACK", skin);
        table.add(backButton).colspan(2).row();
        
        // Add listeners
        moveLeftButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startRebinding("moveLeft", moveLeftButton);
            }
        });
        
        moveRightButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startRebinding("moveRight", moveRightButton);
            }
        });
        
        jumpButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startRebinding("jump", jumpButton);
            }
        });
        
        dashButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                startRebinding("dash", dashButton);
            }
        });
        
        backButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                game.setScreen(previousScreen);
            }
        });
    }
    
    private void startRebinding(String action, TextButton button) {
        waitingForKey = action;
        button.setText("Press a key...");
    }
    
    private String getKeyName(int keyCode) {
        return Input.Keys.toString(keyCode);
    }

    @Override
    public void render(float delta) {
        // Handle key input for rebinding
        if (waitingForKey != null) {
            for (int i = 0; i < 256; i++) {
                if (Gdx.input.isKeyJustPressed(i)) {
                    // Update the keybind
                    switch (waitingForKey) {
                        case "moveLeft":
                            keybindManager.setMoveLeft(i);
                            moveLeftButton.setText(getKeyName(i));
                            break;
                        case "moveRight":
                            keybindManager.setMoveRight(i);
                            moveRightButton.setText(getKeyName(i));
                            break;
                        case "jump":
                            keybindManager.setJump(i);
                            jumpButton.setText(getKeyName(i));
                            break;
                        case "dash":
                            keybindManager.setDash(i);
                            dashButton.setText(getKeyName(i));
                            break;
                    }
                    waitingForKey = null;
                    break;
                }
            }
        }
        
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
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
}