//CollisionManager.java
package cyberrunner;

import com.badlogic.gdx.math.Rectangle;
import cyberrunner.Enemys.Bomber;
import cyberrunner.Enemys.Enemy;
import cyberrunner.Player.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles player <-> enemy touch interactions without modifying the list
 * while iterating (prevents nested iterator errors).
 */
public class CollisionManager {

    public static void checkPlayerEnemyCollisions(Player player, List<Enemy> enemies) {
        Rectangle playerBounds = player.getBoundingBox();
        List<Enemy> toRemove = new ArrayList<>();

        for (Enemy enemy : enemies) {
            if (!playerBounds.overlaps(enemy.getBoundingBox())) continue;

            if (enemy instanceof Bomber) {
                // Bombers: no touch damage; signal a bomb drop and despawn
                Bomber b = (Bomber) enemy;
                b.onPlayerCollision();   // sets dropBombRequested = true
                toRemove.add(enemy);     // let the game loop spawn the bomb at its position
            } else {
                // Default enemies: apply their touch damage (if any), then notify them
                int dmg = Math.max(0, enemy.getDamage());
                if (dmg > 0) {
                    player.takeDamage(dmg);
                }
                enemy.onPlayerCollision();

                // If you want certain enemies to despawn on touch, you can add them to toRemove here.
                // e.g., if (enemy instanceof Goblin) toRemove.add(enemy);
            }
        }

        // Remove after the loop to avoid iterator nesting issues
        if (!toRemove.isEmpty()) {
            enemies.removeAll(toRemove);
        }
    }
}
