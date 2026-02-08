package io.github.DKICooked.physics;

import com.badlogic.gdx.math.Rectangle;
import io.github.DKICooked.entities.Platform;

public class CollisionResolver {

    public enum Result {
        NONE, LANDED_ON_TOP, HIT_SIDE, HIT_BOTTOM
    }

    private static final float EPSILON = 0.5f;

    public static Result resolve(Rectangle actor, PhysicsBody body, Platform platform) {
        float aL = actor.x;
        float aR = actor.x + actor.width;
        float aB = actor.y;
        float aT = actor.y + actor.height;

        float pL = platform.getX();
        float pR = platform.getX() + platform.getWidth();
        float pB = platform.getY();
        float pT = platform.getY() + platform.getHeight();

        boolean overlap = aR > pL && aL < pR && aT > pB && aB < pT;
        if (!overlap) return Result.NONE;

        float oL = aR - pL;
        float oR = pR - aL;
        float oT = aT - pB;
        float oB = pT - aB;

        float min = Math.min(Math.min(oL, oR), Math.min(oT, oB));

        if (min == oB && body.velocityY <= 0f) {
            actor.y = pT; // FIXED: Use .y not .setY()
            body.velocityY = 0f;
            return Result.LANDED_ON_TOP;
        }


        if (min == oT && body.velocityY > 0f) {
            actor.y = pB - actor.height; // FIXED: Use .y and .height
            body.velocityY = 0f;
            return Result.HIT_BOTTOM;
        }

        if (min == oL) {
            actor.x = pL - actor.width - EPSILON; // FIXED: Use .x and .width
            body.velocityX = -Math.abs(body.velocityX);
            return Result.HIT_SIDE;
        }

        if (min == oR) {
            actor.x = pR + EPSILON; // FIXED: Use .x
            body.velocityX = Math.abs(body.velocityX);
            return Result.HIT_SIDE;
        }

        return Result.NONE;
    }
}
