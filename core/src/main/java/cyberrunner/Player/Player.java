// Author: Martin Taylor
// File: Player.java
// Date: 2025-11-04
// Description:
//   Simple player DTO with AABB, health, and basic movement helpers.

package cyberrunner.Player;

import com.badlogic.gdx.math.Rectangle;

public class Player {
    private final Rectangle boundingBox;
    private int health;
    private int maxHealth;

    public Player(float x, float y, float width, float height, int health) {
        this.boundingBox = new Rectangle(x, y, width, height);
        this.maxHealth = Math.max(1, health);
        this.health = this.maxHealth;
    }

    public Rectangle getBoundingBox() { return boundingBox; }

    public void moveTo(float x, float y) { boundingBox.setPosition(x, y); }

    public void translate(float dx, float dy) {
        boundingBox.setPosition(boundingBox.x + dx, boundingBox.y + dy);
    }

    public void takeDamage(int amount) {
        if (amount <= 0) return;
        health = Math.max(0, health - amount);
        System.out.println("Player took " + amount + " damage. Health: " + health);
    }

    public void heal(int amount) {
        if (amount <= 0) return;
        health = Math.min(maxHealth, health + amount);
    }

    public int getHealth() { return health; }
    public int getMaxHealth() { return maxHealth; }
    public void setMaxHealth(int max) {
        maxHealth = Math.max(1, max);
        health = Math.min(health, maxHealth);
    }
}
