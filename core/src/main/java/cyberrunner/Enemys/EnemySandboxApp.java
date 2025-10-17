package cyberrunner.Enemys;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;

/**
 * Enemy sandbox:
 * - Player WASD movement, camera follow (player centered).
 * - Melee slash on SPACE toward last move direction (one-hit kill) — larger range/scope.
 * - Scrolling tiled grid background.
 * - Goblins (1 dmg), Hobgoblins (5 dmg) – contact damages once, then they despawn.
 * - Archers: ranged enemies that shoot arrows at intervals.
 * - Anti-stacking between enemies.
 * - Auto-spawn enemies at view edges every few seconds (no R key).
 * - Player health 100 + red hit flash.
 * - Health bar centered at top; larger FOV via camera zoom.
 */
public class EnemySandboxApp extends ApplicationAdapter {

    private SpriteBatch batch;
    private OrthographicCamera camera;

    // Background grid
    private Texture gridTex;
    private int gridTile = 128;

    // HUD / primitives
    private Texture whiteTex;   // 1x1 white for bars & tints

    // Player
    private Rectangle playerBounds;
    private Texture playerTex;
    private float playerSpeed = 280f;
    private int   playerHealth = 100;
    private final int playerHealthMax = 100;
    private float hitFlashTimer = 0f;        // seconds
    private final float hitFlashDuration = 0.12f;

    // Last move direction (aim direction for melee)
    private float lastDirX = 0f, lastDirY = 1f; // default aim up

    // Enemies
    private final Array<Enemy> enemies = new Array<>();
    private Texture goblinTex, hobgoblinTex, archerTex;
    private float goblinSize = 64f;
    private float hobgoblinSize = 80f; // slightly larger
    private float archerSize = 70f;

    // Projectiles (arrows)
    private final Array<Arrow> arrows = new Array<>();
    private Texture arrowTex;
    private final int arrowDamage = 2;        // tweakable
    private final float arrowSpeed = 420f;    // px/sec
    private final float archerFireInterval = 1.4f; // seconds between shots

    // Spawn timing
    private float spawnInterval = 2.5f; // seconds between spawns
    private float spawnTimer = 0f;

    // Melee attack (larger now)
    private boolean meleeActive = false;
    private float   meleeTimer  = 0f;
    private final float meleeActiveTime  = 0.12f;  // attack is "live" a bit longer
    private final float meleeCooldown    = 0.25f;  // time between slashes
    private float   meleeCooldownTimer   = 0f;
    private final float meleeRange       = 72f;    // bigger reach from player center
    private final float meleeSize        = 96f;    // bigger hitbox square
    private final Color meleeTint        = new Color(1f, 1f, 0.5f, 0.35f); // pale yellow
    private final Rectangle meleeBox     = new Rectangle(); // reused

    @Override
    public void create() {
        batch = new SpriteBatch();

        float W = Gdx.graphics.getWidth();
        float H = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, W, H);
        camera.zoom = 1.4f; // larger field of view

        // Textures
        playerTex     = makeSolidTexture(64, 64, new Color(0.2f, 0.5f, 1f, 1f));
        goblinTex     = makeSolidTexture((int)goblinSize, (int)goblinSize, new Color(0.2f, 0.8f, 0.2f, 1f));
        hobgoblinTex  = makeSolidTexture((int)hobgoblinSize, (int)hobgoblinSize, new Color(0.95f, 0.6f, 0.2f, 1f));
        archerTex     = makeSolidTexture((int)archerSize, (int)archerSize, new Color(0.6f, 0.4f, 0.9f, 1f)); // purple
        arrowTex      = makeSolidTexture(18, 6, new Color(1f, 0.95f, 0.3f, 1f)); // yellow-ish
        gridTex       = makeGridTexture(gridTile, gridTile,
                          new Color(0,0,0,0), new Color(0.18f,0.18f,0.22f,1f), new Color(0.14f,0.14f,0.18f,1f));
        gridTex.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        whiteTex      = makeSolidTexture(1, 1, Color.WHITE);

