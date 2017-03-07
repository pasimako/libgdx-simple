package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.ArrayList;

public class MyGdxGame extends ApplicationAdapter implements InputProcessor {
    SpriteBatch batch;
    Texture img;

    final int SPAWN_DELTA = 1000;
    // We'll split screen into 6 columns
    final int COLUMNS = 6;

    ArrayList<Sprite>[] blocks = new ArrayList[COLUMNS];
    OrthographicCamera camera;
    float blockSize;
    float velocity;
    long lastSpawn = 0;

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");

        for (int i = 0; i < blocks.length; i++) {
            blocks[i] = new ArrayList<Sprite>();
        }

        // Create a camera with orthographic projection
        camera = new OrthographicCamera();
        // Set the viewport to fit the screen resolution and center camera at the center of the screen
        camera.setToOrtho(false);

        // Calculate Sprite size and velocity
        blockSize = camera.viewportWidth / COLUMNS;
        velocity = camera.viewportHeight / 200;

        // Use the InputProcessor so we don't have to poll for input events
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        update();

        // Clear screen
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Show the SpriteBatch where to render stuff
        batch.setProjectionMatrix(camera.combined);

        // Draw all sprites in a loop
        batch.begin();
        for (int column = 0; column < blocks.length; column++) {
            for (int i = 0; i < blocks[column].size(); i++) {
                blocks[column].get(i).draw(batch);
            }
        }
        batch.end();
    }

    @Override
    public void dispose() {
        // Resources in LibGDX are not managed by the garbage collector, so they have to be disposed of manually. You've been warned!
        batch.dispose();
        img.dispose();
    }

    private void update() {
        long now = TimeUtils.millis();

        // Create new Sprite if time since last spawn > SPAWN_DELTA
        if (now - lastSpawn > SPAWN_DELTA) {
            // Choose a random column
            int column = MathUtils.random(0, COLUMNS - 1);

            // Create a new Sprite, set dimensions and position below our viewable area (-blocksize)
            Sprite block = new Sprite(img);
            block.setBounds(column * blockSize, -blockSize, blockSize, blockSize);
            block.setOriginCenter();

            // Insert the Sprite in our array for future reference
            blocks[column].add(block);
            lastSpawn = now;
        }

        // Loop through all Sprites
        for (int column = 0; column < blocks.length; column++) {
            for (int i = 0; i < blocks[column].size(); i++) {
                Sprite block = blocks[column].get(i);
                // Increase Y position
                block.translateY(velocity);

                float maxY;

                if (i > 0) {
                    // If Y overlaps the previous Sprite, then stop
                    Sprite previous = blocks[column].get(i - 1);
                    maxY = previous.getY() - block.getHeight();
                } else {
                    // If Y is above our viewable area, then stop
                    maxY = camera.viewportHeight - block.getHeight();
                }

                if (block.getY() > maxY) {
                    block.setY(maxY);
                }
            }
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        // Translate screen coordinates to our camera coordinates
        Vector3 position = camera.unproject(new Vector3(screenX, screenY, 0));

        // Loop through all Sprites
        for (int column = 0; column < blocks.length; column++) {
            for (int i = 0; i < blocks[column].size(); i++) {
                Sprite block = blocks[column].get(i);

                // If event coordinates within the Sprite, then rotate
                if (block.getBoundingRectangle().contains(position.x, position.y)) {
                    block.rotate90(true);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
