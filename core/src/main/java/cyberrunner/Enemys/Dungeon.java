// Author: Martin Taylor
// File: Dungeon.java
// Date: 2025-11-04
// Description: Simple tile-based dungeon generator with random rooms and
//              3-tile-wide L-shaped corridors. Provides collision tests and
//              near-open queries in world space.

package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/**
 * Simple dungeon generator: random rooms + L-shaped corridors on a tile grid.
 * Corridors are 3 tiles wide and a thick central cross guarantees exits.
 */
public class Dungeon {
    public final int width;     // tiles
    public final int height;    // tiles
    public final int tileSize;  // pixels per tile

    // solid = true means wall; false means floor
    private final boolean[][] solid;

    public Dungeon(int width, int height, int tileSize) {
        this.width = width;
        this.height = height;
        this.tileSize = tileSize;
        this.solid = new boolean[width][height];
    }

    /** Build a new dungeon using the provided seed. */
    public void generate(long seed) {
        MathUtils.random.setSeed(seed);

        // Start fully solid (all walls)
        fill(true);

        // Carve random rooms
        int roomCount = 40;
        for (int iRoom = 0; iRoom < roomCount; iRoom++) {
            int roomWidthTiles  = MathUtils.random(5, 11);
            int roomHeightTiles = MathUtils.random(5, 11);
            int roomX = MathUtils.random(1, width - roomWidthTiles - 2);
            int roomY = MathUtils.random(1, height - roomHeightTiles - 2);
            carveRect(roomX, roomY, roomWidthTiles, roomHeightTiles);
        }

        // Choose random open anchors and connect them with corridors
        int anchorCount = 30;
        int[] anchorX = new int[anchorCount];
        int[] anchorY = new int[anchorCount];
        for (int iAnchor = 0; iAnchor < anchorCount; iAnchor++) {
            int attempts = 0;
            int x, y;
            do {
                x = MathUtils.random(2, width - 3);
                y = MathUtils.random(2, height - 3);
                attempts++;
            } while (isSolid(x, y) && attempts < 100);
            anchorX[iAnchor] = x;
            anchorY[iAnchor] = y;
        }
        for (int i = 0; i < anchorCount - 1; i++) {
            carveCorridor(anchorX[i], anchorY[i], anchorX[i + 1], anchorY[i + 1]);
        }

        // Ensure a small spawn area around the center is open
        int centerTileX = width / 2;
        int centerTileY = height / 2;
        carveRect(centerTileX - 2, centerTileY - 2, 5, 5);

        // Thick central cross so the player can always leave spawn
        carveLineX(centerTileX - 12, centerTileX + 12, centerTileY); // 3 tiles thick horizontally
        carveLineY(centerTileY - 12, centerTileY + 12, centerTileX); // 3 tiles thick vertically
        thickenAround(centerTileX, centerTileY, 3);
    }

    private void fill(boolean value) {
        for (int tx = 0; tx < width; tx++)
            for (int ty = 0; ty < height; ty++)
                solid[tx][ty] = value;
    }

    private void carveRect(int x, int y, int w, int h) {
        for (int tx = x; tx < x + w; tx++) {
            for (int ty = y; ty < y + h; ty++) {
                if (inBounds(tx, ty)) solid[tx][ty] = false;
            }
        }
    }

    private void carveCorridor(int x0, int y0, int x1, int y1) {
        // L-shaped: horizontal then vertical (randomize order). Each line is 3 tiles thick.
        if (MathUtils.randomBoolean()) {
            carveLineX(x0, x1, y0);
            carveLineY(y0, y1, x1);
        } else {
            carveLineY(y0, y1, x0);
            carveLineX(x0, x1, y1);
        }
    }

    // 3-tile thick horizontal corridor centered on y
    private void carveLineX(int x0, int x1, int y) {
        if (x0 > x1) { int swap = x0; x0 = x1; x1 = swap; }
        for (int tx = x0; tx <= x1; tx++) {
            for (int offsetY = -1; offsetY <= 1; offsetY++) {
                int ty = y + offsetY;
                if (inBounds(tx, ty)) solid[tx][ty] = false;
            }
        }
    }

