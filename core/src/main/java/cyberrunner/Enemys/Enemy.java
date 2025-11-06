// Author: Martin Taylor
// File: Enemy.java
// Date: 2025-11-04
// Description:
//   Base enemy class that owns common state (texture, bounds, speed,
//   touch damage, HP) and timing (touch-damage cooldown, melee i-frames).
//   Child classes can override update(dt, EnemyContext) to implement
//   behavior so the game loop stays clean.

package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Enemy {

    // ------------------------------------------------------------------------
    // Identity
    // ------------------------------------------------------------------------
    private static int NEXT_UID = 1;
    private final int uid; // unique per enemy instance (for logs / debugging)

    // ------------------------------------------------------------------------
    // Visuals & Collision
    // ------------------------------------------------------------------------
    protected final Texture texture;
    protected final Rectangle bounds = new Rectangle();

    // ------------------------------------------------------------------------
    // Movement / Contact Damage
    // ------------------------------------------------------------------------
    protected float speed;
    protected int touchDamage;

    // ------------------------------------------------------------------------
    // Health
    // ------------------------------------------------------------------------
    protected int hp = 1;
    protected int maxHp = 1;

    // ------------------------------------------------------------------------
    // Cooldowns & I-Frames
    // ------------------------------------------------------------------------
    /** Cooldown between applying touch damage to the player (seconds). */
    protected float touchDamageCooldownDuration = 0.45f;
    protected float touchDamageCooldownTimer = 0f;

    /** Melee invincibility window after being hit (seconds). */
    protected float meleeIFrameDuration = 0.14f;
    protected float meleeIFrameTimer = 0f;

    /**
     * De-dupe protection so a single sword swing ID can only damage
     * this enemy once.
     */
    private int lastRegisteredSwingId = -1;

    // ------------------------------------------------------------------------
    // Constructors
    // ------------------------------------------------------------------------
    public Enemy(Texture texture,
                 float x, float y,
                 float width, float height,
                 float speed,
                 int touchDamage) {

        this.uid = NEXT_UID++;
        this.texture = texture;
        this.bounds.set(x, y, width, height);
        this.speed = speed;
        this.touchDamage = touchDamage;
        this.maxHp = 1;
        this.hp = 1;
    }

    public Enemy(Texture texture,
                 float x, float y,
                 float width, float height,
                 float speed,
                 int touchDamage,
                 int hp) {

        this.uid = NEXT_UID++;
        this.texture = texture;
        this.bounds.set(x, y, width, height);
        this.speed = speed;
        this.touchDamage = touchDamage;
        this.maxHp = Math.max(1, hp);
        this.hp = this.maxHp;
    }

    public Enemy(Texture texture,
                 float x, float y,
                 float width, float height) {

        this.uid = NEXT_UID++;
        this.texture = texture;
        this.bounds.set(x, y, width, height);
    }

    // ------------------------------------------------------------------------
    // Core Update
    // ------------------------------------------------------------------------

    /**
     * Preferred update: subclasses override this and use EnemyContext hooks
     * to query the world and perform actions.
     */
    public void update(float deltaTime, EnemyContext context) {
        // Default: only advance timers.
        tickTimers(deltaTime);
    }

    /**
     * Backwards-compatible update when no EnemyContext is provided.
     * Keeps old call sites from breaking.
     */
    public void update(float deltaTime) {
        tickTimers(deltaTime);
    }

    /** Advance cooldown and i-frame timers. */
    protected void tickTimers(float deltaTime) {
        if (touchDamageCooldownTimer > 0f) {
            touchDamageCooldownTimer =
                    Math.max(0f, touchDamageCooldownTimer - deltaTime);
        }

        if (meleeIFrameTimer > 0f) {
            meleeIFrameTimer =
                    Math.max(0f, meleeIFrameTimer - deltaTime);
        }
    }

    // ------------------------------------------------------------------------
    // Movement Helper
    // ------------------------------------------------------------------------

    /**
     * Move toward a target point with per-axis collision via EnemyContext.
     */
    protected void moveToward(float targetX,
                              float targetY,
                              float deltaTime,
                              EnemyContext context) {

        float centerX = bounds.x + bounds.width * 0.5f;
        float centerY = bounds.y + bounds.height * 0.5f;

        float dx = targetX - centerX;
        float dy = targetY - centerY;
        float length = (float) Math.sqrt(dx * dx + dy * dy);

        if (length < 1e-4f) {
            return;
        }

        dx /= length;
        dy /= length;

        float stepX = dx * speed * deltaTime;
        float stepY = dy * speed * deltaTime;

        float originalX = bounds.x;
        float originalY = bounds.y;

        // Move X
        bounds.x = originalX + stepX;
        if (context.isBlocked(bounds)) {
            bounds.x = originalX;
        }

        // Move Y
        bounds.y = originalY + stepY;
        if (context.isBlocked(bounds)) {
            bounds.y = originalY;
        }
    }

    // ------------------------------------------------------------------------
    // Hooks / Behavior
    // ------------------------------------------------------------------------

    /** Override in subclasses that need to react when touching the player. */
    public void onPlayerCollision() {
        // no-op by default
    }

    // ------------------------------------------------------------------------
    // Health
    // ------------------------------------------------------------------------

    public int getHealth() {
        return hp;
    }

    public int getMaxHealth() {
        return maxHp;
    }

    public void setHealth(int newHp) {
        hp = Math.max(0, Math.min(newHp, maxHp));
    }

    public void setMaxHealth(int newMax) {
        maxHp = Math.max(1, newMax);
        if (hp > maxHp) {
            hp = maxHp;
        }
    }

    // Legacy aliases for compatibility
    public void setHp(int newHp) {
        setHealth(newHp);
    }

    public void setMaxHp(int newMax) {
        setMaxHealth(newMax);
    }

    /** Apply generic damage to this enemy. */
    public void applyHit(int damage) {
        if (damage <= 0 || hp <= 0) {
            return;
        }
        hp = Math.max(0, hp - damage);
    }

    // ------------------------------------------------------------------------
    // Touch Damage Cooldown
    // ------------------------------------------------------------------------

    public void setTouchDamageCooldownDuration(float seconds) {
        touchDamageCooldownDuration = Math.max(0f, seconds);
    }

    public void markTouchDamageUsedForCooldown() {
        touchDamageCooldownTimer = touchDamageCooldownDuration;
    }

    public boolean canDealTouchDamage() {
        return touchDamageCooldownTimer <= 0f;
    }

    // ------------------------------------------------------------------------
    // Melee I-Frames
    // ------------------------------------------------------------------------

    public boolean canTakeMeleeHit() {
        return meleeIFrameTimer <= 0f;
    }

    public void markMeleeHitRegistered() {
        meleeIFrameTimer = meleeIFrameDuration;
    }

    // ------------------------------------------------------------------------
    // Swing De-Duplication
    // ------------------------------------------------------------------------

    /**
     * Returns true if this enemy can take damage from the given swingId,
     * and records that swingId so it won't be applied twice.
     */
    public boolean tryRegisterMeleeSwing(int swingId) {
        if (swingId < 0) {
            return canTakeMeleeHit();
        }
        if (lastRegisteredSwingId == swingId) {
            return false;
        }
        if (!canTakeMeleeHit()) {
            return false;
        }

        lastRegisteredSwingId = swingId;
        return true;
    }

    // ------------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------------

    public Texture getTexture() {
        return texture;
    }

    public Rectangle getBoundingBox() {
        return bounds;
    }

    public float getSpeed() {
        return speed;
    }

    public void setSpeed(float speed) {
        this.speed = speed;
    }

    public int getDamage() {
        return touchDamage;
    }

    public void setDamage(int damage) {
        this.touchDamage = Math.max(0, damage);
    }

    public int getUid() {
        return uid;
    }

} // End of class Enemy
