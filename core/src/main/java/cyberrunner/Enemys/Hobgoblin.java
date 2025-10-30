//Hobgoblin.java
package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;

/** Larger, tougher goblin variant. You set hobgoblin damage to 5. HP = 1. */
public class Hobgoblin extends Enemy {

    public static final float DEFAULT_SPEED = 280f;

    public Hobgoblin(Texture tex, float x, float y, float w, float h) {
        // 7-arg ctor (HP defaults to 1)
        super(tex, x, y, w, h, DEFAULT_SPEED, 5);
        setMaxHp(1);
        setHp(1);
    }

    public Hobgoblin(Texture tex, float x, float y, float w, float h, float speed, int damage) {
        super(tex, x, y, w, h, speed, damage);
        setMaxHp(1);
        setHp(1);
    }
}
