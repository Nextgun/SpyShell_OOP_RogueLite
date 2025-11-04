// Author: Martin Taylor
// File: EnemyContext.java
// Date: 2025-11-04
// Description: Gameplay hooks exposed to Enemy subclasses so behavior lives
//              in the enemy classes, not the sandbox/app loop.

package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public interface EnemyContext {
    // -----------------------------
    // Player info
    // -----------------------------
    Vector2 getPlayerCenter();                 // (px, py)
    Rectangle getPlayerBounds();

    // Handy scalar accessors, if you want them
    default float getPlayerCenterX() { return getPlayerCenter().x; }
    default float getPlayerCenterY() { return getPlayerCenter().y; }

    // -----------------------------
    // World / collision
    // -----------------------------
    boolean isBlocked(Rectangle worldRect);    // AABB vs world (true = blocked)

    Vector2 nearestOpen(Vector2 worldPos, int maxRadiusTiles);

    // Float overload: return a new Vector2 at the nearest open spot
    default Vector2 nearestOpen(float x, float y, int maxRadiusTiles) {
        return nearestOpen(new Vector2(x, y), maxRadiusTiles);
    }

    // -----------------------------
    // Spawns / actions
    // -----------------------------
    void spawnArrow(Texture tex, Vector2 origin, Vector2 velocity, int damage);
    void spawnBomb(Vector2 center);
    void damagePlayer(int amount);

    // Float overloads for convenience
    default void spawnArrow(Texture tex, float ox, float oy, float vx, float vy, int damage) {
        spawnArrow(tex, new Vector2(ox, oy), new Vector2(vx, vy), damage);
    }
    default void spawnBomb(float cx, float cy) {
        spawnBomb(new Vector2(cx, cy));
    }

    // -----------------------------
    // Utilities
    // -----------------------------
    float getTileSize();
    boolean hasLineOfSight(Vector2 a, Vector2 b);

    // ✅ Float overload used by EnemySandboxApp
    default boolean hasLineOfSight(float x0, float y0, float x1, float y1) {
        return hasLineOfSight(new Vector2(x0, y0), new Vector2(x1, y1));
    }
}
