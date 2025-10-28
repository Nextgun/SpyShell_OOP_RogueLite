//EnemySandboxApp.java
package cyberrunner.Enemys;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * Enemy sandbox with player (WASD + smooth dash on SHIFT + SPACE melee),
 * coins, 25% health orbs, and enemies:
 *  - Goblin/Hobgoblin (melee chasers),
 *  - Archer (bows + arrows),
 *  - Bomber (drops ticking bomb on hit/death),
 *  - Berserker (approach -> short charge -> straight dash with single-hit).
 *
 * This version adds:
 *  - Per-axis enemy movement and collision resolution (slide along walls),
 *  - Tiny “stuck” recovery nudge + quick repath,
 *  - Sword-swing de-dupe,
 *  - Goblin/Hobgoblin tusks,
 *  - Archer shooting restored (wind-up + fire).
 */
public class EnemySandboxApp extends ApplicationAdapter {

    // ------------------------------------------------------------------------
    // Core
    // ------------------------------------------------------------------------
    private SpriteBatch batch;
    private OrthographicCamera camera;
    private BitmapFont font;
    private final GlyphLayout glyph = new GlyphLayout();

    private Dungeon dungeon;
    private final int TILE_SIZE = 64;

    // Textures
    private Texture whiteTex, floorTex, wallTex;
    private Texture playerBodyTex, goblinBodyTex, hobgoblinBodyTex, archerBodyTex, bomberBodyTex, berserkerBodyTex;
    private Texture orbTex, coinTex, arrowTex;

    // Faces drawing helper
    private Sprite rotSprite;

    // Sizes (ellipse bodies)
    private static class TextureSize { int w,h; TextureSize(int w,int h){this.w=w; this.h=h;} }
    private final TextureSize goblinSz   = new TextureSize(56, 56);
    private final TextureSize hobSz      = new TextureSize(72, 72);
    private final TextureSize archerSz   = new TextureSize(60, 60);
    private final TextureSize bomberSz   = new TextureSize(60, 60);
    private final TextureSize berserkSz  = new TextureSize(72, 72); // match hobgoblin size to reduce snagging

    // ------------------------------------------------------------------------
    // Player
    // ------------------------------------------------------------------------
    private Rectangle playerBounds;
    private float playerSpeed = 280f;
    private final int playerHealthMax = 100;
    private int playerHealth = playerHealthMax;

    private float hitFlashTimer = 0f;
    private final float hitFlashDuration = 0.12f;

    private float lastDirX = 0f, lastDirY = 1f;

    // Eyes / brows
    private float playerBlinkTimer = 0f;
    private float playerBlinkDur   = 0f;
    private final float BLINK_MIN  = 2.6f, BLINK_MAX = 5.2f, BLINK_DUR = 0.11f;
    private boolean attackBrows = false;

    // Dash (cooldown + smooth interpolation)
    private final float dashCooldown = 5f;
    private float dashCdTimer = 0f;
    private final float dashDistance = 220f;
    private boolean isDashing = false;
    private float dashAnimT = 0f;
    private final float dashAnimDur = 0.12f;
    private float dashStartX, dashStartY, dashEndX, dashEndY;

    // Melee (hit-boxed, swats arrows)
    private boolean meleeActive = false;
    private float   meleeTimer  = 0f;
    private final float meleeActiveTime = 0.12f;
    private final float meleeCooldown   = 0.25f;
    private float   meleeCooldownTimer  = 0f;
    private final float meleeRange = 72f;
    private final float meleeSize  = 96f;
    private final Color meleeTint  = new Color(1f, 1f, 0.5f, 0.30f);
    private final Rectangle meleeBox = new Rectangle();

    // Unique ID per sword swing (prevents double-hits per swing)
    private int globalSwingIdCounter = 0;
    private int currentSwingId = -1;

    // Simple sword arc (visual only)
    private Texture swordTex;
    private Sprite  swordSprite;
    private final float SWORD_W = 64f, SWORD_H = 12f;
    private float swingTimer = 0f, swingTotal = 0.14f, swingArcDeg = 65f;
    private boolean swordRightHand = true;

    // ------------------------------------------------------------------------
    // Enemies & projectiles & drops
    // ------------------------------------------------------------------------
    private final Array<Enemy> enemies = new Array<>();
    private final ObjectMap<Enemy, PathState> paths = new ObjectMap<>();

    // Arrows
    private final Array<Arrow> arrows = new Array<>();
    private final int   arrowDamage = 3;
    private final float arrowSpeed  = 420f;
    private final float archerFireInterval = 1.4f;
    private final ObjectMap<Archer, Float> archerDrawT = new ObjectMap<>();
    private final float archerDrawWindup = 0.35f;

    // Bombs
    private final Array<Bomb> bombs = new Array<>();
    private static final int   BOMB_TICKS         = 3;
    private static final float BOMB_TICK_INTERVAL = 0.45f;
    private static final float BOMB_RADIUS        = 170f;
    private static final int   BOMB_DAMAGE        = 10;

    // Orbs (25% drop)
    private final Array<Orb> orbs = new Array<>();
    private static final float ORB_DROP_CHANCE = 0.25f;
    private static final int   ORB_HEAL = 5;

    // Coins
    private final Array<Coin> coins = new Array<>();
    private int coinCount = 0;

    // Spawner
    private float spawnInterval = 2.5f;
    private float spawnTimer    = 0f;

    // Pathing helpers
    private static class PathState {
        final Array<Vector2> waypoints = new Array<>();
        int current = 0;
        float repathTimer = 0f;
        int lastTargetTx = Integer.MIN_VALUE, lastTargetTy = Integer.MIN_VALUE;

        // STUCK RECOVERY STATE
        float lastX, lastY;
        float stillTimer = 0f;
        float nudgeCooldown = 0f;
    }
    private final float REPTH_INTERVAL = 0.35f;

    // Inset collision rect for enemies to reduce snag
    private static final float ENEMY_COLLISION_INSET = 12f;
    private final Rectangle tmpCollRect = new Rectangle();

    // Stuck recovery tuning
    private static final float STUCK_SPEED_EPS       = 0.25f;
    private static final float STUCK_TIME            = 0.30f;
    private static final float STUCK_REPATH_COOLDOWN = 0.25f;
    private static final float STUCK_RECOVER_STEP    = 10f;

    // Clearance field (soft cost away from walls)
    private float[][] tileClearance;
    private static final float MIN_CLEAR_TILES       = 0.50f;
    private static final float PREFERRED_CLEAR_TILES = 1.20f;
    private static final float CLEAR_SOFTCOST        = 12f;

    // Face styles
    private enum FaceStyle { PLAYER_SMILE, GOBLIN_DEVIOUS, HOB_DEVIOUS, ARCHER_MASK, BOMBER_ANGRY, BERSERKER_HELM }

    // ------------------------------------------------------------------------
    // LibGDX lifecycle
    // ------------------------------------------------------------------------
    @Override public void create() {
        batch = new SpriteBatch();
        font  = new BitmapFont();

        float W = Gdx.graphics.getWidth();
        float H = Gdx.graphics.getHeight();

        camera = new OrthographicCamera();
        camera.setToOrtho(false, W, H);
        camera.zoom = 1.4f;

        whiteTex = makeSolidTexture(1,1, Color.WHITE);
        floorTex = makeSolidTexture(TILE_SIZE, TILE_SIZE, new Color(0.18f, 0.18f, 0.22f, 1f));
        wallTex  = makeStoneWallTexture(TILE_SIZE);
        orbTex   = makeSolidTexture(16,16, new Color(0.85f,0.2f,0.25f,1f));
        coinTex  = makeCoinTexture();
        arrowTex = makeSolidTexture(18, 6, new Color(0.95f, 0.9f, 0.2f, 1f));
        rotSprite = new Sprite(whiteTex);

        // Bodies (circles)
        playerBodyTex    = makeEllipseTexture(64, 64, new Color(0.20f, 0.50f, 1f, 1f));
        goblinBodyTex    = makeEllipseTexture(goblinSz.w, goblinSz.h, new Color(0.20f, 0.80f, 0.20f, 1f));
        hobgoblinBodyTex = makeEllipseTexture(hobSz.w,    hobSz.h,    new Color(0.95f, 0.60f, 0.20f, 1f));
        archerBodyTex    = makeEllipseTexture(archerSz.w, archerSz.h, new Color(0.60f, 0.40f, 0.90f, 1f));
        bomberBodyTex    = makeEllipseTexture(bomberSz.w, bomberSz.h, new Color(0.90f, 0.10f, 0.10f, 1f));
        berserkerBodyTex = makeEllipseTexture(berserkSz.w, berserkSz.h, new Color(0.98f, 0.90f, 0.15f, 1f));

        // Sword sprite
        swordTex = makeSwordTexture();
        swordSprite = new Sprite(swordTex);
        swordSprite.setSize(SWORD_W, SWORD_H);
        swordSprite.setOrigin(12f, SWORD_H/2f);

        // Dungeon
        dungeon = new Dungeon(100, 100, TILE_SIZE);
        dungeon.generate(MathUtils.random(Long.MIN_VALUE, Long.MAX_VALUE));
        tileClearance = new float[dungeon.width][dungeon.height];
        buildClearanceField();

        // Player
        float cx = (dungeon.width / 2f) * TILE_SIZE;
        float cy = (dungeon.height/ 2f) * TILE_SIZE;
        playerBounds = new Rectangle(cx-32, cy-32, 64, 64);
        snapRectToOpen(playerBounds);
        centerCameraOnPlayer();
        scheduleNextPlayerBlink();

        // Initial spawns
        spawnGoblinAnywhere();
        spawnHobgoblinAnywhere();
        spawnArcherAnywhere();
        spawnBomberAnywhere();
        spawnBerserkerAnywhere();
    }

