package io.github.DKICooked.gameLogic.generationLogic;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.entities.Platform;

public class LevelGenerator {

    // These constants should match your Player physics capabilities
    private static final float MAX_JUMP_HEIGHT = 170f;
    private static final float MIN_JUMP_HEIGHT = 130f;
    private static final float MAX_JUMP_WIDTH = 250f;

    private static final float SCREEN_WIDTH = 800f;
    private static final float MARGIN = 100f; // Keep platforms away from screen edges

    // Track the "Anchor" from the previous chunk to ensure continuity
    private float lastX = SCREEN_WIDTH / 2;
    private float lastY = 100f;

    /**
     * Generates a new chunk of platforms.
     * @param chunkYStart The world Y coordinate where this chunk begins.
     * @param chunkHeight The vertical size of the chunk.
     * @return An array of Platforms forming a guaranteed path.
     */
    public Array<Platform> generateChunk(float chunkYStart, float chunkHeight) {
        Array<Platform> platforms = new Array<>();

        if (chunkYStart == 0) {
            platforms.add(new Platform(0, 50, 800, 50));
            lastX = 400; lastY = 50;
        }

        float targetChunkTop = chunkYStart + chunkHeight;
        int chunkIndex = (int)(chunkYStart / chunkHeight);

        while (lastY < targetChunkTop - 100f) {
            // 1. DENSITY SCALING: Jumps get slightly harder as you go higher
            float difficultyMod = Math.min(1.2f, 1.0f + (chunkIndex * 0.02f));
            float jumpX = MathUtils.random(-MAX_JUMP_WIDTH, MAX_JUMP_WIDTH) * difficultyMod;
            float jumpY = MathUtils.random(MIN_JUMP_HEIGHT, MAX_JUMP_HEIGHT);

            float nextX = MathUtils.clamp(lastX + jumpX, MARGIN, SCREEN_WIDTH - MARGIN);
            float nextY = lastY + jumpY;

            // 2. LEDGE HANG GRACE: If it's a wide horizontal jump, make the platform wider
            float jumpDistance = Math.abs(nextX - lastX);
            float width = MathUtils.random(100f, 150f);
            if (jumpDistance > MAX_JUMP_WIDTH * 0.8f) width += 40f;

            float slope = MathUtils.random(-20f, 20f);
            Platform p = createPlatformFromCenter(nextX, nextY, width, slope);

            // 3. THE HEAD-BONK FILTER
            // Check if this new platform is too close to the one below it
            if (isValidPlacement(p, platforms)) {
                platforms.add(p);
                lastX = nextX;
                lastY = nextY;
            }
        }
        return platforms;
    }

    private boolean isValidPlacement(Platform newP, Array<Platform> existing) {
        for (Platform other : existing) {
            // Simple AABB-style check: Is it directly above another platform
            // with less than Player_Height (60) + Buffer (20) space?
            float xOverlap = Math.min(newP.x2, other.x2) - Math.max(newP.x1, other.x1);
            if (xOverlap > 0) {
                float yDist = Math.abs(newP.y1 - other.y1);
                if (yDist < 80f) return false;
            }
        }
        return true;
    }

    private void addDecoys(Array<Platform> platforms, float yStart, float height) {
        for (int i = 0; i < 3; i++) {
            float dx = MathUtils.random(MARGIN, SCREEN_WIDTH - MARGIN);
            float dy = MathUtils.random(yStart, yStart + height);
            float dw = MathUtils.random(80, 120);
            platforms.add(new Platform(dx - dw/2, dy, dx + dw/2, dy + MathUtils.random(-30, 30)));
        }
    }
    private Platform createPlatformFromCenter(float x, float y, float width, float slope) {
        float x1 = x - (width / 2f);
        float x2 = x + (width / 2f);

        // We split the slope so the 'y' coordinate is exactly in the middle
        // of the tilt. This makes the jump math way more accurate.
        float y1 = y - (slope / 2f);
        float y2 = y + (slope / 2f);

        return new Platform(x1, y1, x2, y2);
    }
}
