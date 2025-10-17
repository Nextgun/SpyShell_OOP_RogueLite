package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;

/** Tougher chaser. Slightly faster; deals 5 damage on hit. */
public class Hobgoblin extends Enemy {

    // Defaults (tweak as you like)
    public static final float DEFAULT_SPEED  = 170f; // px/sec (faster than goblin)
    public static final int   DEFAULT_DAMAGE = 5;    // per hit

    /** Use this when you just want a normal hobgoblin. */
    public Hobgoblin(Texture texture, float x, float y, float w, float h) {
        super(texture, x, y, w, h, DEFAULT_SPEED, DEFAULT_DAMAGE);
    }

    /** Optional: custom speed/damage variant. */
    public Hobgoblin(Texture texture, float x, float y, float w, float h, float speed, int damage) {
        super(texture, x, y, w, h, speed, damage);
    }
}