package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import io.github.DKICooked.Main;
import io.github.DKICooked.entities.Girder;
import io.github.DKICooked.entities.PlayerActor;
import io.github.DKICooked.entities.PlayerSprite;
import io.github.DKICooked.render.DebugRenderer;
import io.github.DKICooked.screen.BaseScreen;

public class GameScreen extends BaseScreen {

    private static final float SCREEN_WIDTH = 800f;
    private static final float SCREEN_HEIGHT = 600f;
    private static final float CHUNK_HEIGHT = SCREEN_HEIGHT;
    private static final float GIRDER_HEIGHT = 18f;

    private final Main main;
    private final Array<Girder> activeGirders = new Array<>();

    private PlayerActor player;
    private PlayerSprite sprite;

    private int currentChunk = 0;
    private final IntMap<Chunk> chunks = new IntMap<>();

    private static class Chunk {
        int index;
        float yStart;
        Array<Girder> girders = new Array<>();
        boolean loaded;

        Chunk(int index) {
            this.index = index;
            this.yStart = index * CHUNK_HEIGHT;
        }
    }

    public GameScreen(Main main) {
        this.main = main;

        player = new PlayerActor();
        player.setSize(40, 60);
        player.setPosition(100, 120);
        stage.addActor(player);

        sprite = new PlayerSprite(player);

        getOrCreateChunk(0);
        getOrCreateChunk(1);

        snapCamera(0);
    }

    // ------------------------------------------------------

    private Chunk getOrCreateChunk(int index) {
        if (chunks.containsKey(index)) {
            Chunk c = chunks.get(index);
            if (!c.loaded) loadChunk(c);
            return c;
        }

        Chunk chunk = new Chunk(index);
        generateChunk(chunk);
        chunks.put(index, chunk);
        loadChunk(chunk);
        return chunk;
    }

    private void generateChunk(Chunk chunk) {
        float y = chunk.yStart + 90f;
        boolean slopeLeft = chunk.index % 2 == 0;

        int rows = MathUtils.random(4, 6);

        for (int i = 0; i < rows; i++) {
            float slope = slopeLeft ? 45f : -45f;

            Girder girder = new Girder(
                0,
                y,
                SCREEN_WIDTH,
                GIRDER_HEIGHT,
                slope
            );

            // Controlled hole chance
            if (i > 0 && MathUtils.random() < 0.35f) {
                float holeWidth = MathUtils.random(90f, 140f);
                float holeX = MathUtils.random(150f, SCREEN_WIDTH - holeWidth - 150f);
                girder.addHole(holeX, holeWidth);
            }

            chunk.girders.add(girder);

            y += MathUtils.random(120f, 150f);
            slopeLeft = !slopeLeft;
        }
    }

    private void loadChunk(Chunk chunk) {
        for (Girder g : chunk.girders) {
            stage.addActor(g);
            activeGirders.add(g);
        }
        chunk.loaded = true;
    }

    private void unloadChunk(Chunk chunk) {
        for (Girder g : chunk.girders) {
            g.remove();
            activeGirders.removeValue(g, true);
        }
        chunk.loaded = false;
    }

    // ------------------------------------------------------

    private void updateChunks() {
        int playerChunk = (int)(player.getY() / CHUNK_HEIGHT);

        if (playerChunk != currentChunk) {
            currentChunk = playerChunk;

            getOrCreateChunk(playerChunk + 1);

            for (IntMap.Entry<Chunk> e : chunks) {
                if (Math.abs(e.key - playerChunk) > 1) {
                    unloadChunk(e.value);
                }
            }

            snapCamera(playerChunk);
        }
    }

    private void snapCamera(int chunkIndex) {
        OrthographicCamera cam = (OrthographicCamera) stage.getCamera();
        cam.position.set(
            SCREEN_WIDTH / 2,
            chunkIndex * CHUNK_HEIGHT + SCREEN_HEIGHT / 2,
            0
        );
        cam.update();
    }

    // ------------------------------------------------------

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (!Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            updateChunks();
        }

        stage.act(delta);
        stage.draw();

        var batch = stage.getBatch();
        batch.begin();
        sprite.draw(batch, player);
        batch.end();

        drawScreenOutline();
    }

    private void drawScreenOutline() {
        DebugRenderer.begin(stage.getCamera());
        DebugRenderer.renderer.setColor(1, 1, 1, 1);

        OrthographicCamera cam = (OrthographicCamera) stage.getCamera();
        DebugRenderer.renderer.rect(
            cam.position.x - SCREEN_WIDTH / 2,
            cam.position.y - SCREEN_HEIGHT / 2,
            SCREEN_WIDTH,
            SCREEN_HEIGHT
        );

        DebugRenderer.end();
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
