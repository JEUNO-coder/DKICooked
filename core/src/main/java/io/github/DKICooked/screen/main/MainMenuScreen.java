package io.github.DKICooked.screen.main;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Scaling;
import io.github.DKICooked.Main;
import io.github.DKICooked.screen.BaseScreen;
import io.github.DKICooked.screen.game.GameScreen;

public class MainMenuScreen extends BaseScreen {
    private final Texture startText;
    private final Texture startTextHov;
    private final Texture tutText;
    private final Texture tutTextHov;
    private final Texture setText;
    private final Texture setTextHov;
    private final Texture exitText;
    private final Texture exitTextHov;
    private final Texture titleText;
    private final Texture subTitleText;
    private final Main main;

    public MainMenuScreen(Main main) {
        super();
        this.main = main;
        startText = new Texture(Gdx.files.internal("Start.png"));
        startTextHov = new Texture(Gdx.files.internal("Start_pressed.png"));
        tutText = new Texture(Gdx.files.internal("tutorial.png"));
        tutTextHov = new Texture(Gdx.files.internal("Tutorial_pressed.png"));
        setText = new Texture(Gdx.files.internal("settings.png"));
        setTextHov = new Texture(Gdx.files.internal("Setting_pressed.png"));

        exitText = new Texture(Gdx.files.internal("exit.png"));
        exitTextHov = new Texture(Gdx.files.internal("exit_pressed.png"));

        titleText = new Texture(Gdx.files.internal("DonkeyKong.png"));
        subTitleText = new Texture(Gdx.files.internal("Infinity.png"));

        Image title = new Image(titleText);
        Image subTitle = new Image(subTitleText);

        Table table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        title.setScaling(Scaling.fit);
        subTitle.setScaling(Scaling.fit);

        table.top().center();
        table.add(title).width(Gdx.graphics.getWidth() * 0.37f).padBottom(-133).row();
        table.add(subTitle).width(Gdx.graphics.getWidth() * 0.5f).padBottom(50).row();


        ImageButton.ImageButtonStyle startStyle = new ImageButton.ImageButtonStyle();
        startStyle.imageUp = new TextureRegionDrawable( new TextureRegion(startText));
        startStyle.imageOver = new TextureRegionDrawable( new TextureRegion(startTextHov));
        startStyle.imageDown = new TextureRegionDrawable( new TextureRegion(startTextHov));

        ImageButton startButton = new ImageButton(startStyle);
        startButton.setTouchable(Touchable.enabled);

        ImageButton.ImageButtonStyle tutStyle = new ImageButton.ImageButtonStyle();
        tutStyle.imageUp = new TextureRegionDrawable( new TextureRegion(tutText));
        tutStyle.imageOver = new TextureRegionDrawable( new TextureRegion(tutTextHov));
        tutStyle.imageDown = new TextureRegionDrawable( new TextureRegion(tutTextHov));
        ImageButton tutButton = new ImageButton(tutStyle);

        ImageButton.ImageButtonStyle settStyle = new ImageButton.ImageButtonStyle();
        settStyle.imageUp = new TextureRegionDrawable( new TextureRegion(setText));
        settStyle.imageOver = new TextureRegionDrawable( new TextureRegion(setTextHov));
        settStyle.imageDown = new TextureRegionDrawable( new TextureRegion(setTextHov));
        ImageButton settButton = new ImageButton(settStyle);

        ImageButton.ImageButtonStyle exitStyle = new ImageButton.ImageButtonStyle();
        exitStyle.imageUp = new TextureRegionDrawable( new TextureRegion(exitText));
        exitStyle.imageOver = new TextureRegionDrawable( new TextureRegion(exitTextHov));
        exitStyle.imageDown = new TextureRegionDrawable( new TextureRegion(exitTextHov));
        ImageButton exitButton = new ImageButton(exitStyle);

        startButton.getImage().setScaling(Scaling.fit);
        tutButton.getImage().setScaling(Scaling.fit);
        settButton.getImage().setScaling(Scaling.fit);
        exitButton.getImage().setScaling(Scaling.fit);

        table.add(startButton).width(120).height(25).padTop(-60).center().row();
        table.add(tutButton).width(140).height(50).padTop(-20).center().row();
        table.add(settButton).width(140).height(50).padTop(-14).padBottom(50).center().row();
        table.add(exitButton).width(140).height(30).center().row();

        subTitle.addAction(
            Actions.forever(
                Actions.sequence(
                    Actions.moveBy(0, 10, 0.7f),
                    Actions.moveBy(0, -10, 0.7f)
                )
            )
        );
        startButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new GameScreen(main));
                System.out.println("Start Button was clicked");

            }
        });

        tutButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Tutorial Button was clicked");
            }
        });

        settButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                System.out.println("Setting Button was clicked");
            }
        });

        exitButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
                System.out.println("Exit Button was clicked");
            }
        });
    }

    @Override
    public void dispose() {
        super.dispose();
        startText.dispose();
        startTextHov.dispose();
        tutText.dispose();
        tutTextHov.dispose();
        setText.dispose();
        setTextHov.dispose();
        exitText.dispose();
        exitTextHov.dispose();
        titleText.dispose();
        subTitleText.dispose();
    }
}
