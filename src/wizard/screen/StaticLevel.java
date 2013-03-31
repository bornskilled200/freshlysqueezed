package wizard.screen;

import box2D.Box2DFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import wizard.PlayerStats;
import wizard.box2D.WizardCategory;

import static wizard.Constants.GRAVITY_Y_DEFAULT;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 2/18/13
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class StaticLevel extends Level {

    public static final int COMMAND_RESTART_LEVEL = Input.Keys.F1;
    public static final float LEVEL_WIDTH = 30;
    public static final float LEVEL_HEIGHT = 80;
    protected final Box2DFactory box2DFactory;
    private final BodyDef bodyDef;
    private final FixtureDef fixtureDef;
    private Fixture playerCrouching;       // not too sure how to do this without destryoing the fixture

    public StaticLevel() {
        super();
        box2DFactory = new Box2DFactory();

        bodyDef = new BodyDef();
        fixtureDef = new FixtureDef();
        box2DFactory.begin();

        setupWorld();

        Gdx.input.setInputProcessor(new CommandsInputProcessor(Gdx.input.getInputProcessor()));
    }

    private void setupWorld() {
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);
        levelBody = world.createBody(bodyDef);

        setFilter(WizardCategory.BOUNDARY.filter, fixtureDef.filter);
        fixtureDef.isSensor = false;
        fixtureDef.friction = .2f;
        box2DFactory.createEdge(levelBody, fixtureDef, 0, 0, LEVEL_WIDTH, 0);
        box2DFactory.createEdge(levelBody, fixtureDef, 0, 0, 0, LEVEL_HEIGHT);
        box2DFactory.createEdge(levelBody, fixtureDef, LEVEL_WIDTH, 0, LEVEL_WIDTH, LEVEL_HEIGHT);

        box2DFactory.createEdge(levelBody, fixtureDef, 0, 1, 5, 1);
        box2DFactory.createEdge(levelBody, fixtureDef, 7, 2, 9, 2);
        box2DFactory.createEdge(levelBody, fixtureDef, 10, 8f, 11, 8f);
        box2DFactory.createEdge(levelBody, fixtureDef, 10, 1, 11, 1);

        float ladderX = LEVEL_WIDTH - 2;
        for (float i = 1; i < 20; i++) {
            box2DFactory.createEdge(levelBody, fixtureDef, ladderX, i, LEVEL_WIDTH, i);
        }

        box2DFactory.createEdge(levelBody, fixtureDef, 15, 0f, 20, 2f);

        bodyDef.position.set(2, 2);
        createPlayer(bodyDef, box2DFactory, fixtureDef);
    }

    @Override
    public void dispose() {
        box2DFactory.end();
        renderer.dispose();
        super.dispose();
    }

    public class CommandsInputProcessor extends InputAdapter {
        InputProcessor levelInputProcessor;

        public CommandsInputProcessor(InputProcessor levelInputProcessor) {
            this.levelInputProcessor = levelInputProcessor;
        }

        @Override
        public boolean keyDown(int keycode) {
            return levelInputProcessor.keyDown(keycode);
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            return levelInputProcessor.touchDown(screenX, screenY, pointer, button);
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

        @Override
        public boolean keyUp(int keycode) {
            boolean b = levelInputProcessor.keyUp(keycode);
            if (b == true)
                return true;

            if (keycode == COMMAND_RESTART_LEVEL) {
                world.dispose();
                world = new World(new Vector2(0, GRAVITY_Y_DEFAULT), true);
                setupWorld();
                return true;
            }
            return false;    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public boolean keyTyped(char character) {
            return levelInputProcessor.keyTyped(character);
        }
    }
}
