// Author: Martin Taylor
// File: Goblin.java
// Date: 2025-11-04
// Description:
//   Small melee chaser. Uses base class movement helper in future if needed.
//   Defaults to HP=1 and modest speed.

package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;

public class Goblin extends Enemy {

    public static final float DEFAULT_SPEED = 260f; // a bit slower than bomber
    public static final int   TOUCH_DAMAGE  = 3;

    public Goblin(Texture tex, float x, float y, float w, float h) {
        super(tex, x, y, w, h, DEFAULT_SPEED, TOUCH_DAMAGE);
        setMaxHp(1);
        setHp(1);
    }

    public Goblin(Texture tex, float x, float y, float w, float h, float speed, int damage) {
        super(tex, x, y, w, h, speed, damage);
        setMaxHp(1);
        setHp(1);
    }
}
