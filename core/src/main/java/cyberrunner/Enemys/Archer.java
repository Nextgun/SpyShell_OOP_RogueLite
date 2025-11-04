// Author: Martin Taylor
// File: Archer.java
// Date: 2025-11-04
// Description:
//   Ranged enemy. Keeps distance and periodically fires an arrow toward
//   the player using EnemyContext.spawnArrow(...).

package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Vector2;

public class Archer extends Enemy {

    public static final float DEFAULT_SPEED = 180f;
    public static final int   TOUCH_DAMAGE  = 3;
    public static final int   DEFAULT_HP    = 1;

    // Firing
    private float fireCooldown = 0f;
    private static final float FIRE_INTERVAL = 1.4f; // seconds
    private static final float ARROW_SPEED   = 420f;
    private static final int   ARROW_DAMAGE  = 3;

    // Simple strafe motion timing
    private float strafeTimer = 0f;
    private static final float STRAFE_PERIOD = 0.8f;

    public Archer(Texture texture, float x, float y, float w, float h) {
        super(texture, x, y, w, h, DEFAULT_SPEED, TOUCH_DAMAGE, DEFAULT_HP);
    }

    public Archer(Texture texture, float x, float y, float w, float h,
                  float speed, int touchDamage, int hp) {
        super(texture, x, y, w, h, speed, touchDamage, hp);
    }

    @Override
    public void update(float dt, EnemyContext ctx) {
        tickTimers(dt);

        // Desired distance from player
        final Vector2 player = ctx.getPlayerCenter();
        final float centerX = bounds.x + bounds.width  * 0.5f;
        final float centerY = bounds.y + bounds.height * 0.5f;

        float dx = player.x - centerX;
        float dy = player.y - centerY;
        float dist2 = dx*dx + dy*dy;

        float desired = 5.5f * ctx.getTileSize();
        float desired2 = desired * desired;

        Vector2 target = new Vector2(player);
        if (dist2 < desired2) {
            // Too close: back up a bit
            float len = (float)Math.sqrt(dist2); if (len < 1e-4f) len = 1f;
            float ux = -dx/len, uy = -dy/len;
            target.set(centerX + ux * 80f, centerY + uy * 80f);
        } else {
            // Strafe around player
            strafeTimer += dt;
            float angle = (strafeTimer / STRAFE_PERIOD) * (float)Math.PI * 2f;
            target.set(player.x + (float)Math.cos(angle) * desired,
                       player.y + (float)Math.sin(angle) * desired);
        }

        moveToward(target.x, target.y, dt, ctx);

        // Shoot when ready
        fireCooldown -= dt;
        if (fireCooldown <= 0f) {
            fireCooldown = FIRE_INTERVAL;

            Vector2 dir = new Vector2(player.x - centerX, player.y - centerY);
            float len = dir.len(); if (len < 1e-4f) len = 1f;
            dir.scl(1f / len).scl(ARROW_SPEED);

            // origin is slightly offset so the arrow isn't spawned “inside” the body
            Vector2 origin = new Vector2(centerX - 9f, centerY - 3f);
            ctx.spawnArrow(getTexture(), origin, dir, ARROW_DAMAGE);
        }
    }

    @Override
    public void onPlayerCollision() {
        // Touch damage is handled by the collision manager/game loop.
    }
}
