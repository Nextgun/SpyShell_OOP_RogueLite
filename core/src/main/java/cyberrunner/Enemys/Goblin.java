//Goblin.java
package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;

/** Small chaser. You set goblin damage to 3. HP = 1. */
public class Goblin extends Enemy {

    public static final float DEFAULT_SPEED = 260f; // a bit slower than bomber

    public Goblin(Texture tex, float x, float y, float w, float h) {
        // 7-arg ctor (HP defaults to 1)
        super(tex, x, y, w, h, DEFAULT_SPEED, 3);
        setMaxHp(1);
        setHp(1);
    }

    public Goblin(Texture tex, float x, float y, float w, float h, float speed, int damage) {
        super(tex, x, y, w, h, speed, damage);
        setMaxHp(1);
        setHp(1);
    }
}
