package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Array;
import io.github.DKICooked.entities.Platform;
import io.github.DKICooked.entities.PlayerActor;
import io.github.DKICooked.screen.BaseScreen;

public class GameScreen extends BaseScreen {
    private PlayerActor player;
    private Array<Platform> platforms;

    public GameScreen() {
        super(); // calls BaseScreen constructor

        platforms = new Array<>();

        // Create player
        player = new PlayerActor();
        player.setSize(40, 60);
        player.setPosition(400, 200);
        stage.addActor(player);

        // Create some test platforms
        createTestPlatforms();
    }

    private void createTestPlatforms() {
        // Ground platform
        Platform ground = new Platform(0, 100, 800, 20);
        platforms.add(ground);
        stage.addActor(ground);

        // Some climbing platforms
        Platform p1 = new Platform(300, 250, 150, 15);
        platforms.add(p1);
        stage.addActor(p1);

        Platform p2 = new Platform(150, 400, 120, 15);
        platforms.add(p2);
        stage.addActor(p2);

        Platform p3 = new Platform(450, 550, 140, 15);
        platforms.add(p3);
        stage.addActor(p3);

        Platform p4 = new Platform(250, 700, 130, 15);
        platforms.add(p4);
        stage.addActor(p4);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Check collisions FIRST, before stage.act()
        player.checkPlatformCollision(platforms);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        stage.dispose();
        // Don't dispose skin since BaseScreen already does it
    }
}
