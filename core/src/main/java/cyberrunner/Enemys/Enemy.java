package cyberrunner.Enemys;

import com.badlogic.gdx.math.Rectangle;

public class Enemy {
    private Rectangle boundingBox;
    private int damage;

    public Enemy(float x, float y, float width, float height, int damage) {
        this.boundingBox = new Rectangle(x, y, width, height);
        this.damage = damage;
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public int getDamage() {
        return damage;
    }

    public void onPlayerCollision() {
        System.out.println("Enemy collided with player!");
        // Add optional behavior
    }

    public void moveTo(float x, float y) {
        boundingBox.setPosition(x, y);
    }
}
