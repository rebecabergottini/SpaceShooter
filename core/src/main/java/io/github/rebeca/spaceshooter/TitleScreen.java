package io.github.rebeca.spaceshooter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.viewport.ScreenViewport;


public class TitleScreen implements Screen {

    private final Game game;
    private final Stage stage;
    private Texture backgroundTex;
    private BitmapFont titleFont;
    private BitmapFont buttonFont;

    public TitleScreen(Game game) {
        this.game = game;
        this.stage = new Stage(new ScreenViewport());
    }

    @Override
    public void show() {
        Gdx.input.setInputProcessor(stage);

        //Musica
        Main main = (Main) game;

        // Detener cualquier música que esté sonando (menos la de background)
        main.stopAllMusic();

        // Reproducir la música de fondo solo si no está sonando ya
        if (!main.background.isPlaying()) {
            main.background.play();
        }

        // Load fonts
        titleFont = new BitmapFont(Gdx.files.internal("fonts/audiowide.fnt"));
        titleFont.getData().setScale(1.1f); // Large size for title
        titleFont.setColor(Color.GOLD);

        buttonFont = new BitmapFont(Gdx.files.internal("fonts/audiowide.fnt"));
        buttonFont.getData().setScale(0.9f); // Medium size for buttons
        buttonFont.setColor(Color.WHITE);

        // Load background texture
        backgroundTex = new Texture(Gdx.files.internal("ui/MainMenu/background.png"));

        // Background image
        Image background = new Image(backgroundTex);
        background.setSize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        background.setPosition(0, 0);

        // Game title label
        Label titleLabel = new Label("SPACE SHOOTER", new Label.LabelStyle(titleFont, Color.WHITE));
        titleLabel.setAlignment(Align.center);
        titleLabel.setPosition(
            Gdx.graphics.getWidth() / 2f - titleLabel.getWidth() / 2f,
            Gdx.graphics.getHeight() * 0.6f
        );

        // Start button label
        Label startLabel = new Label("START", new Label.LabelStyle(buttonFont, Color.WHITE));
        startLabel.setAlignment(Align.center);
        startLabel.setPosition(
            Gdx.graphics.getWidth() / 2f - startLabel.getWidth() / 2f,
            Gdx.graphics.getHeight() * 0.4f
        );
        startLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                game.setScreen(new GameScreen(game, 0));
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                startLabel.setColor(Color.YELLOW);
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                startLabel.setColor(Color.WHITE);
            }
        });

        // Exit button label
        Label exitLabel = new Label("EXIT", new Label.LabelStyle(buttonFont, Color.WHITE));
        exitLabel.setAlignment(Align.center);
        exitLabel.setPosition(
            Gdx.graphics.getWidth() / 2f - exitLabel.getWidth() / 2f,
            Gdx.graphics.getHeight() * 0.3f
        );
        exitLabel.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                Gdx.app.exit();
            }

            @Override
            public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
                exitLabel.setColor(Color.RED); // Highlight on hover
            }

            @Override
            public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
                exitLabel.setColor(Color.WHITE); // Return to normal
            }
        });

        // Add actors to stage (order matters - background first)
        stage.addActor(background);
        stage.addActor(titleLabel);
        stage.addActor(startLabel);
        stage.addActor(exitLabel);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        stage.act(delta);
        stage.draw();
    }

    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {
        Gdx.input.setInputProcessor(null);
    }

    @Override
    public void dispose() {
        stage.dispose();
        backgroundTex.dispose();
        titleFont.dispose();
        buttonFont.dispose();
    }
}
