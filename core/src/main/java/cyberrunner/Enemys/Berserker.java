//Berserker.java
package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Berserker: approaches -> short charge -> straight dash.
 * - 2 HP (two player hits).
 * - Walk speed similar to Bomber.
 * - Slightly smaller collision bounds to reduce snagging.
 * - hasDealtDashDamage flag ensures single hit per dash (sandbox checks it).
 */
public class Berserker extends Enemy {

    public enum State { APPROACH, CHARGING, DASHING, RECOVER }
    public State state = State.APPROACH;

    public float chargeTimer = 0f;
    public final Vector2 dashDir = new Vector2();
    public float dashSpeed = 520f;
    public float dashTimer = 0f;
    public float recoverTimer = 0f;
    public float shakeSeed = 0f;

    // Read by the sandbox to prevent multiple damage applications per dash
    public boolean hasDealtDashDamage = false;

    public static final float WALK_SPEED = 340f; // equals Bomber speed in the sandbox

    public Berserker(Texture tex, float x, float y, float w, float h) {
        // Enemy(texture, x, y, w, h, speed, touchDamage, hp)
        super(tex, x, y, w, h, WALK_SPEED, /*touch dmg*/ 0, /*hp*/ 2);

        // shrink collision box (helps avoid getting snagged on corners)
        float inset = Math.min(w, h) * 0.15f; // 15%
        Rectangle b = getBoundingBox();
        b.x += inset * 0.5f; b.y += inset * 0.5f;
        b.width  = Math.max(1f, b.width  - inset);
        b.height = Math.max(1f, b.height - inset);

        // short cooldown if you ever rely on touch damage by base class
        setTouchDamageCooldownDuration(0.45f);
    }

    @Override
    public void update(float dt) {
        super.update(dt);
        // Light jitter in approach to break corner pinning (sandbox also recovers)
        if (state == State.APPROACH) {
            Rectangle r = getBoundingBox();
            r.x += Math.signum(Math.random() - 0.5) * 0.0005f;
            r.y += Math.signum(Math.random() - 0.5) * 0.0005f;
        }
    }
}
