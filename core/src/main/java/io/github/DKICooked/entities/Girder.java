package io.github.DKICooked.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.Batch;
import io.github.DKICooked.render.DebugRenderer;

/**
 * Donkey-Kong-style girder:
 * - Slanted surface
 * - Optional hole
 * - Long, readable structure
 */
public class Girder extends Actor {

    // Vertical rise across the entire width
    private final float slope;

    // Hole (local X space)
    private float holeX = -1f;
    private float holeWidth = 0f;

    private static final Color GIRDER_RED =
        new Color(0.85f, 0.15f, 0.15f, 1f);

    public Girder(float x, float y, float width, float height, float slope) {
        setPosition(x, y);
        setSize(width, height);
        this.slope = slope;
    }

    /** Adds a gap players can fall through */
    public void addHole(float holeX, float holeWidth) {
        this.holeX = holeX;
        this.holeWidth = holeWidth;
    }

    /** Surface height at a given world X (for collision logic later) */
    public float getSurfaceY(float worldX) {
        float localX = MathUtils.clamp(worldX - getX(), 0, getWidth());
        float t = localX / getWidth();
        // Return the Y position + the slope height + the girder's own thickness
        return getY() + (slope * t) + getHeight();
    }

    public boolean hasHole() {
        return holeX >= 0;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        DebugRenderer.begin(getStage().getCamera());
        DebugRenderer.renderer.setColor(GIRDER_RED);

        float x = getX();
        float y = getY();
        float w = getWidth();
        float h = getHeight();


        if (hasHole()) {
            // Draw Left Segment
            float holeStartX = holeX;

            // Connect (x, y) to (x+holeX, holeStartBottomY) etc.
            drawSlopedRect(x, y, holeStartX, h, slope * (holeStartX / w));

            // Draw Right Segment
            float holeEndX = holeX + holeWidth;
            float segment2Width = w - holeEndX;
            float seg2StartBottomY = y + (slope * (holeEndX / w));

            drawSlopedRect(x + holeEndX, seg2StartBottomY, segment2Width, h, slope * (segment2Width / w));
        } else {
            drawSlopedRect(x, y, w, h, slope);
        }

        DebugRenderer.end();
        batch.begin();
    }

    /** Helper to draw a tilted rectangle using 4 lines */
    private void drawSlopedRect(float x, float y, float width, float height, float segmentSlope) {
        // Bottom line
        DebugRenderer.renderer.line(x, y, x + width, y + segmentSlope);
        // Top line
        DebugRenderer.renderer.line(x, y + height, x + width, y + height + segmentSlope);
        // Left side
        DebugRenderer.renderer.line(x, y, x, y + height);
        // Right side
        DebugRenderer.renderer.line(x + width, y + segmentSlope, x + width, y + height + segmentSlope);
    }

    public float getHoleX() { return holeX; }
    public float getHoleWidth() { return holeWidth; }
}
