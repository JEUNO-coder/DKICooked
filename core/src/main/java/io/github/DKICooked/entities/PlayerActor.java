package io.github.DKICooked.entities;

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

    public PlayerActor(SoundPlayer soundPlayer) {
        this.physicsProcessor = new PlayerPhysicsProcessor(this, body, soundPlayer);
    }

    @Override
    public void act(float delta) {
        super.act(delta);
        accumulator += delta;
        while (accumulator >= STEP) {
            physicsProcessor.update(STEP, platforms);
            accumulator -= STEP;
        }
    }

    // Delegate getters to the processor
    public boolean isGrounded() { return physicsProcessor.isGrounded; }
    public boolean isCharging() { return physicsProcessor.isCharging; }
    public float getJumpCharge() { return physicsProcessor.jumpCharge; }
    public boolean isFacingRight() { return physicsProcessor.facingRight; }
    public PhysicsBody getBody() { return body; }

    public void setPlatforms(Array<Platform> platforms) {
        this.platforms = platforms;
    }
}
