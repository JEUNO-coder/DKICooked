package io.github.DKICooked.physics;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.DKICooked.entities.Platform;

public class CollisionResolver {

    public enum Result {
        NONE, LANDED_ON_TOP, HIT_SIDE, HIT_BOTTOM
    }

    private static final float EPSILON = 0.5f; // tiny buffer to prevent stutter

    public static Result resolve(Actor actor, PhysicsBody body, Platform platform) {
        float aL = actor.getX();
        float aR = actor.getX() + actor.getWidth();
        float aB = actor.getY();
        float aT = actor.getY() + actor.getHeight();

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
            actor.setY(pT);
            body.velocityY = 0f;
            return Result.LANDED_ON_TOP;
        }

        if (min == oT && body.velocityY > 0f) {
            actor.setY(pB - actor.getHeight());
            body.velocityY = 0f;
            return Result.HIT_BOTTOM;
        }

        if (min == oL) {
            actor.setX(pL - actor.getWidth() - EPSILON);
            body.velocityX = -Math.abs(body.velocityX);
            return Result.HIT_SIDE;
        }

        if (min == oR) {
            actor.setX(pR + EPSILON);
            body.velocityX = Math.abs(body.velocityX);
            return Result.HIT_SIDE;
        }

        return Result.NONE;
    }
}
