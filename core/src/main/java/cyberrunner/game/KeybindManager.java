package cyberrunner.game;

import com.badlogic.gdx.Input;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Gdx;

public class KeybindManager {
    private int moveLeft;
    private int moveRight;
    private int jump;
    private int dash;
    
    private Preferences prefs;
    
    public KeybindManager() {
        prefs = Gdx.app.getPreferences("cyberrunner-keybinds");
        loadKeybinds();
    }
    
    private void loadKeybinds() {
        // Load keybinds from preferences, or use defaults
        moveLeft = prefs.getInteger("moveLeft", Input.Keys.A);
        moveRight = prefs.getInteger("moveRight", Input.Keys.D);
        jump = prefs.getInteger("jump", Input.Keys.SPACE);
        dash = prefs.getInteger("dash", Input.Keys.SHIFT_LEFT);
    }
    
    private void saveKeybinds() {
        prefs.putInteger("moveLeft", moveLeft);
        prefs.putInteger("moveRight", moveRight);
        prefs.putInteger("jump", jump);
        prefs.putInteger("dash", dash);
        prefs.flush();
    }
    
    // Getters
    public int getMoveLeft() { return moveLeft; }
    public int getMoveRight() { return moveRight; }
    public int getJump() { return jump; }
    public int getDash() { return dash; }
    
    // Setters that also save
    public void setMoveLeft(int key) {
        moveLeft = key;
        saveKeybinds();
    }
    
    public void setMoveRight(int key) {
        moveRight = key;
        saveKeybinds();
    }
    
    public void setJump(int key) {
        jump = key;
        saveKeybinds();
    }
    
    public void setDash(int key) {
        dash = key;
        saveKeybinds();
    }
    
    // Helper method to check if a key is pressed (use this in your game logic)
    public boolean isMoveLeftPressed() {
        return Gdx.input.isKeyPressed(moveLeft);
    }
    
    public boolean isMoveRightPressed() {
        return Gdx.input.isKeyPressed(moveRight);
    }
    
    public boolean isJumpPressed() {
        return Gdx.input.isKeyPressed(jump);
    }
    
    public boolean isDashPressed() {
        return Gdx.input.isKeyPressed(dash);
    }
}