package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import io.github.DKICooked.Main;
import io.github.DKICooked.entities.Platform;
import io.github.DKICooked.entities.PlayerActor;
import io.github.DKICooked.entities.PlayerSprite;
import io.github.DKICooked.render.DebugRenderer;
import io.github.DKICooked.screen.BaseScreen;
import io.github.DKICooked.screen.main.MainMenuScreen;

public class GameScreen extends BaseScreen {
    private PlayerActor player;
    private PlayerSprite sprite;
    private Array<Platform> platforms;
    private boolean paused = false;
    private final Main main;

    //For Paused Screen
    private PausedScreen pause;

    // Screen parameters
    private static final float SCREEN_WIDTH = 800f;
    private static final float SCREEN_HEIGHT = 600f;
    private static final float CHUNK_HEIGHT = SCREEN_HEIGHT;
    private static final float PLATFORM_HEIGHT = 15f;

    // Jump constraints
    private static final float MAX_JUMP_HEIGHT = 600f;
    private static final float MAX_JUMP_DISTANCE = 450f;
    private static final float SAFE_JUMP_HEIGHT = 200f;
    private static final float SAFE_JUMP_DISTANCE = 300f;

    // Platform sizing
    private static final float MIN_PLATFORM_WIDTH = 120f;
    private static final float MAX_PLATFORM_WIDTH = 400f;

    // Chunk management
    private int currentChunkIndex = 0;
    private IntMap<Chunk> allChunks = new IntMap<>(); // Store all generated chunks by index

    // Pattern generation state (per chunk)
    private float lastPlatformY;
    private float lastPlatformX;
    private float lastPlatformWidth;
    private int consecutiveHardJumps = 0;

    private class Chunk {
        int index;
        float yStart;
        float yEnd;
        Array<Platform> platforms;
        boolean isLoaded; // Whether platforms are in the stage

        Chunk(int index) {
            this.index = index;
            this.yStart = index * CHUNK_HEIGHT;
            this.yEnd = (index + 1) * CHUNK_HEIGHT;
            this.platforms = new Array<>();
            this.isLoaded = false;
        }
    }

