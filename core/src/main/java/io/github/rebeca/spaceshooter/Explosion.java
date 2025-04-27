package io.github.rebeca.spaceshooter;

import com.badlogic.gdx.graphics.Texture;

public class Explosion {
    float x, y;
    float timer = 0;
    Texture explosionTexture;  // Aquí puedes asignar la textura de la explosión
    float duration = 0.5f; // Duración de la explosión en segundos

    // Constructor
    public Explosion(float x, float y, Texture explosionTexture) {
        this.x = x;
        this.y = y;
        this.explosionTexture = explosionTexture;
    }
}
