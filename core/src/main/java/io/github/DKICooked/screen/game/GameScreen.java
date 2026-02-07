package io.github.DKICooked.screen.game;

import com.badlogic.gdx.scenes.scene2d.Actor;
import io.github.DKICooked.screen.BaseScreen;

public class GameScreen extends BaseScreen {

    public GameScreen() {
        super(); // calls BaseScreen constructor
        setupGame();
    }

    private void setupGame() {
        // temporary test actor
        Actor testActor = new Actor();
        testActor.setBounds(100, 100, 50, 50);

        stage.addActor(testActor);
    }
}
