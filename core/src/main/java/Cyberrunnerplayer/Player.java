package Cyberrunnerplayer;


import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;


public class Player {
    private int x, y;          // Position
    private int speed = 5;     // Movement speed
    private Set<Integer> keys; // Track pressed keys

    public Player(int startX, int startY) {
        this.x = startX;
        this.y = startY;
        this.keys = new HashSet<>();
    }

    // Handle key press
    public void keyPressed(int keyCode) {
        keys.add(keyCode);
    }

    // Handle key release
    public void keyReleased(int keyCode) {
        keys.remove(keyCode);
    }

    // Update position based on keys pressed
    public void update() {
        if (keys.contains(KeyEvent.VK_W) || keys.contains(KeyEvent.VK_UP))    y -= speed;
        if (keys.contains(KeyEvent.VK_S) || keys.contains(KeyEvent.VK_DOWN))  y += speed;
        if (keys.contains(KeyEvent.VK_A) || keys.contains(KeyEvent.VK_LEFT))  x -= speed;
        if (keys.contains(KeyEvent.VK_D) || keys.contains(KeyEvent.VK_RIGHT)) x += speed;
    }

    // Draw player
    public void draw(Graphics2D g) {
        g.setColor(Color.BLUE);
        g.fillRect(x, y, 32, 32); // simple red square
    }

    // Getters
    public int getX() { return x; }
    public int getY() { return y; }
}