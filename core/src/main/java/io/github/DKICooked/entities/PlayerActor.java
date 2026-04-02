package io.github.DKICooked.entities;

import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.audio.SoundPlayer;
import io.github.DKICooked.gameLogic.physics.PhysicsBody;
import io.github.DKICooked.gameLogic.physics.PlayerPhysicsProcessor;

public class PlayerActor extends Actor {
    private final PhysicsBody body = new PhysicsBody(2000f, 300f, 1300f, -1800f);
    private final PlayerPhysicsProcessor physicsProcessor;
    private Array<Platform> platforms = new Array<>();
    private float accumulator = 0f;
    private static final float STEP = 1f / 180f;
    private boolean dead = false;
    private Rectangle collisionRect = new Rectangle();
    private float currentMoveDirection = 0;

    //for stats
    private float jumpForce;      // How high they launch
    private float gravityScale;   // How heavy they feel
    private int maxJumps;         // Total jumps allowed (1 for most, 2 for Jerick)
    private int remainingJumps;   // Current jumps available


    public PlayerActor(SoundPlayer soundPlayer) {
        this.physicsProcessor = new PlayerPhysicsProcessor(this, body, soundPlayer);
    }

    public void handleHorizontalMovement(float direction, float delta) {
        this.currentMoveDirection = direction;
    }

    public void initStats(String characterName) {
        float baseGravity = -1800f;
        float baseSpeed = 300f;

        switch (characterName) {
            case "Timothy":
                this.jumpForce = 1000f;
                this.gravityScale = 1.0f;
                this.maxJumps = 1;
                break;

            case "Alaine":
                this.jumpForce = 600f;
                this.gravityScale = 0.5f;
                this.maxJumps = 1;
                break;

            case "Jerick":
                this.jumpForce = 800f;
                this.gravityScale = 1.0f;
                this.maxJumps = 2;
                break;

            default:
                this.jumpForce = 600f;
                this.gravityScale = 1.0f;
                this.maxJumps = 1;
                break;
        }

        this.remainingJumps = this.maxJumps;

        body.setGravity(baseGravity * gravityScale);
        body.setMaxSpeed(baseSpeed);
    }

    public void resetJumps() {
        this.remainingJumps = this.maxJumps;
    }

    public void useJump() {
        this.remainingJumps--;
    }

    public int getRemainingJumps() {
        return this.remainingJumps;
    }

    public float getJumpForce() {
        return this.jumpForce;
    }

    public int getMaxJumps() {
        // If it's Timothy, allow a higher max charge
        return this.maxJumps;
    }

    public Rectangle getCollisionRect() {
        collisionRect.set(getX() + 5, getY() + 5, getWidth() - 10, getHeight() - 10);
        return collisionRect;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        accumulator += delta;
        while (accumulator >= STEP) {
            physicsProcessor.update(STEP, platforms, currentMoveDirection);
            accumulator -= STEP;
        }
    }

    // Delegate getters to the processor
    public void setDead(boolean dead) { this.dead = dead; }
    public boolean isDead() { return dead; }
    public boolean isGrounded() { return physicsProcessor.isGrounded; }
    public boolean isCharging() { return physicsProcessor.isCharging; }
    public float getJumpCharge() { return physicsProcessor.jumpCharge; }
    public boolean isFacingRight() { return physicsProcessor.facingRight; }
    public PhysicsBody getBody() { return body; }

    public void setPlatforms(Array<Platform> platforms) {
        this.platforms = platforms;
    }
}
