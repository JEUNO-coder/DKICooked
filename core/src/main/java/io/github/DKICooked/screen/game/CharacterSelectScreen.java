package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.NinePatchDrawable;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.DKICooked.Main;
import io.github.DKICooked.screen.BaseScreen;
import io.github.DKICooked.screen.main.MainMenuScreen;

public class CharacterSelectScreen extends BaseScreen {
    private final Main main;
    private final Stage uiStage;
    private final NinePatchDrawable cardDrawable;
    private Texture headerTex;
    private BitmapFont customFont;

    // Define your custom hex color
    private final Color yellowColor = Color.valueOf("f8c72c");
    private final Color goldColor = Color.valueOf("#ef901f");

    public CharacterSelectScreen(Main main) {
        this.main = main;
        this.uiStage = new Stage(new FitViewport(800, 600));
        Gdx.input.setInputProcessor(uiStage);

        createFonts();

        this.cardDrawable = new NinePatchDrawable(createRoundedNinePatch(12, Color.WHITE));

        Table mainTable = new Table();
        mainTable.setFillParent(true);

        // 1. LOAD YOUR HEADER IMAGE
        // Replace "select_header.png" with your actual filename
        headerTex = new Texture(Gdx.files.internal("select.png"));
        Image titleImage = new Image(headerTex);

        titleImage.addAction(
            Actions.forever(
                Actions.sequence(
                    // Move UP 10 pixels over 0.7 seconds
                    Actions.moveBy(0, 10, 0.7f),
                    // Move DOWN 10 pixels over 0.7 seconds
                    Actions.moveBy(0, -10, 0.7f)
                )
            )
        );

        // 2. CREATE THE CARDS
        // Inside CharacterSelectScreen constructor:
        ImageButton alaineCard = createCharacterCard("Alaine", "Low gravity: Floats longer in the air", "aidle.png");
        ImageButton jerickCard = createCharacterCard("Jerick", "Ninja: Can jump again while in mid-air", "jidle.png");
        ImageButton timothyCard = createCharacterCard("Timothy", "Power: Charge for a massive high jump", "tidle.png");

        // ... (Keep your listeners the same) ...
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
        // 3. UPDATED LAYOUT
        // Add the image to the table instead of the label
        // Adjust size(width, height) to match your image's dimensions
        mainTable.add(titleImage).colspan(3).size(500, 155).padBottom(40).row();

        mainTable.add(alaineCard).pad(15);
        mainTable.add(jerickCard).pad(15);
        mainTable.add(timothyCard).pad(15);

        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = customFont;
        btnStyle.fontColor = yellowColor;
        btnStyle.overFontColor = goldColor;

        TextButton backBtn = new TextButton("BACK TO MENU", btnStyle);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new MainMenuScreen(main));
            }
        });

        mainTable.row().colspan(3).padTop(30);
        mainTable.add(backBtn);
        uiStage.addActor(mainTable);
    }

    private void createFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("new_font.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();

        parameter.size = 20;
        parameter.color = Color.WHITE;

        customFont = generator.generateFont(parameter);
        customFont.getRegion().getTexture().setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        generator.dispose();
    }

    private ImageButton createCharacterCard(String name, String description, String internalPath) {
        Texture charTex = new Texture(Gdx.files.internal(internalPath));
        Image characterImg = new Image(charTex);

        Label.LabelStyle labelStyle = new Label.LabelStyle(customFont, Color.WHITE);
        Label nameLabel = new Label(name, labelStyle);

        Label descLabel = new Label(description, labelStyle);
        descLabel.setColor(new Color(goldColor.r, goldColor.g, goldColor.b, 0f));
        descLabel.setFontScale(0.5f);
        descLabel.setWrap(true);
        descLabel.setAlignment(com.badlogic.gdx.utils.Align.center);
        // CRITICAL: Hide the label entirely so it takes 0 space initially
        descLabel.setVisible(false);

        Table borderTable = new Table();
        borderTable.setBackground(cardDrawable.tint(yellowColor));

        Table innerTable = new Table();
        innerTable.setBackground(cardDrawable.tint(Color.BLACK));

        innerTable.add(characterImg).size(120, 140).pad(10).row();
        innerTable.add(nameLabel).padBottom(5).row();

        // Add the description with NO fixed height.
        // We use 'managed(false)' or just toggle 'visible' and call pack()
        innerTable.add(descLabel).width(140).pad(0, 10, 0, 10);

        borderTable.add(innerTable).pad(2);

        ImageButton.ImageButtonStyle style = new ImageButton.ImageButtonStyle();
        ImageButton cardButton = new ImageButton(style);
        cardButton.add(borderTable);
        cardButton.setTransform(true);

        cardButton.addListener(new ClickListener() {
            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                cardButton.setScale(1.05f);
                borderTable.setBackground(cardDrawable.tint(Color.WHITE));
                nameLabel.setColor(yellowColor);

                // 1. Make visible and show color
                descLabel.setVisible(true);
                descLabel.setColor(new Color(goldColor.r, goldColor.g, goldColor.b, 1f));

                // 2. Force the table to recalculate its size to "expand"
                innerTable.invalidateHierarchy();
                borderTable.invalidateHierarchy();
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                if (pointer == -1) {
                    cardButton.setScale(1f);
                    borderTable.setBackground(cardDrawable.tint(yellowColor));
                    nameLabel.setColor(Color.WHITE);

                    // 1. Hide it so it takes up 0 space again
                    descLabel.setVisible(false);
                    descLabel.setColor(new Color(goldColor.r, goldColor.g, goldColor.b, 0f));

                    // 2. Force the table to "shrink" back down
                    innerTable.invalidateHierarchy();
                    borderTable.invalidateHierarchy();
                }
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
    public void show() {
        Gdx.input.setInputProcessor(uiStage);
    }

    @Override
    public void resize(int width, int height) {
        uiStage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        uiStage.dispose();
        if (customFont != null) customFont.dispose();
        if (cardDrawable.getPatch().getTexture() != null) {
            cardDrawable.getPatch().getTexture().dispose();
        }
    }
}


