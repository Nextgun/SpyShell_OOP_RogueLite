//Bomber,java
package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Fast chaser that drops a bomb on contact or death (explosion handled elsewhere). */
public class Bomber extends Enemy {

    public static final float DEFAULT_SPEED  = 340f; // faster than player
    public static final int   CONTACT_DAMAGE = 0;    // bomber’s explosion does damage, not the touch

    public Bomber(Texture tex, float x, float y, float w, float h) {
        // 7-arg ctor (Enemy supplies HP=1 by default)
        super(tex, x, y, w, h, DEFAULT_SPEED, CONTACT_DAMAGE);
    }

    public Bomber(Texture tex, float x, float y, float w, float h, float speed, int damage) {
        super(tex, x, y, w, h, speed, damage);
    }

    /** Hook called by CollisionManager / sandbox when the player overlaps this enemy. */
    @Override
    public void onPlayerCollision() {
        // left intentionally empty; your game loop handles spawning the bomb
    }

    /** Basic render; sandbox may draw enemies itself. */
    public void render(SpriteBatch batch) {
        Rectangle r = getBoundingBox();
        batch.draw(getTexture(), r.x, r.y, r.width, r.height);
    }
}
