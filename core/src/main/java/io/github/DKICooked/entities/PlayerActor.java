package io.github.DKICooked.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.physics.CollisionResolver;
import io.github.DKICooked.physics.PhysicsBody;

public class PlayerActor extends Actor {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();
    private final PhysicsBody body = new PhysicsBody(2000f, 300f, 1800f, -1800f);

    // Jump charge
    private float jumpCharge = 0f;
    private final float maxJumpCharge = 900f;
    private final float chargeRate = 1600f;

    // State
    private boolean isCharging = false;
    private boolean isGrounded = false;
    private float jumpCooldown = 0f;

    // Side collision stun
    private float stunTime = 0f;
    private final float stunDuration = 0.3f;
    private final float bounceForce = 0.6f;

    // Fixed timestep
    private static final float PHYSICS_STEP = 1/180f;
    private float physicsAccumulator = 0f;

    @Override
    public void act(float delta) {
        super.act(delta);

        physicsAccumulator += delta;
        while (physicsAccumulator >= PHYSICS_STEP) {
            updatePhysics(PHYSICS_STEP);
            physicsAccumulator -= PHYSICS_STEP;
        }
    }

    private void updatePhysics(float dt) {
        boolean space = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        if (jumpCooldown > 0f) jumpCooldown -= dt;
        if (stunTime > 0f) stunTime -= dt;

        // ===== JUMP CHARGE =====
        if (isGrounded && space && jumpCooldown <= 0f && !isCharging) {
            isCharging = true;
            jumpCharge = 0f;
        }

        if (isCharging && space) {
            jumpCharge = Math.min(maxJumpCharge, jumpCharge + chargeRate * dt);
        }

        if (isCharging && !space) {
            body.velocityY = jumpCharge;
            isCharging = false;
            isGrounded = false;
            jumpCooldown = 0.15f;
        }

        // ===== HORIZONTAL INPUT =====
        float input = 0f;
        if (!isCharging && stunTime <= 0f) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) input -= 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) input += 1f;
        }

        body.applyHorizontalInput(input, dt);

        if (!isGrounded) body.applyGravity(dt);

        body.move(this, dt);

        // ===== EPSILON STOP =====
        if (Math.abs(body.velocityX) < 0.5f) body.velocityX = 0f;
        if (Math.abs(body.velocityY) < 0.5f) body.velocityY = 0f;
    }

    public void checkPlatformCollision(Array<Platform> platforms) {
        isGrounded = false;

        for (Platform platform : platforms) {
            CollisionResolver.Result r = CollisionResolver.resolve(this, body, platform);

            if (r == CollisionResolver.Result.LANDED_ON_TOP) {
                isGrounded = true;
            }

            if (r == CollisionResolver.Result.HIT_SIDE) {
                body.velocityX *= bounceForce;
                stunTime = stunDuration;
            }
        }
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();
        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(Color.RED);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();
        batch.begin();
    }
}
