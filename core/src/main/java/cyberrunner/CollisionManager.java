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

    public static void checkPlayerEnemyCollisions(Player player, List<Enemy> enemies) {
        Rectangle playerBounds = player.getBoundingBox();
        List<Enemy> toRemove = new ArrayList<>();

        for (Enemy enemy : enemies) {
            if (!playerBounds.overlaps(enemy.getBoundingBox())) continue;

            if (enemy instanceof Bomber) {
                // Signal bomb drop (your game loop performs the actual spawn at enemy position)
                enemy.onPlayerCollision();
                toRemove.add(enemy);
            } else {
                int damage = Math.max(0, enemy.getDamage());
                if (damage > 0) player.takeDamage(damage);
                enemy.onPlayerCollision();
                // If certain enemies should despawn on touch, add to 'toRemove' here.
            }
        }

        if (!toRemove.isEmpty()) enemies.removeAll(toRemove);
    }
}
