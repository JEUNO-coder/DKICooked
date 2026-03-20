package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.DKICooked.Main;
import io.github.DKICooked.screen.BaseScreen;

public class CharacterSelectScreen extends BaseScreen {
    private final Main main;
    private final Stage uiStage;
    private final NinePatchDrawable cardDrawable;

    // Define your custom hex color
    private final Color goldColor = Color.valueOf("fed546");

    public CharacterSelectScreen(Main main) {
        this.main = main;
        this.uiStage = new Stage(new FitViewport(800, 600));
        Gdx.input.setInputProcessor(uiStage);

        // Generate the rounded NinePatch once to use for all cards
        this.cardDrawable = new NinePatchDrawable(createRoundedNinePatch(12, Color.WHITE));

        Table mainTable = new Table();
        mainTable.setFillParent(true);

        Label.LabelStyle titleStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        Label title = new Label("SELECT YOUR CHARACTER", titleStyle);
        title.setFontScale(2.5f);

        // Create the cards
        ImageButton alaineCard = createCharacterCard("Alaine", "aidle.png");
        ImageButton jerickCard = createCharacterCard("Jerick", "jidle.png");
        ImageButton timothyCard = createCharacterCard("Timothy", "tidle.png");

        // Listeners
        alaineCard.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new GameScreen(main, "Alaine"));
            }
        });
        jerickCard.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new GameScreen(main, "Jerick"));
            }
        });
        timothyCard.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new GameScreen(main, "Timothy"));
            }
        });

        // Layout
        mainTable.add(title).colspan(3).padBottom(60).row();
        mainTable.add(alaineCard).pad(15);
        mainTable.add(jerickCard).pad(15);
        mainTable.add(timothyCard).pad(15);

        uiStage.addActor(mainTable);
    }

    private ImageButton createCharacterCard(String name, String internalPath) {
        // 1. Assets
        Texture charTex = new Texture(Gdx.files.internal(internalPath));
        Image characterImg = new Image(charTex);
        Label.LabelStyle labelStyle = new Label.LabelStyle(new BitmapFont(), Color.WHITE);
        Label nameLabel = new Label(name, labelStyle);

        // 2. The Border (Outer Table)
        Table borderTable = new Table();
        // Set border color to your #fed546 hex color
        borderTable.setBackground(cardDrawable.tint(goldColor));

        // 3. The Content (Inner Table)
        Table innerTable = new Table();
        innerTable.setBackground(cardDrawable.tint(Color.BLACK)); // Pure black background
        innerTable.add(characterImg).size(120, 140).pad(15).row();
        innerTable.add(nameLabel).padBottom(15);

        // Add inner table to outer table with 2px padding to create the 2px border
        borderTable.add(innerTable).pad(2);

        // 4. Button Setup
        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        ImageButton cardButton = new ImageButton(style);
        cardButton.add(borderTable);
        cardButton.setTransform(true);

        // 5. Interaction
        cardButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                cardButton.setScale(1.05f);
                // When hovering, the border turns white to show it's selected
                borderTable.setBackground(cardDrawable.tint(Color.WHITE));
                nameLabel.setColor(goldColor);
            }
            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                cardButton.setScale(1f);
                // When leaving, it goes back to your gold border
                borderTable.setBackground(cardDrawable.tint(goldColor));
                nameLabel.setColor(Color.WHITE);
            }
        });

        return cardButton;
    }

    /**
     * Generates a rounded rectangle NinePatch programmatically.
     */
    private NinePatch createRoundedNinePatch(int size, Color color) {
        Pixmap pixmap = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        pixmap.setColor(color);
        // Fill a circle to create rounded corners
        pixmap.fillCircle(size / 2, size / 2, size / 2);

        // Convert to texture
        Texture texture = new Texture(pixmap);
        pixmap.dispose();

        // Create NinePatch: split at the middle so it stretches correctly
        return new NinePatch(texture, size / 2 - 1, size / 2 - 1, size / 2 - 1, size / 2 - 1);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0f, 0f, 0f, 1f);
        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void resize(int width, int height) {
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        uiStage.dispose();
        if (cardDrawable.getPatch().getTexture() != null) {
            cardDrawable.getPatch().getTexture().dispose();
        }
    }
}
