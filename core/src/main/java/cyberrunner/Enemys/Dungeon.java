//Dungeon.java
package cyberrunner.Enemys;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

/** Simple dungeon generator: random rooms + L-shaped corridors on a tile grid.
 *  Corridors are 3 tiles wide and a thick central cross guarantees exits. */
public class Dungeon {
    public final int width, height;   // tiles
    public final int tileSize;        // pixels per tile

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
        for (int i = 0; i < roomCount; i++) {
            int rw = MathUtils.random(5, 11);
            int rh = MathUtils.random(5, 11);
            int rx = MathUtils.random(1, width - rw - 2);
            int ry = MathUtils.random(1, height - rh - 2);
            carveRect(rx, ry, rw, rh);
        }

        // Choose random open anchors and connect them with corridors
        int anchorCount = 30;
        int[] cx = new int[anchorCount];
        int[] cy = new int[anchorCount];
        for (int i = 0; i < anchorCount; i++) {
            int tries = 0;
            int x, y;
            do {
                x = MathUtils.random(2, width - 3);
                y = MathUtils.random(2, height - 3);
                tries++;
            } while (isSolid(x, y) && tries < 100);
            cx[i] = x; cy[i] = y;
        }
        for (int i = 0; i < anchorCount - 1; i++) {
            carveCorridor(cx[i], cy[i], cx[i + 1], cy[i + 1]);
        }

        // Ensure a small spawn area around the center is open
        int scx = width / 2;
        int scy = height / 2;
        carveRect(scx - 2, scy - 2, 5, 5);

        // Thick central cross so the player can always leave spawn
        carveLineX(scx - 12, scx + 12, scy); // 3 tiles thick horizontally
        carveLineY(scy - 12, scy + 12, scx); // 3 tiles thick vertically
        thickenAround(scx, scy, 3);
    }

    private void fill(boolean value) {
        for (int x = 0; x < width; x++)
            for (int y = 0; y < height; y++)
                solid[x][y] = value;
    }

    private void carveRect(int x, int y, int w, int h) {
        for (int i = x; i < x + w; i++) {
            for (int j = y; j < y + h; j++) {
                if (inBounds(i, j)) solid[i][j] = false;
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
        if (x0 > x1) { int t = x0; x0 = x1; x1 = t; }
        for (int x = x0; x <= x1; x++) {
            for (int dy = -1; dy <= 1; dy++) {
                int yy = y + dy;
                if (inBounds(x, yy)) solid[x][yy] = false;
            }
        }
    }

    // 3-tile thick vertical corridor centered on x
    private void carveLineY(int y0, int y1, int x) {
        if (y0 > y1) { int t = y0; y0 = y1; y1 = t; }
        for (int y = y0; y <= y1; y++) {
            for (int dx = -1; dx <= 1; dx++) {
                int xx = x + dx;
                if (inBounds(xx, y)) solid[xx][y] = false;
            }
        }
    }

    private void thickenAround(int cx, int cy, int r) {
        for (int x = cx - r; x <= cx + r; x++)
            for (int y = cy - r; y <= cy + r; y++)
                if (inBounds(x, y)) solid[x][y] = false;
    }

    private boolean inBounds(int x, int y) {
        return x >= 0 && y >= 0 && x < width && y < height;
    }

    /** True if tile is a wall. */
    public boolean isSolid(int tx, int ty) {
        if (!inBounds(tx, ty)) return true;
        return solid[tx][ty];
    }

    /** World-space (pixels) check: is this world position inside a wall tile? */
    public boolean isSolidWorld(float wx, float wy) {
        int tx = (int)Math.floor(wx / tileSize);
        int ty = (int)Math.floor(wy / tileSize);
        return isSolid(tx, ty);
    }

    /** Axis-aligned rectangle vs. solid tiles collision test (world space). */
    public boolean rectBlocked(Rectangle r) {
        float eps = 0.5f;
        float x0 = r.x + eps, y0 = r.y + eps;
        float x1 = r.x + r.width - eps, y1 = r.y + r.height - eps;
        return isSolidWorld(x0, y0) || isSolidWorld(x1, y0) ||
               isSolidWorld(x0, y1) || isSolidWorld(x1, y1);
    }

    /** Find nearest open tile center to the given world position. */
    public float[] nearestOpen(float wx, float wy, int maxRadiusTiles) {
        int sx = MathUtils.floor(wx / tileSize);
        int sy = MathUtils.floor(wy / tileSize);
        if (!isSolid(sx, sy)) return new float[]{centerX(sx), centerY(sy)};

        for (int r = 1; r <= maxRadiusTiles; r++) {
            for (int dx = -r; dx <= r; dx++) {
                int x = sx + dx;
                int yTop = sy + r, yBot = sy - r;
                if (inBounds(x, yTop) && !solid[x][yTop]) return new float[]{centerX(x), centerY(yTop)};
                if (inBounds(x, yBot) && !solid[x][yBot]) return new float[]{centerX(x), centerY(yBot)};
            }
            for (int dy = -r + 1; dy <= r - 1; dy++) {
                int y = sy + dy;
                int xRight = sx + r, xLeft = sx - r;
                if (inBounds(xRight, y) && !solid[xRight][y]) return new float[]{centerX(xRight), centerY(y)};
                if (inBounds(xLeft, y) && !solid[xLeft][y])  return new float[]{centerX(xLeft),  centerY(y)};
            }
        }
        return new float[]{0f, 0f};
    }

    private float centerX(int tx) { return tx * tileSize + tileSize * 0.5f; }
    private float centerY(int ty) { return ty * tileSize + tileSize * 0.5f; }

    /** Draw only tiles that intersect the camera view rectangle. */
    public void render(SpriteBatch batch, Texture floorTex, Texture wallTex,
                       float viewLeft, float viewBottom, float viewW, float viewH) {
        int x0 = Math.max(0, (int)Math.floor(viewLeft / tileSize) - 1);
        int y0 = Math.max(0, (int)Math.floor(viewBottom / tileSize) - 1);
        int x1 = Math.min(width - 1, (int)Math.ceil((viewLeft + viewW) / tileSize) + 1);
        int y1 = Math.min(height - 1, (int)Math.ceil((viewBottom + viewH) / tileSize) + 1);

        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                Texture t = solid[x][y] ? wallTex : floorTex;
                batch.draw(t, x * tileSize, y * tileSize, tileSize, tileSize);
            }
        }
    }
}
