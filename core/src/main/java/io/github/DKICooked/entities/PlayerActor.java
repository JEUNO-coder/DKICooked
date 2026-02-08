package io.github.DKICooked.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.physics.CollisionResolver;
import io.github.DKICooked.physics.PhysicsBody;
import io.github.DKICooked.render.DebugRenderer;
import com.badlogic.gdx.math.Rectangle;

public class PlayerActor extends Actor {
    private float groundTimer = 0f;
    private final PhysicsBody body = new PhysicsBody(2000f, 300f, 1300f, -1800f);
    private final Rectangle bounds = new Rectangle();
    private Array<Platform> platforms = new Array<>();

    private float jumpCharge;
    private final float maxJumpCharge;
    private final float chargeRate;

    private boolean isCharging;
    private boolean isGrounded;
    private float jumpCooldown;

    private float stunTime;
    private final float stunDuration;
    private final float bounceForce;

    private float physicsAccumulator;

    private static final float PHYSICS_STEP = 1/180f;

    {
        // Jump charge
        maxJumpCharge = 900f;
        jumpCharge = 0f;
        chargeRate = 1600f;

        // State
        isCharging = false;
        isGrounded = false;
        jumpCooldown = 0f;

        // Side collision stun
        stunTime = 0f;
        stunDuration = 0.3f;
        bounceForce = 1.3f;

        // Fixed timestep
        physicsAccumulator = 0f;
    }

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
            isGrounded = false;
            isCharging = false;
            jumpCooldown = 0.15f;
        }

        // ===== INPUT =====
        float input = 0f;
        if (!isCharging && stunTime <= 0f) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) input -= 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) input += 1f;
        }

        body.applyHorizontalInput(input, dt);

        // ==================================================
        // HORIZONTAL MOVE FIRST
        // ==================================================
        moveBy(body.velocityX * dt, 0);
        bounds.set(getX(), getY(), getWidth(), getHeight());

        // Resolve SIDE collisions only
        for (Platform p : platforms) {
            CollisionResolver.Result r =
                CollisionResolver.resolve(bounds, body, p);

            if (r == CollisionResolver.Result.HIT_SIDE) {
                body.velocityX *= bounceForce;
                stunTime = stunDuration;
            }
        }

        // Sync actor position with bounds after collision
        setPosition(bounds.x, bounds.y);

        // ==================================================
        // VERTICAL MOVE SECOND
        // ==================================================
        if (!isGrounded) {
            body.applyGravity(dt);
            moveBy(0, body.velocityY * dt);
        }

        bounds.set(getX(), getY(), getWidth(), getHeight());

        // Resolve vertical collisions (ground / ceiling)
        boolean foundGround = false;
        for (Platform p : platforms) {
            CollisionResolver.Result r =
                CollisionResolver.resolve(bounds, body, p);

            if (r == CollisionResolver.Result.LANDED_ON_TOP) {
                foundGround = true;
            }
        }

        // Sync actor position with bounds after collision
        setPosition(bounds.x, bounds.y);

        isGrounded = foundGround;

        if (isGrounded) {
           // groundTimer = 0.1f;
            body.velocityY = 0f;
        }
//        } else {
//            groundTimer -= dt;
//            if (groundTimer > 0) isGrounded = true;
//        }

        // Small horizontal snap-to-zero
        if (Math.abs(body.velocityX) < 0.5f) {
            body.velocityX = 0f;
        }
    }

    public void setPlatforms(Array<Platform> platforms) {
        this.platforms = platforms;
    }

    public PhysicsBody getBody() {
        return body;
    }
    public boolean isGrounded() {
        // System.out.println("Grounded: " + isGrounded + " VelocityX: " + body.velocityX);
        return isGrounded; }
    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();

        DebugRenderer.begin(getStage().getCamera());
        DebugRenderer.renderer.rect(getX(), getY(), getWidth(), getHeight());
        DebugRenderer.end();

        batch.begin();
    }
}
