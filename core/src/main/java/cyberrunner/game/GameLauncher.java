//package cyberrunner.game;
//
//import com.badlogic.gdx.ApplicationAdapter;
//import com.badlogic.gdx.Gdx;
//import com.badlogic.gdx.graphics.GL20;
//import com.badlogic.gdx.math.MathUtils;
//import com.badlogic.gdx.scenes.scene2d.Actor;
//import com.badlogic.gdx.scenes.scene2d.Stage;
//import com.badlogic.gdx.scenes.scene2d.actions.Actions;
//import com.badlogic.gdx.scenes.scene2d.ui.Skin;
//import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
//import com.badlogic.gdx.scenes.scene2d.ui.Window;
//import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
//import com.badlogic.gdx.utils.ScreenUtils;
//import com.badlogic.gdx.utils.viewport.FitViewport;
//
///** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
//public class GameLauncher extends ApplicationAdapter {
//    private Stage stage;
//    private Skin skin;
//
//    @Override
//    public void create() {
//        stage = new Stage(new FitViewport(640, 480));
//        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));
//
//        Window window = new Window("Example screen", skin, "border");
//        window.defaults().pad(4f);
//        window.add("This is a simple Scene2D view.").row();
//        final TextButton button = new TextButton("Click me!", skin);
//        button.pad(8f);
//        button.addListener(new ChangeListener() {
//            @Override
//            public void changed(final ChangeEvent event, final Actor actor) {
//                button.setText("Clicked.");
//            }
//        });
//        window.add(button);
//        window.pack();
//        // We round the window position to avoid awkward half-pixel artifacts.
//        // Casting using (int) would also work.
//        window.setPosition(MathUtils.roundPositive(stage.getWidth() / 2f - window.getWidth() / 2f),
//            MathUtils.roundPositive(stage.getHeight() / 2f - window.getHeight() / 2f));
//        window.addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(1f)));
//        stage.addActor(window);
//
//        Gdx.input.setInputProcessor(stage);
//    }
//
//    @Override
//    public void render() {
//        ScreenUtils.clear(0f, 0f, 0f, 1f);
//        stage.act(Gdx.graphics.getDeltaTime());
//        stage.draw();
//    }
//
//    @Override
//    public void resize(int width, int height) {
//        // If the window is minimized on a desktop (LWJGL3) platform, width and height are 0, which causes problems.
//        // In that case, we don't resize anything, and wait for the window to be a normal size before updating.
//        if(width <= 0 || height <= 0) return;
//
//        stage.getViewport().update(width, height);
//    }
//
//    @Override
//    public void dispose() {
//        stage.dispose();
//        skin.dispose();
//    }
//}
package cyberrunner.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class GameLauncher extends ApplicationAdapter {

    private Stage stage;
    private Skin skin;

    @Override
    public void create() {
        stage = new Stage(new FitViewport(640, 480));
        Gdx.input.setInputProcessor(stage);
        skin = new Skin(Gdx.files.internal("ui/uiskin.json"));

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // Title
        Label title = new Label("🚀 X-RUNNER", skin);
        title.setFontScale(2f);
        table.add(title).padBottom(40).row();

        // Buttons
        TextButton startButton = new TextButton("START GAME", skin);
        TextButton optionsButton = new TextButton("OPTIONS", skin);
        TextButton exitButton = new TextButton("EXIT", skin);

        startButton.getLabel().setFontScale(1.5f);
        optionsButton.getLabel().setFontScale(1.5f);
        exitButton.getLabel().setFontScale(1.5f);

        table.add(startButton).width(250).pad(10).row();
        table.add(optionsButton).width(250).pad(10).row();
        table.add(exitButton).width(250).pad(10).row();

        // Button logic
        startButton.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, Actor actor) {
                System.out.println("Start game clicked!");
                // You could swap some internal state here to go to gameplay
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
    public void render() {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(Gdx.graphics.getDeltaTime());
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
        skin.dispose();
    }
}
