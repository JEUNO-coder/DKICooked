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
        player.setPosition(100, 200);
        stage.addActor(player);
        player.setGirders(activeGirders);

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

    private enum ChunkType {
        ZIG_ZAG,      // Classic DK style
        THE_GAP,      // Large central gap, requires precise jump power
        STAGGERED,    // Small floating platforms (Jump King style)
    }

    private void generateChunk(Chunk chunk) {
        // Pick a random puzzle type
        ChunkType type = ChunkType.values()[MathUtils.random(ChunkType.values().length - 1)];

        // We can also make it harder based on height
        if (chunk.index == 0) type = ChunkType.ZIG_ZAG; // Keep start easy

        switch (type) {
            case ZIG_ZAG:
                createZigZag(chunk);
                break;
            case THE_GAP:
                createTheGap(chunk);
                break;
            case STAGGERED:
                createStaggered(chunk);
                break;
        }
    }

// --- Puzzle Templates ---

    private void createZigZag(Chunk chunk) {
        float y = chunk.yStart + 100f;
        boolean slopeLeft = chunk.index % 2 == 0;

        for (int i = 0; i < 4; i++) {
            float slope = slopeLeft ? 30f : -30f;
            Girder g = new Girder(0, y, SCREEN_WIDTH, GIRDER_HEIGHT, slope);

            // Create the "climb-up" hole at the end of the slope
            float holeX = slopeLeft ? SCREEN_WIDTH - 120f : 40f;
            g.addHole(holeX, 80f);

            chunk.girders.add(g);
            y += 140f;
            slopeLeft = !slopeLeft;
        }
    }

    private void createTheGap(Chunk chunk) {
        // A huge central hole that requires a near-max charge jump
        float y = chunk.yStart + 150f;

        // Bottom platform
        Girder bottom = new Girder(0, y, SCREEN_WIDTH, GIRDER_HEIGHT, 0);
        bottom.addHole(200, 400); // Massive hole in the middle
        chunk.girders.add(bottom);

        // Small "island" in the middle of the screen higher up
        chunk.girders.add(new Girder(350, y + 200, 100, GIRDER_HEIGHT, 0));

        // Top platforms
        Girder top = new Girder(0, y + 400, SCREEN_WIDTH, GIRDER_HEIGHT, 0);
        top.addHole(300, 200);
        chunk.girders.add(top);
    }

    private void createStaggered(Chunk chunk) {
        // Jump King style: Tiny platforms that require exact horizontal landing
        float y = chunk.yStart + 80f;
        float lastX = 100;

        for (int i = 0; i < 5; i++) {
            float width = MathUtils.random(80f, 150f);
            float x = (lastX < 400) ? MathUtils.random(450f, 650f) : MathUtils.random(50f, 250f);

            chunk.girders.add(new Girder(x, y, width, GIRDER_HEIGHT, MathUtils.random(-10f, 10f)));
            y += 110f;
            lastX = x;
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
        DebugRenderer.renderer.setColor(0, 0, 0, 0);

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
