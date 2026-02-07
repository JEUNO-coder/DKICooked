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
    private final PhysicsBody body =
        new PhysicsBody(2000f, 300f, 1800f, -1800f);

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

    @Override
    public void act(float delta) {
        super.act(delta);

        boolean space = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        if (jumpCooldown > 0) jumpCooldown -= delta;
        if (stunTime > 0) stunTime -= delta;

        // ===== JUMP CHARGE =====
        if (isGrounded && space && jumpCooldown <= 0 && !isCharging) {
            isCharging = true;
            jumpCharge = 0;
        }

        if (isCharging && space) {
            jumpCharge = Math.min(maxJumpCharge, jumpCharge + chargeRate * delta);
        }

        if (isCharging && !space) {
            body.velocityY = jumpCharge;
            isCharging = false;
            isGrounded = false;
            jumpCooldown = 0.15f;
        }

        // ===== HORIZONTAL INPUT =====
        float input = 0f;
        if (!isCharging && stunTime <= 0) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) input -= 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) input += 1f;
        }

        body.applyHorizontalInput(input, delta);

        if (!isGrounded) body.applyGravity(delta);

        body.move(this, delta);
    }

    public void checkPlatformCollision(Array<Platform> platforms) {
        isGrounded = false;

        for (Platform platform : platforms) {
            CollisionResolver.Result r =
                CollisionResolver.resolve(this, body, platform);

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
