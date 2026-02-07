package io.github.DKICooked.physics;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class PhysicsBody {

    public float velocityX = 0f;
    public float velocityY = 0f;

    private final float accel;
    private final float maxSpeed;
    private final float friction;
    private final float gravity;

    public PhysicsBody(float accel, float maxSpeed, float friction, float gravity) {
        this.accel = accel;
        this.maxSpeed = maxSpeed;
        this.friction = friction;
        this.gravity = gravity;
    }

    public void applyHorizontalInput(float input, float delta) {
        if (input != 0) {
            velocityX += input * accel * delta;
        } else {
            applyFriction(delta);
        }

        velocityX = MathUtils.clamp(velocityX, -maxSpeed, maxSpeed);
    }

    private void applyFriction(float delta) {
        if (velocityX > 0) {
            velocityX -= friction * delta;
            if (velocityX < 0) velocityX = 0;
        } else if (velocityX < 0) {
            velocityX += friction * delta;
            if (velocityX > 0) velocityX = 0;
        }
    }

    public void applyGravity(float delta) {
        velocityY += gravity * delta;
    }

    public void move(Actor actor, float delta) {
        actor.moveBy(velocityX * delta, velocityY * delta);
    }
}
