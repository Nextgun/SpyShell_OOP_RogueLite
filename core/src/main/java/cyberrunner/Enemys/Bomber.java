// Author: Martin Taylor
// File: Bomber.java
// Date: 2025-11-04
// Description:
//   Fast chaser that causes damage via dropped bomb (on hit/death).
//   Touch collision itself deals 0 here; your loop spawns the bomb.

package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

public class Bomber extends Enemy {

    public static final float DEFAULT_SPEED  = 340f;
    public static final int   CONTACT_DAMAGE = 0; // explosion is the threat, not the touch

    public Bomber(Texture tex, float x, float y, float w, float h) {
        super(tex, x, y, w, h, DEFAULT_SPEED, CONTACT_DAMAGE);
    }

    public Bomber(Texture tex, float x, float y, float w, float h, float speed, int damage) {
        super(tex, x, y, w, h, speed, damage);
    }

    @Override
    public void onPlayerCollision() {
        // Intentionally empty – your game loop/CollisionManager drops the bomb.
    }

    public void render(SpriteBatch batch) {
        Rectangle r = getBoundingBox();
        batch.draw(getTexture(), r.x, r.y, r.width, r.height);
    }
}
