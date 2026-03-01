package io.github.DKICooked.gameLogic.physics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.entities.Platform;
import io.github.DKICooked.entities.PlayerActor;
import io.github.DKICooked.audio.SoundPlayer;

public class PlayerPhysicsProcessor {
    private final PlayerActor player;
    private final PhysicsBody body;
    private final SoundPlayer soundPlayer;

    public float jumpCharge = 0f;
    public final float maxJumpCharge = 900f;
    public final float chargeRate = 1600f;
    public boolean isCharging = false;
    public boolean isGrounded = false;
    public float jumpCooldown = 0f;
    public float stunTime = 0f;
    public boolean facingRight = true;

    public PlayerPhysicsProcessor(PlayerActor player, PhysicsBody body, SoundPlayer soundPlayer) {
        this.player = player;
        this.body = body;
        this.soundPlayer = soundPlayer;
    }

    public void update(float dt, Array<Platform> platforms) {
        float oldX = player.getX();
        float oldY = player.getY();
        float oldHeadY = oldY + player.getHeight();

        boolean space = Gdx.input.isKeyPressed(Input.Keys.SPACE);
        if (jumpCooldown > 0f) jumpCooldown -= dt;
        if (stunTime > 0f) stunTime -= dt;

        handleJump(dt, space);

        handleHorizontal(dt, oldX, platforms);

        handleVertical(dt, oldY, oldHeadY, platforms);

        if (body.velocityX > 0.1f) facingRight = true;
        else if (body.velocityX < -0.1f) facingRight = false;
    }

    private void handleJump(float dt, boolean space) {
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
            soundPlayer.playJump();
        }
    }

    private void handleHorizontal(float dt, float oldX, Array<Platform> platforms) {
        float input = 0f;
        if (!isCharging && stunTime <= 0f) {
            if (Gdx.input.isKeyPressed(Input.Keys.A)) input -= 1f;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) input += 1f;
        }
        body.applyHorizontalInput(input, dt);
        player.moveBy(body.velocityX * dt, 0);

// Screen Bounce
        if (player.getX() < 0 || player.getX() + player.getWidth() > 800) {
            body.velocityX *= -1.25f;
            stunTime = 0.25f;
            player.setX(MathUtils.clamp(player.getX(), 0, 800 - player.getWidth()));
        }

// Wall Collision
        for (Platform p : platforms) {
            float topOfSlab = Math.max(p.y1, p.y2);
            float bottomOfSlab = Math.min(p.y1, p.y2) - p.thickness;
            if (player.getY() < topOfSlab - 2f && (player.getY() + player.getHeight()) > bottomOfSlab + 2f) {
                if (body.velocityX > 0 && (oldX + player.getWidth()) <= p.x1 && (player.getX() + player.getWidth()) >= p.x1) {
                    player.setX(p.x1 - player.getWidth());
                    body.velocityX *= -1.3f;
                    stunTime = 0.3f;
                } else if (body.velocityX < -0.1f && oldX >= p.x2 && player.getX() <= p.x2) {
                    player.setX(p.x2);
                    body.velocityX *= -1.3f;
                    stunTime = 0.3f;
                }
            }
        }
    }

    private void handleVertical(float dt, float oldY, float oldHeadY, Array<Platform> platforms) {
        if (!isGrounded) body.applyGravity(dt);
        player.moveBy(0, body.velocityY * dt);

        boolean groundedThisFrame = false;
        float footY = player.getY();
        float headY = footY + player.getHeight();

        float[] checkPointsX = { player.getX() + 2f, player.getX() + player.getWidth() / 2f, player.getX() + player.getWidth() - 2f };

        for (Platform p : platforms) {
            for (float x : checkPointsX) {
                float surfaceY = p.getSurfaceY(x);
                if (surfaceY != -1) {
                    if (body.velocityY <= 0 && oldY >= surfaceY - 5f && footY <= surfaceY + 2f) {
                        player.setY(surfaceY);
                        body.velocityY = 0;
                        groundedThisFrame = true;
                        break;
                    }
                    float bottomY = surfaceY - p.thickness;
                    if (body.velocityY > 0 && oldHeadY <= bottomY + 5f && headY >= bottomY) {
                        player.setY(bottomY - player.getHeight() - 1f);
                        body.velocityY = -200f;
                        break;
                    }
                }
            }
            if (groundedThisFrame) break;
        }
        isGrounded = groundedThisFrame;
    }
}
