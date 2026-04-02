package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.DKICooked.Main;
import io.github.DKICooked.audio.SoundPlayer;
import io.github.DKICooked.entities.*;
import io.github.DKICooked.gameLogic.WorldManager;
import io.github.DKICooked.screen.BaseScreen;

public class GameScreen extends BaseScreen {
    private final String selection;
    private enum State { PLAYING, DYING, GAMEOVER }
    private State currentState = State.PLAYING;
    private float deathTimer = 0f;
    private static final float DEATH_DURATION = 1.5f;
    private Table gameOverTable;

    private static final float SCREEN_WIDTH  = 800f;
    private static final float SCREEN_HEIGHT = 600f;

    private final Main main;
    private final WorldManager world;
    private final PlayerActor player;
    private final PlayerSprite sprite;
    private final SoundPlayer soundPlayer;

    private Texture platformTileTexture;
    private PlatformTiles platformTile;
    private Texture playerFallenTexture;
    private Texture backgroundTexture;
    private Texture railTexture;
    private Texture titleTex;
    private Texture retryTex;
    private Texture whitePixel;

    private Texture asteroidTex;
    private float preRaidTimer = 0;
    private final float WARNING_DURATION = 3.0f;

    private enum RaidType { NONE, ASTEROIDS, UFO }
    private RaidType activeRaid = RaidType.NONE;

    private float raidEndHeight = 0;
    private int lastCheckedChunk = -1;

    private float ufoSpawnTimer = 0f;
    private final float UFO_SPAWN_INTERVAL = 4.0f;
    private Animation<TextureRegion> ufoHorizontalAnim;
    private final UfoManager ufoManager;

    // Fonts — stored so they can be disposed
    private BitmapFont scoreFont;
    private BitmapFont gameOverFont;

    private Label scoreLabel;
    private Label finalScoreLabel;
    private final StringBuilder scoreBuilder = new StringBuilder();

    private final Stage uiStage;
    private float backgroundTintAlpha = 0f; // 0 = Normal, 1 = Full Red
    private final float FADE_SPEED = 1.5f;   // Higher = faster fade

    private int highestChunkReached = 0;
    private int lastSnapChunk = -1;       // tracks which chunk we last snapped to
    private boolean paused = false;
    private boolean escWasPressed = false; // debounce for ESC key
    private PausedScreen pauseOverlay;

    private final AsteroidManager asteroidManager;
    private Texture anomalyTex;
    private float anomalyTimer = 0;

    private int recordHeight = 0;

    public GameScreen(Main main, String selection) {
        this.main = main;
        this.selection = selection;
        playerFallenTexture  = new Texture(Gdx.files.internal("dead.png"));
        anomalyTex = new Texture(Gdx.files.internal("emer.png"));
        soundPlayer = new SoundPlayer();
        soundPlayer.playMusic();

        // UI stage — fixed viewport, never scrolls
        this.uiStage = new Stage(new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT));

        //Asteroid
        asteroidTex = new Texture(Gdx.files.internal("asteroid.png"));
        this.asteroidManager = new AsteroidManager(asteroidTex);

        // World
        this.world = new WorldManager();

        platformTileTexture = new Texture(Gdx.files.internal("wallTile.jpg"));
        platformTile        = new PlatformTiles(platformTileTexture);

        player = new PlayerActor(soundPlayer);
        player.setSize(40, 60);
        player.setPosition(400, 150);
        player.setPlatforms(world.getActivePlatforms());

        player.initStats(selection);

        stage.addActor(player);
        sprite = new PlayerSprite(selection);

