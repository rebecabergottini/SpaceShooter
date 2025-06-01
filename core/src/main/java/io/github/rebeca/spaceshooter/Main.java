package io.github.rebeca.spaceshooter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends Game {
    public Music background;
    public Music gameOverMusic;
    public Sound laserSound;


    @Override
    public void create() {
        // Carga los archivos de sonido
        background = Gdx.audio.newMusic(Gdx.files.internal("sounds/backgroundMusic.ogg"));
        gameOverMusic = Gdx.audio.newMusic(Gdx.files.internal("sounds/gameover.mp3"));
        laserSound = Gdx.audio.newSound(Gdx.files.internal("sounds/laser.ogg"));

        // Configuramos musica de fondo
        background.setLooping(true); // true para que se repita continuamente
        gameOverMusic.setLooping(false); // false para que la musica de gameover no se repita

        // Se ajusta el volumen de la música de fondo y game over a la mitad
        background.setVolume(0.5f);
        gameOverMusic.setVolume(0.5f);

        // Establece la pantalla inicial del juego (pantalla de título)
        setScreen(new TitleScreen(this));
    }

    @Override
    public void dispose() {
        // Libera los recursos de musica y sonido cuando se destruye el juego
        background.dispose();
        gameOverMusic.dispose();
        laserSound.dispose();
        super.dispose();
    }

    // Metodo para detener toda la musica que se este reproduciendo para evitar que suenen simultaneamente
    public void stopAllMusic() {
        if (background.isPlaying()) background.stop();
        if (gameOverMusic.isPlaying()) gameOverMusic.stop();
    }
}
