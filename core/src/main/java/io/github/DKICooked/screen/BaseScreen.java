package io.github.DKICooked.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.utils.viewport.FitViewport;

public abstract class BaseScreen implements Screen {
    protected Stage stage;
    // protected Skin skin;


    public BaseScreen() {
        stage = new Stage(new FitViewport(800, 600));
        Gdx.input.setInputProcessor(stage);
        //skin = new Skin(Gdx.files.internal("uiskin.json"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f); // RGBA (0,0,0,1) = black
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void dispose() {
        stage.dispose();
       // skin.dispose();
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);
    }
    @Override public void hide() {}
    @Override public void pause() {}
    @Override public void resume() {}
}
