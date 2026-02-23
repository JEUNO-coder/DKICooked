package io.github.DKICooked.screen.game;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Scaling;
import io.github.DKICooked.Main;
import io.github.DKICooked.screen.main.MainMenuScreen;

public class PausedScreen extends Table {
    private Image pausedImage;
    private Image bg;
    private Image resume;
    private Image exit;
    private final Main main;

    public PausedScreen(Runnable onResume, Main main) {
        this.main = main;
        setFillParent(true);

        //For bg
        Pixmap pixmap = new Pixmap(1, 1, Pixmap.Format.RGB888);
        pixmap.setColor(Color.BLACK);
        pixmap.fill();

        Texture greyBg = new Texture(pixmap);
        pixmap.dispose();

        bg = new Image(greyBg);
        bg.setColor(new Color(0.8f, 0.8f, 0.8f, 0.5f));
        bg.setFillParent(true);
        addActor(bg);

        Texture pausedLabel = new Texture(Gdx.files.internal("Paused.png"));
        pausedImage = new Image(pausedLabel);

        center();
        add(pausedImage).size(200, 200);
        pausedImage.setScaling(Scaling.fit);

        pausedImage.addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.moveBy(0, 10, 0.7f),
                    Actions.moveBy(0, -10, 0.7f)
                )
            )
        );

        Texture resLabel = new Texture(Gdx.files.internal("resume.png"));
        resume = new Image(resLabel);

        row();
        center();
        add(resume).size(100,100);
        resume.setScaling(Scaling.fit);

        resume.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               onResume.run();
           }
        });

        Texture exitLabel = new Texture(Gdx.files.internal("exit.png"));
        exit = new Image(exitLabel);

        row();
        center();
        add(exit).size(50, 50);
        exit.setScaling(Scaling.fit);

        exit.addListener(new ClickListener() {
           @Override
           public void clicked(InputEvent event, float x, float y) {
               main.setScreen(new MainMenuScreen(main));
           }
        });
        setVisible(false);
    }

    public void toggle(boolean show) {
        setVisible(show);
    }
}
