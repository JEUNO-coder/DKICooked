package io.github.DKICooked.screen.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Container;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import io.github.DKICooked.Main;
import io.github.DKICooked.audio.SoundPlayer;
import io.github.DKICooked.screen.BaseScreen;
import io.github.DKICooked.screen.game.CharacterSelectScreen;
import io.github.DKICooked.screen.game.LeaderboardScreen;
import io.github.DKICooked.screen.SettingsScreen;


public class MainMenuScreen extends BaseScreen {
    private final Main main;
    private final Texture titleText;
    private final Texture subTitleText;
    private BitmapFont menuFont;


    public MainMenuScreen(Main main) {
        super();
        this.main = main;

        main.soundPlayer.playMenuMusic();


        // 1. GENERATE FONT
        createFonts();

        // 2. KEEP TITLES (Your original logic)
        titleText = new Texture(Gdx.files.internal("toyour.png"));
        subTitleText = new Texture(Gdx.files.internal("Infinity.png"));

        Image title = new Image(titleText);
        Image subTitle = new Image(subTitleText);

        title.setScaling(Scaling.fit);
        subTitle.setScaling(Scaling.fit);

        Container<Image> subTitleContainer = new Container<>(subTitle);
        subTitleContainer.setTransform(true);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        // KEEP ORIGINAL TITLE SIZING
        table.top().center();
        table.add(title).width(350).padBottom(-80).row();
        table.add(subTitleContainer).width(500).padBottom(50).row();

        // 3. CREATE TEXT BUTTON STYLE
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = menuFont;
        btnStyle.fontColor = Color.valueOf("f8c72c");
        btnStyle.overFontColor = Color.valueOf("#ef901f"); // Your Gold color
        btnStyle.downFontColor = Color.GRAY;

        // 4. DEFINE BUTTONS
        TextButton startButton = new TextButton("START GAME", btnStyle);
        TextButton lbButton    = new TextButton("LEADERBOARD", btnStyle);
        TextButton settingsButton = new TextButton("SETTINGS", btnStyle);
        TextButton exitButton  = new TextButton("EXIT", btnStyle);

        // 5. ADD TO TABLE (Cleaned up padding to avoid overlapping)
        table.add(startButton).padTop(-50).padBottom(10).row();
        table.add(lbButton).padBottom(10).row();
        table.add(settingsButton).padBottom(10).row();
        table.add(exitButton).padTop(50).padBottom(10).row();

        // 6. KEEP ANIMATION
        subTitle.setOrigin(Align.center);
        subTitle.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0, 10, 0.7f),
            Actions.moveBy(0, -10, 0.7f)
        )));

        // 7. LISTENERS
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.soundPlayer.stopMenuMusic();
                main.setScreen(new CharacterSelectScreen(main));
            }
        });



        lbButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new LeaderboardScreen(main));
            }
        });

        settingsButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // This will trigger your new SettingsScreen
                main.setScreen(new SettingsScreen(main));
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }
        });

        Gdx.input.setInputProcessor(stage);
    }

    private void createFonts() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("new_font.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 26; // Adjust this to match your UI scale
        parameter.borderWidth = 1.5f;
        parameter.borderColor = Color.BLACK;
        parameter.shadowOffsetX = 2;
        parameter.shadowOffsetY = 2;
        parameter.shadowColor = new Color(0, 0, 0, 0.5f);

        menuFont = generator.generateFont(parameter);
        generator.dispose();
    }

    @Override
    public void dispose() {
        super.dispose();
        titleText.dispose();
        subTitleText.dispose();
        if (menuFont != null) menuFont.dispose();
    }
}
