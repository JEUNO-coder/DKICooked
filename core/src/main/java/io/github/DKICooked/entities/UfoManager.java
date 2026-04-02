package io.github.DKICooked.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class UfoManager {
    private float spawnTimer = 0f;
    private final float SPAWN_INTERVAL = 4.0f;
    private final Animation<TextureRegion> ufoAnim;
    private boolean active = false; // The new flag

    public UfoManager(Animation<TextureRegion> ufoAnim) {
        this.ufoAnim = ufoAnim;
    }

    public void update(float delta, Stage stage) {
        active = true; // If update is being called, the raid is active
        spawnTimer += delta;

        if (spawnTimer >= SPAWN_INTERVAL) {
            int count = MathUtils.random(1, 10);
            float cameraY = stage.getCamera().position.y;
            float screenBottom = cameraY - 300f;

            for (int i = 0; i < count; i++) {
                UfoActor ufo = new UfoActor(ufoAnim);
                boolean spawnLeft = MathUtils.randomBoolean();
                float spawnX = spawnLeft ? -100f : 900f;
                float spawnY = screenBottom + MathUtils.random(50f, 550f);

                ufo.setPosition(spawnX, spawnY);
                stage.addActor(ufo);
            }
            spawnTimer = 0;
        }
    }

    public void stop() {
        active = false;
        spawnTimer = 0;
    }

    public boolean isRaidActive() {
        return active;
    }
}
