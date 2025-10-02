package cyberrunner.Player;

import com.badlogic.gdx.math.Rectangle;

public class Player {
    private Rectangle boundingBox;
    private int health;

    public Player(float x, float y, float width, float height, int health) {
        this.boundingBox = new Rectangle(x, y, width, height);
        this.health = health;
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public void moveTo(float x, float y) {
        boundingBox.setPosition(x, y);
    }

    public void takeDamage(int amount) {
        health -= amount;
        System.out.println("Player took " + amount + " damage. Health: " + health);
    }

    public int getHealth() {
        return health;
    }
}
