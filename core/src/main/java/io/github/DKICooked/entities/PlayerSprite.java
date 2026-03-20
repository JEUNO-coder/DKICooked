package io.github.DKICooked.entities;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class PlayerSprite {
    private final Array<Texture> textures = new Array<>();
    private float stateTime;

    private final TextureRegion idleRegion;
    private final TextureRegion jumpRegion;
    private final TextureRegion chargeReg;
    private final TextureRegion deathRegion;
    private final Animation<TextureRegion> walkAnim;

    public PlayerSprite(String selection) {
        // 1. Get the first letter: "Alaine" -> "a", "Jerick" -> "j", "Timothy" -> "t"
        String p = selection.toLowerCase().substring(0, 1);

        // 2. Load the specific textures for that character
        idleRegion = new TextureRegion(loadTexture(p + "idle.png"));
        jumpRegion = new TextureRegion(loadTexture(p + "J.png"));
        chargeReg = new TextureRegion(loadTexture(p + "LC.png"));
        deathRegion = new TextureRegion(loadTexture(p + "dead.png"));

        walkAnim = new Animation<>(0.1f,
            new TextureRegion(loadTexture(p + "W1.png")),
            new TextureRegion(loadTexture(p + "W2.png")),
            new TextureRegion(loadTexture(p + "W3.png"))
        );

        stateTime = 0f;
    }

    private Texture loadTexture(String path) {
        Texture t = new Texture(Gdx.files.internal(path));
        textures.add(t);
        return t;
    }

    public void draw(Batch batch, PlayerActor player) {
        stateTime += Gdx.graphics.getDeltaTime();
        TextureRegion frame;

        // ... [Your Priority Logic stays the same] ...
        if (player.isDead()) {
            frame = deathRegion;
        } else if (player.isCharging()) {
            stateTime = 0f;
            frame = chargeReg;
        } else if (!player.isGrounded() || Math.abs(player.getBody().velocityY) > 0.1f) {
            frame = jumpRegion;
        } else if (Math.abs(player.getBody().velocityX) > 0.5f) {
            frame = walkAnim.getKeyFrame(stateTime, true);
        } else {
            stateTime = 0f;
            frame = idleRegion;
        }

        // --- NEW FLIP LOGIC FOR LEFT-FACING ART ---

        // 1. Force the frame to its "Natural" state (Facing Left)
        if (frame.isFlipX()) {
            frame.flip(true, false);
        }

        // 2. If the player is moving/facing RIGHT, we must flip the Left-facing art
        if (player.isFacingRight()) {
            frame.flip(true, false);
        }

        batch.draw(
            frame,
            player.getX(), player.getY(),
            player.getOriginX(), player.getOriginY(),
            player.getWidth(), player.getHeight(),
            player.getScaleX(), player.getScaleY(),
            player.getRotation()
        );
    }

    public void dispose() {
        for (Texture t : textures) {
            t.dispose();
        }
        textures.clear();
    }
}
