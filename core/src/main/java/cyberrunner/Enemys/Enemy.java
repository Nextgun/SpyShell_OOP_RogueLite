//Enemy.java
package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

/**
 * Base enemy: position, movement, touch damage, HP, and melee hit-protection.
 * This version adds a per-swing lock so a single sword swing can only deal
 * damage once to a given enemy, no matter how many frames the hitbox overlaps.
 */
public class Enemy {

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

    // Short melee i-frame (kept as a general guard, but swingId is authoritative)
    protected float meleeIFrameDuration = 0.14f;
    protected float meleeIFrameTimer = 0f;

    // ---- NEW: identity + per-swing de-dupe ---------------------------------
    private static int NEXT_UID = 1;
    public final int uid = NEXT_UID++;

    private int lastMeleeSwingId = -1; // which swing last hit me

    /**
     * Register the current melee swing. Returns true if this enemy
     * should take damage for this swing (i.e., hasn't been hit by this swing yet).
     */
    public boolean tryRegisterMeleeSwing(int swingId) {
        if (swingId == lastMeleeSwingId) return false; // already hit by this swing
        lastMeleeSwingId = swingId;
        return true;
    }
    // ------------------------------------------------------------------------

    /** 7-arg ctor (defaults HP=1) */
    public Enemy(Texture texture, float x, float y, float w, float h,
                 float speed, int touchDamage) {
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
        this.texture = texture;
        this.bounds.set(x, y, w, h);
        this.speed = speed;
        this.touchDamage = touchDamage;
        this.maxHp = Math.max(1, hp);
        this.hp = this.maxHp;
    }

    /** Minimal ctor */
    public Enemy(Texture texture, float x, float y, float w, float h) {
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

    // Legacy i-frame helpers (still fine to keep; swingId prevents same-swing doubles)
    public boolean canTakeMeleeHit() { return meleeIFrameTimer <= 0f; }
    public void markMeleeHitRegistered() { meleeIFrameTimer = meleeIFrameDuration; }
}
