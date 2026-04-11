package io.github.DKICooked.screen.main;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.DKICooked.Main;
import io.github.DKICooked.screen.BaseScreen;

public class SplashScreen extends BaseScreen {
    private final Main main;
    private final Texture titleTex;
    private final Texture subTitleTex;

    public SplashScreen(Main main) {
        this.main = main;

        titleTex = new Texture(Gdx.files.internal("toyour.png"));
        subTitleTex = new Texture(Gdx.files.internal("Infinity.png"));

        Image titleImg = new Image(titleTex);
        Image subTitleImg = new Image(subTitleTex);

        titleImg.getColor().a = 0;
        subTitleImg.getColor().a = 0;

        Table table = new Table();
        table.setFillParent(true);
        table.center(); // Everything is perfectly centered now

        table.add(titleImg).width(300).height(55).padBottom(5).row();
        table.add(subTitleImg).width(460).height(115).row();
        stage.addActor(table);

        // Sequential fade in for the logo
        titleImg.addAction(Actions.fadeIn(1f));
        subTitleImg.addAction(Actions.sequence(
            Actions.fadeIn(1f),
            Actions.delay(2.5f),
            Actions.fadeOut(1f),
            Actions.run(() -> main.setScreen(new MainMenuScreen(main)))
        ));

        titleImg.addAction(Actions.sequence(
            Actions.fadeIn(1f),
            Actions.delay(2.5f),
            Actions.fadeOut(1f)
        ));

        // Floating animation
        subTitleImg.addAction(Actions.forever(Actions.sequence(
            Actions.moveBy(0, 10, 0.7f),
            Actions.moveBy(0, -10, 0.7f)
        )));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0, 1); // Black background for that retro intro feel
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        super.dispose();
        titleTex.dispose();
        subTitleTex.dispose();
    }

}

