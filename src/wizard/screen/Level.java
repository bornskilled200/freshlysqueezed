package wizard.screen;

import box2D.Box2DFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import wizard.box2D.WizardCategory;

import static wizard.Constants.*;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 2/18/13
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class Level implements Screen {
    // CONSTANTS
    private final float GRAVITY_Y_DEFAULT = -9.8f;
    // DRAWING OBJECTS
    private final Box2DDebugRenderer box2DDebugRenderer;
    private final ShapeRenderer renderer;
    private final OrthographicCamera cam;
    // LIBGDX OBJECTS
    private GL11 gl;
    private Input input;
    // BOX2D STUFF
    private World world;
    // GAME VARIABLES
    private boolean isFeetIsTouchingGround;
    private float playerCanJump; //when it it >0 then it can still move upwards
    private Body player;
    private Fixture playerFeet;
    private Fixture playerBox;

    public Level() {
        world = new World(new Vector2(0, GRAVITY_Y_DEFAULT), true);

        gl = Gdx.graphics.getGL11();
        cam = new OrthographicCamera();

        input = Gdx.input;
        World.setVelocityThreshold(.1f);
        box2DDebugRenderer = new Box2DDebugRenderer(true, true, true, true, true);
        renderer = new ShapeRenderer();


        BodyDef bodyDef = new BodyDef();
        FixtureDef fixtureDef = new FixtureDef();
        Box2DFactory box2DFactory = new Box2DFactory();


        box2DFactory.begin();

        createPlayer(bodyDef, fixtureDef, box2DFactory);
        createPlatform(bodyDef, fixtureDef, box2DFactory);

        box2DFactory.end();

        world.setContactListener(new PlayerContactListener());
    }

    public static void setFilter(Filter filter, Filter target) {
        target.categoryBits = filter.categoryBits;
        target.groupIndex = filter.groupIndex;
        target.maskBits = filter.maskBits;
    }

    private void createPlatform(BodyDef bodyDef, FixtureDef fixtureDef, Box2DFactory box2DFactory) {
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);
        Body body = world.createBody(bodyDef);

        setFilter(WizardCategory.BOUNDARY.filter, fixtureDef.filter);
        fixtureDef.isSensor = false;
        fixtureDef.friction = .1f;
        box2DFactory.createEdge(body, fixtureDef, 0, 0, 20, 0);
        box2DFactory.createEdge(body, fixtureDef, 0, 0, 0, 20);
        box2DFactory.createEdge(body, fixtureDef, 20, 0, 20, 20);

        box2DFactory.createEdge(body, fixtureDef, 0, 1, 5, 1);
        box2DFactory.createEdge(body, fixtureDef, 7, 2, 9, 2);
    }

    private void createPlayer(BodyDef bodyDef, FixtureDef fixtureDef, Box2DFactory box2DFactory) {
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(2, 2);
        bodyDef.fixedRotation = true;
        player = world.createBody(bodyDef);

        setFilter(WizardCategory.PLAYER.filter, fixtureDef.filter);
        fixtureDef.density = PLAYER_DENSITY;
        fixtureDef.friction = 0;
        box2DFactory.createBox(player, fixtureDef, 0, 0, PLAYER_BOUNDARY_WIDTH, PLAYER_BOUNDARY_HEIGHT);

        fixtureDef.isSensor = true;
        setFilter(WizardCategory.PLAYER_FEET.filter, fixtureDef.filter);
        box2DFactory.createBox(player, fixtureDef, 0, -PLAYER_BOUNDARY_HEIGHT, PLAYER_BOUNDARY_WIDTH * .9f, .05f);
    }

    @Override
    public void render(float delta) {
        // INPUT PROCESSING
        Vector2 worldCenter = player.getWorldCenter();

        // JUMPING
        playerCanJump -= delta;
        if (input.isKeyPressed(Input.Keys.W)) {
            if (isFeetIsTouchingGround == true) {
                player.applyLinearImpulse(0, PLAYER_JUMP_START, worldCenter.x, worldCenter.y);
                playerCanJump = PLAYER_JUMP_FLOAT_TIME;
            } else if (playerCanJump > 0)
                player.applyForce(0, PLAYER_JUMP_CONSTANT, worldCenter.x, worldCenter.y);
        } else playerCanJump = 0;

        // HORIZONTAL MOVEMENT
        float vx = 0;
        if (input.isKeyPressed(Input.Keys.A))
            vx += -PLAYER_WALK_SPEED;
        if (input.isKeyPressed(Input.Keys.D))
            vx += PLAYER_WALK_SPEED;

        if (vx == 0) {
            if (isFeetIsTouchingGround)
                player.applyForce(player.getLinearVelocity().x * -8f, 0, worldCenter.x, worldCenter.y);
        } else if (vx > 0 && player.getLinearVelocity().x < PLAYER_MAX_SPEED) {
            player.applyForce(vx, 0, worldCenter.x, worldCenter.y);
        } else if (vx < 0 && player.getLinearVelocity().x > -PLAYER_MAX_SPEED) {
            player.applyForce(vx, 0, worldCenter.x, worldCenter.y);
        }


        // GAME PROCESSING
        world.step(delta, 8, 3); // RECOMMENDED IS EITHER 6,2 OR 8,3

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        cam.apply(gl);
        box2DDebugRenderer.render(world, cam.combined);
    }

    @Override
    public void resize(int width, int height) {
        float ratio = (float) width / height;
        cam.setToOrtho(false, 20 * ratio, 20);
        cam.update();
        renderer.setProjectionMatrix(cam.combined);
    }

    @Override
    public void show() {
        //To change player of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void hide() {
        //To change player of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void pause() {
        //To change player of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resume() {
        //To change player of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void dispose() {
        world.dispose();
        renderer.dispose();
    }

    private class PlayerContactListener implements ContactListener {
        @Override
        public void beginContact(Contact contact) {
            int collision = contact.getFixtureA().getFilterData().categoryBits | contact.getFixtureB().getFilterData().categoryBits;
            int playerFeetTouchingBoundary = WizardCategory.BOUNDARY.getID() | WizardCategory.PLAYER_FEET.getID();

            if (playerFeetTouchingBoundary == collision && contact.isTouching())
                isFeetIsTouchingGround = true;

        }

        @Override
        public void endContact(Contact contact) {
            int collision = contact.getFixtureA().getFilterData().categoryBits | contact.getFixtureB().getFilterData().categoryBits;
            int playerFeetTouchingBoundary = WizardCategory.BOUNDARY.getID() | WizardCategory.PLAYER_FEET.getID();

            if (playerFeetTouchingBoundary == collision && !contact.isTouching())
                isFeetIsTouchingGround = false;
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
            //To change body of implemented methods use File | Settings | File Templates.
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }
}
