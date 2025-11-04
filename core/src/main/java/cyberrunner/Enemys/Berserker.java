// Author: Martin Taylor
// File: Berserker.java
// Date: 2025-11-04
// Description:
//   Approach -> short charge -> fast dash -> brief recover.
//   Uses EnemyContext to check collisions during dash.

package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Berserker extends Enemy {

    public enum State { APPROACH, CHARGING, DASHING, RECOVER }
    public State state = State.APPROACH;

    private float chargeTimer = 0f;
    private final Vector2 dashDir = new Vector2();
    private float dashTimer = 0f;
    private float recoverTimer = 0f;

    // Prevent double-damage per dash on the player
    public boolean hasDealtDashDamage = false;

    private static final float WALK_SPEED = 340f;
    private static final float DASH_SPEED = 520f;

    public Berserker(Texture tex, float x, float y, float w, float h) {
        super(tex, x, y, w, h, WALK_SPEED, /*touch dmg*/ 0, /*hp*/ 2);

        // Slightly shrink the collision rect to reduce snagging
        float inset = Math.min(w, h) * 0.15f;
        Rectangle b = getBoundingBox();
        b.x += inset * 0.5f; b.y += inset * 0.5f;
        b.width  = Math.max(1f, b.width  - inset);
        b.height = Math.max(1f, b.height - inset);

        setTouchDamageCooldownDuration(0.45f);
    }

    @Override
    public void update(float dt, EnemyContext ctx) {
        tickTimers(dt);

        final Rectangle b = getBoundingBox();
        final Vector2 playerCenter = ctx.getPlayerCenter();
        final Vector2 myCenter = new Vector2(b.x + b.width*0.5f, b.y + b.height*0.5f);

        switch (state) {
            case APPROACH: {
                // Walk toward player
                moveToward(playerCenter.x, playerCenter.y, dt, ctx);

                // Start charge when close
                float trigger = 3.5f * ctx.getTileSize();
                if (myCenter.dst2(playerCenter) <= trigger * trigger) {
                    state = State.CHARGING;
                    chargeTimer = 0.45f; // windup
                    hasDealtDashDamage = false;
                }
                break;
            }
            case CHARGING: {
                chargeTimer -= dt;
                if (chargeTimer <= 0f) {
                    // lock dash direction at charge end
                    dashDir.set(playerCenter).sub(myCenter).nor();
                    state = State.DASHING;
                    dashTimer = 0.8f;
                }
                break;
            }
            case DASHING: {
                float step = DASH_SPEED * dt;

                float oldX = b.x, oldY = b.y;
                b.x = oldX + dashDir.x * step; if (ctx.isBlocked(b)) b.x = oldX;
                b.y = oldY + dashDir.y * step; if (ctx.isBlocked(b)) b.y = oldY;

                dashTimer -= dt;
                if (dashTimer <= 0f) {
                    state = State.RECOVER;
                    recoverTimer = 0.4f;
                }
                break;
            }
            case RECOVER: {
                recoverTimer -= dt;
                if (recoverTimer <= 0f) state = State.APPROACH;
                break;
            }
        }
    }
}
