package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/**
 * Base enemy: holds position/size (bounds), speed, damage, and common behavior.
 * Children (Goblin, Hobgoblin) just choose texture/size/speed/damage.
 */
public class Enemy {

    protected final Texture texture;
    protected final Rectangle bounds;  // x, y, width, height
    protected float speed;             // pixels per second
    protected int damage;              // collision damage

    /**
     * @param texture sprite/texture to draw
     * @param x,y,w,h screen-space rectangle (pixels)
     * @param speed   movement speed (px/sec)
     * @param damage  damage dealt on collision
     */
    public Enemy(Texture texture, float x, float y, float w, float h, float speed, int damage) {
        this.texture = texture;
        this.bounds  = new Rectangle(x, y, w, h);
        this.speed   = speed;
        this.damage  = damage;
    }

    /** Move toward (targetX, targetY). Pass the player's center. */
    public void updateToward(float targetX, float targetY, float dt) {
        float cx = bounds.x + bounds.width  * 0.5f;
        float cy = bounds.y + bounds.height * 0.5f;
        float dx = targetX - cx;
        float dy = targetY - cy;
        float len = (float)Math.sqrt(dx*dx + dy*dy);
        if (len > 1e-4f) {
            bounds.x += (dx / len) * speed * dt;
            bounds.y += (dy / len) * speed * dt;
        }
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, bounds.x, bounds.y, bounds.width, bounds.height);
    }

    public Rectangle getBoundingBox() { return bounds; }
    public int       getDamage()      { return damage; }
    public float     getSpeed()       { return speed;  }
    public void      setSpeed(float s){ speed = s;     }

    /** Optional hook when colliding with player (knockback, sound, etc.). */
    public void onPlayerCollision() {
        // default: do nothing
    }

    /** Teleport/move instantly (useful for spawns). */
    public void moveTo(float x, float y) {
        bounds.setPosition(x, y);
    }
}
