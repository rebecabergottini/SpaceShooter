package io.github.rebeca.spaceshooter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class LevelTransitionScreen implements Screen {
    private final Game game;

    // Fondo de la pantalla de transicion
    private Texture background;

    // Numero del nivel al que se transiciona
    private final int levelNumber;

    // Escenario para mostrar elementos visuales
    private Stage stage;

    // Fuente para mostrar el texto
    private BitmapFont font;

    // Temporizador para controlar duracion de la pantalla
    private float timer = 0f;

    // Tiempo que dura la transicion en segundos
    private final float displayTime = 3f;

    // Constructor que recibe el juego y el numero de nivel
    public LevelTransitionScreen(Game game, int levelNumber) {
        this.game = game;
        this.levelNumber = levelNumber;
    }

    @Override
    public void show() {
        // Crear el escenario con un viewport que se adapta a la pantalla
        stage = new Stage(new ScreenViewport());

        // Desactivar la entrada del usuario durante la transicion
        Gdx.input.setInputProcessor(null);

        // Cargar la imagen de fondo
        background = new Texture(Gdx.files.internal("ui/MainMenu/background.png"));

        // Cargar la fuente y ajustar su tamano
        font = new BitmapFont(Gdx.files.internal("fonts/audiowide.fnt"));
        font.getData().setScale(1f); // Puedes cambiar este valor para aumentar o reducir el tamano
    }

    @Override
    public void render(float delta) {
        // Sumar el tiempo transcurrido
        timer += delta;

        // Limpiar la pantalla con color negro
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Comenzar a dibujar
        stage.getBatch().begin();

        // Dibujar la imagen de fondo ocupando toda la pantalla
        stage.getBatch().draw(background, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

        // Texto centrado con el numero de nivel
        String text = "Level " + (levelNumber + 1);
        font.setColor(Color.WHITE);
        font.draw(stage.getBatch(), text,
            Gdx.graphics.getWidth() / 2f,
            Gdx.graphics.getHeight() / 2f,
            0, Align.center, false);

        // Terminar de dibujar
        stage.getBatch().end();

        // Si el tiempo ha pasado, cambiar a la pantalla del juego
        if (timer >= displayTime) {
            game.setScreen(new GameScreen(game, levelNumber));
            dispose();
        }
    }

    // Ajustar el viewport cuando cambia el tamano de la pantalla
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    // Liberar los recursos utilizados
    @Override
    public void dispose() {
        stage.dispose();
        font.dispose();
        background.dispose();
    }
}
