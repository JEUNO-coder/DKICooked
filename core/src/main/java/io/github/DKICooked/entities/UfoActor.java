package io.github.DKICooked.entities;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class UfoActor extends Actor {
    private final Animation<TextureRegion> animation;
    private float stateTime = 0;
    private float speed;
    private boolean movingRight;
    private boolean directionSet = false;

    public UfoActor(Animation<TextureRegion> anim) {
        this.animation = anim;

        // Random speed for variety
        this.speed = MathUtils.random(300f, 500f);

        // Set size based on the first frame of animation
        TextureRegion firstFrame = anim.getKeyFrame(0);
        setSize(firstFrame.getRegionWidth(), firstFrame.getRegionHeight());
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        stateTime += delta;

        // 1. LOCK DIRECTION: Only check the spawn point once
        if (!directionSet) {
            movingRight = getX() < 400; // If spawned left of center, move right
            directionSet = true;
        }

        // 2. MOVE: Apply velocity based on the locked direction
        if (movingRight) {
            setX(getX() + speed * delta);
        } else {
            setX(getX() - speed * delta);
        }

        // 3. CLEANUP: Remove if it leaves the screen area
        if (getX() > 1000 || getX() < -200) {
            this.remove();
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        TextureRegion currentFrame = animation.getKeyFrame(stateTime, true);

        // If movingRight is true, we draw normally (width).
        // If movingRight is false (moving left), we draw with negative width (-width) to flip it.

        if (movingRight) {
            // Draw normal
            batch.draw(currentFrame, getX(), getY(), getWidth(), getHeight());
        } else {
            // Draw flipped: We offset the X by the width and send a negative width
            batch.draw(currentFrame, getX() + getWidth(), getY(), -getWidth(), getHeight());
        }
    }

    public Circle getCollisionCircle() {
        // Returns a circle centered on the UFO for hit detection
        return new Circle(getX() + getWidth() / 2f,
            getY() + getHeight() / 2f,
            getHeight() * 0.4f);
    }
}
