package io.github.DKICooked.entities;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.graphics.g2d.Batch;

public class Platform extends Actor {
    private ShapeRenderer shapeRenderer = new ShapeRenderer();
    private Color color;

    public Platform(float x, float y, float width, float height) {
        setPosition(x, y);
        setSize(width, height);
        this.color = new Color(1f, 1f, 1f, 1f);
    }

    public Platform(float x, float y, float width, float height, Color color) {
        setPosition(x, y);
        setSize(width, height);
        this.color = color;
    }

    @Override
    public void draw(Batch batch, float parentAlpha) {
        batch.end();

        shapeRenderer.setProjectionMatrix(getStage().getCamera().combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        shapeRenderer.setColor(color);
        shapeRenderer.rect(getX(), getY(), getWidth(), getHeight());
        shapeRenderer.end();

        batch.begin();
    }

    public boolean isPlayerOn(PlayerActor player) {
        return player.getX() + player.getWidth() > getX() &&
            player.getX() < getX() + getWidth() &&
            player.getY() >= getY() + getHeight() - 5 &&
            player.getY() <= getY() + getHeight() + 5;
    }

    @Override
    public void act(float delta) {
        super.act(delta);
    }
}
