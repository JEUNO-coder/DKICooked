package io.github.DKICooked.gameLogic.generationLogic;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.entities.Platform;

public class LevelGenerator {
    private final float SCREEN_WIDTH = 800f;
    private final float MARGIN = 120f;
    private final float MAX_JUMP_H = 225f;
    private final float MAX_JUMP_W = 300f;

    // THESE MUST BE OUTSIDE THE METHOD TO PERSIST BETWEEN CHUNKS
    private float anchor1X = 300f, anchor1Y = 50f;
    private float anchor2X = 500f, anchor2Y = 50f;

    public Array<Platform> generateChunk(float chunkYStart, float chunkHeight) {
        Array<Platform> platforms = new Array<>();
        float chunkTop = chunkYStart + chunkHeight;

        // 1. THE STITCH: If this isn't the first chunk, we MUST anchor to the previous one
        if (chunkYStart == 0) {
            platforms.add(new Platform(0, 50, SCREEN_WIDTH, 50));
            anchor1X = 300f; anchor2X = 500f;
            anchor1Y = anchor2Y = 80f;
        }

        // 2. DUAL-PATH GENERATION
        // We continue until the paths reach the top of THIS specific chunk
        while (anchor1Y < chunkTop - 150f || anchor2Y < chunkTop - 150f) {

            // Generate Path 1 (Left Option)
            if (anchor1Y < chunkTop - 150f) {
                anchor1Y = attemptStep(platforms, anchor1X, anchor1Y, true);
                anchor1X = lastX;
            }

            // Generate Path 2 (Right Option)
            if (anchor2Y < chunkTop - 150f) {
                anchor2Y = attemptStep(platforms, anchor2X, anchor2Y, false);
                anchor2X = lastX;
            }
        }
        return platforms;
    }

    private float lastX; // Temporary storage

    private float attemptStep(Array<Platform> platforms, float curX, float curY, boolean leftPath) {
        // Use 80% of max jump for a "Safe" human-reachable distance
        float jumpY = MAX_JUMP_H * MathUtils.random(0.65f, 0.82f);
        float jumpX = MAX_JUMP_W * MathUtils.random(0.4f, 0.75f) * (MathUtils.randomBoolean() ? 1 : -1);

        float nX = MathUtils.clamp(curX + jumpX, MARGIN, SCREEN_WIDTH - MARGIN);
        float nY = curY + jumpY;

        Platform p = new Platform(nX - 60, nY, nX + 60, nY);

        // 3. THE "ARC" CHECK: Ensure nothing blocks the head mid-jump
        if (isPathPlayable(curX, curY, p, platforms)) {
            // Sporadic Architecture: Thickness + Occasional Back-Walls
            p.thickness = MathUtils.randomBoolean(0.3f) ? 50f : 10f;

            if (MathUtils.randomBoolean(0.3f)) { // Only 30% have walls
                float wallX = (nX > curX) ? p.x2 - 12 : p.x1;
                platforms.add(new Platform(wallX, p.y1, wallX + 12, p.y1 + 100));
            }

            platforms.add(p);
            lastX = nX;
            return nY;
        }

        lastX = curX;
        return curY + 40f; // Nudge up to try finding a valid spot
    }

    private boolean isPathPlayable(float startX, float startY, Platform target, Array<Platform> existing) {
        for (Platform other : existing) {
            // Horizontal overlap check
            float overlap = Math.min(target.x2, other.x2) - Math.max(target.x1, other.x1);

            // If they overlap, we need a 170px vertical "Headroom" corridor
            if (overlap > -30f) {
                float gap = Math.abs(other.y1 - startY);
                if (gap < 170f && other.y1 > startY) return false;
            }
        }
        return true;
    }
}
