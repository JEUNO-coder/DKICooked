package io.github.DKICooked.gameLogic.generationLogic;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.entities.Platform;

public class LevelGenerator {
    private final float SCREEN_WIDTH = 800f;
    private final float MARGIN = 120f;
    private final float MAX_JUMP_H = 225f;
    private final float MAX_JUMP_W = 300f;

    // Safety padding to prevent platforms from being "shoulder-to-shoulder"
    private final float HORIZONTAL_PADDING = 40f;
    private final float VERTICAL_PADDING = 60f;

    private float anchor1X = 300f, anchor1Y = 50f;
    private float anchor2X = 500f, anchor2Y = 50f;
    private float lastX;

    public Array<Platform> generateChunk(float chunkYStart, float chunkHeight) {
        Array<Platform> platforms = new Array<>();
        float chunkTop = chunkYStart + chunkHeight;

        if (chunkYStart == 0) {
            platforms.add(new Platform(0, 50, SCREEN_WIDTH, 50));
            anchor1X = 300f; anchor2X = 500f;
            anchor1Y = anchor2Y = 80f;
        }

        while (anchor1Y < chunkTop - 150f || anchor2Y < chunkTop - 150f) {
            if (anchor1Y < chunkTop - 150f) {
                anchor1Y = attemptStep(platforms, anchor1X, anchor1Y);
                anchor1X = lastX;
            }
            if (anchor2Y < chunkTop - 150f) {
                anchor2Y = attemptStep(platforms, anchor2X, anchor2Y);
                anchor2X = lastX;
            }
        }
        return platforms;
    }

    private float attemptStep(Array<Platform> platforms, float curX, float curY) {
        float jumpY = MAX_JUMP_H * MathUtils.random(0.65f, 0.85f);
        float jumpX = MAX_JUMP_W * MathUtils.random(0.4f, 0.8f) * (MathUtils.randomBoolean() ? 1 : -1);

        float nX = MathUtils.clamp(curX + jumpX, MARGIN, SCREEN_WIDTH - MARGIN);
        float nY = curY + jumpY;

        // Create a temporary platform object to test against existing ones
        Platform p = new Platform(nX - 60, nY, nX + 60, nY);

        if (isPathPlayable(p, platforms)) {
            p.thickness = MathUtils.randomBoolean(0.3f) ? 40f : 10f;

            // Thick Wall Logic (Integrated from previous fix)
            if (MathUtils.randomBoolean(0.3f)) {
                float wallThickness = 12f;
                // If moving right, put wall on right edge. If left, put on left.
                float wallX = (nX > curX) ? p.x2 - wallThickness : p.x1;
                // Vertical wall: x1 == x2 (The draw method will apply thickness)
                platforms.add(new Platform(wallX, p.y1, wallX, p.y1 + 100));
            }

            platforms.add(p);
            lastX = nX;
            return nY;
        }

        lastX = curX;
        return curY + 30f; // Nudge and try again
    }

    private boolean isPathPlayable(Platform target, Array<Platform> existing) {
        for (Platform other : existing) {
            // Check for AABB (Axis-Aligned Bounding Box) overlap with padding
            boolean xOverlap = target.x1 - HORIZONTAL_PADDING < other.x2 &&
                target.x2 + HORIZONTAL_PADDING > other.x1;

            boolean yOverlap = target.y1 - VERTICAL_PADDING < other.y2 &&
                target.y2 + VERTICAL_PADDING > other.y1;

            // If they overlap in both X and Y, it's a collision
            if (xOverlap && yOverlap) {
                return false;
            }

            // Headroom check: Ensure there isn't a platform directly above
            // the target that would bonk the player's head immediately
            if (xOverlap && other.y1 > target.y1 && (other.y1 - target.y1) < 160f) {
                return false;
            }
        }
        return true;
    }
}
