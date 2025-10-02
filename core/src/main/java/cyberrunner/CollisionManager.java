package cyberrunner;

import com.badlogic.gdx.math.Rectangle;
import cyberrunner.Enemys.Enemy;
import cyberrunner.Player.Player;

import java.util.List;

public class CollisionManager {

    public static void checkPlayerEnemyCollisions(Player player, List<Enemy> enemies) {
        Rectangle playerBounds = player.getBoundingBox();

        for (Enemy enemy : enemies) {
            if (playerBounds.overlaps(enemy.getBoundingBox())) {
                handleCollision(player, enemy);
            }
        }
    }

    private static void handleCollision(Player player, Enemy enemy) {
        player.takeDamage(enemy.getDamage());
        enemy.onPlayerCollision();
    }
}
