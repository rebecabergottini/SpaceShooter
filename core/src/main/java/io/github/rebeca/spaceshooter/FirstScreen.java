package io.github.rebeca.spaceshooter;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g3d.attributes.PointLightsAttribute;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;
import com.badlogic.gdx.math.Rectangle;


import org.w3c.dom.Text;

import java.util.Iterator;

/** First screen of the application. Displayed after the application is created. */
public class FirstScreen implements Screen {

    private final Game game;
    private OrthographicCamera camera;
    private SpriteBatch batch;

    private Texture background;
    private Texture playerTexture;
    private Texture laserTexture;
    private Texture explosionTexture;
    private Array<Explosion> explosions = new Array<>();
    private Texture[] enemyTextures;
    private Texture lifeTexture;

    private Rectangle player;
    private Array<Rectangle> lasers;
    private Array<Enemy> enemies;
    private long lastEnemySpawnTime;
    private float backgroundY1 = 0;
    private float backgroundY2;
    private float backgroundSpeed = 100;

    private int playerLives = 3;
    private float blinkTimer = 0;
    private boolean isBlinking = false;
    private float blinkDuration = 0.5f;
    private float blinkInterval = 0.1f;

    private Sound laserSound;
    private Music backgroundMusic;

    public FirstScreen(Game game) {
        this.game = game;
    }

    @Override
    public void show() {
        // Prepare your screen here.
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);

        batch = new SpriteBatch();

        background = new Texture("assets/backgrounds/background.png");
        playerTexture = new Texture("assets/spaceships/player.png");
        laserTexture = new Texture("assets/lasers/laserGreen10.png");
        explosionTexture = new Texture("assets/lasers/laserGreenExplosion.png");

        enemyTextures = new Texture[] {
            new Texture("assets/spaceships/enemyBlack1.png"),
            new Texture("assets/spaceships/enemyBlack2.png"),
            new Texture("assets/spaceships/enemyBlack3.png")
        };

        lifeTexture = new Texture("assets/life/playerLife.png");
        laserSound = Gdx.audio.newSound(Gdx.files.internal("assets/sounds/laser.ogg"));
        backgroundMusic = Gdx.audio.newMusic(Gdx.files.internal("assets/sounds/backgroundMusic.ogg"));

        backgroundMusic.setLooping(true);
        backgroundMusic.setVolume(0.5f);
        backgroundMusic.play();

        player = new Rectangle();
        player.x = 800f / 2f - 32f;
        player.y = 20;
        player.width = 64;
        player.height = 64;

        lasers = new Array<>();
        enemies = new Array<>();
        explosions = new Array<>();

        spawnEnemy();

