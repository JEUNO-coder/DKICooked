package io.github.DKICooked.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.audio.Music;

public class SoundPlayer {

    private Sound jumpSound;
    private Sound deathSound;
    private Sound splatSound;

    private Music backgroundMusic;

    public SoundPlayer() {
        // Load short sound effects
        jumpSound = Gdx.audio.newSound(Gdx.files.internal("sounds/jump.wav"));

        //deathSound = Gdx.audio.newSound(Gdx.files.internal("sounds/death.wav"));
        //splatSound = Gdx.audio.newSound(Gdx.files.internal("sounds/splat.wav"));

        // Load background music (mp3 recommended)
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/music.wav"));
        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);
    }

    public void playJump() {
        jumpSound.play(1.0f);
    }

    public void playDeath() {
        deathSound.play(1.0f);
    }

    public void playSplat() {
        splatSound.play(1.0f);
    }

    public void playMusic() {
        backgroundMusic.play();
    }

    public void stopMusic() {
        backgroundMusic.stop();
    }

    public void dispose() {
        jumpSound.dispose();
        deathSound.dispose();
        splatSound.dispose();
        backgroundMusic.dispose();
    }
}
