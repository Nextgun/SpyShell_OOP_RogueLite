package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;

/** Ranged enemy that shoots arrows toward the player. */
public class Archer extends Enemy {

    // Defaults: a bit slower than hobgoblin, contact damage is modest
    public static final float DEFAULT_SPEED  = 150f; // px/sec
    public static final int   DEFAULT_DAMAGE = 2;    // only applies on physical contact

    // Shooting handled in EnemySandboxApp (fireTimer)
    public float fireTimer = 0f;

    public Archer(Texture texture, float x, float y, float w, float h) {
        super(texture, x, y, w, h, DEFAULT_SPEED, DEFAULT_DAMAGE);
    }

    public Archer(Texture texture, float x, float y, float w, float h, float speed, int damage) {
        super(texture, x, y, w, h, speed, damage);
    }
}