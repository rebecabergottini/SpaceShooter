package io.github.rebeca.spaceshooter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;

public class VictoryScreen implements Screen {
    private final Game game;
    private Stage stage;
    private SpriteBatch batch;

    // Textura de fondo
    private Texture background;

    // Estadisticas del juego al momento de la victoria
    private final float timeElapsed;
    private final int obstaclesPassed;
    private final int enemiesDefeated;

    // Fuentes para los textos
    private BitmapFont victoryFont;
    private BitmapFont statsFont;
    private BitmapFont buttonFont;

    // Numero de nivel alcanzado
    private final int levelNumber;

    // Constructor que recibe el juego y las estadisticas
    public VictoryScreen(Game game, int levelNumber, float timeElapsed, int obstaclesPassed, int enemiesDefeated) {
        this.game = game;
        this.levelNumber = levelNumber;
        this.timeElapsed = timeElapsed;
        this.obstaclesPassed = obstaclesPassed;
        this.enemiesDefeated = enemiesDefeated;
    }

    @Override
    public void show() {
        // Inicializa el escenario y el batch para dibujar
        stage = new Stage(new ScreenViewport());
        batch = new SpriteBatch();
        Gdx.input.setInputProcessor(stage); // Asigna el escenario como receptor de eventos

        // Manejo de musica de fondo
        Main main = (Main) game;
        main.stopAllMusic(); // Detiene toda la musica actual

        if (!main.background.isPlaying()) {
            main.background.play(); // Reproduce musica de fondo si no esta sonando
        }

        // Carga la imagen de fondo
        background = new Texture("ui/MainMenu/background.png");

        // Configura las fuentes con distintos tamaños
        victoryFont = new BitmapFont(Gdx.files.internal("fonts/audiowide.fnt"));
        victoryFont.getData().setScale(1f); // Fuente grande para "VICTORY"

        statsFont = new BitmapFont(Gdx.files.internal("fonts/audiowide.fnt"));
        statsFont.getData().setScale(0.5f); // Fuente mediana para estadisticas

        buttonFont = new BitmapFont(Gdx.files.internal("fonts/audiowide.fnt"));
        buttonFont.getData().setScale(0.6f); // Fuente para boton

        // Agrega la imagen de fondo al escenario
        Image bgImage = new Image(background);
        bgImage.setFillParent(true);
        stage.addActor(bgImage);

        // Posiciones iniciales para colocar los textos
        float yPos = Gdx.graphics.getHeight() * 0.55f;
        float yStep = 50f;

        // Texto de titulo "VICTORY"
        Label victoryLabel = new Label("VICTORY", new Label.LabelStyle(victoryFont, Color.GOLD));
        victoryLabel.setAlignment(Align.center);
        victoryLabel.setPosition(
            Gdx.graphics.getWidth()/2f - victoryLabel.getWidth()/2,
            Gdx.graphics.getHeight() * 0.8f
        );
        stage.addActor(victoryLabel);
        yPos = Gdx.graphics.getHeight() * 0.6f;

        yPos+=30;
        // Nivel alcanzado
        addStatLabel("Level Reached: " + levelNumber, statsFont, Color.WHITE, yPos);
        yPos -= yStep;

        // Estadisticas del juego
        addStatLabel("Time Survived: " + (int)timeElapsed + " seconds", statsFont, Color.WHITE, yPos);
        yPos -= yStep;

        addStatLabel("Obstacles Passed: " + obstaclesPassed, statsFont, Color.WHITE, yPos);
        yPos -= yStep;

        addStatLabel("Enemies Defeated: " + enemiesDefeated, statsFont, Color.WHITE, yPos);
        yPos -= yStep * 0.8f;

        // Boton para volver a jugar desde el principio
        Label playAgainLabel = new Label("PLAY AGAIN", new Label.LabelStyle(buttonFont, Color.WHITE));
        playAgainLabel.setAlignment(Align.center);
        playAgainLabel.setPosition(
            Gdx.graphics.getWidth()/2f - playAgainLabel.getWidth()/2,
            yPos - 40
        );

        // Listener para el boton de volver a jugar
        playAgainLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Reinicia el juego desde el nivel 0
                game.setScreen(new GameScreen(game, 0));
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                playAgainLabel.setColor(Color.GREEN);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                playAgainLabel.setColor(Color.WHITE);
            }
        });
        stage.addActor(playAgainLabel);

        // Boton para volver al menu principal
        Label menuLabel = new Label("MAIN MENU", new Label.LabelStyle(buttonFont, Color.WHITE));
        menuLabel.setAlignment(Align.center);
        menuLabel.setPosition(
            Gdx.graphics.getWidth()/2f - menuLabel.getWidth()/2,
            yPos - 90
        );

        // Listeners para el boton de MainMenu
        menuLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                // Al hacer click, volver al menu principal
                game.setScreen(new TitleScreen(game));
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                // Cambia el color al pasar el mouse por encima
                menuLabel.setColor(Color.YELLOW);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                // Restaura el color original al salir
                menuLabel.setColor(Color.WHITE);
            }
        });
        stage.addActor(menuLabel);
    }

    // Metodo auxiliar para agregar etiquetas de texto de estadisticas
    private void addStatLabel(String text, BitmapFont font, Color color, float y) {
        Label.LabelStyle style = new Label.LabelStyle(font, color);
        Label label = new Label(text, style);
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

        stage.act(delta); // Actualiza logica
        stage.draw();     // Dibuja elementos
    }

    @Override
    public void dispose() {
        // Libera recursos
        stage.dispose();
        batch.dispose();
        background.dispose();
        victoryFont.dispose();
        statsFont.dispose();
        buttonFont.dispose();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() { Gdx.input.setInputProcessor(null); }
}
