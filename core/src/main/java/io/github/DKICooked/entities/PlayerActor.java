package io.github.DKICooked.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.physics.PhysicsBody;
import io.github.DKICooked.render.DebugRenderer;

public class PlayerActor extends Actor {

    private final PhysicsBody body =
        new PhysicsBody(2000f, 300f, 1300f, -1800f);

    private final Rectangle bounds = new Rectangle();
    private Array<Girder> girders = new Array<>();

    // Jump charging
    private float jumpCharge = 0f;
    private final float maxJumpCharge = 900f;
    private final float chargeRate = 1600f;

    private boolean isCharging = false;
    private boolean isGrounded = false;
    private float jumpCooldown = 0f;

    // Stun / knockback
    private float stunTime = 0f;
    private final float stunDuration = 0.25f;
    private final float bounceForce = 1.25f;

    // Fixed timestep
    private float accumulator = 0f;
    private static final float STEP = 1f / 180f;

    @Override
    public void act(float delta) {
        super.act(delta);

        accumulator += delta;
        while (accumulator >= STEP) {
            updatePhysics(STEP);
            accumulator -= STEP;
        }
    }

    private void updatePhysics(float dt) {

        boolean space = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        if (jumpCooldown > 0f) jumpCooldown -= dt;
        if (stunTime > 0f) stunTime -= dt;

        // ================================
        // JUMP CHARGING (Jump King style)
        // ================================
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

        // ================================
        // HORIZONTAL INPUT
        // ================================
        float input = 0f;
        if (!isCharging && stunTime <= 0f) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) input -= 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) input += 1f;
        }

        body.applyHorizontalInput(input, dt);

        // ================================
        // HORIZONTAL MOVE
        // ================================
        moveBy(body.velocityX * dt, 0);
        bounds.set(getX(), getY(), getWidth(), getHeight());

        // Simple wall bounce (screen bounds)
        if (getX() < 0 || getX() + getWidth() > 800) {
            body.velocityX *= -bounceForce;
            stunTime = stunDuration;
        }

        setPosition(
            MathUtils.clamp(getX(), 0, 800 - getWidth()),
            getY()
        );

        // ================================
        // VERTICAL MOVE
        // ================================
        body.applyGravity(dt);
        moveBy(0, body.velocityY * dt);

        bounds.set(getX(), getY(), getWidth(), getHeight());

        // ================================
        // GIRDER GROUNDING
        // ================================
        boolean groundedThisFrame = false;

        for (Girder g : girders) {

            // Ignore if not horizontally overlapping
            if (bounds.x + bounds.width < g.getX()
                || bounds.x > g.getX() + g.getWidth())
                continue;

            float footX = bounds.x + bounds.width * 0.5f;
            float surfaceY = g.getSurfaceY(footX);

            boolean fallingOnto =
                body.velocityY <= 0 &&
                    bounds.y >= surfaceY &&
                    bounds.y - body.velocityY * dt <= surfaceY + g.getHeight();

            if (fallingOnto) {
                setY(surfaceY);
                body.velocityY = 0f;
                groundedThisFrame = true;
                break;
            }
        }

        isGrounded = groundedThisFrame;

        // ================================
        // CLEANUP
        // ================================
        if (Math.abs(body.velocityX) < 0.5f) {
            body.velocityX = 0f;
        }
    }

    // ================================
    // API
    // ================================
    public void setGirders(Array<Girder> girders) {
        this.girders = girders;
    }

    public PhysicsBody getBody() {
        return body;
    }

    public boolean isGrounded() {
        return isGrounded;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();

        DebugRenderer.begin(getStage().getCamera());
        DebugRenderer.renderer.rect(getX(), getY(), getWidth(), getHeight());
        DebugRenderer.end();

        batch.begin();
    }
}