        backgroundTexture = new Texture(Gdx.files.internal("background.png"));
        railTexture = new Texture(Gdx.files.internal("rail.png"));
        backgroundTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);
        railTexture.setWrap(Texture.TextureWrap.Repeat, Texture.TextureWrap.Repeat);

        Texture f1 = new Texture(Gdx.files.internal("ufoH1.png"));
        Texture f2 = new Texture(Gdx.files.internal("ufoH2.png"));

        TextureRegion[] frames = {
            new TextureRegion(f1),
            new TextureRegion(f2),
        };

        ufoHorizontalAnim = new Animation<>(0.3f, frames);
        ufoHorizontalAnim.setPlayMode(Animation.PlayMode.LOOP);

        this.ufoManager = new UfoManager(ufoHorizontalAnim);

        // Input — UI gets first dibs
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage);
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        setupUI();
        snapCamera(0);
    }

    // ── Update ────────────────────────────────────────────────────────────────

    private void updateLogic(float delta) {
        boolean escDown = Gdx.input.isKeyPressed(Input.Keys.ESCAPE);
        if (escDown && !escWasPressed) {
            paused = !paused;
            pauseOverlay.toggle(paused);
        }
        escWasPressed = escDown;

        if (currentState == State.PLAYING) {
            world.update(player.getY());
            player.setPlatforms(world.getActivePlatforms());

            int currentChunk = world.getCurrentChunk();

            if (currentChunk > highestChunkReached) {
                highestChunkReached = currentChunk;
            }

            if (currentChunk != lastSnapChunk) {
                snapCamera(currentChunk);
                lastSnapChunk = currentChunk;
            }

            if (activeRaid != RaidType.NONE) {
                checkCollisions(); // This checks both classes, but only the active ones will be on stage
            }

            if (highestChunkReached > 0 && currentChunk < highestChunkReached - 1) {
                startDeathSequence();
            }

            // Calculate and update score text
            int currentHeight = (int) (player.getY() / 100f);
            if (currentHeight > recordHeight) recordHeight = currentHeight;

            scoreBuilder.setLength(0);
            scoreBuilder.append("Best: ").append(recordHeight).append("m");
            scoreLabel.setText(scoreBuilder);



        } else if (currentState == State.DYING) {
            deathTimer += delta;
            float bounce = (float) Math.sin(deathTimer * 5) * 50f;
            float fall   = 300f * deathTimer;
            player.setY(player.getY() + (bounce * (1 - deathTimer)) - (fall * delta));
            player.rotateBy(400 * delta);

            if (deathTimer >= DEATH_DURATION) showGameOverScreen();
        }

        stage.act(delta);
    }

    private void startDeathSequence() {
        currentState = State.DYING;
        player.setDead(true);
        player.clearActions();
        player.setOrigin(player.getWidth() / 2f, player.getHeight() / 2f);
    }

    private void showGameOverScreen() {
        currentState = State.GAMEOVER;
        paused = true;
        if (finalScoreLabel != null) finalScoreLabel.setText("Best Score: " + recordHeight + "m");
        gameOverTable.setVisible(true);
    }

    private void snapCamera(int chunkIndex) {
        float newY = chunkIndex * SCREEN_HEIGHT;
        OrthographicCamera cam = (OrthographicCamera) stage.getCamera();
        cam.position.set(SCREEN_WIDTH / 2f, newY + SCREEN_HEIGHT / 2f, 0);
        cam.update();
    }

    private void checkCollisions() {
        if (currentState != State.PLAYING) return;

        for (com.badlogic.gdx.scenes.scene2d.Actor actor : stage.getActors()) {
            if (actor instanceof AsteroidActor) {
                AsteroidActor meteor = (AsteroidActor) actor;

                if (com.badlogic.gdx.math.Intersector.overlaps(meteor.getCollisionCircle(), player.getCollisionRect())) {
                    // Trigger the sequence, but let updateLogic handle the timer
                    startDeathSequence();
                    break;
                }
            }

            if (actor instanceof UfoActor) {
                UfoActor ufo = (UfoActor) actor;
                if (com.badlogic.gdx.math.Intersector.overlaps(ufo.getCollisionCircle(), player.getCollisionRect())) {
                    startDeathSequence();
                    break;
                }
            }
        }
    }

    // ── Render ────────────────────────────────────────────────────────────────
    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);

        // --- 1. LOGIC UPDATES ---
        if (!paused && (currentState == State.PLAYING || currentState == State.DYING)) {
            updateLogic(delta);

            if (currentState == State.PLAYING) {
                handleAnomalyLogic(delta); // Move your if(isAnomalyZone) logic here for cleanliness
            }
        }

        uiStage.act(delta);
        var batch = (com.badlogic.gdx.graphics.g2d.SpriteBatch) stage.getBatch();

        // --- 2. LAYER 1 & 2: BACKGROUND (Stars & Rails) ---
        batch.setProjectionMatrix(uiStage.getCamera().combined);
        batch.begin();

        float currentGreenBlue = 1f - (backgroundTintAlpha * 0.7f);
        batch.setColor(1f, currentGreenBlue, currentGreenBlue, 1f);

        float starScrollV = (player.getY() * 0.05f) / backgroundTexture.getHeight();
        batch.draw(backgroundTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, 0, starScrollV + 1, 1, starScrollV);

        float railScrollV = (player.getY() * 0.3f) / railTexture.getHeight();
        batch.draw(railTexture, 0, 0, SCREEN_WIDTH, SCREEN_HEIGHT, 0, (railScrollV / 2f), 1, (railScrollV / 2f) + 0.5f);

        batch.setColor(Color.WHITE); // Reset
        batch.end();

        // --- 3. LAYER 3: GAME WORLD (Platforms & Player) ---
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        for (Platform p : world.getActivePlatforms()) {
            platformTile.render(batch, p);
        }
        sprite.draw(batch, player);
        batch.end();

        // This draws the Asteroids (Actors) added to the stage
        stage.draw();

        // --- 4. LAYER 4: UI (Score & Pause) ---
        uiStage.draw();

        // --- 5. LAYER 5: ANOMALY OVERLAY ("EMER" Sign) ---
        if (backgroundTintAlpha > 0) { // Show if even slightly tinted
            anomalyTimer += delta;
            batch.setProjectionMatrix(uiStage.getCamera().combined);
            batch.begin();

            if (!com.badlogic.gdx.math.MathUtils.randomBoolean(0.03f)) {
                float shakeX = com.badlogic.gdx.math.MathUtils.random(-2f, 2f);
                float shakeY = com.badlogic.gdx.math.MathUtils.random(-1f, 1f);

                // Pulse speed
                float pulse = 0.6f + (float) Math.sin(anomalyTimer * 6f) * 0.4f;

                // Multiply pulse by the global background fade so it fades in too!
                batch.setColor(1, 1, 1, pulse * backgroundTintAlpha);

                batch.draw(anomalyTex, (SCREEN_WIDTH / 2f) - 285f + shakeX, 420 + shakeY, 570f, 85f);
                batch.setColor(Color.WHITE);
            }
            batch.end();
        }
    }

    private void handleAsteroidRaid(float delta) {
        // 1. Visuals: Fade the tint IN
        backgroundTintAlpha += delta * FADE_SPEED;
        if (backgroundTintAlpha > 1f) backgroundTintAlpha = 1f;

        // 2. Initial Warning Delay
        if (preRaidTimer < 2.0f) {
            preRaidTimer += delta;
        } else {
            // 3. Use your existing Manager to spawn asteroids
            asteroidManager.update(delta, player.getY(), stage);
        }
    }

    private void handleAnomalyLogic(float delta) {
        float py = player.getY();

        // 1. START TRIGGER: If we are above 15m and no raid is currently happening
        if (py >= 1500 && activeRaid == RaidType.NONE) {

            // Pick which one you want to show first (or randomize just the type)
            activeRaid = com.badlogic.gdx.math.MathUtils.randomBoolean() ? RaidType.ASTEROIDS : RaidType.UFO;

            // Set how long this static raid lasts (e.g., 2000 pixels)
            raidEndHeight = py + 2000f;

            System.out.println("STATIC RAID START: " + activeRaid);
        }

        // 2. EXECUTION: Keep the raid running while active
        if (activeRaid != RaidType.NONE) {
            backgroundTintAlpha += delta * FADE_SPEED;
            if (backgroundTintAlpha > 1f) backgroundTintAlpha = 1f;

            if (activeRaid == RaidType.ASTEROIDS) {
                asteroidManager.update(delta, py, stage);
            } else if (activeRaid == RaidType.UFO) {
                ufoManager.update(delta, stage);
            }

            // 3. TERMINATION: End the raid once the player has climbed the duration
            if (py >= raidEndHeight) {
                stopAllRaids();
                System.out.println("Raid Cycle Complete.");
            }
        } else {
            // Smoothly fade out the red tint when between raids
            backgroundTintAlpha -= delta * FADE_SPEED;
            if (backgroundTintAlpha < 0f) backgroundTintAlpha = 0f;
        }
    }

    private void stopAllRaids() {
        activeRaid = RaidType.NONE;
        ufoManager.stop();
        // asteroidManager.stop(); // If yours has a stop/reset method
    }
    // ── UI setup ──────────────────────────────────────────────────────────────

    public void setupUI() {
        scoreFont = new BitmapFont();
        Label.LabelStyle labelStyle = new Label.LabelStyle(scoreFont, Color.WHITE);

        scoreLabel = new Label("Best: 0m", labelStyle);
        scoreLabel.setFontScale(1.5f);

        Table scoreTable = new Table();
        scoreTable.setFillParent(true);
        scoreTable.top().left().pad(20);
        scoreTable.add(scoreLabel);
        uiStage.addActor(scoreTable);

        Texture pauseTex   = new Texture(Gdx.files.internal("Pause.png"));
        ImageButton pauseButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(pauseTex)));

        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.top().right();
        uiTable.add(pauseButton).size(40, 40).pad(10);
        uiStage.addActor(uiTable);

        pauseOverlay = new PausedScreen(() -> {
            paused = false;
            pauseOverlay.toggle(false);
        }, soundPlayer::stopMusic, main);
        uiStage.addActor(pauseOverlay);

        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                paused = !paused;
                pauseOverlay.toggle(paused);
            }
        });

        setupGameOverUI();
    }

    private Texture createWhitePixel() {
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(
            1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(Color.WHITE);
        pixmap.fill();
        Texture t = new Texture(pixmap);
        pixmap.dispose();
        return t;
    }

    private void setupGameOverUI() {
        retryTex = new Texture(Gdx.files.internal("retry.png"));
        ImageButton retryButton = new ImageButton(
            new TextureRegionDrawable(new TextureRegion(retryTex)));

        titleTex = new Texture(Gdx.files.internal("GO.png"));
        com.badlogic.gdx.scenes.scene2d.ui.Image titleImage =
            new com.badlogic.gdx.scenes.scene2d.ui.Image(titleTex);

        gameOverFont = new BitmapFont();
        Label.LabelStyle scoreStyle = new Label.LabelStyle(gameOverFont, Color.WHITE);
        finalScoreLabel = new Label("Best Score: 0m", scoreStyle);
        finalScoreLabel.setFontScale(2f);

        whitePixel = createWhitePixel();
        TextureRegionDrawable bgDrawable =
            new TextureRegionDrawable(new TextureRegion(whitePixel));

        gameOverTable = new Table();
        gameOverTable.setFillParent(true);
        gameOverTable.center();
        gameOverTable.setBackground(bgDrawable.tint(new Color(0, 0, 0, 0.6f)));
        gameOverTable.add(titleImage).size(400, 100).padBottom(20).row();
        gameOverTable.add(finalScoreLabel).padBottom(40).row();
        gameOverTable.add(retryButton).size(100, 100);

        retryButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                soundPlayer.stopMusic();
                main.setScreen(new GameScreen(main, selection));
            }
        });

        gameOverTable.setVisible(false);
        uiStage.addActor(gameOverTable);
    }


    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        uiStage.dispose();
        if (sprite != null) sprite.dispose();
        if (scoreFont           != null) scoreFont.dispose();
        if (gameOverFont        != null) gameOverFont.dispose();
        if (playerFallenTexture != null) playerFallenTexture.dispose();
        if (platformTileTexture != null) platformTileTexture.dispose();
        if (backgroundTexture   != null) backgroundTexture.dispose();
        if (titleTex            != null) titleTex.dispose();
        if (retryTex            != null) retryTex.dispose();
        if (whitePixel          != null) whitePixel.dispose();
        if (asteroidTex != null) asteroidTex.dispose();
        if (anomalyTex != null) anomalyTex.dispose();
    }
}
