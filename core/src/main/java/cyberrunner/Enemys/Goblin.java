package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Rectangle;

/** Simple melee enemy that moves toward a target (the turtle). */
public class Goblin {
    private final Texture texture;
    private final Rectangle bounds; // x, y, width, height
    private float speed;            // pixels per second

    public Goblin(Texture texture, float x, float y, float w, float h, float speed) {
        this.texture = texture;
        this.bounds  = new Rectangle(x, y, w, h);
        this.speed   = speed;
    }

    /** Move toward (targetX, targetY) — pass the turtle’s center. */
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

    public Rectangle getBounds() { return bounds; }

    public void setSpeed(float speed) { this.speed = speed; }
    public float getSpeed() { return speed; }

    public void dispose() { texture.dispose(); }
}
