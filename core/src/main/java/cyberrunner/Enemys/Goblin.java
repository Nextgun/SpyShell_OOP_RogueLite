package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;

/** Small melee chaser. Slower; deals 1 damage on hit. */
public class Goblin extends Enemy {

    // Defaults (tweak as you like)
    public static final float DEFAULT_SPEED  = 140f; // px/sec
    public static final int   DEFAULT_DAMAGE = 1;    // per hit

    /** Use this when you just want a normal goblin. */
    public Goblin(Texture texture, float x, float y, float w, float h) {
        super(texture, x, y, w, h, DEFAULT_SPEED, DEFAULT_DAMAGE);
    }

    /** Optional: custom speed/damage variant (e.g., for special spawns). */
    public Goblin(Texture texture, float x, float y, float w, float h, float speed, int damage) {
        super(texture, x, y, w, h, speed, damage);
    }
}