    public GameScreen(Main main) {
        super();
        this.main = main;
        platforms = new Array<>();

        // Generate initial chunks
        getOrCreateChunk(-1); // Safety chunk (empty initially)
        getOrCreateChunk(0);  // Active chunk
        getOrCreateChunk(1);  // Preview chunk

        // Create player in chunk 0
        player = new PlayerActor();
        player.setSize(40, 60);
        Chunk startChunk = allChunks.get(0);
        player.setPosition(100, startChunk.yStart + 100);
        stage.addActor(player);

        // Give player reference to platforms
        player.setPlatforms(platforms);

        //Create Sprite
        sprite = new PlayerSprite(player);

        // Position camera at chunk 0
        snapCameraToChunk(allChunks.get(0));

        //For paused button
        Texture pauseTex = new Texture(Gdx.files.internal("Pause.png"));
        ImageButton.ImageButtonStyle pauseStyle = new ImageButton.ImageButtonStyle();
        pauseStyle.imageUp = new TextureRegionDrawable(new TextureRegion(pauseTex));

        ImageButton pauseButton = new ImageButton(pauseStyle);
        pauseButton.setPosition(10, Gdx.graphics.getHeight() - 50);
        stage.addActor(pauseButton);

        Table uiTable = new Table();
        uiTable.setFillParent(true);
        stage.addActor(uiTable);

        uiTable.top().right();
        uiTable.add(pauseButton).size(40, 40).pad(10);

        //stage.setDebugAll(true);

        // For paused screen
        pause = new PausedScreen(() -> {
            paused = false;
            pause.toggle(false);;
        }, main);
        stage.addActor(pause);

        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                paused = !paused;
                pause.toggle(paused);
                System.out.println("Paused: " + paused);
            }
        });

    }

    private Chunk getOrCreateChunk(int index) {
        if (allChunks.containsKey(index)) {
            Chunk existing = allChunks.get(index);
            // Load if not already loaded
            if (!existing.isLoaded) {
                loadChunk(existing);
            }
            return existing;
        }

        // Create new chunk
        Chunk newChunk = new Chunk(index);
        allChunks.put(index, newChunk);

        if (index >= 0) { // Don't generate platforms for negative chunks (below start)
            generateChunk(newChunk);
        }

        loadChunk(newChunk);
        return newChunk;
    }

    private void loadChunk(Chunk chunk) {
        if (chunk.isLoaded) return;

        System.out.println("Loading chunk " + chunk.index + " with " + chunk.platforms.size + " platforms");
        for (Platform p : chunk.platforms) {
            stage.addActor(p);
            if (!platforms.contains(p, true)) {
                platforms.add(p);
            }
        }
        chunk.isLoaded = true;
    }

    private void unloadChunk(Chunk chunk) {
        if (!chunk.isLoaded) return;

        System.out.println("Unloading chunk " + chunk.index);
        for (Platform p : chunk.platforms) {
            p.remove();
            platforms.removeValue(p, true);
        }
        chunk.isLoaded = false;
    }

    private void generateChunk(Chunk chunk) {
        System.out.println("Generating NEW chunk " + chunk.index + " (Y: " + chunk.yStart + " - " + chunk.yEnd + ")");

        // Reset generation state for this chunk
        lastPlatformY = chunk.yStart;
        lastPlatformX = SCREEN_WIDTH / 2;
        lastPlatformWidth = SCREEN_WIDTH;
        consecutiveHardJumps = 0;

        // Ground platform for chunk 0, otherwise start with a safe platform
        if (chunk.index == 0) {
            createGirderInChunk(chunk, 0, chunk.yStart + 100, SCREEN_WIDTH, 20);
            lastPlatformY = chunk.yStart + 120;
        } else {
            // Create connecting platform at bottom of chunk
            float width = MathUtils.random(300f, 500f);
            float x = MathUtils.random(0, SCREEN_WIDTH - width);
            createGirderInChunk(chunk, x, chunk.yStart + 50, width, PLATFORM_HEIGHT);
            lastPlatformY = chunk.yStart + 50;
            lastPlatformX = x + width / 2;
            lastPlatformWidth = width;
        }

        // Generate platforms until we fill the chunk
        int safetyCounter = 0;
        int maxIterations = 50;
        while (lastPlatformY < chunk.yEnd - 100 && safetyCounter < maxIterations) {
            generateNextPattern(chunk);
            safetyCounter++;
        }

        if (safetyCounter >= maxIterations) {
            System.out.println("WARNING: Hit max iterations for chunk " + chunk.index);
        }
    }

    private void generateNextPattern(Chunk chunk) {
        float difficulty = Math.min(chunk.index / 10f, 1.0f);

        if (consecutiveHardJumps >= 2 || MathUtils.random() < 0.15f) {
            generateSafePlatform(chunk);
            consecutiveHardJumps = 0;
            return;
        }

        float roll = MathUtils.random();

        if (roll < 0.4f) {
            generateStaircase(chunk, difficulty);
        } else if (roll < 0.7f) {
            generateZigzag(chunk, difficulty);
        } else if (roll < 0.85f) {
            generateGapJump(chunk, difficulty);
        } else {
            generateChoice(chunk, difficulty);
        }
    }

    private void generateSafePlatform(Chunk chunk) {
        float y = lastPlatformY + MathUtils.random(120f, 160f);
        if (y >= chunk.yEnd) return;

        float width = MathUtils.random(500f, SCREEN_WIDTH-300f);
        float x = MathUtils.random(0, SCREEN_WIDTH - width);

        createGirderInChunk(chunk, x, y, width, PLATFORM_HEIGHT);
        updateLastPlatform(y, x + width / 2, width);
    }

    private void generateStaircase(Chunk chunk, float difficulty) {
        boolean goingLeft = MathUtils.randomBoolean();
        int steps = MathUtils.random(2, 3);

        for (int i = 0; i < steps; i++) {
            float verticalGap = lerp(120f, 180f, difficulty);
            float horizontalGap = lerp(150f, 250f, difficulty);
            float width = lerp(250f, 180f, difficulty);

            float y = lastPlatformY + verticalGap;
            if (y >= chunk.yEnd) break;

            float x;
            if (goingLeft) {
                x = lastPlatformX - horizontalGap - width / 2;
                x = Math.max(0, Math.min(x, SCREEN_WIDTH - width));
            } else {
                x = lastPlatformX + horizontalGap - width / 2;
                x = Math.max(0, Math.min(x, SCREEN_WIDTH - width));
            }

            createGirderInChunk(chunk, x, y, width, PLATFORM_HEIGHT);
            updateLastPlatform(y, x + width / 2, width);
        }

        consecutiveHardJumps++;
    }

    private void generateZigzag(Chunk chunk, float difficulty) {
        boolean startLeft = lastPlatformX < SCREEN_WIDTH / 2;

        for (int i = 0; i < 3; i++) {
            float verticalGap = lerp(140f, 190f, difficulty);
            float width = lerp(300f, 200f, difficulty);
            float y = lastPlatformY + verticalGap;
            if (y >= chunk.yEnd) break;

            float x;
            if ((i % 2 == 0) == startLeft) {
                x = MathUtils.random(0f, 100f);
            } else {
                x = MathUtils.random(SCREEN_WIDTH - width - 100f, SCREEN_WIDTH - width);
            }

            createGirderInChunk(chunk, x, y, width, PLATFORM_HEIGHT);
            updateLastPlatform(y, x + width / 2, width);
        }

        consecutiveHardJumps++;
    }

    private void generateGapJump(Chunk chunk, float difficulty) {
        float verticalGap = lerp(150f, 200f, difficulty);
        float y = lastPlatformY + verticalGap;
        if (y >= chunk.yEnd) return;

        float width1 = lerp(200f, 150f, difficulty);
        float width2 = lerp(200f, 150f, difficulty);
        float gapSize = lerp(200f, 300f, difficulty);

        float totalWidth = width1 + gapSize + width2;
        if (totalWidth > SCREEN_WIDTH) {
            float scale = SCREEN_WIDTH / totalWidth;
            width1 *= scale;
            width2 *= scale;
            gapSize *= scale;
        }

        float x1 = MathUtils.random(0f, SCREEN_WIDTH - totalWidth);
        float x2 = x1 + width1 + gapSize;

        createGirderInChunk(chunk, x1, y, width1, PLATFORM_HEIGHT);
        createGirderInChunk(chunk, x2, y, width2, PLATFORM_HEIGHT);

        updateLastPlatform(y, x2 + width2 / 2, width2);
        consecutiveHardJumps++;
    }

    private void generateChoice(Chunk chunk, float difficulty) {
        float verticalGap = lerp(160f, 200f, difficulty);
        float y = lastPlatformY + verticalGap;
        if (y >= chunk.yEnd) return;

        float width = lerp(220f, 180f, difficulty);

        float leftX = MathUtils.random(0f, 80f);
        float rightX = MathUtils.random(SCREEN_WIDTH - width - 80f, SCREEN_WIDTH - width);

        createGirderInChunk(chunk, leftX, y, width, PLATFORM_HEIGHT);
        createGirderInChunk(chunk, rightX, y, width, PLATFORM_HEIGHT);

        updateLastPlatform(y, SCREEN_WIDTH / 2, width);
        consecutiveHardJumps++;
    }

    private void createGirderInChunk(Chunk chunk, float x, float y, float width, float height) {
        Platform platform = new Platform(x, y, width, height);
        chunk.platforms.add(platform);
    }

    private void updateLastPlatform(float y, float centerX, float width) {
        lastPlatformY = y;
        lastPlatformX = centerX;
        lastPlatformWidth = width;
    }

    private float lerp(float min, float max, float t) {
        return min + (max - min) * t;
    }

    private void snapCameraToChunk(Chunk chunk) {
        OrthographicCamera cam = (OrthographicCamera) stage.getCamera();
        cam.position.set(SCREEN_WIDTH / 2, chunk.yStart + SCREEN_HEIGHT / 2, 0);
        cam.update();
    }

    private void updateChunks() {
        int playerChunk = (int) (player.getY() / CHUNK_HEIGHT);

        if (playerChunk != currentChunkIndex) {
            System.out.println("Player moved from chunk " + currentChunkIndex + " to chunk " + playerChunk);
            currentChunkIndex = playerChunk;

            // Ensure the chunks around the player exist and are loaded
            Chunk safetyChunk = getOrCreateChunk(playerChunk - 1);
            Chunk activeChunk = getOrCreateChunk(playerChunk);
            Chunk previewChunk = getOrCreateChunk(playerChunk + 1);

            // Unload chunks that are too far away (more than 2 chunks away)
            for (IntMap.Entry<Chunk> entry : allChunks) {
                int chunkIndex = entry.key;
                if (chunkIndex < playerChunk - 1 || chunkIndex > playerChunk + 1) {
                    unloadChunk(entry.value);
                }
            }

            // Snap camera
            snapCameraToChunk(activeChunk);
        }

        // Check death (fell below safety chunk)
        Chunk safetyChunk = allChunks.get(playerChunk - 1);
        if (safetyChunk != null && player.getY() < safetyChunk.yStart) {
            System.out.println("GAME OVER - Fell below safety chunk!");
        }
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if(Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            paused = !paused;
            pause.toggle(paused);
        }

        if (!paused) {
            updateChunks();
        }

        stage.act(delta);
        stage.draw();

        Batch batch = stage.getBatch();
        batch.begin();
        sprite.draw(batch, player);
        batch.end();

        checkScreenBounds();
        drawScreenOutline();
    }

    private void drawScreenOutline() {
        DebugRenderer.begin(stage.getCamera());
        DebugRenderer.renderer.setColor(1f, 1f, 1f, 1f);

        OrthographicCamera cam = (OrthographicCamera) stage.getCamera();
        float camBottom = cam.position.y - SCREEN_HEIGHT / 2;
        float camLeft = cam.position.x - SCREEN_WIDTH / 2;

        DebugRenderer.renderer.rect(camLeft, camBottom, SCREEN_WIDTH, SCREEN_HEIGHT);
        DebugRenderer.end();
    }

    private void checkScreenBounds() {
        if (player.getX() < 0) {
            player.setX(0);
            player.getBody().velocityX = Math.abs(player.getBody().velocityX) * 1.3f;
        }

        if (player.getX() + player.getWidth() > SCREEN_WIDTH) {
            player.setX(SCREEN_WIDTH - player.getWidth());
            player.getBody().velocityX = -Math.abs(player.getBody().velocityX) * 1.3f;
        }
    }

    @Override
    public void dispose() {
        stage.dispose();
    }
}
