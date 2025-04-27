package io.github.rebeca.spaceshooter;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;

public class Enemy {
    public Rectangle rect;
    public Texture texture;

    public Enemy(Rectangle rect, Texture texture) {
        this.rect = rect;
        this.texture = texture;
    }
}
