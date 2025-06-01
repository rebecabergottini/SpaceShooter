package io.github.rebeca.spaceshooter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class GameOverScreen implements Screen {
    private final Game game; // Referencia al juego principal
    private Stage stage; // Escenario donde se colocan los elementos
    private Texture backgroundTex; // Textura de fondo
    private BitmapFont titleFont;  // Fuente para el titulo
    private BitmapFont statsFont;  // Fuente para estadisticas
    private BitmapFont buttonFont; // Fuente para botones

    // Estadisticas del juego
    private final boolean isVictory;      // Indica si fue una victoria
    private final float timeElapsed;      // Tiempo que sobrevivio el jugador
    private final int obstaclesPassed;    // Obstaculos que evadio el jugador
    private final int enemiesDefeated;    // Enemigos derrotados

    // Numero de nivel alcanzado
    private final int levelNumber;

    // Constructor
    public GameOverScreen(Game game, boolean isVictory, int levelNumber, float timeElapsed, int obstaclesPassed, int enemiesDefeated) {
        this.game = game;
        this.isVictory = isVictory;
        this.levelNumber = levelNumber;
        this.timeElapsed = timeElapsed;
        this.obstaclesPassed = obstaclesPassed;
        this.enemiesDefeated = enemiesDefeated;
    }

    @Override
    public void show() {
        stage = new Stage(new ScreenViewport());
        Gdx.input.setInputProcessor(stage); // Establece el escenario como receptor de eventos

        // Reproduce la musica de game over
        Main main = (Main) game;
        main.stopAllMusic(); // Detiene otras musicas

        if (!main.gameOverMusic.isPlaying()) {
            main.gameOverMusic.play();
        }

        // Carga las fuentes de texto
        titleFont = new BitmapFont(Gdx.files.internal("fonts/audiowide.fnt"));
        titleFont.getData().setScale(1f);

        statsFont = new BitmapFont(Gdx.files.internal("fonts/audiowide.fnt"));
        statsFont.getData().setScale(0.6f);

        buttonFont = new BitmapFont(Gdx.files.internal("fonts/audiowide.fnt"));
        buttonFont.getData().setScale(0.8f);

        // Carga la imagen de fondo
        backgroundTex = new Texture("ui/MainMenu/background.png");

        // Agrega el fondo al escenario
        Image background = new Image(backgroundTex);
        background.setFillParent(true);
        stage.addActor(background);

        // Determina el titulo a mostrar
        String titleText = isVictory ? "VICTORY!" : "GAME OVER";
        Color titleColor = isVictory ? Color.GOLD : Color.RED;

        // Muestra el titulo
        Label titleLabel = new Label(titleText, new Label.LabelStyle(titleFont, titleColor));
        titleLabel.setAlignment(Align.center);
        titleLabel.setPosition(
            Gdx.graphics.getWidth()/2f - titleLabel.getWidth()/2,
            Gdx.graphics.getHeight() * 0.7f
        );
        stage.addActor(titleLabel);

        // Posicion inicial para las estadisticas
        float yPos = Gdx.graphics.getHeight() * 0.55f;
        float yStep = 40f;

        // Muestra las estadisticas del jugador
        addStatLabel("Level Reached: " + levelNumber, yPos);
        yPos -= yStep;

        addStatLabel("Time: " + (int)timeElapsed + " seconds", yPos);
        yPos -= yStep;

        addStatLabel("Obstacles: " + obstaclesPassed, yPos);
        yPos -= yStep;

        addStatLabel("Enemies: " + enemiesDefeated, yPos);
        yPos -= yStep * 1.5f;

        // Boton para reintentar el juego
        Label retryLabel = new Label("RETRY", new Label.LabelStyle(buttonFont, Color.WHITE));
        retryLabel.setAlignment(Align.center);
        retryLabel.setPosition(
            Gdx.graphics.getWidth()/2f - retryLabel.getWidth()/2,
            yPos - 25
        );
        retryLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, 0)); // Reinicia el juego en el nivel 0
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                retryLabel.setColor(Color.GREEN); // Cambia color al pasar el mouse
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                retryLabel.setColor(Color.WHITE); // Restaura el color
            }
        });
        stage.addActor(retryLabel);

        // Boton para volver al menu principal
        Label menuLabel = new Label("MAIN MENU", new Label.LabelStyle(buttonFont, Color.WHITE));
        menuLabel.setAlignment(Align.center);
        menuLabel.setPosition(
            Gdx.graphics.getWidth()/2f - menuLabel.getWidth()/2,
            yPos - yStep - 30
        );
        menuLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new TitleScreen(game)); // Regresa al menu principal
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                menuLabel.setColor(Color.YELLOW); // Resalta el texto
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                menuLabel.setColor(Color.WHITE); // Restaura el color
            }
        });
        stage.addActor(menuLabel);
    }

    // Metodo auxiliar para agregar etiquetas de estadisticas
    private void addStatLabel(String text, float y) {
        Label label = new Label(text, new Label.LabelStyle(statsFont, Color.WHITE));
        label.setAlignment(Align.center);
        label.setPosition(
            Gdx.graphics.getWidth()/2f - label.getWidth()/2,
            y
        );
        stage.addActor(label);
    }

    @Override
    public void render(float delta) {
        // Limpia la pantalla y dibuja el escenario
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void dispose() {
        // Libera los recursos usados
        stage.dispose();
        backgroundTex.dispose();
        titleFont.dispose();
        statsFont.dispose();
        buttonFont.dispose();
    }

    // Otros metodos requeridos por la interfaz Screen
    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
}
