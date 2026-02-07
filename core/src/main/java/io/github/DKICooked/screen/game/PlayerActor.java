package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;

public class PlayerActor extends Actor {

    private ShapeRenderer shapeRenderer = new ShapeRenderer();

    // Movement
    private final float moveSpeed = 250f;

    // Jump physics
    private float velocityY = 0f;
    private final float gravity = -1400f;

    // Jump charge
    private float jumpCharge = 0f;
    private final float maxJumpCharge = 900f;
    private final float chargeRate = 1200f;

    // State
    private boolean isCharging = false;
    private boolean isGrounded = true;
    private boolean wasSpacePressed = false; // Track previous frame state

    private final float groundY = 100f; // temporary ground

    @Override
    public void act(float delta) {
        super.act(delta);

        boolean spacePressed = Gdx.input.isKeyPressed(Input.Keys.SPACE);

        // ===== CHARGING JUMP =====
        if (isGrounded && spacePressed) {
            isCharging = true;
            jumpCharge += chargeRate * delta;
            jumpCharge = Math.min(jumpCharge, maxJumpCharge);
        }

        // ===== RELEASE JUMP =====
        if (isGrounded && isCharging && wasSpacePressed && !spacePressed) {
            velocityY = jumpCharge;
            jumpCharge = 0f;
            isCharging = false;
            isGrounded = false;
        }

        // ===== HORIZONTAL MOVEMENT (GROUND + AIR) =====
        if (!isCharging) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) {
                moveBy(-moveSpeed * delta, 0);
            }
            if (Gdx.input.isKeyPressed(Input.Keys.D)) {
                moveBy(moveSpeed * delta, 0);
            }
        }

        // ===== APPLY GRAVITY =====
        if (!isGrounded) {
            velocityY += gravity * delta;
            moveBy(0, velocityY * delta);
        }

        // ===== GROUND COLLISION =====
        if (getY() < groundY) {
            setY(groundY);
            velocityY = 0f;
            isGrounded = true;
            isCharging = false;
            jumpCharge = 0f;
        }

        // Update previous frame state
        wasSpacePressed = spacePressed;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();

        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();

        batch.begin();
    }
}
