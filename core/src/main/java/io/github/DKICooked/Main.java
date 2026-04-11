package io.github.DKICooked;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;
import io.github.DKICooked.screen.main.IntroScreen;
import io.github.DKICooked.screen.main.MainMenuScreen;
import io.github.DKICooked.screen.main.SplashScreen;


/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;
    private Texture image;
    private Screen currentScreen;


    @Override
    public void create() {
        batch = new SpriteBatch();

        setScreen(new IntroScreen(this));
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.15f, 0.15f, 0.2f, 1f);

        float delta = Gdx.graphics.getDeltaTime();

        if (currentScreen != null) {
            currentScreen.render(delta);
        }
    }

    @Override
    public void resize(int width, int height) {
        currentScreen.resize(width, height);
    }

    @Override
    public void dispose() {
        batch.dispose();
        image.dispose();
    }

    public void setScreen(Screen newScreen) {
        if (currentScreen != null) {
            currentScreen.hide(); // Best practice to call hide
            currentScreen.dispose();
        }
        currentScreen = newScreen;
        if (currentScreen != null) {
            currentScreen.show(); // Best practice to call show
            currentScreen.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        }
    }
}
