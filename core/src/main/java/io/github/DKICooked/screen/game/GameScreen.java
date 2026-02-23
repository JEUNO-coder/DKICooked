package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.DKICooked.Main;
import io.github.DKICooked.entities.Platform;
import io.github.DKICooked.entities.PlayerActor;
import io.github.DKICooked.entities.PlayerSprite;
import io.github.DKICooked.gameLogic.generationLogic.LevelGenerator;
import io.github.DKICooked.render.DebugRenderer;
import io.github.DKICooked.screen.BaseScreen;
import io.github.DKICooked.screen.main.MainMenuScreen;

public class GameScreen extends BaseScreen {

    private static final float SCREEN_WIDTH = 800f;
    private static final float SCREEN_HEIGHT = 600f;
    private static final float CHUNK_HEIGHT = SCREEN_HEIGHT;

    private final Main main;
    private final LevelGenerator generator = new LevelGenerator();

    // The "Source of Truth" for physics and rendering
    private final Array<Platform> activePlatforms = new Array<>();
    private final IntMap<Chunk> chunks = new IntMap<>();

    private PlayerActor player;
    private PlayerSprite sprite;

    private int currentChunk = 0;
    private int highestChunkReached = 0;

    private PausedScreen pause;
    private boolean paused = false;
    Table uiTable = new Table();
    private ImageButton pauseButton;

    private static class Chunk {
        int index;
        float yStart;
        Array<Platform> platforms = new Array<>();

        Chunk(int index) {
            this.index = index;
            this.yStart = index * CHUNK_HEIGHT;
        }
    }

    public GameScreen(Main main) {
        this.main = main;

        // Setup Player
        player = new PlayerActor();
        player.setSize(40, 60);
        player.setPosition(400, 150); // Start middle-bottom
        stage.addActor(player);

        sprite = new PlayerSprite(player);

        // Initial world generation
        getOrCreateChunk(0);
        getOrCreateChunk(1);

        // Pass initial platforms to player
        updateActivePlatformsList();

        displayPause();

        snapCamera(0);
    }

    private void getOrCreateChunk(int index) {
        if (chunks.containsKey(index)) return;

        Chunk chunk = new Chunk(index);
        // Path-First Generation
        Array<Platform> generated = generator.generateChunk(chunk.yStart, CHUNK_HEIGHT);
        chunk.platforms.addAll(generated);

        chunks.put(index, chunk);
    }

    private void updateActivePlatformsList() {
        activePlatforms.clear();
        // Only keep platforms from the previous, current, and next chunk for performance
        for (int i = currentChunk - 1; i <= currentChunk + 1; i++) {
            if (chunks.containsKey(i)) {
                activePlatforms.addAll(chunks.get(i).platforms);
            }
        }
        player.setPlatforms(activePlatforms);
    }

    private void updateChunks() {
        int playerChunk = (int)(player.getY() / CHUNK_HEIGHT);

        // Update Highest Point & Game Over Check
        if (playerChunk > highestChunkReached) highestChunkReached = playerChunk;

        if (highestChunkReached > 0 && playerChunk < highestChunkReached - 1) {
            Gdx.app.postRunnable(() -> main.setScreen(new MainMenuScreen(main)));
            return;
        }

        // Chunk Management
        if (playerChunk != currentChunk) {
            currentChunk = playerChunk;
            getOrCreateChunk(currentChunk);
            getOrCreateChunk(currentChunk + 1);

            updateActivePlatformsList();
            snapCamera(currentChunk);
        }
    }

    private void snapCamera(int chunkIndex) {
        float newY = chunkIndex * CHUNK_HEIGHT;
        OrthographicCamera cam = (OrthographicCamera) stage.getCamera();
        cam.position.set(SCREEN_WIDTH / 2, newY + SCREEN_HEIGHT / 2, 0);
        cam.update();

        if (uiTable != null) {
            uiTable.setPosition(0, newY);
        }
        if (pause != null) {
            pause.setPosition(0, newY);
        }
    }

    @Override
    public void render(float delta) {
        // 1. Clear Screen
        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);

        // 2. Logic Update
        if (!Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            updateChunks();
            stage.act(delta);
        }

        // 3. Render Platforms (World Layer)
        DebugRenderer.begin(stage.getCamera());
        DebugRenderer.renderer.setColor(Color.RED);
        for (Platform p : activePlatforms) {
            p.draw(DebugRenderer.renderer);
        }
        DebugRenderer.end();

        // 4. Render Stage (UI/Actors)
        stage.draw();

        // 5. Render Player Sprite (Entity Layer)
        var batch = stage.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        sprite.draw(batch, player);
        batch.end();
    }

    public void displayPause() {
        Texture pauseTex = new Texture(Gdx.files.internal("Pause.png"));
        ImageButton.ImageButtonStyle pauseStyle = new ImageButton.ImageButtonStyle();
        pauseStyle.imageUp = new TextureRegionDrawable(new TextureRegion(pauseTex));

        pauseButton = new ImageButton(pauseStyle);

        uiTable = new Table();
        uiTable.setFillParent(true); // This makes the table the size of the screen
        uiTable.top().right();
        uiTable.add(pauseButton).size(40, 40).pad(10);

        stage.addActor(uiTable);

        // Initialize Pause Screen Logic
        pause = new PausedScreen(() -> {
            paused = false;
            pause.toggle(false);
        }, main);
        stage.addActor(pause);

        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                paused = !paused;
                pause.toggle(paused);
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
}
