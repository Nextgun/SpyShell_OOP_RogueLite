//Archer.java
package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;

/**
 * Archer – ranged enemy. Touch damage is modest; actual ranged damage is handled
 * by arrows in EnemySandboxApp. HP is set via the Enemy constructor.
 */
public class Archer extends Enemy {

    public static final float DEFAULT_SPEED = 250f;
    public static final int   TOUCH_DAMAGE  = 3;  // bump damage on collision
    public static final int   DEFAULT_HP    = 1;  // dies in one hit

    // The sandbox reads/writes this
    public float fireTimer = 0f;

    public Archer(Texture texture, float x, float y, float w, float h) {
        super(texture, x, y, w, h, DEFAULT_SPEED, TOUCH_DAMAGE, DEFAULT_HP);
    }

    public Archer(Texture texture, float x, float y, float w, float h,
                  float speed, int touchDamage, int hp) {
        super(texture, x, y, w, h, speed, touchDamage, hp);
    }
}