        // Player at world origin (camera centers on it)
        playerBounds = new Rectangle(-32, -32, 64, 64);
        centerCameraOnPlayer();

        // Start with one of each enemy
        spawnGoblinAtViewEdge();
        spawnHobgoblinAtViewEdge();
        spawnArcherAtViewEdge();
    }

    @Override
    public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        // Input + movement
        handleMovement(dt);
        handleMelee(dt);

        centerCameraOnPlayer();

        // Update enemies + arrows
        updateEnemies(dt);
        updateArrows(dt);

        // Interval spawning
        spawnTimer += dt;
        if (spawnTimer >= spawnInterval) {
            spawnTimer = 0f;
            spawnRandomEnemyAtViewEdge();
        }

        // Player hit flash decay
        if (hitFlashTimer > 0f) hitFlashTimer -= dt;

        // Draw
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        drawScrollingGrid();

        // Melee visual (under player but above grid)
        if (meleeActive) {
            batch.setColor(meleeTint);
            batch.draw(whiteTex, meleeBox.x, meleeBox.y, meleeBox.width, meleeBox.height);
            batch.setColor(Color.WHITE);
        }

        // Player (red flash tint if hit)
        if (hitFlashTimer > 0f) {
            float t = MathUtils.clamp(hitFlashTimer / hitFlashDuration, 0f, 1f);
            batch.setColor(1f, 0.3f + 0.7f*(1f - t), 0.3f + 0.7f*(1f - t), 1f);
        }
        batch.draw(playerTex, playerBounds.x, playerBounds.y, playerBounds.width, playerBounds.height);
        batch.setColor(Color.WHITE);

        // Enemies
        for (Enemy e : enemies) e.render(batch);

        // Arrows
        for (Arrow a : arrows) a.render(batch);

        batch.end();

        // HUD (screen-space, top-center)
        drawHUD();
    }

    @Override
    public void dispose() {
        if (batch != null) batch.dispose();
        if (gridTex != null) gridTex.dispose();
        if (whiteTex != null) whiteTex.dispose();
        if (playerTex != null) playerTex.dispose();
        if (goblinTex != null) goblinTex.dispose();
        if (hobgoblinTex != null) hobgoblinTex.dispose();
        if (archerTex != null) archerTex.dispose();
        if (arrowTex != null) arrowTex.dispose();
    }

    // --------------------- Movement / Camera ---------------------

    private void handleMovement(float dt) {
        float vx = 0, vy = 0;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) vy += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) vy -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) vx -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) vx += 1f;

        float len = (float)Math.sqrt(vx*vx + vy*vy);
        if (len > 0f) {
            // update last non-zero direction (aim)
            lastDirX = vx / len;
            lastDirY = vy / len;

            vx = lastDirX * playerSpeed;
            vy = lastDirY * playerSpeed;
            playerBounds.x += vx * dt;
            playerBounds.y += vy * dt;
        }
    }

    private void centerCameraOnPlayer() {
        float px = playerBounds.x + playerBounds.width  * 0.5f;
        float py = playerBounds.y + playerBounds.height * 0.5f;
        camera.position.set(px, py, 0);
        camera.update();
    }

    // --------------------- Melee (SPACE) ---------------------

    private void handleMelee(float dt) {
        // Cooldown tick
        if (meleeCooldownTimer > 0f) meleeCooldownTimer -= dt;

        // Trigger attack on SPACE
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && meleeCooldownTimer <= 0f) {
            startMelee();
        }

        if (meleeActive) {
            meleeTimer -= dt;
            if (meleeTimer <= 0f) {
                meleeActive = false;
            } else {
                // Active window: kill any enemies overlapping meleeBox
                for (int i = enemies.size - 1; i >= 0; --i) {
                    Enemy e = enemies.get(i);
                    if (e.getBoundingBox().overlaps(meleeBox)) {
                        enemies.removeIndex(i); // one-hit kill
                    }
                }
                // Optionally cut arrows:
                // for (int i = arrows.size - 1; i >= 0; --i)
                //     if (arrows.get(i).bounds.overlaps(meleeBox)) arrows.removeIndex(i);
            }
        }
    }

    private void startMelee() {
        meleeActive = true;
        meleeTimer  = meleeActiveTime;
        meleeCooldownTimer = meleeCooldown;

        // Place the box in front of the player along lastDir
        float px = playerBounds.x + playerBounds.width  * 0.5f;
        float py = playerBounds.y + playerBounds.height * 0.5f;

        float dx = lastDirX, dy = lastDirY;
        if (Math.abs(dx) < 1e-5f && Math.abs(dy) < 1e-5f) { dx = 0f; dy = 1f; } // default up

        float cx = px + dx * (playerBounds.width * 0.5f + meleeRange);
        float cy = py + dy * (playerBounds.height * 0.5f + meleeRange);

        meleeBox.set(cx - meleeSize * 0.5f, cy - meleeSize * 0.5f, meleeSize, meleeSize);
    }

    // --------------------- Enemies & Arrows ---------------------

    private void updateEnemies(float dt) {
        // Chase and special logic
        float px = playerBounds.x + playerBounds.width * 0.5f;
        float py = playerBounds.y + playerBounds.height * 0.5f;

        for (Enemy e : enemies) {
            e.updateToward(px, py, dt);

            // Archer special: fire arrows on its own timer
            if (e instanceof Archer) {
                Archer a = (Archer)e;
                a.fireTimer -= dt;
                // keep archers a bit standoff-ish (optional): slow down when close
                keepArcherPreferredRange(a, px, py, 220f);

                if (a.fireTimer <= 0f) {
                    a.fireTimer += archerFireInterval;
                    shootArrowFrom(a, px, py);
                }
            }
        }

        // Anti-stacking (simple pairwise separation using circle approx)
        separateEnemies();

        // Contact damage + despawn on hit (melee enemies & archer if it touches you)
        for (int i = enemies.size - 1; i >= 0; --i) {
            Enemy e = enemies.get(i);
            if (e.getBoundingBox().overlaps(playerBounds)) {
                int dmg = e.getDamage(); // Goblin=1, Hobgoblin=5, Archer contact optional (2)
                if (dmg > 0 && playerHealth > 0) {
                    playerHealth = Math.max(0, playerHealth - dmg);
                    hitFlashTimer = hitFlashDuration;
                }
                enemies.removeIndex(i); // remove enemy after a single contact hit
            }
        }
    }

    private void keepArcherPreferredRange(Archer a, float px, float py, float preferDist) {
        Rectangle r = a.getBoundingBox();
        float cx = r.x + r.width  * 0.5f;
        float cy = r.y + r.height * 0.5f;
        float dx = px - cx;
        float dy = py - cy;
        float d  = (float)Math.sqrt(dx*dx + dy*dy);
        if (d < preferDist) {
            // back up a bit (simple: move opposite a tiny amount)
            float back = (preferDist - d) * 0.50f; // strength
            r.x -= (dx / (d + 1e-4f)) * Math.min(back, 60f) * Gdx.graphics.getDeltaTime();
            r.y -= (dy / (d + 1e-4f)) * Math.min(back, 60f) * Gdx.graphics.getDeltaTime();
        }
    }

    private void updateArrows(float dt) {
        // Move arrows
        for (int i = arrows.size - 1; i >= 0; --i) {
            Arrow a = arrows.get(i);
            a.bounds.x += a.vx * dt;
            a.bounds.y += a.vy * dt;

            // If arrow hits player
            if (a.bounds.overlaps(playerBounds)) {
                if (playerHealth > 0) {
                    playerHealth = Math.max(0, playerHealth - a.damage);
                    hitFlashTimer = hitFlashDuration;
                }
                arrows.removeIndex(i);
                continue;
            }

            // Despawn far off-screen to avoid leaks
            float[] rect = viewWorldRect();
            float margin = 600f;
            float left = rect[0] - margin, bottom = rect[1] - margin;
            float right = rect[0] + rect[2] + margin, top = rect[1] + rect[3] + margin;
            if (a.bounds.x < left || a.bounds.x > right || a.bounds.y < bottom || a.bounds.y > top) {
                arrows.removeIndex(i);
            }
        }
    }

    private void shootArrowFrom(Archer archer, float targetX, float targetY) {
        Rectangle rb = archer.getBoundingBox();
        float sx = rb.x + rb.width * 0.5f;
        float sy = rb.y + rb.height * 0.5f;

        float dx = targetX - sx;
        float dy = targetY - sy;
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        if (len < 1e-4f) return;
        dx /= len; dy /= len;

        Arrow a = new Arrow(arrowTex, sx - 9f, sy - 3f, 18f, 6f, dx * arrowSpeed, dy * arrowSpeed, arrowDamage);
        arrows.add(a);
    }

    /** Pushes overlapping enemies apart so they don't stack. */
    private void separateEnemies() {
        for (int i = 0; i < enemies.size; ++i) {
            Enemy a = enemies.get(i);
            Rectangle ra = a.getBoundingBox();
            float ax = ra.x + ra.width * 0.5f;
            float ay = ra.y + ra.height * 0.5f;
            float ar = Math.min(ra.width, ra.height) * 0.5f;

            for (int j = i + 1; j < enemies.size; ++j) {
                Enemy b = enemies.get(j);
                Rectangle rb = b.getBoundingBox();
                float bx = rb.x + rb.width * 0.5f;
                float by = rb.y + rb.height * 0.5f;
                float br = Math.min(rb.width, rb.height) * 0.5f;

                float dx = bx - ax;
                float dy = by - ay;
                float d2 = dx*dx + dy*dy;
                float minDist = ar + br;

                if (d2 < minDist * minDist && d2 > 1e-5f) {
                    float d = (float)Math.sqrt(d2);
                    float push = (minDist - d) * 0.5f; // push each half way

                    float nx = dx / d;
                    float ny = dy / d;

                    // Move rectangles directly
                    ra.x -= nx * push;
                    ra.y -= ny * push;
                    rb.x += nx * push;
                    rb.y += ny * push;
                }
            }
        }
    }

    // --------------------- Spawning ---------------------

    private void spawnRandomEnemyAtViewEdge() {
        float r = MathUtils.random();
        if      (r < 0.40f) spawnGoblinAtViewEdge();
        else if (r < 0.75f) spawnHobgoblinAtViewEdge();
        else                 spawnArcherAtViewEdge();
    }

    private void spawnGoblinAtViewEdge() {
        float[] rect = viewWorldRect();
        float left=rect[0], bottom=rect[1], width=rect[2], height=rect[3];
        float right = left + width, top = bottom + height;

        float w = goblinSize, h = goblinSize;
        int side = MathUtils.random(3);
        float x=0, y=0;
        switch (side) {
            case 0:  x = left - w;  y = MathUtils.random(bottom, top - h); break; // left
            case 1:  x = right;     y = MathUtils.random(bottom, top - h); break; // right
            case 2:  x = MathUtils.random(left, right - w); y = bottom - h; break; // bottom
            default: x = MathUtils.random(left, right - w); y = top; break;        // top
        }
        enemies.add(new Goblin(goblinTex, x, y, w, h)); // Goblin: slower, 1 dmg
    }

    private void spawnHobgoblinAtViewEdge() {
        float[] rect = viewWorldRect();
        float left=rect[0], bottom=rect[1], width=rect[2], height=rect[3];
        float right = left + width, top = bottom + height;

        float w = hobgoblinSize, h = hobgoblinSize;
        int side = MathUtils.random(3);
        float x=0, y=0;
        switch (side) {
            case 0:  x = left - w;  y = MathUtils.random(bottom, top - h); break;
            case 1:  x = right;     y = MathUtils.random(bottom, top - h); break;
            case 2:  x = MathUtils.random(left, right - w); y = bottom - h; break;
            default: x = MathUtils.random(left, right - w); y = top; break;
        }
        enemies.add(new Hobgoblin(hobgoblinTex, x, y, w, h)); // Hobgoblin: faster, 5 dmg
    }

    private void spawnArcherAtViewEdge() {
        float[] rect = viewWorldRect();
        float left=rect[0], bottom=rect[1], width=rect[2], height=rect[3];
        float right = left + width, top = bottom + height;

        float w = archerSize, h = archerSize;
        int side = MathUtils.random(3);
        float x=0, y=0;
        switch (side) {
            case 0:  x = left - w;  y = MathUtils.random(bottom, top - h); break;
            case 1:  x = right;     y = MathUtils.random(bottom, top - h); break;
            case 2:  x = MathUtils.random(left, right - w); y = bottom - h; break;
            default: x = MathUtils.random(left, right - w); y = top; break;
        }
        Archer a = new Archer(archerTex, x, y, w, h); // fireTimer starts at 0 so it can shoot soon
        a.fireTimer = MathUtils.random(0.2f, archerFireInterval); // de-sync shooters
        enemies.add(a);
    }

    /** world rect currently visible (left, bottom, width, height). */
    private float[] viewWorldRect() {
        float vw = camera.viewportWidth  * camera.zoom;
        float vh = camera.viewportHeight * camera.zoom;
        float left   = camera.position.x - vw * 0.5f;
        float bottom = camera.position.y - vh * 0.5f;
        return new float[]{ left, bottom, vw, vh };
    }

    // --------------------- Background & HUD ---------------------

    /** Scrolling, repeated grid covering the camera view. */
    private void drawScrollingGrid() {
        float[] rect = viewWorldRect();
        float left=rect[0], bottom=rect[1], width=rect[2], height=rect[3];

        float u  = left / gridTile;
        float v  = bottom / gridTile;
        float u2 = (left + width)  / gridTile;
        float v2 = (bottom + height) / gridTile;

        batch.draw(gridTex, left, bottom, width, height, u, v, u2, v2);
    }

    /** Health bar centered at the top (screen-space). */
    private void drawHUD() {
        OrthographicCamera hudCam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCam.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCam.update();

        batch.setProjectionMatrix(hudCam.combined);
        batch.begin();

        float marginTop = 16f;
        float barW = 280f, barH = 18f;
        float x = (Gdx.graphics.getWidth() - barW) * 0.5f; // centered
        float y = Gdx.graphics.getHeight() - marginTop - barH;

        // Background (dark outline)
        batch.setColor(0f, 0f, 0f, 0.6f);
        batch.draw(whiteTex, x-2, y-2, barW+4, barH+4);

        // Red full bar
        batch.setColor(0.6f, 0.1f, 0.1f, 1f);
        batch.draw(whiteTex, x, y, barW, barH);

        // Green current fill
        float pct = Math.max(0f, Math.min(1f, (float)playerHealth / playerHealthMax));
        batch.setColor(0.15f, 0.9f, 0.2f, 1f);
        batch.draw(whiteTex, x, y, barW * pct, barH);

        batch.setColor(Color.WHITE);
        batch.end();

        // Return to world projection
        batch.setProjectionMatrix(camera.combined);
    }

    // --------------------- Texture helpers ---------------------

    private static Texture makeSolidTexture(int w, int h, Color color) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(color);
        pm.fill();
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private static Texture makeGridTexture(int w, int h, Color transparent, Color baseColor, Color altLine) {
        Pixmap pm = new Pixmap(w, h, Pixmap.Format.RGBA8888);
        pm.setColor(baseColor);
        pm.fill();

        pm.setColor(altLine);
        pm.drawLine(w/2, 0, w/2, h-1);
        pm.drawLine(0, h/2, w-1, h/2);
        pm.drawRectangle(0, 0, w, h);

        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    // --------------------- Arrow helper class ---------------------

    private static class Arrow {
        final Texture tex;
        final Rectangle bounds = new Rectangle();
        float vx, vy;
        int damage;

        Arrow(Texture tex, float x, float y, float w, float h, float vx, float vy, int damage) {
            this.tex = tex;
            this.bounds.set(x, y, w, h);
            this.vx = vx; this.vy = vy;
            this.damage = damage;
        }

        void render(SpriteBatch batch) {
            batch.draw(tex, bounds.x, bounds.y, bounds.width, bounds.height);
        }
    }
}