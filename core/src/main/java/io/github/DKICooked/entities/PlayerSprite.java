package io.github.DKICooked.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class PlayerSprite {
    private final TextureRegion jumpRegion;
    private final TextureRegion idleRegion;
    private Texture idleText;
    private Animation<TextureRegion> walkAnim;
    private Texture jumpText;

    private float stateTime;

    public PlayerSprite(PlayerActor player) {
            idleText = new Texture(Gdx.files.internal("StandR.png"));
            idleRegion = new TextureRegion(idleText);

            Texture walk1 = new Texture(Gdx.files.internal("Walk1R.png"));
            Texture walk2 = new Texture(Gdx.files.internal("Walk2R.png"));
            Texture walk3 = new Texture(Gdx.files.internal("Walk3R.png"));

            walkAnim = new Animation<>(0.1f,
                new TextureRegion(walk1),
                new TextureRegion(walk2),
                new TextureRegion(walk3)
                );

            jumpText = new Texture(Gdx.files.internal("JumpR.png"));
            jumpRegion = new TextureRegion(jumpText);

            stateTime = 0f;
    }

    public void draw(Batch batch, PlayerActor player) {
        stateTime += Gdx.graphics.getDeltaTime();

        TextureRegion frame;
        if (!player.isGrounded() || Math.abs(player.getBody().velocityY) > 0.1f) {
            frame = jumpRegion;
        } else if (Math.abs(player.getBody().velocityX) > 0.5f) {
            frame = walkAnim.getKeyFrame(stateTime, true);
        } else {
            stateTime = 0f;
            frame = idleRegion;
        }

        if (player.isFacingRight() && frame.isFlipX()) {
            frame.flip(true, false);
        } else if (!player.isFacingRight() && !frame.isFlipX()) {
            frame.flip(true, false);
        }

        batch.draw(frame, player.getX(), player.getY(), player.getWidth(), player.getHeight());
    }

}