        backgroundY2 = background.getHeight();
    }

    private void spawnEnemy() {
        Rectangle enemyRect = new Rectangle();
        enemyRect.x = (float) (Math.random() * (800 - 64));
        enemyRect.y = 480;
        enemyRect.width = 64;
        enemyRect.height = 64;

        Texture enemyTexture = enemyTextures [(int) (Math.random() * enemyTextures.length)];
        Enemy enemy = new Enemy(enemyRect, enemyTexture);
        enemies.add(enemy);

        lastEnemySpawnTime = TimeUtils.nanoTime();
    };

    @Override
    public void render(float delta) {
        // Draw your screen here. "delta" is the time since last render in seconds.
        Gdx.gl.glClearColor(0, 0, 0, 1); // Color negro de fondo
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();

        backgroundY1 -= backgroundSpeed * delta;
        backgroundY2 -= backgroundSpeed * delta;

        if (backgroundY1 + background.getHeight() <= 0) {
            backgroundY1 = backgroundY2 + background.getHeight();
        }

        if (backgroundY2 + background.getHeight() <= 0) {
            backgroundY2 = backgroundY1 + background.getHeight();
        }

        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        batch.draw(background, 0, backgroundY1);
        batch.draw(background, 0, backgroundY2);

        if (!isBlinking) {
            batch.draw(playerTexture, player.x, player.y);
        }

        for (Rectangle laser : lasers) {
            batch.draw(laserTexture, laser.x, laser.y);
        };

        for (Enemy enemy : enemies) {
            batch.draw(enemy.texture, enemy.rect.x, enemy.rect.y);
        };

        for (Explosion explosion : explosions) {
            batch.draw(explosionTexture, explosion.x, explosion.y);
            explosion.timer += delta;
            if (explosion.timer >= explosion.duration) {
                explosions.removeValue(explosion, false);
            }
        }

        for (int i = 0; i < playerLives; i++) {
            batch.draw(lifeTexture, 20 + i * 60, 440 - 50, 50,50);
        }
        batch.end();

        // Movimiento jugador
        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3(Gdx.input.getX(), Gdx.input.getY(), 0);
            camera.unproject(touchPos);
            player.x = touchPos.x - player.width / 2;
        }

        // Disparar
        if (Gdx.input.justTouched()) {
            float laserWidth = 6;
            float laserHeight = 15;
            float offset = 10;

            Rectangle laserLeft = new Rectangle();
            laserLeft.x = player.x + offset - laserWidth / 2;
            laserLeft.y = player.y + player.height;
            laserLeft.width = laserWidth;
            laserLeft.height = laserHeight;

            Rectangle laserRight = new Rectangle();
            laserRight.x = player.x + player.width - offset - laserWidth / 2;
            laserRight.y = player.y + player.height;
            laserRight.width = laserWidth;
            laserRight.height = laserHeight;

            lasers.add(laserLeft);
            lasers.add(laserRight);
            laserSound.play();
        }

        // Movimiento laser
        Iterator<Rectangle> laserIter = lasers.iterator();
        while (laserIter.hasNext()) {
            Rectangle laser = laserIter.next();
            laser.y += 300 * delta;
            if (laser.y > 480) laserIter.remove();
        }

        // Movimiento enemigos
        Iterator<Enemy> enemyIter = enemies.iterator();
        while (enemyIter.hasNext()) {
            Enemy enemy = enemyIter.next();
            enemy.rect.y -= 200 * delta;
            if (enemy.rect.y + enemy.rect.height < 0) enemyIter.remove();
        };

        // Crear enemigos cada segundo
        if (TimeUtils.nanoTime() - lastEnemySpawnTime > 1000000000) {
            spawnEnemy();
        };

        // Colisiones y parpadeo
        Iterator<Enemy> enemyIterator = enemies.iterator();
        while (enemyIterator.hasNext()) {
            Enemy enemy = enemyIterator.next();
            if (player.overlaps(enemy.rect)) {
                loseLife();
                enemyIterator.remove();
                break;
            }
        }

        laserIter = lasers.iterator();
        while (laserIter.hasNext()) {
            Rectangle laser = laserIter.next();
            enemyIter = enemies.iterator();
            while (enemyIter.hasNext()) {
                Enemy enemy = enemyIter.next();
                if (laser.overlaps(enemy.rect)) {
                    explosions.add(new Explosion(enemy.rect.x, enemy.rect.y, explosionTexture));
                    enemyIter.remove();
                    laserIter.remove();
                    break;
                }
            }
        }

        Iterator<Explosion> explosionIter = explosions.iterator();
        while (explosionIter.hasNext()) {
            Explosion explosion = explosionIter.next();
            explosion.timer += delta;
            if (explosion.timer >= 0.5f) {
                explosionIter.remove();
            }
        }

        // Parpadeo
        if (isBlinking) {
            blinkTimer += delta;
            if (blinkTimer >= blinkInterval) {
                blinkTimer = 0;
                isBlinking = !isBlinking;
            }
        }

    }

    private void loseLife() {
        playerLives--;
        isBlinking = true;
        blinkTimer = 0;

        if (playerLives <= 0) {
            System.out.println("Game Over");
        }
    }

    @Override
    public void resize(int width, int height) {
        // Resize your screen here. The parameters represent the new window size.
    }

    @Override
    public void pause() {
        // Invoked when your application is paused.
    }

    @Override
    public void resume() {
        // Invoked when your application is resumed after pause.
    }

    @Override
    public void hide() {
        // This method is called when another screen replaces this one.
    }

    @Override
    public void dispose() {
        // Destroy screen's assets here.
        laserSound.dispose();
        backgroundMusic.stop();
        backgroundMusic.dispose();
        batch.dispose();
        background.dispose();
        playerTexture.dispose();
        laserTexture.dispose();
        explosionTexture.dispose();
        lifeTexture.dispose();
        for (Texture texture : enemyTextures) {
            texture.dispose();
        }
    }
}
