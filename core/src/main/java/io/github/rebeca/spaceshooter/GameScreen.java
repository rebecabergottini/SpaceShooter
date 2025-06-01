package io.github.rebeca.spaceshooter;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.*;
import com.badlogic.gdx.utils.Array;
import java.util.Iterator;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class GameScreen implements Screen {
    private final Game game;
    private final OrthographicCamera camera;
    private final SpriteBatch batch;

    // Jugador
    private final Texture playerTexture;
    private final Rectangle player;
    private static final float PLAYER_SPEED = 300;

    // Lasers disparados por el jugador
    private final Texture laserTexture;
    private final Array<Rectangle> lasers;
    private static final float LASER_SPEED = 500;
    private float shootCooldown = 0;         // Tiempo entre disparos
    private static final float SHOOT_DELAY = 0.2f;

    // Enemigos que aparecen en pantalla
    private final Texture enemyTexture;
    private final Array<Rectangle> enemies;
    private float enemySpawnTimer = 0;       // Temporizador para generar enemigos

    // Fondo del juego
    private final Texture background;
    private float backgroundOffset = 0;      // Para desplazar el fondo y simular movimiento

    // Power-ups de invulnerabilidad
    private final Texture invincibilityTexture;
    private final Array<Rectangle> invincibilityPowerups;
    private float powerupSpawnTimer = 0;
    private static final float POWERUP_SPAWN_INTERVAL = 15f;
    private float invincibilityTimer = 0;
    private boolean isInvincible = false;    // Estado de invulnerabilidad
    private float blinkTimer = 0;             // Para efecto parpadeo durante invulnerabilidad
    private static final float BLINK_INTERVAL = 0.1f;
    private static final float INVINCIBILITY_DURATION = 5f;
    private final Texture shieldTexture;     // Imagen del escudo para invulnerabilidad visual

    // Obstaculos que caen y el jugador debe evitar
    private final Texture obstacleTexture;
    private final Array<Rectangle> obstacles;
    private float obstacleSpawnTimer = 0;
    private int obstaclesPassed = 0;          // Contador de obstaculos que se han pasado sin colision

    // Fuente para texto en pantalla
    private final BitmapFont audiowideFont;

    // Estadisticas del juego
    private float timeElapsed = 0;            // Tiempo transcurrido en la partida
    private int enemiesDefeated = 0;          // Enemigos derrotados
    private boolean isVictory = false;        // Estado de victoria

    // Sistema de niveles
    private static class Level {
        int victoryObstacles;                 // Obstaculos para victoria
        int victoryEnemies;                   // Enemigos para victoria
        float victoryTime;                    // Tiempo para victoria
        float enemySpawnInterval;             // Intervalo para generar enemigos
        float obstacleSpawnInterval;          // Intervalo para generar obstaculos
        float enemySpeed;                     // Velocidad de los enemigos
        float obstacleSpeed;                  // Velocidad de los obstaculos
        String name;                         // Nombre del nivel

        public Level(int victoryObstacles, int victoryEnemies, float victoryTime,
                     float enemySpawnInterval, float obstacleSpawnInterval,
                     float enemySpeed, float obstacleSpeed, String name) {
            this.victoryObstacles = victoryObstacles;
            this.victoryEnemies = victoryEnemies;
            this.victoryTime = victoryTime;
            this.enemySpawnInterval = enemySpawnInterval;
            this.obstacleSpawnInterval = obstacleSpawnInterval;
            this.enemySpeed = enemySpeed;
            this.obstacleSpeed = obstacleSpeed;
            this.name = name;
        }
    }

    private final Level[] levels;             // Arreglo con todos los niveles
    private final int currentLevelIndex;      // Indice del nivel actual
    private final Level currentLevel;         // Nivel actual

    public GameScreen(Game game, int levelIndex) {
        this.game = game;

        // Inicializamos los niveles
        levels = new Level[]{
            new Level(8, 8, 45f, 2.0f, 4f, 200f, 120f, "LEVEL 1 - TRAINING"),
            new Level(15, 15, 70f, 1.5f, 3.5f, 300f, 180f, "LEVEL 2 - COMBAT"),
            new Level(25, 25, 100f, 1.2f, 2.5f, 400f, 250f, "LEVEL 3 - BOSS")
        };


        // Seleccionamos el nivel actual segun el indice recibido
        this.currentLevelIndex = Math.min(levelIndex, levels.length - 1);
        this.currentLevel = levels[currentLevelIndex];

        // Musica
        Main main = (Main) game;
        main.stopAllMusic();                    // Parar todas las musicas
        if (!main.background.isPlaying()) {
            main.background.play();             // Reproducir musica de fondo si no esta sonando
        }

        // Configuracion de camara y batch para dibujar
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();

        // Cargar texturas
        playerTexture = new Texture("spaceships/player.png");
        laserTexture = new Texture("lasers/laserGreen10.png");
        enemyTexture = new Texture("spaceships/enemyBlack1.png");
        background = new Texture("ui/MainMenu/background.png");
        shieldTexture = new Texture("life/shield.png");
        invincibilityTexture = new Texture("life/powerup.png");
        obstacleTexture = new Texture("obstacle/meteor.png");

        // Crear jugador y colecciones vacias
        player = new Rectangle(400 - 32, 20, 64, 64);
        lasers = new Array<>();
        enemies = new Array<>();
        invincibilityPowerups = new Array<>();
        obstacles = new Array<>();

        // Cargar fuente para texto
        audiowideFont = new BitmapFont(Gdx.files.internal("fonts/audiowide.fnt"));
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0, 0, 1);       // Limpiar pantalla a negro
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        update(delta);                         // Actualizar estado del juego
        draw(); // Dibujar todo
    }

    private void update(float delta) {
        if (isVictory) return;                 // Si ya ganamos, no actualizar nada

        timeElapsed += delta;                  // Incrementar tiempo jugado
        checkVictory();                       // Verificar condiciones de victoria
        handleInput(delta);                    // Procesar entrada del jugador
        updateLasers(delta);                   // Mover lasers
        updateEnemies(delta);                  // Mover y generar enemigos
        updatePowerups(delta);                 // Mover y generar power-ups
        updateBackground(delta);               // Mover fondo para efecto scroll
        updateInvincibility(delta);            // Actualizar estado invulnerabilidad
        checkCollisions();                     // Detectar colisiones
        updateObstacles(delta);                // Mover y generar obstaculos

        // Disminuir tiempo de recarga para disparar
        if (shootCooldown > 0) {
            shootCooldown -= delta;
        }
    }

    private void handleInput(float delta) {
        // Control por teclado
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            player.x -= PLAYER_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            player.x += PLAYER_SPEED * delta;
        }
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            shoot();
        }

        // Control tactil
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                Vector3 touchPos = new Vector3(Gdx.input.getX(i), Gdx.input.getY(i), 0);
                camera.unproject(touchPos);

                // Mover jugador
                if (!player.contains(touchPos.x, touchPos.y)) {
                    player.x = touchPos.x - player.width / 2;
                    player.x = MathUtils.clamp(player.x, 0, 800 - player.width);
                } else {
                    // Si se toca el jugador, disparar
                    shoot();
                }
            }
        }

        // Limitar movimiento para que no salga de la pantalla
        player.x = MathUtils.clamp(player.x, 0, 800 - player.width);
    }

    private void shoot() {
        // Disparar solo si no estamos en periodo de recarga
        if (shootCooldown <= 0) {
            Rectangle laser = new Rectangle(
                (float) (player.x + player.width / 1.5 - (double) 10 / 7),
                player.y + player.height,
                10,
                25
            );
            lasers.add(laser);
            shootCooldown = SHOOT_DELAY;        // Reiniciar tiempo de recarga
            ((Main) game).laserSound.play();    // Sonido de disparo
        }
    }

    private void updateLasers(float delta) {
        // Mover cada laser hacia arriba, eliminar si sale de pantalla
        for (Iterator<Rectangle> iter = lasers.iterator(); iter.hasNext(); ) {
            Rectangle laser = iter.next();
            laser.y += LASER_SPEED * delta;
            if (laser.y > 480) iter.remove();
        }
    }

    private void updateEnemies(float delta) {
        enemySpawnTimer += delta;
        // Generar enemigos segun intervalo definido en nivel
        if (enemySpawnTimer > currentLevel.enemySpawnInterval) {
            Rectangle enemy = new Rectangle(
                MathUtils.random(0, 800 - 64),
                480,
                64, 64
            );
            enemies.add(enemy);
            enemySpawnTimer = 0;
        }

        // Mover enemigos hacia abajo y eliminar si salen de pantalla
        for (Iterator<Rectangle> iter = enemies.iterator(); iter.hasNext(); ) {
            Rectangle enemy = iter.next();
            enemy.y -= currentLevel.enemySpeed * delta;
            if (enemy.y + enemy.height < 0) iter.remove();
        }
    }

    private void updateObstacles(float delta) {
        obstacleSpawnTimer += delta;

        // Generar obstaculos segun intervalo definido
        if (obstacleSpawnTimer > currentLevel.obstacleSpawnInterval) {
            Rectangle obstacle = new Rectangle(
                MathUtils.random(0, 800 - 48),
                480,
                48, 48
            );
            obstacles.add(obstacle);
            obstacleSpawnTimer = 0;
        }

        // Mover obstaculos hacia abajo
        for (Iterator<Rectangle> iter = obstacles.iterator(); iter.hasNext(); ) {
            Rectangle obstacle = iter.next();
            obstacle.y -= currentLevel.obstacleSpeed * delta;

            // Contar obstaculos que pasaron sin colisionar y eliminar
            if (obstacle.y + obstacle.height < 0) {
                obstaclesPassed++;
                iter.remove();
            }
        }
    }

    private void updatePowerups(float delta) {
        powerupSpawnTimer += delta;

        // Generar power-up de invulnerabilidad periodicamente
        if (powerupSpawnTimer > POWERUP_SPAWN_INTERVAL) {
            spawnInvincibilityPowerup();
            powerupSpawnTimer = 0;
        }

        // Mover power-ups hacia abajo y eliminar si salen de pantalla
        for (Iterator<Rectangle> iter = invincibilityPowerups.iterator(); iter.hasNext(); ) {
            Rectangle powerup = iter.next();
            powerup.y -= currentLevel.enemySpeed * 0.7f * delta;
            if (powerup.y + powerup.height < 0) iter.remove();
        }
    }

    private void spawnInvincibilityPowerup() {
        // Crear un power-up en posicion aleatoria arriba de la pantalla
        Rectangle powerup = new Rectangle(
            MathUtils.random(0, 800 - 50),
            480,
            50, 50
        );
        invincibilityPowerups.add(powerup);
    }

    private void updateInvincibility(float delta) {
        if (isInvincible) {
            invincibilityTimer -= delta;    // Reducir tiempo de invulnerabilidad
            blinkTimer += delta;            // Control del parpadeo

            if (blinkTimer > BLINK_INTERVAL) {
                blinkTimer = 0;
            }

            // Terminar invulnerabilidad cuando el tiempo se acabe
            if (invincibilityTimer <= 0) {
                isInvincible = false;
            }
        }
    }

    private void updateBackground(float delta) {
        backgroundOffset += 50 * delta;      // Mover fondo para simular scroll vertical
        if (backgroundOffset > background.getHeight()) {
            backgroundOffset = 0;
        }
    }

    private void checkCollisions() {
        // Colisiones entre lasers y enemigos
        for (Iterator<Rectangle> iter = lasers.iterator(); iter.hasNext(); ) {
            Rectangle laser = iter.next();
            for (Iterator<Rectangle> iterEnemy = enemies.iterator(); iterEnemy.hasNext(); ) {
                Rectangle enemy = iterEnemy.next();
                if (laser.overlaps(enemy)) {
                    iter.remove();
                    iterEnemy.remove();
                    enemiesDefeated++;           // Aumentar contador de enemigos derrotados
                    checkVictory();             // Verificar si se cumple condicion de victoria
                    break;
                }
            }
        }

        // Colisiones entre jugador y enemigos (si no es invulnerable)
        if (!isInvincible) {
            for (Rectangle enemy : enemies) {
                if (player.overlaps(enemy)) {
                    // Pasar a pantalla de Game Over
                    game.setScreen(new GameOverScreen(game, false, currentLevelIndex, timeElapsed, obstaclesPassed, enemiesDefeated));
                    return;
                }
            }
        }

        // Colisiones entre jugador y obstaculos (si no es invulnerable)
        if (!isInvincible) {
            for (Rectangle obstacle : obstacles) {
                if (player.overlaps(obstacle)) {
                    // Pasar a pantalla de Game Over
                    game.setScreen(new GameOverScreen(game, false, currentLevelIndex, timeElapsed, obstaclesPassed, enemiesDefeated));
                    return;
                }
            }
        }

        // Colisiones entre jugador y power-ups de invulnerabilidad
        for (Iterator<Rectangle> iter = invincibilityPowerups.iterator(); iter.hasNext(); ) {
            Rectangle powerup = iter.next();
            if (player.overlaps(powerup)) {
                activateInvincibility();
                iter.remove();
                break;
            }
        }
    }

    private void checkVictory() {
        // Comprobar si alguna condicion de victoria se cumple
        if (enemiesDefeated >= currentLevel.victoryEnemies ||
            obstaclesPassed >= currentLevel.victoryObstacles ||
            timeElapsed >= currentLevel.victoryTime) {

            isVictory = true;

            if (currentLevelIndex < levels.length - 1) {
                // Si no es el ultimo nivel, pasar al siguiente
                game.setScreen(new LevelTransitionScreen(game, currentLevelIndex + 1));

            } else {
                // Si es el ultimo nivel, mostrar pantalla de victoria
                game.setScreen(new VictoryScreen(game, currentLevelIndex + 1, timeElapsed, obstaclesPassed, enemiesDefeated));
            }
        }
    }

    private void activateInvincibility() {
        isInvincible = true;
        invincibilityTimer = INVINCIBILITY_DURATION;  // Duracion del poder de invulnerabilidad
    }

    private void draw() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Dibujar fondo con efecto scroll
        batch.draw(background, 0, -backgroundOffset);
        batch.draw(background, 0, -backgroundOffset + background.getHeight());

        // Dibujar jugador, parpadeando si esta invulnerable
        boolean shouldDrawPlayer = !isInvincible || (blinkTimer < BLINK_INTERVAL / 2);
        if (shouldDrawPlayer) {
            batch.draw(playerTexture, player.x, player.y, player.width, player.height);
        }
        // Dibujar escudo si esta invulnerable
        if (isInvincible) {
            batch.draw(shieldTexture, player.x - 5, player.y - 5, player.width + 10, player.height + 10);
        }

        // Dibujar lasers
        for (Rectangle laser : lasers) {
            batch.draw(laserTexture, laser.x, laser.y, laser.width, laser.height);
        }
        // Dibujar enemigos
        for (Rectangle enemy : enemies) {
            batch.draw(enemyTexture, enemy.x, enemy.y, enemy.width, enemy.height);
        }
        // Dibujar power-ups
        for (Rectangle powerup : invincibilityPowerups) {
            batch.draw(invincibilityTexture, powerup.x, powerup.y, powerup.width, powerup.height);
        }
        // Dibujar obstaculos
        for (Rectangle obstacle : obstacles) {
            batch.draw(obstacleTexture, obstacle.x, obstacle.y, obstacle.width, obstacle.height);
        }

        // Mostrar informacion en pantalla
        GlyphLayout layout = new GlyphLayout();

        // Level name (center top, más grande)
        audiowideFont.getData().setScale(0.6f);
        layout.setText(audiowideFont, currentLevel.name.toUpperCase());
        float xCentered = (800 - layout.width) / 2f;  // Centrar en X

        audiowideFont.draw(batch, currentLevel.name.toUpperCase(), xCentered, 470);

        // Time elapsed (top left)
        audiowideFont.getData().setScale(0.4f);
        String timeText = "TIME: " + (int) timeElapsed + "s";
        audiowideFont.draw(batch, timeText, 10, 425);

        // Enemies defeated (top right)
        String enemiesText = "ENEMIES DEFEATED: " + enemiesDefeated;
        layout.setText(audiowideFont, enemiesText);
        float xRight = 800 - layout.width - 10;  // 10px margen derecha
        audiowideFont.draw(batch, enemiesText, xRight, 425);

        // Obstacles avoided (below enemies defeated)
        String obstaclesText = "OBSTACLES AVOIDED: " + obstaclesPassed;
        layout.setText(audiowideFont, obstaclesText);
        float xRight2 = 800 - layout.width - 10;
        audiowideFont.draw(batch, obstaclesText, xRight2, 405);


        batch.end();
    }

    // Metodos obligatorios de la interfaz Screen
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void show() {}
    @Override public void hide() {}
    @Override public void dispose() {
        // Liberar recursos
        playerTexture.dispose();
        laserTexture.dispose();
        enemyTexture.dispose();
        background.dispose();
        invincibilityTexture.dispose();
        shieldTexture.dispose();
        obstacleTexture.dispose();
        audiowideFont.dispose();
        batch.dispose();
    }
}
