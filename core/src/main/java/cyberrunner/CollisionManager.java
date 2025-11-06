// Author: Martin Taylor
// File: CollisionManager.java
// Date: 2025-11-04
// Description:
//   Handles player <-> enemy touch interactions without mutating the enemy
//   list during iteration (avoids concurrent modification).

package cyberrunner;

import com.badlogic.gdx.math.Rectangle;
import cyberrunner.Enemys.Bomber;
import cyberrunner.Enemys.Enemy;
import cyberrunner.Player.Player;

import java.util.ArrayList;
import java.util.List;

public class CollisionManager {

    /**
     * Check for collisions between the player and all enemies.
     * Applies touch damage / collision hooks and removes any enemies
     * that should despawn after contact.
     */
    public static void checkPlayerEnemyCollisions(Player player, List<Enemy> enemies) {
        Rectangle playerBounds = player.getBoundingBox();
        List<Enemy> enemiesToRemove = new ArrayList<>();

        for (Enemy enemy : enemies) {
            if (!playerBounds.overlaps(enemy.getBoundingBox())) {
                continue;
            }

            if (enemy instanceof Bomber) {
                // Bombers: signal bomb-drop behavior and mark for removal.
                enemy.onPlayerCollision();
                enemiesToRemove.add(enemy);
            } else {
                // Default enemies: apply their touch damage (if any),
                // then allow subclass-specific behavior via onPlayerCollision().
                int damage = Math.max(0, enemy.getDamage());
                if (damage > 0) {
                    player.takeDamage(damage);
                }

                enemy.onPlayerCollision();
                // If specific enemy types should despawn on touch,
                // add them to enemiesToRemove here.
            }
        }

        // Remove after the loop to avoid iterator/concurrent modification issues.
        if (!enemiesToRemove.isEmpty()) {
            enemies.removeAll(enemiesToRemove);
        }
    } // End of method checkPlayerEnemyCollisions

} // End of class CollisionManager