    @Override public void render() {
        float dt = Gdx.graphics.getDeltaTime();

        if (dashCdTimer > 0f) dashCdTimer -= dt;
        updatePlayerBlink(dt);

        handleMovement(dt);
        handleDash();
        updateDashAnim(dt);
        handleMelee(dt);
        centerCameraOnPlayer();

        updateEnemies(dt);
        updateArrows(dt);
        updateBombs(dt);
        updateOrbs(dt);
        updateCoins(dt);

        spawnTimer += dt;
        if (spawnTimer >= spawnInterval) { spawnTimer = 0f; spawnRandomEnemyAnywhere(); }

        if (hitFlashTimer > 0f) hitFlashTimer -= dt;

        // draw
        Gdx.gl.glClearColor(0.08f, 0.08f, 0.10f, 1f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        dungeon.render(batch, floorTex, wallTex, viewLeft(), viewBottom(), viewWidth(), viewHeight());

        // orbs below coins (so coins render on top)
        for (Orb o : orbs) o.render(batch);
        for (Coin c : coins) c.render(batch);

        renderBombs(batch);

        if (meleeActive) {
            batch.setColor(meleeTint);
            batch.draw(whiteTex, meleeBox.x, meleeBox.y, meleeBox.width, meleeBox.height);
            batch.setColor(Color.WHITE);
        }

        // Player tint on hit
        if (hitFlashTimer > 0f) {
            float t = MathUtils.clamp(hitFlashTimer / hitFlashDuration, 0f, 1f);
            batch.setColor(1f, 0.3f + 0.7f*(1f - t), 0.3f + 0.7f*(1f - t), 1f);
        }
        batch.draw(playerBodyTex, playerBounds.x, playerBounds.y, playerBounds.width, playerBounds.height);
        batch.setColor(Color.WHITE);

        drawFaceOverlay(FaceStyle.PLAYER_SMILE,
                playerBounds.x, playerBounds.y, playerBounds.width, playerBounds.height,
                (playerBlinkDur > 0f), attackBrows);

        if (meleeActive) drawSwordSwing();

        // Enemies
        for (Enemy e : enemies) {
            Rectangle rb = e.getBoundingBox();
            float jx=0f, jy=0f;
            if (e instanceof Berserker) {
                Berserker b = (Berserker)e;
                if (b.state == Berserker.State.CHARGING) {
                    float t = time()*28f + b.shakeSeed;
                    float amp = 4f;
                    jx = MathUtils.sin(t)*amp;
                    jy = MathUtils.cos(t*1.37f)*amp;
                }
            }
            batch.draw(bodyFor(e), rb.x + jx, rb.y + jy, rb.width, rb.height);
            boolean blink = false;
            float phase = ((System.identityHashCode(e) & 1023)*0.013f) + (time()*0.22f);
            if ((int)(phase) % 7 == 0 && (phase - Math.floor(phase)) < 0.06f) blink = true;

            drawFaceOverlay(styleFor(e), rb.x + jx, rb.y + jy, rb.width, rb.height, blink,
                    (e instanceof Berserker) && ((Berserker)e).state == Berserker.State.CHARGING);

            if (e instanceof Archer) drawArcherBow((Archer)e);

            // tiny HP label over Berserkers for debugging
            if (e instanceof Berserker) {
                Rectangle rb2 = e.getBoundingBox();
                font.draw(batch,
                        String.valueOf(e.getHealth()),
                        rb2.x + rb2.width * 0.5f - 4f,
                        rb2.y + rb2.height + 12f);
            }
        }

        for (Arrow a : arrows) a.render(batch);
        batch.end();

        drawHUD();
    }

    @Override public void dispose() {
        if (batch!=null) batch.dispose();
        if (font !=null) font.dispose();
        if (whiteTex!=null) whiteTex.dispose();
        if (floorTex!=null) floorTex.dispose();
        if (wallTex !=null) wallTex.dispose();
        if (playerBodyTex!=null) playerBodyTex.dispose();
        if (goblinBodyTex !=null) goblinBodyTex.dispose();
        if (hobgoblinBodyTex!=null) hobgoblinBodyTex.dispose();
        if (archerBodyTex !=null) archerBodyTex.dispose();
        if (bomberBodyTex!=null) bomberBodyTex.dispose();
        if (berserkerBodyTex!=null) berserkerBodyTex.dispose();
        if (orbTex!=null) orbTex.dispose();
        if (coinTex!=null) coinTex.dispose();
        if (arrowTex!=null) arrowTex.dispose();
        if (swordTex!=null) swordTex.dispose();
    }

    // ------------------------------------------------------------------------
    // Player control
    // ------------------------------------------------------------------------
    private void handleMovement(float dt) {
        if (isDashing) return;

        float vx=0f, vy=0f;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) vy += 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) vy -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.A)) vx -= 1f;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) vx += 1f;

        float len = (float)Math.sqrt(vx*vx+vy*vy);
        if (len > 0f) {
            lastDirX = vx/len; lastDirY = vy/len;
            float dx = lastDirX * playerSpeed * dt;
            float dy = lastDirY * playerSpeed * dt;

            float oldX = playerBounds.x, oldY = playerBounds.y;
            playerBounds.x += dx;
            if (dungeon.rectBlocked(playerBounds)) playerBounds.x = oldX;
            playerBounds.y += dy;
            if (dungeon.rectBlocked(playerBounds)) playerBounds.y = oldY;
        }
    }

    private void handleDash() {
        boolean pressed = Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)
                       || Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_RIGHT);
        if (!pressed || dashCdTimer>0f || isDashing) return;

        float dx = lastDirX, dy = lastDirY;
        if (Math.abs(dx)<1e-5f && Math.abs(dy)<1e-5f) { dx=0f; dy=1f; }
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        dx/=len; dy/=len;

        // “sweep” forward and stop before collisions
        float step = 8f;
        int steps = Math.max(1, (int)(dashDistance/step));
        float ox = playerBounds.x, oy = playerBounds.y;
        float curX = ox, curY = oy;
        Rectangle test = new Rectangle(playerBounds);

        for (int i=0;i<steps;i++){
            float tryX = curX + dx*step;
            float tryY = curY + dy*step;
            float keepX = tryX; test.setPosition(tryX, curY);
            if (dungeon.rectBlocked(test)) keepX = curX;
            float keepY = tryY; test.setPosition(keepX, tryY);
            if (dungeon.rectBlocked(test)) keepY = curY;
            curX = keepX; curY = keepY;
            if (Math.abs(curX-ox)+Math.abs(curY-oy) < 0.001f) break;
        }

        dashStartX = ox; dashStartY = oy;
        dashEndX   = curX; dashEndY = curY;
        playerBounds.x = ox; playerBounds.y = oy;
        dashAnimT = 0f;
        isDashing = true;
        dashCdTimer = dashCooldown;
    }

    private void updateDashAnim(float dt){
        if (!isDashing) return;
        dashAnimT += dt;
        float t = MathUtils.clamp(dashAnimT / dashAnimDur, 0f,1f);
        float ease = 1f - (1f - t)*(1f - t);
        playerBounds.x = MathUtils.lerp(dashStartX, dashEndX, ease);
        playerBounds.y = MathUtils.lerp(dashStartY, dashEndY, ease);
        if (t>=1f) isDashing=false;
    }

    private void centerCameraOnPlayer(){
        float px = playerBounds.x + playerBounds.width * 0.5f;
        float py = playerBounds.y + playerBounds.height* 0.5f;
        camera.position.set(px, py, 0f);
        camera.update();
    }

    // ------------------------------------------------------------------------
    // Melee
    // ------------------------------------------------------------------------
    private void handleMelee(float dt) {
        if (meleeCooldownTimer>0f) meleeCooldownTimer -= dt;

        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE) && meleeCooldownTimer<=0f) {
            startMelee();
        }
        if (!meleeActive) return;

        meleeTimer -= dt;
        swingTimer += dt;

        if (meleeTimer <= 0f) {
            meleeActive = false;
            attackBrows = false;
            currentSwingId = -1;
            return;
        }

        // damage enemies in box (at most once per enemy per swing)
        for (int i = enemies.size - 1; i >= 0; --i) {
            Enemy e = enemies.get(i);
            if (!e.getBoundingBox().overlaps(meleeBox)) continue;
            if (!e.tryRegisterMeleeSwing(currentSwingId)) continue;

            int before = e.getHealth();
            e.applyHit(1);
            e.markMeleeHitRegistered();
            Gdx.app.log("HIT",
                    e.getClass().getSimpleName() + " hp " + before + " -> " + e.getHealth());

            if (e instanceof Bomber && e.getHealth() <= 0) {
                Rectangle br = e.getBoundingBox();
                dropBombAt(br.x + br.width * 0.5f, br.y + br.height * 0.5f);
            }

            if (e.getHealth() <= 0) {
                Gdx.app.log("DEATH", e.getClass().getSimpleName() + " removed");
                dropCoin(e);
                maybeDropOrb(e);
                enemies.removeIndex(i);
                paths.remove(e);
                if (e instanceof Archer) archerDrawT.remove((Archer) e);
            }
        }

        // swat arrows
        for (int i = arrows.size-1; i>=0; --i){
            if (arrows.get(i).bounds.overlaps(meleeBox)) arrows.removeIndex(i);
        }
    }

    private void startMelee(){
        meleeActive = true;
        meleeTimer  = meleeActiveTime;
        meleeCooldownTimer = meleeCooldown;
        swingTimer = 0f;
        attackBrows = true;
        swordRightHand = !swordRightHand;

        // fresh swing ID
        currentSwingId = ++globalSwingIdCounter;

        float px=playerBounds.x + playerBounds.width*0.5f;
        float py=playerBounds.y + playerBounds.height*0.5f;

        float dx=lastDirX, dy=lastDirY;
        if (Math.abs(dx)<1e-5f && Math.abs(dy)<1e-5f){ dx=0f; dy=1f; }

        float cx = px + dx*(playerBounds.width*0.5f + meleeRange);
        float cy = py + dy*(playerBounds.height*0.5f + meleeRange);
        meleeBox.set(cx - meleeSize*0.5f, cy - meleeSize*0.5f, meleeSize, meleeSize);
    }

    private void drawSwordSwing(){
        float px=playerBounds.x + playerBounds.width*0.5f;
        float py=playerBounds.y + playerBounds.height*0.5f;

        float dx=lastDirX, dy=lastDirY;
        if (Math.abs(dx)<1e-5f && Math.abs(dy)<1e-5f){ dx=0f; dy=1f; }
        float baseAngle = MathUtils.atan2(dy, dx) * MathUtils.radiansToDegrees;

        float t = MathUtils.clamp(swingTimer/swingTotal, 0f,1f);
        float arc = (swordRightHand ? -1f : 1f)*swingArcDeg;
        float cur = (-arc*0.5f) + arc*t;

        float nx=-dy, ny=dx;
        float handDist = Math.min(playerBounds.width, playerBounds.height)*0.35f;
        float side = swordRightHand?1f:-1f;
        float hx = px + nx*handDist*side;
        float hy = py + ny*handDist*side;

        swordSprite.setRotation(baseAngle + cur);
        swordSprite.setPosition(hx - swordSprite.getOriginX(), hy - swordSprite.getOriginY());
        swordSprite.draw(batch);
    }

    // ------------------------------------------------------------------------
    // Enemies update (per-axis move + slide + stuck recovery)
    // ------------------------------------------------------------------------
    private void updateEnemies(float dt){
        int pTx = worldToTileX(playerBounds.x + playerBounds.width*0.5f);
        int pTy = worldToTileY(playerBounds.y + playerBounds.height*0.5f);

        for (Enemy e : enemies){
            e.update(dt);

            // --- Archer wind-up and fire logic ---
            if (e instanceof Archer) {
                Archer a = (Archer)e;
                Float draw = archerDrawT.get(a);
                if (draw != null) {
                    draw -= dt;
                    if (draw <= 0f) {
                        float px = playerBounds.x + playerBounds.width*0.5f;
                        float py = playerBounds.y + playerBounds.height*0.5f;
                        shootArrowFrom(a, px, py);
                        archerDrawT.remove(a);
                        a.fireTimer = archerFireInterval;
                    } else {
                        archerDrawT.put(a, draw);
                    }
                } else {
                    a.fireTimer -= dt;
                    if (a.fireTimer <= 0f) {
                        // start the draw animation
                        archerDrawT.put(a, archerDrawWindup);
                    }
                }
            }

            if (e instanceof Berserker) {
                updateBerserker((Berserker)e, dt);
                continue;
            }

            PathState ps = paths.get(e);
            if (ps == null) { ps = new PathState(); paths.put(e, ps); initStuckTrack(e, ps); }

            ps.repathTimer -= dt;
            if (ps.nudgeCooldown > 0f) ps.nudgeCooldown -= dt;

            boolean needRepath = (ps.repathTimer<=0f) || (ps.lastTargetTx!=pTx || ps.lastTargetTy!=pTy);

            Rectangle rb = e.getBoundingBox();
            int eTx = worldToTileX(rb.x + rb.width*0.5f);
            int eTy = worldToTileY(rb.y + rb.height*0.5f);

            if (needRepath){
                ps.waypoints.clear();
                findPathAStar(eTx, eTy, pTx, pTy, ps.waypoints);
                smoothWaypoints(ps.waypoints, rb.x + rb.width*0.5f, rb.y + rb.height*0.5f);
                ps.current = 0;
                ps.repathTimer = REPTH_INTERVAL;
                ps.lastTargetTx = pTx; ps.lastTargetTy = pTy;
            }

            // choose target waypoint (or player center as fallback)
            Vector2 target = null;
            while (ps.current < ps.waypoints.size) {
                Vector2 wp = ps.waypoints.get(ps.current);
                float dx = (rb.x + rb.width*0.5f) - wp.x;
                float dy = (rb.y + rb.height*0.5f) - wp.y;
                if (dx*dx + dy*dy < 14f*14f) ps.current++;
                else { target = wp.cpy(); target.add(clearanceNudgeAtWorld(target.x, target.y, 0.18f*TILE_SIZE)); break; }
            }
            if (target == null) target = new Vector2(
                    playerBounds.x + playerBounds.width*0.5f,
                    playerBounds.y + playerBounds.height*0.5f
            );

            // PER-AXIS MOVE + SLIDE
            float cx = rb.x + rb.width*0.5f, cy = rb.y + rb.height*0.5f;
            float dx = target.x - cx, dy = target.y - cy;
            float len = (float)Math.sqrt(dx*dx + dy*dy);
            if (len < 1e-4f) len = 1f;
            dx /= len; dy /= len;

            float step = e.getSpeed() * dt;
            float moveX = dx * step;
            float moveY = dy * step;

            float oldX = rb.x, oldY = rb.y;

            // X first
            rb.x = oldX + moveX;
            if (enemyRectBlockedInset(rb)) rb.x = oldX;

            // Y next
            rb.y = oldY + moveY;
            if (enemyRectBlockedInset(rb)) rb.y = oldY;

            // STUCK RECOVERY
            updateStuckTrack(e, ps, dt);
        }

        // separate a bit (circle push)
        separateEnemies();

        // contact rules with player
        for (int i = enemies.size-1; i>=0; --i) {
            Enemy e = enemies.get(i);
            if (!e.getBoundingBox().overlaps(playerBounds)) continue;

            if (e instanceof Berserker) {
                Berserker b = (Berserker)e;
                if (b.state == Berserker.State.DASHING && !b.hasDealtDashDamage) {
                    playerTakeDamage(20);
                    b.hasDealtDashDamage = true; // only once per dash
                }
                continue; // Berserker never despawns on touch
            }

            // Bomber drops bomb & despawns
            if (e instanceof Bomber) {
                Rectangle br=e.getBoundingBox();
                dropBombAt(br.x + br.width*0.5f, br.y + br.height*0.5f);
            }

            // Apply each enemy’s touch damage then despawn
            if (e instanceof Goblin)         playerTakeDamage(3);
            else if (e instanceof Hobgoblin) playerTakeDamage(5);
            else if (e instanceof Archer)    playerTakeDamage(3);

            dropCoin(e);
            maybeDropOrb(e);
            enemies.removeIndex(i);
            paths.remove(e);
            if (e instanceof Archer) archerDrawT.remove((Archer)e);
        }
    }

    private void initStuckTrack(Enemy e, PathState ps){
        Rectangle rb = e.getBoundingBox();
        ps.lastX = rb.x; ps.lastY = rb.y;
        ps.stillTimer = 0f;
        ps.nudgeCooldown = 0f;
    }

    private void updateStuckTrack(Enemy e, PathState ps, float dt){
        Rectangle rb = e.getBoundingBox();
        float moved = Math.abs(rb.x - ps.lastX) + Math.abs(rb.y - ps.lastY);

        if (moved < STUCK_SPEED_EPS) {
            ps.stillTimer += dt;
        } else {
            ps.stillTimer = 0f;
            ps.lastX = rb.x; ps.lastY = rb.y;
        }

        if (ps.stillTimer >= STUCK_TIME && ps.nudgeCooldown <= 0f) {
            // small perpendicular nudge to break corner pinches
            float jitterAngle = MathUtils.randomBoolean() ? 90f : -90f;
            float angle = MathUtils.atan2(
                    (ps.lastY - rb.y), (ps.lastX - rb.x)) * MathUtils.radiansToDegrees;
            if (Float.isNaN(angle)) angle = MathUtils.random(0f, 360f);
            float nudgeDir = (angle + jitterAngle) * MathUtils.degreesToRadians;

            float nx = MathUtils.cos(nudgeDir) * STUCK_RECOVER_STEP;
            float ny = MathUtils.sin(nudgeDir) * STUCK_RECOVER_STEP;

            float oldX = rb.x, oldY = rb.y;
            rb.x += nx; if (enemyRectBlockedInset(rb)) rb.x = oldX;
            rb.y += ny; if (enemyRectBlockedInset(rb)) rb.y = oldY;

            // force a quick repath soon
            PathState ps2 = paths.get(e);
            if (ps2 != null) ps2.repathTimer = Math.min(ps2.repathTimer, 0.05f);

            // cooldown and reset
            ps.nudgeCooldown = STUCK_REPATH_COOLDOWN;
            ps.stillTimer = 0f;
            ps.lastX = rb.x; ps.lastY = rb.y;
        }
    }

    private void playerTakeDamage(int dmg){
        if (dmg<=0 || playerHealth<=0) return;
        playerHealth = Math.max(0, playerHealth - dmg);
        hitFlashTimer = hitFlashDuration;
    }

    private void updateBerserker(Berserker b, float dt){
        b.update(dt);

        Rectangle rb = b.getBoundingBox();
        float bx = rb.x + rb.width*0.5f, by = rb.y + rb.height*0.5f;
        float px = playerBounds.x + playerBounds.width*0.5f;
        float py = playerBounds.y + playerBounds.height*0.5f;

        switch (b.state){
            case APPROACH: {
                // path like others
                PathState ps = paths.get(b);
                if (ps == null) { ps = new PathState(); paths.put(b, ps); initStuckTrack(b, ps); }
                int pTx = worldToTileX(px), pTy = worldToTileY(py);
                int eTx = worldToTileX(bx), eTy = worldToTileY(by);
                ps.repathTimer -= dt;
                if (ps.repathTimer <= 0f || ps.lastTargetTx!=pTx || ps.lastTargetTy!=pTy) {
                    ps.waypoints.clear();
                    findPathAStar(eTx, eTy, pTx, pTy, ps.waypoints);
                    smoothWaypoints(ps.waypoints, bx, by);
                    ps.current = 0; ps.repathTimer = REPTH_INTERVAL;
                    ps.lastTargetTx = pTx; ps.lastTargetTy = pTy;
                }
                Vector2 target=null;
                while (ps.current < ps.waypoints.size) {
                    Vector2 wp = ps.waypoints.get(ps.current);
                    if (wp.dst2(bx,by) < 14f*14f) ps.current++;
                    else { target=wp.cpy(); target.add(clearanceNudgeAtWorld(target.x,target.y,0.18f*TILE_SIZE)); break; }
                }
                if (target==null) target = new Vector2(px,py);

                // per-axis approach movement (slide)
                float dx = target.x - (rb.x + rb.width*0.5f);
                float dy = target.y - (rb.y + rb.height*0.5f);
                float len = (float)Math.sqrt(dx*dx + dy*dy);
                if (len < 1e-4f) len = 1f;
                dx/=len; dy/=len;

                float step = 340f * dt; // approach speed
                float moveX = dx * step;
                float moveY = dy * step;

                float oldX = rb.x, oldY = rb.y;
                rb.x = oldX + moveX; if (enemyRectBlockedInset(rb)) rb.x = oldX;
                rb.y = oldY + moveY; if (enemyRectBlockedInset(rb)) rb.y = oldY;

                // stuck tracker
                updateStuckTrack(b, ps, dt);

                // start charge a bit farther away (3.5 tiles)
                if (Vector2.dst2(bx,by,px,py) <= (3.5f*TILE_SIZE)*(3.5f*TILE_SIZE)) {
                    b.state = Berserker.State.CHARGING;
                    b.chargeTimer = (BOMB_TICKS * BOMB_TICK_INTERVAL) * 0.5f; // ~half the bomb time
                    b.shakeSeed = MathUtils.random(10000f);
                    b.hasDealtDashDamage = false;
                }
                break;
            }
            case CHARGING: {
                b.chargeTimer -= dt;
                if (b.chargeTimer <= 0f) {
                    // lock direction now
                    float cpx = playerBounds.x + playerBounds.width*0.5f;
                    float cpy = playerBounds.y + playerBounds.height*0.5f;
                    b.dashDir.set(cpx - bx, cpy - by).nor();
                    b.state = Berserker.State.DASHING;
                    b.dashTimer = 0.8f; // short dash
                }
                break;
            }
            case DASHING: {
                float ox=rb.x, oy=rb.y;
                float step = b.dashSpeed * dt;
                rb.x += b.dashDir.x * step; if (enemyRectBlockedInset(rb)) rb.x = ox;
                rb.y += b.dashDir.y * step; if (enemyRectBlockedInset(rb)) rb.y = oy;
                b.dashTimer -= dt;
                if (b.dashTimer <= 0f) {
                    b.state = Berserker.State.RECOVER;
                    b.recoverTimer = 0.4f;
                }
                break;
            }
            case RECOVER: {
                b.recoverTimer -= dt;
                if (b.recoverTimer <= 0f) b.state = Berserker.State.APPROACH;
                break;
            }
        }
    }

    // ------------------------------------------------------------------------
    // Arrows / Orbs / Coins / Bombs
    // ------------------------------------------------------------------------
    private void updateArrows(float dt){
        for (int i = arrows.size-1; i>=0; --i){
            Arrow a = arrows.get(i);
            a.bounds.x += a.vx * dt;
            a.bounds.y += a.vy * dt;

            if (dungeon.rectBlocked(a.bounds)) { arrows.removeIndex(i); continue; }
            if (a.bounds.overlaps(playerBounds)) {
                playerTakeDamage(a.damage);
                arrows.removeIndex(i);
                continue;
            }
            // cull far away
            float m=600f, L=viewLeft()-m, B=viewBottom()-m, R=L+viewWidth()+2*m, T=B+viewHeight()+2*m;
            if (a.bounds.x < L || a.bounds.x > R || a.bounds.y < B || a.bounds.y > T) arrows.removeIndex(i);
        }
    }

    private void updateOrbs(float dt){
        for (int i = orbs.size-1; i>=0; --i){
            if (orbs.get(i).bounds.overlaps(playerBounds)) {
                if (playerHealth < playerHealthMax) playerHealth = Math.min(playerHealthMax, playerHealth + ORB_HEAL);
                orbs.removeIndex(i);
            }
        }
    }

    private void updateCoins(float dt){
        for (int i = coins.size-1; i>=0; --i){
            Coin c = coins.get(i);
            c.bobT += dt;
            c.drawOffsetY = MathUtils.sin(c.bobT * 7f) * 2f;
            if (c.bounds.overlaps(playerBounds)) {
                coinCount += c.value;
                coins.removeIndex(i);
            }
        }
    }

    private void updateBombs(float dt){
        for (int i = bombs.size-1; i>=0; --i){
            Bomb b = bombs.get(i);
            if (!b.exploded){
                b.tickTimer -= dt;
                if (b.tickTimer <= 0f){
                    b.ticksRemaining--;
                    b.tickTimer += BOMB_TICK_INTERVAL;
                    if (b.ticksRemaining <= 0){
                        b.exploded = true;
                        b.explosionTimer = 0.18f;

                        float px = playerBounds.x + playerBounds.width*0.5f;
                        float py = playerBounds.y + playerBounds.height*0.5f;
                        if (Vector2.dst(px,py,b.x,b.y) <= BOMB_RADIUS) playerTakeDamage(BOMB_DAMAGE);

                        // swat nearby arrows
                        for (int a = arrows.size-1; a>=0; --a){
                            Arrow ar = arrows.get(a);
                            float ax = ar.bounds.x + ar.bounds.width*0.5f;
                            float ay = ar.bounds.y + ar.bounds.height*0.5f;
                            if (Vector2.dst(ax,ay,b.x,b.y) <= BOMB_RADIUS) arrows.removeIndex(a);
                        }
                    }
                }
            } else {
                b.explosionTimer -= dt;
                if (b.explosionTimer <= 0f) bombs.removeIndex(i);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Pathing
    // ------------------------------------------------------------------------
    private boolean enemyRectBlockedInset(Rectangle r){
        tmpCollRect.set(
                r.x + ENEMY_COLLISION_INSET,
                r.y + ENEMY_COLLISION_INSET,
                Math.max(1f, r.width  - 2f*ENEMY_COLLISION_INSET),
                Math.max(1f, r.height - 2f*ENEMY_COLLISION_INSET)
        );
        return dungeon.rectBlocked(tmpCollRect);
    }

    private static class Node {
        int x,y; float g,f; Node parent;
        Node(int x,int y,float g,float f,Node p){this.x=x;this.y=y;this.g=g;this.f=f;this.parent=p;}
    }

    private void findPathAStar(int sx,int sy,int tx,int ty, Array<Vector2> out){
        out.clear();
        if (!inBoundsTiles(sx,sy) || !inBoundsTiles(tx,ty)) return;

        if (dungeon.isSolid(tx,ty) || clearanceAtTile(tx,ty) < MIN_CLEAR_TILES) {
            int bestX=tx, bestY=ty;
            for (int r=1;r<=4;r++){
                for (int dx=-r; dx<=r; dx++){
                    int x=tx+dx, y1=ty+r, y2=ty-r;
                    if (inBoundsTiles(x,y1)&&!dungeon.isSolid(x,y1)&&clearanceAtTile(x,y1)>=MIN_CLEAR_TILES){bestX=x;bestY=y1;break;}
                    if (inBoundsTiles(x,y2)&&!dungeon.isSolid(x,y2)&&clearanceAtTile(x,y2)>=MIN_CLEAR_TILES){bestX=x;bestY=y2;break;}
                }
                for (int dy=-r+1; dy<=r-1; dy++){
                    int y=ty+dy, x1=tx+r, x2=tx-r;
                    if (inBoundsTiles(x1,y)&&!dungeon.isSolid(x1,y)&&clearanceAtTile(x1,y)>=MIN_CLEAR_TILES){bestX=x1;bestY=y;break;}
                    if (inBoundsTiles(x2,y)&&!dungeon.isSolid(x2,y)&&clearanceAtTile(x2,y)>=MIN_CLEAR_TILES){bestX=x2;bestY=y;break;}
                }
            }
            tx=bestX; ty=bestY;
        }

        if (sx==tx && sy==ty) { out.add(tileCenter(tx,ty)); return; }

        final int W=dungeon.width,H=dungeon.height;
        final int[] dx8={1,-1,0,0, 1, 1,-1,-1};
        final int[] dy8={0,0,1,-1,1,-1, 1,-1};
        final float[] stepCost={1,1,1,1,1.41421356f,1.41421356f,1.41421356f,1.41421356f};

        float[][] bestG=new float[W][H];
        for(int x=0;x<W;x++) for(int y=0;y<H;y++) bestG[x][y]=Float.POSITIVE_INFINITY;

        Array<Node> open=new Array<>(256);
        Node start=new Node(sx,sy,0f,heuristicOctile(sx,sy,tx,ty),null);
        open.add(start); bestG[sx][sy]=0f;

        Node goal=null;
        int guard=15000;
        while (open.size>0 && guard-- > 0){
            int bi=0; float bf=open.get(0).f;
            for (int i=1;i<open.size;i++){ if(open.get(i).f<bf){bf=open.get(i).f;bi=i;} }
            Node cur=open.removeIndex(bi);
            if (cur.x==tx && cur.y==ty) { goal=cur; break; }

            for (int i=0;i<8;i++){
                int nx=cur.x+dx8[i], ny=cur.y+dy8[i];
                if (!inBoundsTiles(nx,ny) || dungeon.isSolid(nx,ny)) continue;

                float clear = clearanceAtTile(nx,ny);
                if (clear < MIN_CLEAR_TILES) continue;
                if (i>=4){
                    if (dungeon.isSolid(cur.x+dx8[i],cur.y) || dungeon.isSolid(cur.x, cur.y+dy8[i])) continue;
                }
                float ng = cur.g + stepCost[i];
                if (clear < PREFERRED_CLEAR_TILES) ng += CLEAR_SOFTCOST*(PREFERRED_CLEAR_TILES - clear);

                if (ng < bestG[nx][ny]){
                    bestG[nx][ny]=ng;
                    float nf = ng + heuristicOctile(nx,ny,tx,ty);
                    open.add(new Node(nx,ny,ng,nf,cur));
                }
            }
        }
        if (goal==null) return;

        Array<Vector2> rev=new Array<>();
        for(Node n=goal; n!=null; n=n.parent) rev.add(tileCenter(n.x,n.y));
        for (int i=rev.size-1; i>=0; --i) out.add(rev.get(i));
        if (out.size>=2){
            Vector2 first=out.get(0), here=tileCenter(sx,sy);
            if (first.dst2(here)<1f) out.removeIndex(0);
        }
    }

    private void smoothWaypoints(Array<Vector2> wps, float startX, float startY){
        if (wps.size<=2) return;
        Array<Vector2> out=new Array<>(wps.size);
        Vector2 curr=new Vector2(startX,startY);
        int i=0;
        while (i<wps.size){
            int far=i;
            for (int j=i;j<wps.size;j++){
                if (hasLineOfSight(curr.x,curr.y, wps.get(j).x, wps.get(j).y)) far=j;
                else break;
            }
            Vector2 chosen=wps.get(far).cpy();
            chosen.add(clearanceNudgeAtWorld(chosen.x, chosen.y, 0.18f*TILE_SIZE));
            out.add(chosen);
            curr.set(wps.get(far));
            i=far+1;
        }
        wps.clear(); wps.addAll(out);
    }

    private boolean hasLineOfSight(float x0,float y0,float x1,float y1){
        float dx=x1-x0, dy=y1-y0;
        float dist=(float)Math.sqrt(dx*dx+dy*dy);
        if (dist<1f) return true;
        int steps = Math.max(2, (int)(dist/(TILE_SIZE*0.5f)));
        float sx=dx/steps, sy=dy/steps;
        float x=x0, y=y0;
        Rectangle probe=new Rectangle(x,y,1f,1f);
        for (int i=0;i<=steps;i++){
            probe.setPosition(x,y);
            if (dungeon.rectBlocked(probe)) return false;
            int tx=worldToTileX(x), ty=worldToTileY(y);
            if (clearanceAtTile(tx,ty) < MIN_CLEAR_TILES) return false;
            x+=sx; y+=sy;
        }
        return true;
    }

    private Vector2 clearanceNudgeAtWorld(float wx,float wy,float scalePixels){
        int tx=worldToTileX(wx), ty=worldToTileY(wy);
        float cR=clearanceAtTile(tx+1,ty), cL=clearanceAtTile(tx-1,ty);
        float cU=clearanceAtTile(tx,ty+1), cD=clearanceAtTile(tx,ty-1);
        Vector2 g=new Vector2((cR-cL)*0.5f, (cU-cD)*0.5f);
        if (g.len2()>1e-6f) g.nor().scl(scalePixels);
        return g;
    }

    private void separateEnemies(){
        for (int i=0;i<enemies.size;i++){
            Enemy a=enemies.get(i); Rectangle ra=a.getBoundingBox();
            float ax=ra.x + ra.width*0.5f, ay=ra.y + ra.height*0.5f;
            float ar = Math.min(ra.width,ra.height)*0.5f;
            for (int j=i+1;j<enemies.size;j++){
                Enemy b=enemies.get(j); Rectangle rb=b.getBoundingBox();
                float bx=rb.x + rb.width*0.5f, by=rb.y + rb.height*0.5f;
                float br = Math.min(rb.width,rb.height)*0.5f;

                float dx=bx-ax, dy=by-ay, d2=dx*dx+dy*dy, minDist=ar+br;
                if (d2<minDist*minDist && d2>1e-5f){
                    float d=(float)Math.sqrt(d2), push=(minDist-d)*0.5f;
                    float nx=dx/d, ny=dy/d;
                    float oldAx=ra.x, oldAy=ra.y, oldBx=rb.x, oldBy=rb.y;
                    ra.x -= nx*push; if (enemyRectBlockedInset(ra)) ra.x=oldAx;
                    ra.y -= ny*push; if (enemyRectBlockedInset(ra)) ra.y=oldAy;
                    rb.x += nx*push; if (enemyRectBlockedInset(rb)) rb.x=oldBx;
                    rb.y += ny*push; if (enemyRectBlockedInset(rb)) rb.y=oldBy;
                }
            }
        }
    }

    // ------------------------------------------------------------------------
    // Spawning
    // ------------------------------------------------------------------------
    private void spawnRandomEnemyAnywhere(){
        float r=MathUtils.random();
        if      (r < 0.28f) spawnGoblinAnywhere();
        else if (r < 0.52f) spawnHobgoblinAnywhere();
        else if (r < 0.72f) spawnArcherAnywhere();
        else if (r < 0.88f) spawnBomberAnywhere();
        else                spawnBerserkerAnywhere();
    }

    private void spawnGoblinAnywhere(){ spawnAnywhere(new Goblin(goblinBodyTex,0,0,goblinSz.w,goblinSz.h)); }
    private void spawnHobgoblinAnywhere(){ spawnAnywhere(new Hobgoblin(hobgoblinBodyTex,0,0,hobSz.w,hobSz.h)); }
    private void spawnArcherAnywhere(){
        Archer a=new Archer(archerBodyTex,0,0,archerSz.w,archerSz.h);
        a.fireTimer=MathUtils.random(0.2f, archerFireInterval);
        spawnAnywhere(a);
    }
    private void spawnBomberAnywhere(){ spawnAnywhere(new Bomber(bomberBodyTex,0,0,bomberSz.w,bomberSz.h)); }

    private void spawnBerserkerAnywhere(){
        Berserker b = new Berserker(berserkerBodyTex,0,0,berserkSz.w,berserkSz.h);
        spawnAnywhere(b);
        Gdx.app.log("SPAWN", "Berserker HP=" + b.getHealth() + " / " + b.getMaxHealth());
    }

    private void spawnAnywhere(Enemy e){
        Rectangle rb=e.getBoundingBox();
        float minDistFromPlayer = 8f*TILE_SIZE;
        int tries=80;
        while (tries-- > 0){
            int tx=MathUtils.random(1, dungeon.width-2);
            int ty=MathUtils.random(1, dungeon.height-2);
            if (dungeon.isSolid(tx,ty)) continue;
            if (clearanceAtTile(tx,ty) < MIN_CLEAR_TILES) continue;

            float cx=tx*TILE_SIZE+TILE_SIZE*0.5f;
            float cy=ty*TILE_SIZE+TILE_SIZE*0.5f;
            rb.setPosition(cx - rb.width*0.5f, cy - rb.height*0.5f);
            if (enemyRectBlockedInset(rb)) continue;

            float px=playerBounds.x + playerBounds.width*0.5f;
            float py=playerBounds.y + playerBounds.height*0.5f;
            if (Vector2.dst2(cx,cy,px,py) < minDistFromPlayer*minDistFromPlayer) continue;
            break;
        }
        enemies.add(e);
        PathState ps = new PathState();
        paths.put(e, ps);
        initStuckTrack(e, ps);

        // TEMP: detect accidental duplicate references in the enemies array
        int dupCount = 0;
        int id = System.identityHashCode(e);
        for (int k = 0; k < enemies.size; k++) {
            if (System.identityHashCode(enemies.get(k)) == id) dupCount++;
        }
        if (dupCount > 1) {
            Gdx.app.log("WARN", "Duplicate enemy instance in list: " + e.getClass().getSimpleName()
                + " refs=" + dupCount + " id=" + id);
        }
    }

    private void shootArrowFrom(Archer archer,float targetX,float targetY){
        Rectangle rb=archer.getBoundingBox();
        float sx=rb.x + rb.width*0.5f, sy=rb.y + rb.height*0.5f;
        float dx=targetX - sx, dy=targetY - sy, len=(float)Math.sqrt(dx*dx + dy*dy);
        if (len<1e-4f) return;
        dx/=len; dy/=len;
        Arrow a = new Arrow(arrowTex, sx-9f, sy-3f, 18f, 6f, dx*arrowSpeed, dy*arrowSpeed, arrowDamage);
        if (dungeon.rectBlocked(a.bounds)) {
            float[] p=dungeon.nearestOpen(a.bounds.x, a.bounds.y, 6);
            a.bounds.x=p[0]; a.bounds.y=p[1];
        }
        arrows.add(a);
    }

    private void dropBombAt(float wx,float wy){ bombs.add(new Bomb(wx,wy)); }

    private void maybeDropOrb(Enemy e){
        if (MathUtils.random() < ORB_DROP_CHANCE) {
            Rectangle b=e.getBoundingBox();
            float cx=b.x + b.width*0.5f, cy=b.y + b.height*0.5f;
            // orbs drawn below coins
            orbs.add(new Orb(orbTex, cx-8, cy-8, 16, 16));
        }
    }

    private void dropCoin(Enemy e) {
        Rectangle b = e.getBoundingBox();
        float cx = b.x + b.width*0.5f, cy = b.y + b.height*0.5f;
        int value = MathUtils.random(1,3);
        coins.add(new Coin(coinTex, cx-8, cy-8, 16, 16, value));
    }

    private void snapRectToOpen(Rectangle r){
        float cx=r.x + r.width*0.5f, cy=r.y + r.height*0.5f;
        float[] p = nearestOpenWithClearance(cx,cy,12);
        r.x=p[0] - r.width*0.5f; r.y=p[1] - r.height*0.5f;
    }

    // ------------------------------------------------------------------------
    // HUD
    // ------------------------------------------------------------------------
    private void drawHUD(){
        OrthographicCamera hud=new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hud.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hud.update();

        batch.setProjectionMatrix(hud.combined);
        batch.begin();

        float barW=280f, barH=18f, marginTop=16f;
        float x=(Gdx.graphics.getWidth()-barW)*0.5f;
        float y=Gdx.graphics.getHeight()-marginTop-barH;

        // HP bar
        batch.setColor(0f,0f,0f,0.6f); batch.draw(whiteTex, x-2, y-2, barW+4, barH+4);
        batch.setColor(0.6f,0.1f,0.1f,1f); batch.draw(whiteTex, x, y, barW, barH);
        float hpPct = Math.max(0f, Math.min(1f, (float)playerHealth/playerHealthMax));
        batch.setColor(0.15f,0.9f,0.2f,1f); batch.draw(whiteTex, x, y, barW*hpPct, barH);

        batch.setColor(Color.WHITE);
        String hpText = playerHealth + " / " + playerHealthMax;
        glyph.setText(font, hpText);
        float tx = x + (barW - glyph.width)*0.5f;
        float ty = y + (barH + glyph.height)*0.5f;
        font.draw(batch, glyph, tx, ty);

        // Dash bar
        float dashGap=8f, dashH=10f, dy=y - dashGap - dashH;
        batch.setColor(0f,0f,0f,0.6f); batch.draw(whiteTex, x-2, dy-2, barW+4, dashH+4);
        batch.setColor(0.20f,0.20f,0.08f,1f); batch.draw(whiteTex, x, dy, barW, dashH);
        float dashPct = 1f - Math.max(0f, Math.min(1f, dashCdTimer / dashCooldown));
        batch.setColor(0.95f,0.85f,0.20f,1f); batch.draw(whiteTex, x, dy, barW*dashPct, dashH);

        // Coin HUD (top-right)
        float cPad=16f, cSize=24f, cx=Gdx.graphics.getWidth()-cPad-cSize, cy=Gdx.graphics.getHeight()-cPad-cSize;
        batch.setColor(Color.WHITE); batch.draw(coinTex, cx, cy, cSize, cSize);
        String cText = String.valueOf(coinCount);
        glyph.setText(font, cText);
        font.draw(batch, glyph, cx - 8f - glyph.width, cy + glyph.height + 2f);

        batch.end();
        batch.setProjectionMatrix(camera.combined);
    }

    // ------------------------------------------------------------------------
    // Drawing helpers (faces, bow, bombs, textures)
    // ------------------------------------------------------------------------
    private void drawArcherBow(Archer a){
        // simple curved bow with draw animation
        Rectangle rb = a.getBoundingBox();
        float ax = rb.x + rb.width*0.5f;
        float ay = rb.y + rb.height*0.5f;

        float px = playerBounds.x + playerBounds.width*0.5f;
        float py = playerBounds.y + playerBounds.height*0.5f;

        float dx = px - ax, dy = py - ay;
        float len = (float)Math.sqrt(dx*dx + dy*dy); if (len<1e-4f){ dx=1f; dy=0f; len=1f; }
        dx/=len; dy/=len;

        Float drawT = archerDrawT.get(a);
        float progress = 0f;
        if (drawT != null && drawT > 0f) progress = MathUtils.clamp(1f - (drawT / archerDrawWindup), 0f, 1f);

        // tiny “string”
        float angleDeg = MathUtils.atan2(dy,dx)*MathUtils.radiansToDegrees;
        float grip = Math.min(rb.width, rb.height)*0.6f;
        float bx = ax + dx*grip;
        float by = ay + dy*grip;

        // bow arc – draw with thick segments
        drawCurvedBow(bx, by, angleDeg, progress);

        // mini arrow at rest while drawing
        if (progress > 0f) {
            float arrowW=14f, arrowH=3f;
            Sprite s = rotSprite;
            s.setRegion(whiteTex);
            s.setSize(arrowW, arrowH);
            s.setOrigin(0f, arrowH*0.5f);
            s.setRotation(angleDeg);
            s.setPosition(bx - 4f, by - arrowH*0.5f);
            s.setColor(0.95f,0.9f,0.2f,1f);
            s.draw(batch);
            s.setColor(Color.WHITE);
        }
    }

    private void drawCurvedBow(float cx, float cy, float angleDeg, float drawProgress){
        // simple crescent
        float radius = 28f;
        float thickness = 6f;
        int segments = 28;
        float start = -60f * MathUtils.degreesToRadians;
        float end   =  60f * MathUtils.degreesToRadians;

        // rotate frame
        float ang = angleDeg * MathUtils.degreesToRadians;
        float cos= MathUtils.cos(ang), sin=MathUtils.sin(ang);

        float px=0, py=0; boolean has=false;
        for (int i=0;i<=segments;i++){
            float t = MathUtils.lerp(start,end, i/(float)segments);
            float x = MathUtils.cos(t) * radius;
            float y = MathUtils.sin(t) * radius;
            float rx = cx + x*cos - y*sin;
            float ry = cy + x*sin + y*cos;
            if (has) drawThickSegment(px,py, rx,ry, thickness);
            px=rx; py=ry; has=true;
        }
    }

    private void renderBombs(SpriteBatch batch){
        for (Bomb b : bombs){
            if (!b.exploded){
                float phase = 1f - MathUtils.clamp(b.tickTimer / BOMB_TICK_INTERVAL, 0f,1f);
                float pulse = 0.6f + 0.4f * MathUtils.sin(phase * MathUtils.PI);
                Color ring = new Color(1f, 0.85f, 0.25f, 0.55f*pulse);
                drawCircleOutline(b.x, b.y, BOMB_RADIUS, 3f, 96, ring);

                batch.setColor(0.85f,0.10f,0.10f,1f);
                batch.draw(whiteTex, b.x-10f, b.y-10f, 20f, 20f);
                batch.setColor(Color.WHITE);

                if (phase > 0.85f){
                    float rr = 14f * (0.2f + 0.8f*(phase - 0.85f)/0.15f);
                    batch.setColor(1f,0.95f,0.3f,0.6f);
                    batch.draw(whiteTex, b.x-rr, b.y-rr, 2*rr, 2*rr);
                    batch.setColor(Color.WHITE);
                }
            } else {
                float a = MathUtils.clamp(b.explosionTimer / 0.18f, 0f,1f);
                drawCircleOutline(b.x, b.y, BOMB_RADIUS, 6f, 96, new Color(1f,0.35f,0.15f,0.35f*a));
            }
        }
    }

    private void drawFaceOverlay(FaceStyle style, float x,float y,float w,float h, boolean blink, boolean special){
        float eyeW=Math.max(3f,w/8f), eyeH=Math.max(3f,h/8f);
        float eyeY=y + h*0.65f;
        float eyeX1=x + w*0.30f - eyeW*0.5f;
        float eyeX2=x + w*0.70f - eyeW*0.5f;
        float drawnEyeH = blink ? Math.max(1f, eyeH*0.12f) : eyeH;

        switch (style){
            case PLAYER_SMILE:{
                batch.setColor(Color.BLACK);
                batch.draw(whiteTex, eyeX1, eyeY, eyeW, drawnEyeH);
                batch.draw(whiteTex, eyeX2, eyeY, eyeW, drawnEyeH);
                float mw=w*0.52f, sx=x+(w-mw)*0.5f, ex=sx+mw, my=y+h*0.30f, cy2=my - Math.max(5f,h*0.06f);
                drawCurvedMouth(sx,my, ex,my, (sx+ex)/2f, cy2, Math.max(2.5f, h/18f));
                if (special){ // attack brows
                    float browLen = eyeW*1.4f, browThk=Math.max(2.5f,h/28f);
                    float ex1=eyeX1+eyeW*0.5f, ex2=eyeX2+eyeW*0.5f, eyb=eyeY+drawnEyeH+6f;
                    drawRotRect(ex1,eyb, browLen,browThk, -20f, Color.BLACK);
                    drawRotRect(ex2,eyb, browLen,browThk, +20f, Color.BLACK);
                }
                batch.setColor(Color.WHITE);
                break;
            }
            case GOBLIN_DEVIOUS:{
                float eW=eyeW, eH=drawnEyeH;
                float ex1=eyeX1+eW*0.5f, ex2=eyeX2+eW*0.5f, ey=eyeY+eH+6f;
                float browLen=eW*1.15f, browThk=Math.max(2.5f,h/30f);
                drawRotRect(ex1,ey,browLen,browThk,-25f, Color.BLACK);
                drawRotRect(ex2,ey,browLen,browThk,+25f, Color.BLACK);
                batch.setColor(Color.BLACK);
                batch.draw(whiteTex, eyeX1, eyeY, eW, eH);
                batch.draw(whiteTex, eyeX2, eyeY, eW, eH);
                float mw2=w*0.46f, mx=x+(w-mw2)*0.5f, my2=y+h*0.28f, th=Math.max(3f,h/16f), seg=mw2/3f;
                batch.draw(whiteTex, mx, my2+1f, seg, th-1f);
                batch.draw(whiteTex, mx+seg, my2, seg, th);
                batch.draw(whiteTex, mx+2*seg, my2+1f, seg, th-1f);
                batch.setColor(Color.WHITE);

                // tusks
                drawTusksFor(x, y, w, h, false);
                break;
            }
            case HOB_DEVIOUS:{
                batch.setColor(Color.BLACK);
                float browH=Math.max(3f,h/22f);
                batch.draw(whiteTex, eyeX1-2f, eyeY+drawnEyeH+4f, eyeW+6f, browH);
                batch.draw(whiteTex, eyeX2-2f, eyeY+drawnEyeH+4f, eyeW+6f, browH);
                batch.draw(whiteTex, eyeX1, eyeY, eyeW, drawnEyeH);
                batch.draw(whiteTex, eyeX2, eyeY, eyeW, drawnEyeH);
                float my=y+h*0.28f, mw=w*0.50f, mx=x+(w-mw)*0.5f, th=Math.max(3f,h/16f);
                batch.draw(whiteTex, mx, my+1f, mw/3f, th-1f);
                batch.draw(whiteTex, mx+mw/3f, my,    mw/3f, th);
                batch.draw(whiteTex, mx+2*mw/3f, my+1f, mw/3f, th-1f);
                batch.setColor(Color.WHITE);

                // bigger tusks
                drawTusksFor(x, y, w, h, true);
                break;
            }
            case ARCHER_MASK:{
                batch.setColor(0.1f,0.1f,0.1f,1f);
                float bandH=Math.max(4f,h/6f), bandY=eyeY - bandH*0.5f;
                batch.draw(whiteTex, x+w*0.15f, bandY, w*0.70f, bandH);
                batch.setColor(Color.WHITE);
                batch.draw(whiteTex, eyeX1, eyeY, eyeW, drawnEyeH);
                batch.draw(whiteTex, eyeX2, eyeY, eyeW, drawnEyeH);
                batch.setColor(Color.BLACK);
                batch.draw(whiteTex, eyeX1+eyeW/3f, eyeY, eyeW/3f, drawnEyeH);
                batch.draw(whiteTex, eyeX2+eyeW/3f, eyeY, eyeW/3f, drawnEyeH);
                batch.draw(whiteTex, x+w*0.5f-w*0.12f, y+h*0.32f, w*0.24f, Math.max(2f,h/20f));
                batch.setColor(Color.WHITE);
                break;
            }
            case BOMBER_ANGRY:{
                batch.setColor(Color.BLACK);
                batch.draw(whiteTex, eyeX1-2f, eyeY+drawnEyeH+3f, eyeW*0.8f,2f);
                batch.draw(whiteTex, eyeX2+2f, eyeY+drawnEyeH+3f, -eyeW*0.8f,2f);
                batch.draw(whiteTex, eyeX1, eyeY, eyeW, drawnEyeH);
                batch.draw(whiteTex, eyeX2, eyeY, eyeW, drawnEyeH);
                batch.draw(whiteTex, x+w*0.5f-w*0.16f, y+h*0.32f, w*0.32f, Math.max(3f,h/18f));
                batch.setColor(Color.WHITE);
                break;
            }
            case BERSERKER_HELM:{
                batch.setColor(0.75f,0.75f,0.78f,1f);
                float bandH=Math.max(6f,h/8f), bandY=y+h*0.78f - bandH*0.5f;
                batch.draw(whiteTex, x+w*0.16f, bandY, w*0.68f, bandH);
                batch.setColor(0.95f,0.95f,0.92f,1f);
                drawHorn(x+w*0.16f, bandY+bandH*0.6f, w*0.18f, h*0.20f, +60f);
                drawHorn(x+w*0.84f, bandY+bandH*0.6f, w*0.18f, h*0.20f, -60f);
                batch.setColor(Color.BLACK);
                batch.draw(whiteTex, eyeX1-1f, eyeY+drawnEyeH+3f, eyeW*0.8f,2f);
                batch.draw(whiteTex, eyeX2+1f, eyeY+drawnEyeH+3f, -eyeW*0.8f,2f);
                batch.draw(whiteTex, eyeX1, eyeY, eyeW, drawnEyeH);
                batch.draw(whiteTex, eyeX2, eyeY, eyeW, drawnEyeH); // FIX: draw the right eye
                batch.draw(whiteTex, x+w*0.5f-w*0.14f, y+h*0.30f, w*0.28f, Math.max(3f,h/20f));
                batch.setColor(Color.WHITE);
                break;
            }
        }
    }

    // Tusks helpers
    private void drawTusksFor(float x, float y, float w, float h, boolean wider){
        float mouthY = y + h * 0.30f;
        float leftX  = x + w * 0.28f;
        float rightX = x + w * 0.72f;

        float tuskW = wider ? Math.max(3f, w * 0.05f) : Math.max(2.5f, w * 0.04f);
        float tuskH = Math.max(6f, h * 0.16f);

        drawTusk(leftX,  mouthY, tuskW, tuskH, -22f);
        drawTusk(rightX, mouthY, tuskW, tuskH, +22f);
    }

    private void drawTusk(float cx, float cy, float w, float h, float angleDeg){
        // white fang with small dark base so it “pops”
        drawRotRect(cx, cy, w, h, angleDeg, Color.WHITE);
        drawRotRect(cx, cy + Math.max(0.5f, h*0.06f), w, Math.max(1.5f, h * 0.15f), angleDeg, Color.BLACK);
    }

    private void drawHorn(float cx,float cy,float w,float h,float angle){
        rotSprite.setSize(w,h);
        rotSprite.setOrigin(w*0.1f, h*0.1f);
        rotSprite.setRotation(angle);
        rotSprite.setPosition(cx - w*0.1f, cy - h*0.1f);
        rotSprite.setColor(0.95f,0.95f,0.92f,1f);
        rotSprite.draw(batch);
        rotSprite.setColor(Color.WHITE);
    }

    private void drawCurvedMouth(float x0,float y0,float x1,float y1,float cx,float cy,float thickness){
        int segs=28; float lastX=x0,lastY=y0;
        for (int i=1;i<=segs;i++){
            float t=i/(float)segs, it=1f-t;
            // FIX: correct quadratic Bezier formula (was `it*it + x0` which blows up)
            float px=it*it*x0 + 2*it*t*cx + t*t*x1;   // FIX
            float py=it*it*y0 + 2*it*t*cy + t*t*y1;
            drawThickSegment(lastX,lastY, px,py, thickness);
            lastX=px; lastY=py;
        }
    }

    private void drawThickSegment(float x0,float y0,float x1,float y1,float thickness){
        float dx=x1-x0, dy=y1-y0;
        float len=(float)Math.sqrt(dx*dx+dy*dy);
        if (len<0.5f){
            batch.setColor(Color.BLACK);
            batch.draw(whiteTex, x0 - thickness*0.5f, y0 - thickness*0.5f, thickness, thickness);
            batch.setColor(Color.WHITE);
            return;
        }
        float angle = MathUtils.atan2(dy,dx)*MathUtils.radiansToDegrees;
        Sprite s=rotSprite;
        s.setRegion(whiteTex);
        s.setSize(len, thickness);
        s.setOrigin(0f, thickness*0.5f);
        s.setRotation(angle);
        s.setPosition(x0, y0 - thickness*0.5f);
        s.setColor(Color.BLACK);
        s.draw(batch);
        s.setColor(Color.WHITE);
    }

    private void drawRotRect(float cx,float cy,float w,float h,float angleDeg, Color color){
        rotSprite.setSize(w,h);
        rotSprite.setOrigin(w*0.5f,h*0.5f);
        rotSprite.setRotation(angleDeg);
        rotSprite.setPosition(cx - w*0.5f, cy - h*0.5f);
        rotSprite.setColor(color);
        rotSprite.draw(batch);
        rotSprite.setColor(Color.WHITE);
    }

    private void drawCircleOutline(float cx,float cy,float radius,float thickness,int segments, Color color){
        float prevX=cx+radius, prevY=cy;
        batch.setColor(color);
        for (int i=1;i<=segments;i++){
            float t=(i/(float)segments)*MathUtils.PI2;
            float x=cx + radius*MathUtils.cos(t);
            float y=cy + radius*MathUtils.sin(t);
            drawThickSegment(prevX,prevY, x,y, thickness);
            prevX=x; prevY=y;
        }
        batch.setColor(Color.WHITE);
    }

    private Texture bodyFor(Enemy e){
        if (e instanceof Goblin)    return goblinBodyTex;
        if (e instanceof Hobgoblin) return hobgoblinBodyTex;
        if (e instanceof Archer)    return archerBodyTex;
        if (e instanceof Bomber)    return bomberBodyTex;
        if (e instanceof Berserker) return berserkerBodyTex;
        return whiteTex;
    }
    private FaceStyle styleFor(Enemy e){
        if (e instanceof Goblin)    return FaceStyle.GOBLIN_DEVIOUS;
        if (e instanceof Hobgoblin) return FaceStyle.HOB_DEVIOUS;
        if (e instanceof Archer)    return FaceStyle.ARCHER_MASK;
        if (e instanceof Bomber)    return FaceStyle.BOMBER_ANGRY;
        if (e instanceof Berserker) return FaceStyle.BERSERKER_HELM;
        return FaceStyle.PLAYER_SMILE;
    }

    // ------------------------------------------------------------------------
    // Texture makers
    // ------------------------------------------------------------------------
    private static Texture makeSolidTexture(int w,int h, Color c){
        Pixmap pm=new Pixmap(w,h, Pixmap.Format.RGBA8888);
        pm.setColor(c); pm.fill();
        Texture t=new Texture(pm); pm.dispose(); return t;
    }

    private static Texture makeEllipseTexture(int w,int h, Color color){
        Pixmap pm=new Pixmap(w,h, Pixmap.Format.RGBA8888);
        pm.setColor(0,0,0,0); pm.fill();
        pm.setColor(color);
        float rx=w/2f, ry=h/2f, cx=rx, cy=ry;
        for (int y=0; y<h; y++){
            for (int x=0; x<w; x++){
                float dx=(x+0.5f - cx)/rx, dy=(y+0.5f - cy)/ry;
                if (dx*dx + dy*dy <= 1f) pm.drawPixel(x,y);
            }
        }
        Texture t=new Texture(pm); pm.dispose(); return t;
    }

    private static Texture makeSwordTexture(){
        int w=64, h=12;
        Pixmap pm=new Pixmap(w,h, Pixmap.Format.RGBA8888);
        pm.setColor(0.65f,0.65f,0.70f,1f); pm.fillRectangle(12,3, w-12,6);
        pm.setColor(0.45f,0.28f,0.10f,1f); pm.fillRectangle(0,0,12,h);
        Texture t=new Texture(pm); pm.dispose(); return t;
    }

    private Texture makeStoneWallTexture(int size){
        if (size<32) size=32;
        Pixmap pm=new Pixmap(size,size, Pixmap.Format.RGBA8888);

        for (int y=0;y<size;y++){
            float v=0.18f + 0.06f*(y/(float)size);
            pm.setColor(v,v,v+0.02f,1f);
            pm.drawLine(0,y, size-1,y);
        }
        MathUtils.random.setSeed(1337);
        pm.setColor(0,0,0,0.10f);
        for (int i=0;i<size*size/24;i++) pm.drawPixel(MathUtils.random(size-1), MathUtils.random(size-1));
        pm.setColor(1,1,1,0.06f);
        for (int i=0;i<size*size/32;i++) pm.drawPixel(MathUtils.random(size-1), MathUtils.random(size-1));

        int rows=2 + size/48; float mortar=Math.max(2f, size*0.02f), rowH=size/(float)rows;
        for (int r=0;r<rows;r++){
            float y0=r*rowH;
            pm.setColor(0.10f,0.10f,0.12f,1f);
            pm.fillRectangle(0, MathUtils.floor(y0), size, (int)mortar);

            int cols=3 + size/48; float colW=size/(float)cols;
            float xOff=(r%2==0)?0f:colW*0.5f;
            for (int c=0;c<=cols;c++){
                float jitter=(MathUtils.random()-0.5f)*colW*0.08f;
                float x=(c*colW + xOff + jitter);
                int xi=wrapIndex(MathUtils.floor(x), size);
                pm.fillRectangle(xi, MathUtils.floor(y0), (int)mortar, MathUtils.floor(rowH));
            }
        }
        // slight shading near mortar
        for (int y=0;y<size;y++){
            for (int x=0;x<size;x++){
                int rgba=pm.getPixel(x,y);
                float a=(rgba & 0xff)/255f;
                if (a < 0.99f) continue;
                float shade=0f;
                if (((pm.getPixel((x+1)%size,y) & 0xff) < 250) ||
                    ((pm.getPixel((x-1+size)%size,y) & 0xff) < 250) ||
                    ((pm.getPixel(x,(y+1)%size) & 0xff) < 250) ||
                    ((pm.getPixel(x,(y-1+size)%size) & 0xff) < 250)) shade=0.10f;
                if (shade>0f){
                    float rC=((rgba>>>24)&0xff)/255f, gC=((rgba>>>16)&0xff)/255f, bC=((rgba>>>8)&0xff)/255f;
                    pm.setColor(rC*(1f-shade), gC*(1f-shade), bC*(1f-shade), a);
                    pm.drawPixel(x,y);
                }
            }
        }
        Texture t=new Texture(pm); pm.dispose(); return t;
    }

    private static int wrapIndex(int v,int size){ int m=v%size; return (m<0)?m+size:m; }

    private Texture makeCoinTexture(){
        int W=24,H=24; Pixmap pm=new Pixmap(W,H, Pixmap.Format.RGBA8888);
        pm.setColor(0,0,0,0); pm.fill();
        pm.setColor(0.96f,0.82f,0.12f,1f); fillCircle(pm, W/2f,H/2f, W/2f - 1.5f);
        pm.setColor(0.85f,0.70f,0.08f,1f); drawCircle(pm, W/2f,H/2f, W/2f - 2f, 3f);
        pm.setColor(1f,0.95f,0.45f,0.9f); fillCircle(pm, W*0.35f, H*0.65f, W*0.14f);
        Texture t=new Texture(pm); pm.dispose(); return t;
    }
    private void drawCircle(Pixmap pm,float cx,float cy,float r,float th){
        for (float a=0;a<MathUtils.PI2;a+=0.01f){
            float x0=cx + MathUtils.cos(a)*r;
            float y0=cy + MathUtils.sin(a)*r;
            fillCircle(pm, x0,y0, th*0.5f);
        }
    }
    private void fillCircle(Pixmap pm,float cx,float cy,float r){
        int x0=MathUtils.floor(cx - r), x1=MathUtils.ceil(cx + r);
        int y0=MathUtils.floor(cy - r), y1=MathUtils.ceil(cy + r);
        for (int y=y0;y<=y1;y++){
            for (int x=x0;x<=x1;x++){
                float dx=x+0.5f - cx, dy=y+0.5f - cy;
                if (dx*dx + dy*dy <= r*r) pm.drawPixel(x,y);
            }
        }
    }

    // ------------------------------------------------------------------------
    // Misc utils
    // ------------------------------------------------------------------------
    private Vector2 tileCenter(int tx,int ty){ return new Vector2(tx*TILE_SIZE + TILE_SIZE*0.5f, ty*TILE_SIZE + TILE_SIZE*0.5f); }
    private int worldToTileX(float wx){ return (int)Math.floor(wx / TILE_SIZE); }
    private int worldToTileY(float wy){ return (int)Math.floor(wy / TILE_SIZE); }
    private boolean inBoundsTiles(int tx,int ty){ return tx>=0 && ty>=0 && tx<dungeon.width && ty<dungeon.height; }
    private float heuristicOctile(int x0,int y0,int x1,int y1){
        float dx=Math.abs(x1-x0), dy=Math.abs(y1-y0), mi=Math.min(dx,dy), ma=Math.max(dx,dy);
        return (float)(1.41421356*mi + (ma-mi));
    }

    private float[] nearestOpenWithClearance(float wx,float wy,int radiusTiles){
        float[] p=dungeon.nearestOpen(wx,wy,radiusTiles);
        int tx=worldToTileX(p[0]), ty=worldToTileY(p[1]);
        if (clearanceAtTile(tx,ty) >= MIN_CLEAR_TILES) return p;
        for (int r=1;r<=radiusTiles;r++){
            for (int dx=-r; dx<=r; dx++){
                int x=tx+dx, y1=ty+r, y2=ty-r;
                if (inBoundsTiles(x,y1)&&!dungeon.isSolid(x,y1)&&clearanceAtTile(x,y1)>=MIN_CLEAR_TILES)
                    return new float[]{ x*TILE_SIZE+TILE_SIZE*0.5f, y1*TILE_SIZE+TILE_SIZE*0.5f };
                if (inBoundsTiles(x,y2)&&!dungeon.isSolid(x,y2)&&clearanceAtTile(x,y2)>=MIN_CLEAR_TILES)
                    return new float[]{ x*TILE_SIZE+TILE_SIZE*0.5f, y2*TILE_SIZE+TILE_SIZE*0.5f };
            }
            for (int dy=-r+1; dy<=r-1; dy++){
                int y=ty+dy, x1=tx+r, x2=tx-r;
                if (inBoundsTiles(x1,y)&&!dungeon.isSolid(x1,y)&&clearanceAtTile(x1,y)>=MIN_CLEAR_TILES)
                    return new float[]{ x1*TILE_SIZE+TILE_SIZE*0.5f, y*TILE_SIZE+TILE_SIZE*0.5f };
                if (inBoundsTiles(x2,y)&&!dungeon.isSolid(x2,y)&&clearanceAtTile(x2,y)>=MIN_CLEAR_TILES)
                    return new float[]{ x2*TILE_SIZE+TILE_SIZE*0.5f, y*TILE_SIZE+TILE_SIZE*0.5f };
            }
        }
        return p;
    }

    private void buildClearanceField(){
        final int W=dungeon.width, H=dungeon.height;
        final int[] qx=new int[W*H], qy=new int[W*H];
        int qs=0, qe=0;

        for (int x=0;x<W;x++){
            for (int y=0;y<H;y++){
                if (dungeon.isSolid(x,y)){ tileClearance[x][y]=0f; qx[qe]=x; qy[qe]=y; qe++; }
                else tileClearance[x][y]=Float.POSITIVE_INFINITY;
            }
        }
        final int[] dx8={1,-1,0,0, 1, 1,-1,-1};
        final int[] dy8={0,0,1,-1,1,-1, 1,-1};
        while (qs<qe){
            int cx=qx[qs], cy=qy[qs]; qs++;
            float base=tileClearance[cx][cy];
            for (int i=0;i<8;i++){
                int nx=cx+dx8[i], ny=cy+dy8[i];
                if (!inBoundsTiles(nx,ny)) continue;
                float cand=base + 1f;
                if (tileClearance[nx][ny] > cand) { tileClearance[nx][ny]=cand; qx[qe]=nx; qy[qe]=ny; qe++; }
            }
        }
        // light blur
        for (int p=0;p<1;p++){
            for (int x=1;x<W-1;x++){
                for (int y=1;y<H-1;y++){
                    if (dungeon.isSolid(x,y)) continue;
                    float s=0f; int c=0;
                    for (int dx=-1; dx<=1; dx++){
                        for (int dy=-1; dy<=1; dy++){
                            float v=tileClearance[x+dx][y+dy];
                            if (!Float.isInfinite(v)) { s+=v; c++; }
                        }
                    }
                    if (c>0) tileClearance[x][y] = (tileClearance[x][y] + s/c)*0.5f;
                }
            }
        }
    }

    private float clearanceAtTile(int tx,int ty){
        if (!inBoundsTiles(tx,ty)) return 0f;
        float c=tileClearance[tx][ty];
        return Float.isInfinite(c)?0f:c;
    }

    private void moveRectToward(Rectangle r, float tx,float ty, float step){
        float cx=r.x + r.width*0.5f, cy=r.y + r.height*0.5f;
        float dx=tx - cx, dy=ty - cy, len=(float)Math.sqrt(dx*dx+dy*dy);
        if (len<1e-4f) return;
        dx/=len; dy/=len;
        r.x += dx*step; r.y += dy*step;
    }

    private float time(){ return (float)(Gdx.graphics.getFrameId()/60.0); }

    // ------------------------------------------------------------------------
    // DTOs
    // ------------------------------------------------------------------------
    private static class Arrow {
        final Texture tex; final Rectangle bounds=new Rectangle();
        float vx,vy; int damage;
        Arrow(Texture tex,float x,float y,float w,float h,float vx,float vy,int dmg){
            this.tex=tex; this.bounds.set(x,y,w,h); this.vx=vx; this.vy=vy; this.damage=dmg;
        }
        void render(SpriteBatch batch){ batch.setColor(Color.WHITE); batch.draw(tex, bounds.x,bounds.y,bounds.width,bounds.height); }
    }

    private static class Orb {
        final Texture tex; final Rectangle bounds=new Rectangle();
        Orb(Texture tex,float x,float y,float w,float h){ this.tex=tex; this.bounds.set(x,y,w,h); }
        void render(SpriteBatch batch){ batch.draw(tex, bounds.x, bounds.y, bounds.width, bounds.height); }
    }

    private static class Coin {
        final Texture tex; final Rectangle bounds=new Rectangle(); final int value;
        float bobT=0f, drawOffsetY=0f;
        Coin(Texture tex,float x,float y,float w,float h,int value){ this.tex=tex; this.bounds.set(x,y,w,h); this.value=value; }
        void render(SpriteBatch batch){ batch.draw(tex, bounds.x, bounds.y+drawOffsetY, bounds.width, bounds.height); }
    }

    private static class Bomb {
        float x,y; int ticksRemaining=BOMB_TICKS; float tickTimer=BOMB_TICK_INTERVAL;
        boolean exploded=false; float explosionTimer=0.18f;
        Bomb(float x,float y){ this.x=x; this.y=y; }
    }

    // ------------------------------------------------------------------------
    // View helpers
    // ------------------------------------------------------------------------
    private float viewLeft(){ return camera.position.x - camera.viewportWidth*camera.zoom*0.5f; }
    private float viewBottom(){ return camera.position.y - camera.viewportHeight*camera.zoom*0.5f; }
    private float viewWidth(){ return camera.viewportWidth*camera.zoom; }
    private float viewHeight(){ return camera.viewportHeight*camera.zoom; }

    // ------------------------------------------------------------------------
    // Blink
    // ------------------------------------------------------------------------
    private void scheduleNextPlayerBlink(){ playerBlinkTimer=MathUtils.random(BLINK_MIN, BLINK_MAX); playerBlinkDur=0f; }
    private void updatePlayerBlink(float dt){
        if (playerBlinkDur>0f){ playerBlinkDur-=dt; if (playerBlinkDur<=0f) scheduleNextPlayerBlink(); return; }
        playerBlinkTimer -= dt; if (playerBlinkTimer<=0f) playerBlinkDur=BLINK_DUR;
    }
}
