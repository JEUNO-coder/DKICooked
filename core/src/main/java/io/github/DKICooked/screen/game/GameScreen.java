package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.DKICooked.Main;
import io.github.DKICooked.entities.Platform;
import io.github.DKICooked.entities.PlayerActor;
import io.github.DKICooked.entities.PlayerSprite;
import io.github.DKICooked.gameLogic.WorldManager;
import io.github.DKICooked.render.DebugRenderer;
import io.github.DKICooked.screen.BaseScreen;
import io.github.DKICooked.screen.main.MainMenuScreen;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import io.github.DKICooked.audio.SoundPlayer;
import io.github.DKICooked.audio.SoundPlayer;

public class GameScreen extends BaseScreen {

    private static final float SCREEN_WIDTH = 800f;
    private static final float SCREEN_HEIGHT = 600f;

    private final Main main;
    private final WorldManager world;
    private final PlayerActor player;
    private final PlayerSprite sprite;

    private SoundPlayer soundPlayer;

    // THE SECOND STAGE
    private final Stage uiStage;

    private int highestChunkReached = 0;
    private boolean paused = false;
    private PausedScreen pauseOverlay;

    private Label scoreLabel;
    private StringBuilder scoreBuilder = new StringBuilder();
    private int recordHeight = 0;

    public GameScreen(Main main) {
        this.main = main;

        soundPlayer = new SoundPlayer();
        soundPlayer.playMusic();

        // 1. Initialize UI Stage with a FIXED viewport
        // This stage never moves, so 0,0 is always bottom-left of screen
        this.uiStage = new Stage(new FitViewport(SCREEN_WIDTH, SCREEN_HEIGHT));

        // 2. Setup World
        this.world = new WorldManager();
        player = new PlayerActor(soundPlayer);
        player.setSize(40, 60);
        player.setPosition(400, 150);
        player.setPlatforms(world.getActivePlatforms());
        stage.addActor(player);
        sprite = new PlayerSprite(player);

        // 3. Setup Input (Allowing both stages to detect clicks)
        InputMultiplexer multiplexer = new InputMultiplexer();
        multiplexer.addProcessor(uiStage); // UI gets first dibs on clicks
        multiplexer.addProcessor(stage);
        Gdx.input.setInputProcessor(multiplexer);

        // 4. Initialize Components
        setupUI();
        snapCamera(0);
    }

    private void updateLogic(float delta) {
        world.update(player.getY());
        player.setPlatforms(world.getActivePlatforms());

        world.update(player.getY());
        player.setPlatforms(world.getActivePlatforms());

        int currentChunk = world.getCurrentChunk();
        if (currentChunk > highestChunkReached) highestChunkReached = currentChunk;

        // Check if camera needs to snap to player's current chunk
        float cameraTargetY = currentChunk * 600f + 300f;
        if (stage.getCamera().position.y != cameraTargetY) {
            snapCamera(currentChunk);
        }

        if (highestChunkReached > 0 && currentChunk < highestChunkReached - 1) {
            Gdx.app.postRunnable(() -> main.setScreen(new MainMenuScreen(main)));
        }

        int currentHeight = (int) (player.getY() / 100f);
        if (currentHeight > recordHeight) {
            recordHeight = currentHeight;
        }

        scoreBuilder.setLength(0);
        scoreBuilder.append("Best: ").append(recordHeight).append("m");
        scoreLabel.setText(scoreBuilder);

        stage.act(delta);
    }

    private void snapCamera(int chunkIndex) {
        float newY = chunkIndex * 600f;
        OrthographicCamera cam = (OrthographicCamera) stage.getCamera();
        cam.position.set(SCREEN_WIDTH / 2, newY + SCREEN_HEIGHT / 2, 0);
        cam.update();

        // NOTICE: We NO LONGER move uiTable or pauseOverlay here.
        // They are on uiStage, which doesn't move!
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f);

        if (!paused && !Gdx.input.isKeyPressed(Input.Keys.ESCAPE)) {
            updateLogic(delta);
        }

        // Act the UI stage (for button animations/logic)
        uiStage.act(delta);

        // --- DRAWING ---
        // 1. Platforms (World)
        DebugRenderer.begin(stage.getCamera());
        DebugRenderer.renderer.setColor(Color.RED);
        for (Platform p : world.getActivePlatforms()) p.draw(DebugRenderer.renderer);
        DebugRenderer.end();

        // 2. Player Actor (World)
        stage.draw();

        // 3. Player Sprite (World)
        var batch = stage.getBatch();
        batch.setProjectionMatrix(stage.getCamera().combined);
        batch.begin();
        sprite.draw(batch, player);
        batch.end();

        // 4. UI Layer (Fixed)
        // We use uiStage's camera, which is always at 0,0
        uiStage.draw();
    }

    public void setupUI() {
        // 1. Create a Label Style (using a basic font)
        Label.LabelStyle labelStyle = new Label.LabelStyle();
        labelStyle.font = new BitmapFont(); // Default LibGDX font
        labelStyle.fontColor = Color.WHITE;

        scoreLabel = new Label("Height: 0m", labelStyle);
        scoreLabel.setFontScale(1.5f); // Make it a bit bigger

        // 2. Add to your existing Table or a new one
        Table scoreTable = new Table();
        scoreTable.setFillParent(true);
        scoreTable.top().left().pad(20); // Put it in the top left
        scoreTable.add(scoreLabel);

        uiStage.addActor(scoreTable);

        Texture pauseTex = new Texture(Gdx.files.internal("Pause.png"));
        ImageButton pauseButton = new ImageButton(new TextureRegionDrawable(new TextureRegion(pauseTex)));

        Table uiTable = new Table();
        uiTable.setFillParent(true);
        uiTable.top().right();
        uiTable.add(pauseButton).size(40, 40).pad(10);

        // Add to uiStage instead of stage
        uiStage.addActor(uiTable);

        pauseOverlay = new PausedScreen(() -> {
            paused = false;
            pauseOverlay.toggle(false);
        }, main);

        // Add to uiStage
        uiStage.addActor(pauseOverlay);

        pauseButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                paused = !paused;
                pauseOverlay.toggle(paused);
            }
        });
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        uiStage.dispose();
        // BaseScreen usually handles 'stage'
    }
}
