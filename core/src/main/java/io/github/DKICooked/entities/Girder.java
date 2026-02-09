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
        return getY() + slope * t;
    }

    public boolean hasHole() {
        return holeX >= 0;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();

        DebugRenderer.begin(getStage().getCamera());
        DebugRenderer.renderer.setColor(GIRDER_RED);

        if (hasHole()) {
            // Left segment
            DebugRenderer.renderer.rect(
                getX(),
                getY(),
                holeX,
                getHeight()
            );

            // Right segment
            DebugRenderer.renderer.rect(
                getX() + holeX + holeWidth,
                getY(),
                getWidth() - (holeX + holeWidth),
                getHeight()
            );
        } else {
            DebugRenderer.renderer.rect(
                getX(),
                getY(),
                getWidth(),
                getHeight()
            );
        }

        DebugRenderer.end();
        batch.begin();
    }
}
