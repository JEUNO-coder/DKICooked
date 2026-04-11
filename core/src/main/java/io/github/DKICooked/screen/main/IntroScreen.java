package io.github.DKICooked.screen.main;

import io.github.DKICooked.screen.BaseScreen;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.DKICooked.Main;

public class IntroScreen extends BaseScreen {
    private final Main main;
    private BitmapFont font;

    public IntroScreen(Main main) {
        this.main = main;

        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("new_font.ttf"));
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 32;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);
        generator.dispose();

        Label cookedLabel = new Label("COOKED PRESENTS...", new Label.LabelStyle(font, Color.WHITE));
        cookedLabel.getColor().a = 0;

        Table table = new Table();
        table.setFillParent(true);
        table.center();
        table.add(cookedLabel);
        stage.addActor(table);

        // Animation logic: Fade in, stay, fade out, then switch screens
        cookedLabel.addAction(Actions.sequence(
            Actions.fadeIn(1.5f),
            Actions.delay(1.5f),
            Actions.fadeOut(1.0f),
            Actions.run(() -> main.setScreen(new SplashScreen(main)))
        ));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        if (font != null) font.dispose();
    }
}
