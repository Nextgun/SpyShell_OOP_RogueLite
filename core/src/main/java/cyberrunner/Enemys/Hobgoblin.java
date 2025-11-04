// Author: Martin Taylor
// File: Hobgoblin.java
// Date: 2025-11-04
// Description:
//   Larger, tougher goblin variant for touch damage. Defaults to HP=1.

package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;

public class Hobgoblin extends Enemy {

    public static final float DEFAULT_SPEED = 280f; // slightly quicker
    public static final int   TOUCH_DAMAGE  = 5;

    public Hobgoblin(Texture tex, float x, float y, float w, float h) {
        super(tex, x, y, w, h, DEFAULT_SPEED, TOUCH_DAMAGE);
        setMaxHp(1);
        setHp(1);
    }

    public Hobgoblin(Texture tex, float x, float y, float w, float h, float speed, int damage) {
        super(tex, x, y, w, h, speed, damage);
        setMaxHp(1);
        setHp(1);
    }
}
