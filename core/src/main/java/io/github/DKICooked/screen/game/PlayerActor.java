package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;

public class PlayerActor extends Actor {

    private final ShapeRenderer shapeRenderer = new ShapeRenderer();

    // Movement
    private float velocityX = 0f;
    private final float accel = 2000f;
    private final float maxSpeed = 300f;
    private final float friction = 1800f;

    // Jump physics
    private float velocityY = 0f;
    private final float gravity = -1800;

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
    private final float stunDuration = 0.3f; // 300ms stun
    private final float bounceForce = 0.6f; // Bounce multiplier

    private Platform currentPlatform = null;

    // Collision result enum
    private enum CollisionResult {
        NONE, LANDED_ON_TOP, HIT_SIDE, HIT_BOTTOM
    }

    @Override
    public void act(float delta) {
        super.act(delta);

        boolean spacePressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        // Decrease jump cooldown
        if (jumpCooldown > 0) {
            jumpCooldown -= delta;
        }

        // Decrease stun time
        if (stunTime > 0) {
            stunTime -= delta;
        }

        // ===== CHARGING JUMP =====
        if (isGrounded && spacePressed && jumpCooldown <= 0) {
            if (!isCharging) {
                // Start charging on first frame of grounded + space
                isCharging = true;
                jumpCharge = 0f;
            }
        }

        // Continue charging regardless of grounded state (allows coyote time)
        if (isCharging && spacePressed) {
            jumpCharge += chargeRate * delta;
            if (jumpCharge >= maxJumpCharge) {
                jumpCharge = maxJumpCharge;
            }
        }

        // ===== RELEASE JUMP =====
        if (isCharging && !spacePressed) {
            // Only jump if we have charge
            if (jumpCharge > 0) {
                velocityY = jumpCharge;
                isGrounded = false;
                currentPlatform = null;
                jumpCooldown = 0.15f;
            }
            jumpCharge = 0f;
            isCharging = false;
        }

        float input = 0f;

        // Can't control movement during stun
        if (!isCharging && stunTime <= 0) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                input -= 1f;
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                input += 1f;
            }
        }

        if (input != 0 && stunTime <= 0) {
            velocityX += input * accel * delta;
        } else {
            // Apply friction when no input or stunned
            if (velocityX > 0) {
                velocityX -= friction * delta;
                if (velocityX < 0) velocityX = 0;
            } else if (velocityX < 0) {
                velocityX += friction * delta;
                if (velocityX > 0) velocityX = 0;
            }
        }

        velocityX = MathUtils.clamp(velocityX, -maxSpeed, maxSpeed);

        moveBy(velocityX * delta, 0);

        // ===== APPLY GRAVITY =====
        if (!isGrounded) {
            velocityY += gravity * delta;
            moveBy(0, velocityY * delta);
        }
    }

    public void checkPlatformCollision(Array<Platform> platforms) {
        boolean foundGround = false;

        for (Platform platform : platforms) {
            CollisionResult result = resolveCollision(platform);
            if (result == CollisionResult.LANDED_ON_TOP) {
                foundGround = true;
            }
        }

        if (!foundGround) {
            isGrounded = false;
            currentPlatform = null;
        }
    }

    private CollisionResult resolveCollision(Platform platform) {
        float playerLeft = getX();
        float playerRight = getX() + getWidth();
        float playerTop = getY() + getHeight();
        float playerBottom = getY();

        float platformLeft = platform.getX();
        float platformRight = platform.getX() + platform.getWidth();
        float platformTop = platform.getY() + platform.getHeight();
        float platformBottom = platform.getY();

        boolean overlapping = playerRight > platformLeft &&
            playerLeft < platformRight &&
            playerTop > platformBottom &&
            playerBottom < platformTop;

        if (!overlapping) {
            return CollisionResult.NONE;
        }

        float overlapLeft = playerRight - platformLeft;
        float overlapRight = platformRight - playerLeft;
        float overlapTop = playerTop - platformBottom;
        float overlapBottom = platformTop - playerBottom;

        float minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
            Math.min(overlapTop, overlapBottom));

        // Resolve collision based on smallest overlap
        if (minOverlap == overlapBottom && velocityY <= 0) {
            // Only land if cooldown expired
            if (jumpCooldown <= 0) {
                setY(platformTop);
                velocityY = 0f;
                isGrounded = true;
                currentPlatform = platform;
                return CollisionResult.LANDED_ON_TOP;
            }
        } else if (minOverlap == overlapTop && velocityY > 0) {
            // Hit from bottom (head bonk)
            setY(platformBottom - getHeight());
            velocityY = 0f;
            return CollisionResult.HIT_BOTTOM;
        } else if (minOverlap == overlapLeft) {
            // Hit from right side - BOUNCE LEFT
            setX(platformLeft - getWidth());
            velocityX = -Math.abs(velocityX) * bounceForce; // Reverse and reduce
            stunTime = stunDuration;
            return CollisionResult.HIT_SIDE;
        } else if (minOverlap == overlapRight) {
            // Hit from left side - BOUNCE RIGHT
            setX(platformRight);
            velocityX = Math.abs(velocityX) * bounceForce; // Reverse and reduce
            stunTime = stunDuration;
            return CollisionResult.HIT_SIDE;
        }

        return CollisionResult.NONE;
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
