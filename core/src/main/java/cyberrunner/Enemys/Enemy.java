//Enemy.java
package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

/**
 * Base enemy: position, movement, touch damage, HP, melee i-frames,
 * and a swing de-dupe guard so one sword swing can't hit the same enemy twice.
 */
public class Enemy {

    // --- Identity / logging ---
    private static int NEXT_UID = 1;
    public final int uid;

    protected final Texture texture;
    protected final Rectangle bounds = new Rectangle();

    // Movement / contact
    protected float speed;
    protected int touchDamage;

    // Health
    protected int hp = 1;
    protected int maxHp = 1;

    // Touch-damage cooldown (if you use it elsewhere)
    protected float touchDamageCooldownDuration = 0.45f;
    protected float touchDamageCooldownTimer = 0f;

    // Melee i-frames so one swing can't tick multiple times
    protected float meleeIFrameDuration = 0.14f; // slightly > swing frame time
    protected float meleeIFrameTimer = 0f;

    // De-dup per swing: remember last swing ID that damaged this enemy
    private int lastRegisteredSwingId = -1;

    /** 7-arg ctor (defaults HP=1) */
    public Enemy(Texture texture, float x, float y, float w, float h,
                 float speed, int touchDamage) {
        this.uid = NEXT_UID++;
        this.texture = texture;
        this.bounds.set(x, y, w, h);
        this.speed = speed;
        this.touchDamage = touchDamage;
        this.maxHp = 1;
        this.hp = 1;
    }

    /** 8-arg ctor (explicit HP) */
    public Enemy(Texture texture, float x, float y, float w, float h,
                 float speed, int touchDamage, int hp) {
        this.uid = NEXT_UID++;
        this.texture = texture;
        this.bounds.set(x, y, w, h);
        this.speed = speed;
        this.touchDamage = touchDamage;
        this.maxHp = Math.max(1, hp);
        this.hp = this.maxHp;
    }

    /** Minimal ctor */
    public Enemy(Texture texture, float x, float y, float w, float h) {
        this.uid = NEXT_UID++;
        this.texture = texture;
        this.bounds.set(x, y, w, h);
    }

    // --------- Health ----------
    public int getHealth() { return hp; }
    public int getMaxHealth() { return maxHp; }

    public void setHealth(int hp) { this.hp = Math.max(0, Math.min(hp, maxHp)); }
    public void setMaxHealth(int max) {
        this.maxHp = Math.max(1, max);
        if (this.hp > this.maxHp) this.hp = this.maxHp;
    }

    // Compatibility aliases
    public void setHp(int hp) { setHealth(hp); }
    public void setMaxHp(int max) { setMaxHealth(max); }

    /** Apply generic damage (sandbox uses applyHit(1) on melee) */
    public void applyHit(int dmg) {
        if (dmg <= 0 || hp <= 0) return;
        int before = hp;
        hp = Math.max(0, hp - dmg);
        com.badlogic.gdx.Gdx.app.log("APPLY_HIT",
                getClass().getSimpleName() + " " + before + " -> " + hp);
    }

    // --------- Movement ----------
    public void updateToward(float tx, float ty, float dt) {
        float cx = bounds.x + bounds.width * 0.5f;
        float cy = bounds.y + bounds.height * 0.5f;
        float dx = tx - cx, dy = ty - cy;
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        if (len < 1e-4f) return;
        dx /= len; dy /= len;
        bounds.x += dx * speed * dt;
        bounds.y += dy * speed * dt;
    }

    public void update(float dt) {
        if (touchDamageCooldownTimer > 0f) {
            touchDamageCooldownTimer -= dt;
            if (touchDamageCooldownTimer < 0f) touchDamageCooldownTimer = 0f;
        }
        if (meleeIFrameTimer > 0f) {
            meleeIFrameTimer -= dt;
            if (meleeIFrameTimer < 0f) meleeIFrameTimer = 0f;
        }
    }

    // --------- Hooks / misc ----------
    /** So subclasses (e.g., Bomber) can @Override safely. */
    public void onPlayerCollision() { /* no-op by default */ }

    public Texture getTexture() { return texture; }
    public Rectangle getBoundingBox() { return bounds; }

    public float getSpeed() { return speed; }
    public void setSpeed(float s) { this.speed = s; }

    public int getDamage() { return touchDamage; }
    public void setDamage(int dmg) { this.touchDamage = Math.max(0, dmg); }

    public void setTouchDamageCooldownDuration(float seconds) {
        this.touchDamageCooldownDuration = Math.max(0f, seconds);
    }
    public void markTouchDamageUsedForCooldown() {
        this.touchDamageCooldownTimer = touchDamageCooldownDuration;
    }
    public boolean canDealTouchDamage() { return touchDamageCooldownTimer <= 0f; }

    // --- melee i-frame helpers ---
    public boolean canTakeMeleeHit() { return meleeIFrameTimer <= 0f; }
    public void markMeleeHitRegistered() { meleeIFrameTimer = meleeIFrameDuration; }

    // --- swing de-dupe per enemy ---
    /** Returns true if this enemy can register damage for this swingId (and records it). */
    public boolean tryRegisterMeleeSwing(int swingId) {
        // allow if (a) not hit yet this swing AND (b) i-frames are off
        if (swingId < 0) return canTakeMeleeHit();
        if (lastRegisteredSwingId == swingId) return false;
        if (!canTakeMeleeHit()) return false;
        lastRegisteredSwingId = swingId;
        return true;
    }

    // --- UID accessor (for logs/UI) ---
    public int getUid() { return uid; }
}
