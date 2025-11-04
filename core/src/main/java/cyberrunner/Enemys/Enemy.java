// Author: Martin Taylor
// File: Enemy.java
// Date: 2025-11-04
// Description:
//   Base enemy class that owns common state (texture, bounds, speed,
//   touch damage, HP) and timing (touch-damage cooldown, melee i-frames).
//   Child classes override update(dt, EnemyContext) to implement behavior
//   so the game loop stays clean.

package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Enemy {

    // Unique ID for debugging/logging
    private static int NEXT_UID = 1;
    public final int uid;

    // Visual + collision
    protected final Texture texture;
    protected final Rectangle bounds = new Rectangle();

    // Movement / contact damage
    protected float speed;
    protected int touchDamage;

    // Health
    protected int hp = 1;
    protected int maxHp = 1;

    // Cooldowns and i-frames
    protected float touchDamageCooldownDuration = 0.45f;
    protected float touchDamageCooldownTimer = 0f;

    protected float meleeIFrameDuration = 0.14f;
    protected float meleeIFrameTimer = 0f;

    // Prevent multiple hits from the same sword swing
    private int lastRegisteredSwingId = -1;

    // ---------------- Constructors ----------------
    public Enemy(Texture texture, float x, float y, float w, float h,
                 float speed, int touchDamage) {
        this.uid = NEXT_UID++;
        this.texture = texture;
        this.bounds.set(x, y, w, h);
        this.speed = speed;
        this.touchDamage = touchDamage;
        this.maxHp = this.hp = 1;
    }

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

    public Enemy(Texture texture, float x, float y, float w, float h) {
        this.uid = NEXT_UID++;
        this.texture = texture;
        this.bounds.set(x, y, w, h);
    }

    // ---------------- Core Update ----------------
    /** Preferred update path: child classes override this to use world hooks. */
    public void update(float dt, EnemyContext ctx) {
        // default no-op
        tickTimers(dt);
    }

    /** Backward-compat timer tick if someone still calls update(dt) only. */
    public void update(float dt) { tickTimers(dt); }

    protected void tickTimers(float dt) {
        if (touchDamageCooldownTimer > 0f) {
            touchDamageCooldownTimer = Math.max(0f, touchDamageCooldownTimer - dt);
        }
        if (meleeIFrameTimer > 0f) {
            meleeIFrameTimer = Math.max(0f, meleeIFrameTimer - dt);
        }
    }

    // ---------------- Movement Helper ----------------
    protected void moveToward(final float targetX, final float targetY, float dt, EnemyContext ctx) {
        final float centerX = bounds.x + bounds.width  * 0.5f;
        final float centerY = bounds.y + bounds.height * 0.5f;

        float dx = targetX - centerX;
        float dy = targetY - centerY;
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        if (len < 1e-4f) return;
        dx /= len; dy /= len;

        final float stepX = dx * speed * dt;
        final float stepY = dy * speed * dt;

        final float oldX = bounds.x, oldY = bounds.y;

        bounds.x = oldX + stepX;
        if (ctx.isBlocked(bounds)) bounds.x = oldX;

        bounds.y = oldY + stepY;
        if (ctx.isBlocked(bounds)) bounds.y = oldY;
    }

    // ---------------- Hooks / misc ----------------
    /** Override if an enemy needs to react when touching the player. */
    public void onPlayerCollision() { /* no-op */ }

    // Health
    public int getHealth() { return hp; }
    public int getMaxHealth() { return maxHp; }
    public void setHealth(int newHp) { hp = Math.max(0, Math.min(newHp, maxHp)); }
    public void setMaxHealth(int newMax) { maxHp = Math.max(1, newMax); hp = Math.min(hp, maxHp); }
    // Legacy aliases
    public void setHp(int newHp) { setHealth(newHp); }
    public void setMaxHp(int newMax) { setMaxHealth(newMax); }

    /** Apply generic damage. */
    public void applyHit(int damage) {
        if (damage <= 0 || hp <= 0) return;
        hp = Math.max(0, hp - damage);
    }

    // Touch damage cooldown
    public void setTouchDamageCooldownDuration(float seconds) { touchDamageCooldownDuration = Math.max(0f, seconds); }
    public void markTouchDamageUsedForCooldown() { touchDamageCooldownTimer = touchDamageCooldownDuration; }
    public boolean canDealTouchDamage() { return touchDamageCooldownTimer <= 0f; }

    // Melee i-frames
    public boolean canTakeMeleeHit() { return meleeIFrameTimer <= 0f; }
    public void markMeleeHitRegistered() { meleeIFrameTimer = meleeIFrameDuration; }

    // De-dupe per swing
    public boolean tryRegisterMeleeSwing(int swingId) {
        if (swingId < 0) return canTakeMeleeHit();
        if (lastRegisteredSwingId == swingId) return false;
        if (!canTakeMeleeHit()) return false;
        lastRegisteredSwingId = swingId;
        return true;
    }

    // Accessors
    public Texture getTexture() { return texture; }
    public Rectangle getBoundingBox() { return bounds; }

    public float getSpeed() { return speed; }
    public void setSpeed(float s) { speed = s; }

    public int getDamage() { return touchDamage; }
    public void setDamage(int dmg) { touchDamage = Math.max(0, dmg); }

    public int getUid() { return uid; }
}
