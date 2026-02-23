package io.github.DKICooked.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.gameLogic.physics.PhysicsBody;

public class PlayerActor extends Actor {

    // Physics Engine constants
    private final PhysicsBody body = new PhysicsBody(2000f, 300f, 1300f, -1800f);
    private Array<Platform> platforms = new Array<>();

    // Jump charging state
    private float jumpCharge = 0f;
    private final float maxJumpCharge = 900f;
    private final float chargeRate = 1600f;

    private boolean isCharging = false;
    private boolean isGrounded = false;
    private float jumpCooldown = 0f;
    public boolean facingRight = true;

    // Stun / knockback logic
    private float stunTime = 0f;
    private final float stunDuration = 0.25f;
    private final float bounceForce = 1.25f;

    // Fixed timestep accumulator
    private float accumulator = 0f;
    private static final float STEP = 1f / 180f;

    @Override
    public void act(float delta) {
        super.act(delta);

        // Fixed timestep loop to ensure consistent physics regardless of FPS
        accumulator += delta;
        while (accumulator >= STEP) {
            updatePhysics(STEP);
            accumulator -= STEP;
        }
    }
    public boolean facingRight = true;

    private void updatePhysics(float dt) {
        // 1. CAPTURE PREVIOUS STATE
        float oldX = getX();
        float oldY = getY();
        float oldHeadY = oldY + getHeight();

        // 2. TIMERS & INPUT GATHERING
        boolean space = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        if (jumpCooldown > 0f) jumpCooldown -= dt;
        if (stunTime > 0f) stunTime -= dt;

        // 3. JUMP CHARGING & RELEASE
        if (isGrounded && space && jumpCooldown <= 0f && !isCharging) {
            isCharging = true;
            jumpCharge = 0f;
        }
        if (isCharging && space) {
            jumpCharge = Math.min(maxJumpCharge, jumpCharge + (chargeRate * dt));
        }
        if (isCharging && !space) {
            body.velocityY = jumpCharge;
            isCharging = false;
            isGrounded = false;
            jumpCooldown = 0.15f;
        }

        // 4. PHASE 1: HORIZONTAL MOVEMENT & WALLS
        float input = 0f;
        // Only allow movement if not charging a jump and not stunned from a wall bonk
        if (!isCharging && stunTime <= 0f) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) input -= 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) input += 1f;
        }
        body.applyHorizontalInput(input, dt);

        // Apply X movement
        moveBy(body.velocityX * dt, 0);

        // Screen Edge Bounce
        if (getX() < 0 || getX() + getWidth() > 800) {
            body.velocityX *= -1.25f;
            stunTime = 0.25f;
            setX(MathUtils.clamp(getX(), 0, 800 - getWidth()));
        }

        // IRONCLAD WALL CHECK
        for (Platform p : platforms) {
            float topOfSlab = Math.max(p.y1, p.y2);
            float bottomOfSlab = Math.min(p.y1, p.y2) - p.thickness;

            // Vertical Overlap: Is the player's body level with the side of this platform?
            if (getY() < topOfSlab - 2f && (getY() + getHeight()) > bottomOfSlab + 2f) {
                // Hit Left Wall
                if (body.velocityX > 0 && (oldX + getWidth()) <= p.x1 && (getX() + getWidth()) >= p.x1) {
                    setX(p.x1 - getWidth());
                    body.velocityX *= -1.3f;
                    stunTime = 0.3f;
                }
                // Hit Right Wall
                else if (body.velocityX < 0 && oldX >= p.x2 && getX() <= p.x2) {
                    setX(p.x2);
                    body.velocityX *= -1.3f;
                    stunTime = 0.3f;
                }
            }
        }

        // 5. PHASE 2: VERTICAL MOVEMENT & SURFACES
        if (!isGrounded) body.applyGravity(dt);
        moveBy(0, body.velocityY * dt);

        boolean groundedThisFrame = false;
        float footY = getY();
        float headY = footY + getHeight();

// Check three points along the width of the player
        float[] checkPointsX = {
            getX() + 2f,               // Left edge (with tiny 2px buffer)
            getX() + getWidth() / 2f,  // Center
            getX() + getWidth() - 2f   // Right edge
        };

        for (Platform p : platforms) {
            // Check all 3 points for this platform
            for (float x : checkPointsX) {
                float surfaceY = p.getSurfaceY(x);

                if (surfaceY != -1) {
                    // LANDING (Check if any point crosses the surface)
                    if (body.velocityY <= 0 && oldY >= surfaceY - 5f && footY <= surfaceY + 2f) {
                        setY(surfaceY);
                        body.velocityY = 0;
                        groundedThisFrame = true;
                        break; // Stop checking points for this platform
                    }

                    // BONKING (Check if head crosses the bottom)
                    float bottomY = surfaceY - p.thickness;
                    if (body.velocityY > 0 && oldHeadY <= bottomY + 5f && headY >= bottomY) {
                        setY(bottomY - getHeight() - 1f);
                        body.velocityY = -200f;
                        break;
                    }
                }
            }
            if (groundedThisFrame) break; // Stop checking other platforms
        }
        isGrounded = groundedThisFrame;

        // 6. UPDATE VISUAL STATE
        if (body.velocityX > 0.1f) facingRight = true;
        else if (body.velocityX < -0.1f) facingRight = false;
    }

    // API for GameScreen
    public void setPlatforms(Array<Platform> platforms) {
        this.platforms = platforms;
    }

    public boolean isGrounded() { return isGrounded; }
    public boolean isCharging() { return isCharging; }
    public float getJumpCharge() { return jumpCharge; }
    public PhysicsBody getBody() { return body; }
}
