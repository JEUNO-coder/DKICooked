package io.github.DKICooked.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class MagneticStormManager {
    private final ShapeRenderer shapeRenderer;
    private float glitchTimer = 0;

    public MagneticStormManager() {
        // ShapeRenderer is used to draw the vector glitch bars
        this.shapeRenderer = new ShapeRenderer();
    }

    /**
     * Updates any internal storm logic.
     */
    public void update(float delta, Stage stage) {
        glitchTimer += delta;
        // You can add logic here to spawn specific storm-themed actors if needed
    }

    public void drawGlitch(com.badlogic.gdx.graphics.g2d.Batch batch, float alpha) {
        if (alpha <= 0) return;

        // 1. Smart Batch Handling: Only end if it's currently drawing
        boolean wasDrawing = batch.isDrawing();
        if (wasDrawing) batch.end();

        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(batch.getProjectionMatrix());
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (int i = 0; i < 10; i++) {
            if (MathUtils.randomBoolean(0.35f)) {
                float x = MathUtils.random(-50, 800);
                float y = MathUtils.random(0, 600);
                float w = MathUtils.random(50, 500);
                float h = MathUtils.random(1, 4);

                if (MathUtils.randomBoolean(0.7f)) {
                    shapeRenderer.setColor(0.0f, 0.9f, 1.0f, 0.5f * alpha);
                } else {
                    shapeRenderer.setColor(1.0f, 0.0f, 0.3f, 0.4f * alpha);
                }
                shapeRenderer.rect(x, y, w, h);

                if (MathUtils.randomBoolean(0.1f)) {
                    shapeRenderer.rect(x, y, 2, MathUtils.random(10, 40));
                }
            }
        }

        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // 2. Restart the Batch ONLY if we were the ones who stopped it
        if (wasDrawing) batch.begin();
    }

    /**
     * Returns the specific Blue/Cyan color used for the background tint.
     */
    public Color getStormTint(float alpha) {
        // We return a slightly desaturated Cyan to make the Red glitches pop more
        return new Color(0.1f, 0.75f, 0.95f, 1.0f);
    }

    /**
     * Called when the raid ends to clean up any active actors from the stage.
     */
    public void stop(Stage stage) {
        // Logic to remove storm-specific actors if you added any to the stage
    }

    /**
     * IMPORTANT: Call this in GameScreen.dispose() to prevent memory leaks!
     */
    public void dispose() {
        if (shapeRenderer != null) {
            shapeRenderer.dispose();
        }
    }
}
