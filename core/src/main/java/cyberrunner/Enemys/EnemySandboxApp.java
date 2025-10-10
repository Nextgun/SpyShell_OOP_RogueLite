package cyberrunner.Enemys;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class EnemySandboxApp extends ApplicationAdapter {
    private SpriteBatch batch;

    private Texture turtleTex;
    private Texture goblinTex;

    private Enemy  turtle;   // your player-controlled “turtle” (Enemy class)
    private Goblin goblin;   // chaser

    // Make both sprites the same visual size
    private static final float GOBLIN_SCALE  = 0.18f;
    private static final float TURTLE_SCALE  = GOBLIN_SCALE;

    // Speeds
    private float turtleSpeed = 220f;
    private float goblinSpeed = 140f; // slower than turtle

    // Damage flash
    private float hitFlashTimer = 0f;
    private static final float HIT_FLASH_DURATION = 0.18f;

    @Override
    public void create() {
        batch     = new SpriteBatch();
        turtleTex = new Texture(Gdx.files.internal("images/turtle.png"));
        goblinTex = new Texture(Gdx.files.internal("images/goblin.png"));

        // 1) Pick the on-screen size you want for BOTH (in pixels)
        //    Tweak these two numbers to taste.
        float TARGET_W = 72f;
        float TARGET_H = 72f;

        // 2) Goblin uses that size
        // (We’ll actually spawn it in spawnGoblinAtEdge, but keep these handy if you prefer)
        float gw = TARGET_W, gh = TARGET_H;

        // 3) Turtle uses the EXACT same size
        float tw = TARGET_W, th = TARGET_H;

        // Center the turtle
        float startX = (Gdx.graphics.getWidth()  - tw) / 2f;
        float startY = (Gdx.graphics.getHeight() - th) / 2f;

        // Your Enemy(x, y, width, height, damage)
        turtle = new Enemy(startX, startY, tw, th, 5);

        spawnGoblinAtEdge(); // will use TARGET_W/H too — update that method next
    }

    private void spawnGoblinAtEdge() {
        // SAME on-screen size for goblin as turtle
        float gw = 72f; // must match TARGET_W above
        float gh = 72f; // must match TARGET_H above

        float W = Gdx.graphics.getWidth();
        float H = Gdx.graphics.getHeight();

        int side = MathUtils.random(3);
        float x=0, y=0;
        switch (side) {
            case 0:  x = -gw; y = MathUtils.random(0, H-gh); break;      // left
            case 1:  x =  W;  y = MathUtils.random(0, H-gh); break;      // right
            case 2:  x = MathUtils.random(0, W-gw); y = -gh; break;      // bottom
            default: x = MathUtils.random(0, W-gw); y =  H;  break;      // top
        }

        goblin = new Goblin(goblinTex, x, y, gw, gh, goblinSpeed);
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        // --- Turtle movement (WASD) ---
        Rectangle tb = turtle.getBoundingBox();
        float x = tb.x, y = tb.y, tw = tb.width, th = tb.height;

        if (Gdx.input.isKeyPressed(Input.Keys.A)) x -= turtleSpeed * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) x += turtleSpeed * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) y -= turtleSpeed * dt;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) y += turtleSpeed * dt;

        x = MathUtils.clamp(x, 0, Gdx.graphics.getWidth()  - tw);
        y = MathUtils.clamp(y, 0, Gdx.graphics.getHeight() - th);
        if (x != tb.x || y != tb.y) turtle.moveTo(x, y);

        // --- Goblin seeks turtle center ---
        float targetX = tb.x + tb.width  * 0.5f;
        float targetY = tb.y + tb.height * 0.5f;
        goblin.updateToward(targetX, targetY, dt);

        // --- Collision: flash + respawn goblin ---
        if (goblin.getBounds().overlaps(tb)) {
            hitFlashTimer = HIT_FLASH_DURATION;
            // Immediately respawn a new goblin (don’t dispose shared texture)
            spawnGoblinAtEdge();
        }

        if (hitFlashTimer > 0f) hitFlashTimer -= dt;

        // --- Draw ---
        Gdx.gl.glClearColor(0.06f, 0.06f, 0.08f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.begin();

        // Turtle with red tint briefly after hit
        if (hitFlashTimer > 0f) {
            batch.setColor(1f, 0.4f, 0.4f, 1f);
            batch.draw(turtleTex, tb.x, tb.y, tb.width, tb.height);
            batch.setColor(Color.WHITE);
        } else {
            batch.draw(turtleTex, tb.x, tb.y, tb.width, tb.height);
        }

        // Goblin
        goblin.render(batch);

        batch.end();
    }

    @Override
    public void dispose() {
        if (batch != null)     batch.dispose();
        if (turtleTex != null) turtleTex.dispose();
        if (goblinTex != null) goblinTex.dispose();
        // Do NOT dispose goblin here; it uses the shared goblinTex owned above.
    }
}
