package wizard.screen;

import box2D.Box2DFactory;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import wizard.GameState;
import wizard.box2D.WizardCategory;

/**
 * Created with IntelliJ IDEA.
 * User: David Park
 * Date: 3/20/13
 * Time: 8:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class DebugLevel implements Screen {
    private final Box2DDebugRenderer box2DDebugRenderer;
    private final BitmapFont font;
    private final DebugLevel.DebugKeysInputProcessor inputProcessor;
    private Level level;
    private Matrix4 ortho2DMatrix;

    public DebugLevel(Level level) {
        this.level = level;
        box2DDebugRenderer = new Box2DDebugRenderer(true, true, true, true, true);
        ortho2DMatrix = new Matrix4();
        font = new BitmapFont(Gdx.files.internal("com/badlogic/gdx/utils/arial-15.fnt"), false);

        inputProcessor = new DebugKeysInputProcessor(Gdx.input.getInputProcessor());
        Gdx.input.setInputProcessor(inputProcessor);
    }

    @Override
    public void render(float delta) {
        level.render(delta);
        box2DDebugRenderer.render(level.world, level.cam.combined);
        renderDebugText(level.spritebatch);
    }

    private void renderDebugText(SpriteBatch spritebatch) {
        spritebatch.setProjectionMatrix(ortho2DMatrix);
        spritebatch.begin();
        spritebatch.setColor(Color.WHITE);
        font.drawMultiLine(spritebatch, "canJump=\t" + level.canJump + "\n" +
                "justKickedOff=\t" + level.justKickedOff + "\n" +
                "playerCanMoveUpwards = \t" + level.playerCanMoveUpwards + "\n" +
                "isFeetTouchingBoundary=\t" + level.isFeetTouchingBoundary + "\n" +
                "justJumped=\t" + level.justJumped, 0, font.getLineHeight() * 6);
        if (level.getGameState() == GameState.PAUSED)
            font.draw(spritebatch, "PAUSED", Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
        spritebatch.end();
        spritebatch.setProjectionMatrix(level.cam.combined);
    }

    @Override
    public void resize(int width, int height) {
        level.resize(width, height);
        ortho2DMatrix.setToOrtho2D(0, 0, width, height);
    }

    @Override
    public void show() {
        level.show();
    }

    @Override
    public void hide() {
        level.hide();
    }

    @Override
    public void pause() {
        level.pause();
    }

    @Override
    public void resume() {
        level.resume();
    }

    @Override
    public void dispose() {
        level.dispose();
        box2DDebugRenderer.dispose();
        font.dispose();
        inputProcessor.dispose();
    }

    public class DebugKeysInputProcessor extends InputAdapter {
        Vector3 temp3 = new Vector3();
        Vector2 temp2 = new Vector2();
        private InputProcessor levelInputProcessor;
        private PolygonShape polygonShape;
        private BodyDef bodyDef;
        private FixtureDef fixtureDef;

        public DebugKeysInputProcessor(InputProcessor levelInputProcessor) {
            this.levelInputProcessor = levelInputProcessor;
            polygonShape = new PolygonShape();
            bodyDef = new BodyDef();
            fixtureDef = new FixtureDef();
        }

        @Override
        public boolean keyDown(int keycode) {
            return levelInputProcessor.keyDown(keycode);
        }

        @Override
        public boolean keyUp(int keycode) {
            return levelInputProcessor.keyUp(keycode);
        }

        @Override
        public boolean keyTyped(char character) {
            return levelInputProcessor.keyTyped(character);
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT && button != Input.Buttons.RIGHT)
                return super.touchDown(screenX, screenY, pointer, button);    //To change body of overridden methods use File | Settings | File Templates.

            temp3.set(screenX, screenY, 0);
            level.cam.unproject(temp3);

            //Box2DFactory.resetBodyDef(bodyDef);
            bodyDef.angle = (float) Math.toRadians(Math.random() * 360);
            bodyDef.position.set(temp3.x, temp3.y);
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.fixedRotation = false;
            Body body = level.world.createBody(bodyDef);

            //Box2DFactory.resetFixtureDef(fixtureDef);
            level.setFilter(WizardCategory.DEBRIS.filter, fixtureDef.filter);
            fixtureDef.density = 1;
            fixtureDef.friction = .4f;
            if (button == Input.Buttons.LEFT)
                Box2DFactory.createBox(polygonShape, body, fixtureDef, temp2, .2f + (float) Math.random() * .8f, .2f + (float) Math.random() * .8f, 0);
            else if (button == Input.Buttons.RIGHT)
                Box2DFactory.createTriangle(polygonShape, body, fixtureDef, .2f + (float) Math.random() * .8f, .2f + (float) Math.random() * .8f);
            return (button == Input.Buttons.MIDDLE) ? false : true;    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            return levelInputProcessor.touchUp(screenX, screenY, pointer, button);
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return levelInputProcessor.touchDragged(screenX, screenY, pointer);
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return levelInputProcessor.mouseMoved(screenX, screenY);
        }

        @Override
        public boolean scrolled(int amount) {
            return levelInputProcessor.scrolled(amount);
        }

        public void dispose() {
            polygonShape.dispose();
        }
    }
}
