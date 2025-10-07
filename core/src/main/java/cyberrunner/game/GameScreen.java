package cyberrunner.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

public class GameScreen implements Screen {
    private final GameLauncher game;
    private SpriteBatch batch;
    private Player player;

    public GameScreen(GameLauncher game) {
        this.game = game;
    }

    @Override
    public void show() {
        batch = new SpriteBatch();
        player = new Player(100, 100);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);

        player.update(delta);

        batch.begin();
        player.render(batch);
        batch.end();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void dispose() { batch.dispose(); player.dispose(); }
}