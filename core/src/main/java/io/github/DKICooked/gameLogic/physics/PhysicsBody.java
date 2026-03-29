package io.github.DKICooked.gameLogic.physics;

import com.badlogic.gdx.math.MathUtils;

public class PhysicsBody {

    public float velocityX = 0f;
    public float velocityY = 0f;

    private float accel;
    private float maxSpeed;
    private float friction;
    private float gravity;

    public PhysicsBody(float accel, float maxSpeed, float friction, float gravity) {
        this.accel = accel;
        this.maxSpeed = maxSpeed;
        this.friction = friction;
        this.gravity = gravity;
    }

    public void setGravity(float gravity) { this.gravity = gravity; }
    public void setMaxSpeed(float maxSpeed) { this.maxSpeed = maxSpeed; }
    public float getGravity() { return gravity; }

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
}
