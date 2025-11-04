// Author: Martin Taylor
// File: GameLauncher.java
// Date: 2025-11-04
// Description:
//   Minimal Scene2D launcher. The Start button is the hand-off point where
//   you transition to your gameplay screen (e.g., EnemySandboxScreen).

package cyberrunner.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class GameLauncher extends ApplicationAdapter {
    private Stage stage;
    private Skin skin;

    @Override
    public void create() {
        stage = new Stage(new FitViewport(640, 480));
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Window window = new Window("CyberRunner", skin, "border");
        window.defaults().pad(6f);
        window.add("Press Start to launch gameplay.").row();

        final TextButton startBtn = new TextButton("Start Game", skin);
        startBtn.pad(10f);
        startBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                startBtn.setText("Loading...");
                // TODO: Replace with your transition to gameplay:
                // if using Game, do: ((Game)Gdx.app.getApplicationListener()).setScreen(new EnemySandboxScreen());
                Gdx.app.log("MENU", "Start Game clicked (wire to gameplay screen here).");
            }
        });
        window.add(startBtn).row();

        final TextButton aboutBtn = new TextButton("About", skin);
        aboutBtn.addListener(new ChangeListener() {
            @Override public void changed(ChangeEvent event, Actor actor) {
                aboutBtn.setText("v0.1 • Scene2D menu");
            }
        });
        window.add(aboutBtn);

        window.pack();
        window.setPosition(
            MathUtils.roundPositive(stage.getWidth()  / 2f - window.getWidth()  / 2f),
            MathUtils.roundPositive(stage.getHeight() / 2f - window.getHeight() / 2f)
        );
        window.addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.75f)));
        stage.addActor(window);

        Gdx.input.setInputProcessor(stage);
    }

    @Override public void render() {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override public void resize(int width, int height) {
        if (width <= 0 || height <= 0) return;
        stage.getViewport().update(width, height, true);
    }

    @Override public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
