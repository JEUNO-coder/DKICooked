package io.github.DKICooked.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import io.github.DKICooked.Main;
import io.github.DKICooked.audio.SoundPlayer;
import io.github.DKICooked.screen.BaseScreen;
import io.github.DKICooked.screen.main.MainMenuScreen;

public class SettingsScreen extends BaseScreen {
    private final Main main;
    private final Stage uiStage;
    private BitmapFont font;
    private Skin sliderSkin;;

    // Textures for disposal (Memory Optimization)
    private Texture backTex;
    private Texture knobTex;

    public SettingsScreen(Main main) {
        this.main = main;

        this.uiStage = new Stage(new FitViewport(800, 600)); // Standard project scale
        Gdx.input.setInputProcessor(uiStage);

        setupFont();
        setupSliderSkin();
        setupUI();
    }



    private void setupFont() {
        FreeTypeFontGenerator generator = new FreeTypeFontGenerator(Gdx.files.internal("new_font.ttf")); //
        FreeTypeFontGenerator.FreeTypeFontParameter parameter = new FreeTypeFontGenerator.FreeTypeFontParameter();
        parameter.size = 26;
        parameter.color = Color.WHITE;
        font = generator.generateFont(parameter);
        generator.dispose(); //
    }

    private void setupSliderSkin() {
        sliderSkin = new Skin();

        // Load your pixel art assets
        backTex = new Texture(Gdx.files.internal("slider_back.png"));
        knobTex = new Texture(Gdx.files.internal("slider_knob.png"));

        sliderSkin.add("background", backTex);
        sliderSkin.add("knob", knobTex);

        // Build the style based on your image
        Slider.SliderStyle style = new Slider.SliderStyle();
        style.background = sliderSkin.newDrawable("background");
        style.knob = sliderSkin.newDrawable("knob");

        // Adjust these to match the scale of your pixel art
        style.background.setMinHeight(40); // Matches your new 960x540 Canva height
        style.knob.setMinHeight(40);
        style.knob.setMinWidth(40);

        sliderSkin.add("default-horizontal", style);
    }

    private void setupUI() {
        Table table = new Table();
        table.setFillParent(true); //

        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        Texture set = new Texture(Gdx.files.internal("set.png"));

        Image setImg = new Image(set);
        // MUSIC SLIDER

        table.center();
        Label musicLabel = new Label("BGM VOLUME", labelStyle);
        final Slider musicSlider = new Slider(0f, 1f, 0.05f, false, sliderSkin);
        musicSlider.setValue(SoundPlayer.bgmVolume);
        musicSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                SoundPlayer.bgmVolume = musicSlider.getValue();
                main.soundPlayer.updateVolume();
            }
        });

        // SFX SLIDER
        Label sfxLabel = new Label("SFX VOLUME", labelStyle);
        final Slider sfxSlider = new Slider(0f, 1f, 0.05f, false, sliderSkin);
        sfxSlider.setValue(SoundPlayer.sfxVolume);
        sfxSlider.addListener(new ChangeListener() {
            @Override
            public void changed(ChangeEvent event, com.badlogic.gdx.scenes.scene2d.Actor actor) {
                SoundPlayer.sfxVolume = sfxSlider.getValue();
            }
        });

        // BACK BUTTON
        TextButton.TextButtonStyle btnStyle = new TextButton.TextButtonStyle();
        btnStyle.font = font;
        btnStyle.fontColor = Color.valueOf("f8c72c"); //
        btnStyle.overFontColor = Color.valueOf("#ef901f");
        TextButton backBtn = new TextButton("BACK", btnStyle);
        backBtn.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                main.setScreen(new MainMenuScreen(main));
            }
        });

        // Add to layout
        table.add(setImg).size(300, 60).padBottom(40).row();
        table.add(musicLabel).padBottom(10).row();
        table.add(musicSlider).width(200).height(40).padBottom(30).row();
        table.add(sfxLabel).padBottom(10).row();
        table.add(sfxSlider).width(200).padBottom(50).row();
        table.add(backBtn).size(250, 60);

        uiStage.addActor(table);
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.05f, 0.05f, 0.08f, 1f); //
        uiStage.act(delta);
        uiStage.draw();
    }

    @Override
    public void dispose() {
        // SYSTEM RECONFIGURATION: Manual resource cleanup
        uiStage.dispose();
        if (font != null) font.dispose();
        if (sliderSkin != null) sliderSkin.dispose();
        if (backTex != null) backTex.dispose();
        if (knobTex != null) knobTex.dispose();
    }
}