    // 3-tile thick vertical corridor centered on x
    private void carveLineY(int y0, int y1, int x) {
        if (y0 > y1) { int swap = y0; y0 = y1; y1 = swap; }
        for (int ty = y0; ty <= y1; ty++) {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                int tx = x + offsetX;
                if (inBounds(tx, ty)) solid[tx][ty] = false;
            }
        }
    }

    private void thickenAround(int centerX, int centerY, int radius) {
        for (int tx = centerX - radius; tx <= centerX + radius; tx++)
            for (int ty = centerY - radius; ty <= centerY + radius; ty++)
                if (inBounds(tx, ty)) solid[tx][ty] = false;
    }

    private boolean inBounds(int tx, int ty) {
        return tx >= 0 && ty >= 0 && tx < width && ty < height;
    }

    /** True if tile is a wall. */
    public boolean isSolid(int tx, int ty) {
        if (!inBounds(tx, ty)) return true;
        return solid[tx][ty];
    }

    /** World-space (pixels) check: is this world position inside a wall tile? */
    public boolean isSolidWorld(float worldX, float worldY) {
        int tileX = (int)Math.floor(worldX / tileSize);
        int tileY = (int)Math.floor(worldY / tileSize);
        return isSolid(tileX, tileY);
    }

    /** Axis-aligned rectangle vs. solid tiles collision test (world space). */
    public boolean rectBlocked(Rectangle rect) {
        float epsilon = 0.5f;
        float left   = rect.x + epsilon;
        float bottom = rect.y + epsilon;
        float right  = rect.x + rect.width  - epsilon;
        float top    = rect.y + rect.height - epsilon;
        return isSolidWorld(left, bottom) || isSolidWorld(right, bottom) ||
               isSolidWorld(left, top)    || isSolidWorld(right, top);
    }

    /** Find nearest open tile center to the given world position. */
    public float[] nearestOpen(float worldX, float worldY, int maxRadiusTiles) {
        int startTileX = MathUtils.floor(worldX / tileSize);
        int startTileY = MathUtils.floor(worldY / tileSize);
        if (!isSolid(startTileX, startTileY)) return new float[]{centerX(startTileX), centerY(startTileY)};

        for (int radius = 1; radius <= maxRadiusTiles; radius++) {
            for (int deltaX = -radius; deltaX <= radius; deltaX++) {
                int x = startTileX + deltaX;
                int yTop = startTileY + radius;
                int yBottom = startTileY - radius;
                if (inBounds(x, yTop) && !solid[x][yTop])    return new float[]{centerX(x), centerY(yTop)};
                if (inBounds(x, yBottom) && !solid[x][yBottom]) return new float[]{centerX(x), centerY(yBottom)};
            }
            for (int deltaY = -radius + 1; deltaY <= radius - 1; deltaY++) {
                int y = startTileY + deltaY;
                int xRight = startTileX + radius;
                int xLeft  = startTileX - radius;
                if (inBounds(xRight, y) && !solid[xRight][y]) return new float[]{centerX(xRight), centerY(y)};
                if (inBounds(xLeft, y)  && !solid[xLeft][y])  return new float[]{centerX(xLeft),  centerY(y)};
            }
        }
        return new float[]{0f, 0f};
    }

    private float centerX(int tileX) { return tileX * tileSize + tileSize * 0.5f; }
    private float centerY(int tileY) { return tileY * tileSize + tileSize * 0.5f; }

    /** Draw only tiles that intersect the camera view rectangle. */
    public void render(SpriteBatch batch, Texture floorTexture, Texture wallTexture,
                       float viewLeft, float viewBottom, float viewWidth, float viewHeight) {
        int tileLeft   = Math.max(0, (int)Math.floor(viewLeft / tileSize) - 1);
        int tileBottom = Math.max(0, (int)Math.floor(viewBottom / tileSize) - 1);
        int tileRight  = Math.min(width - 1,  (int)Math.ceil((viewLeft + viewWidth) / tileSize)  + 1);
        int tileTop    = Math.min(height - 1, (int)Math.ceil((viewBottom + viewHeight) / tileSize) + 1);

        for (int tx = tileLeft; tx <= tileRight; tx++) {
            for (int ty = tileBottom; ty <= tileTop; ty++) {
                Texture tileTexture = solid[tx][ty] ? wallTexture : floorTexture;
                batch.draw(tileTexture, tx * tileSize, ty * tileSize, tileSize, tileSize);
            }
        }
    }
}
