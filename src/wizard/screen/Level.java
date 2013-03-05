package wizard.screen;

import box2D.Box2DFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
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
    public static final int CONTROL_MOVE_LEFT = Input.Keys.A;
    public static final int CONTROL_MOVE_RIGHT = Input.Keys.D;
    // DRAWING OBJECTS
    private final Box2DDebugRenderer box2DDebugRenderer;
    private final ShapeRenderer renderer;
    private final OrthographicCamera cam;
    private static final int CONTROL_JUMP = Input.Keys.W;
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

    //INPUT PROCESSING
    boolean controlMoveLeft = false;
    boolean controlMoveRight = false;
    private final Box2DFactory box2DFactory;
    private final BodyDef bodyDef;
    private final FixtureDef fixtureDef;
    private boolean controlJump = false;

    public Level() {
        world = new World(new Vector2(0, GRAVITY_Y_DEFAULT), true);

        gl = Gdx.graphics.getGL11();
        cam = new OrthographicCamera();

        input = Gdx.input;
        World.setVelocityThreshold(.1f);
        box2DDebugRenderer = new Box2DDebugRenderer(true, true, true, true, true);
        renderer = new ShapeRenderer();


        bodyDef = new BodyDef();
        fixtureDef = new FixtureDef();
        box2DFactory = new Box2DFactory();


        box2DFactory.begin();

        createPlayer(bodyDef, fixtureDef, box2DFactory);
        createPlatform(bodyDef, fixtureDef, box2DFactory);

        world.setContactListener(new PlayerContactListener());
        Gdx.input.setInputProcessor(new LevelInputProcessor(bodyDef, fixtureDef));
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
        fixtureDef.friction = .2f;
        box2DFactory.createEdge(body, fixtureDef, 0, 0, 20, 0);
        box2DFactory.createEdge(body, fixtureDef, 0, 0, 0, 20);
        box2DFactory.createEdge(body, fixtureDef, 20, 0, 20, 20);

        box2DFactory.createEdge(body, fixtureDef, 0, 1, 5, 1);
        box2DFactory.createEdge(body, fixtureDef, 7, 2, 9, 2);
        box2DFactory.createEdge(body, fixtureDef, 10, 1, 11, 1);

        box2DFactory.createEdge(body, fixtureDef, 16, .5f, 17, .5f);
        box2DFactory.createEdge(body, fixtureDef, 17, 1f, 18, 1f);
        box2DFactory.createEdge(body, fixtureDef, 18, 1.5f, 19, 1.5f);
        box2DFactory.createEdge(body, fixtureDef, 19, 2f, 20, 2f);
    }

    private void createPlayer(BodyDef bodyDef, FixtureDef fixtureDef, Box2DFactory box2DFactory) {
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(2, 2);
        bodyDef.fixedRotation = true;
        player = world.createBody(bodyDef);

        setFilter(WizardCategory.PLAYER.filter, fixtureDef.filter);
        fixtureDef.density = PLAYER_DENSITY;
        fixtureDef.friction = 0f;
        playerBox = box2DFactory.createBox(player, fixtureDef, 0, 0, PLAYER_BOUNDARY_WIDTH, PLAYER_BOUNDARY_HEIGHT);

        fixtureDef.isSensor = false;
        setFilter(WizardCategory.PLAYER_FEET.filter, fixtureDef.filter);
        playerFeet = box2DFactory.createCircle(player, fixtureDef, 0, -PLAYER_BOUNDARY_HEIGHT, PLAYER_BOUNDARY_WIDTH);
        player.resetMassData();
    }

    @Override
    public void render(float delta) {
        // INPUT PROCESSING
        Vector2 worldCenter = player.getWorldCenter();

        // JUMPING
        playerCanJump -= delta;
        if (controlJump) {
            if (isFeetIsTouchingGround == true) {
                player.applyLinearImpulse(0, PLAYER_JUMP_START, worldCenter.x, worldCenter.y);
                playerCanJump = PLAYER_JUMP_FLOAT_TIME;
            } else if (playerCanJump > 0)
                player.applyForce(0, PLAYER_JUMP_CONSTANT, worldCenter.x, worldCenter.y);
        } else playerCanJump = 0;

        // HORIZONTAL MOVEMENT
        float vx = 0;
        if (controlMoveLeft == true)
            vx += -PLAYER_WALK_SPEED;
        if (controlMoveRight == true)
            vx += PLAYER_WALK_SPEED;

        if (vx == 0)
            playerFeet.setFriction(PLAYER_STOP_FRICTION);
        else {
            playerFeet.setFriction(0f);
            if (vx > 0 && player.getLinearVelocity().x < PLAYER_MAX_SPEED) {
                player.applyForce(vx, 0, worldCenter.x, worldCenter.y);
            } else if (vx < 0 && player.getLinearVelocity().x > -PLAYER_MAX_SPEED) {
                player.applyForce(vx, 0, worldCenter.x, worldCenter.y);
            }
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
        box2DFactory.end();
        world.dispose();
        renderer.dispose();
    }

    private class PlayerContactListener implements ContactListener {

        private int playerFeetTouchingBoundary = WizardCategory.BOUNDARY.getID() | WizardCategory.PLAYER_FEET.getID();
        private int playerFeetTouchingDebris = WizardCategory.DEBRIS.getID() | WizardCategory.PLAYER_FEET.getID();

        @Override
        public void beginContact(Contact contact) {
            int collision = contact.getFixtureA().getFilterData().categoryBits | contact.getFixtureB().getFilterData().categoryBits;

            if (contact.isTouching()) {
                if (playerFeetTouchingBoundary == collision || playerFeetTouchingDebris == collision) {
                    isFeetIsTouchingGround = true;
                }
            }

        }

        @Override
        public void endContact(Contact contact) {
            int collision = contact.getFixtureA().getFilterData().categoryBits | contact.getFixtureB().getFilterData().categoryBits;

            if (!contact.isTouching()) {
                if (playerFeetTouchingBoundary == collision || playerFeetTouchingDebris == collision) {
                    isFeetIsTouchingGround = false;
                }
            }
        }

        @Override
        public void preSolve(Contact contact, Manifold oldManifold) {
        }

        @Override
        public void postSolve(Contact contact, ContactImpulse impulse) {
            //To change body of implemented methods use File | Settings | File Templates.
        }
    }

    private class LevelInputProcessor extends InputAdapter {
        private final BodyDef bodyDef;
        private final FixtureDef fixtureDef;

        public LevelInputProcessor(BodyDef bodyDef, FixtureDef fixtureDef) {
            this.bodyDef = bodyDef;
            this.fixtureDef = fixtureDef;
            temp = new Vector3();
        }

        @Override
        public boolean keyDown(int keycode) {
            switch (keycode) {
                case CONTROL_MOVE_LEFT:
                    controlMoveLeft = true;
                    break;
                case CONTROL_MOVE_RIGHT:
                    controlMoveRight = true;
                    break;
                case CONTROL_JUMP:
                    controlJump = true;
                    break;
            }
            return super.keyDown(keycode);    //To change body of overridden methods use File | Settings | File Templates.
        }

        // to unproject the screen coordinates to the camera/viewport
        Vector3 temp;

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (button != Input.Buttons.LEFT && button != Input.Buttons.RIGHT)
                return super.touchDown(screenX, screenY, pointer, button);    //To change body of overridden methods use File | Settings | File Templates.

            temp.set(screenX, screenY, 0);
            cam.unproject(temp);

            //Box2DFactory.resetBodyDef(bodyDef);
            bodyDef.angle = (float) Math.toRadians(Math.random() * 360);
            bodyDef.position.set(temp.x, temp.y);
            bodyDef.type = BodyDef.BodyType.DynamicBody;
            bodyDef.fixedRotation = false;
            Body body = world.createBody(bodyDef);

            //Box2DFactory.resetFixtureDef(fixtureDef);
            setFilter(WizardCategory.DEBRIS.filter, fixtureDef.filter);
            fixtureDef.density = 1;
            if (button == Input.Buttons.LEFT)
                box2DFactory.createBox(body, fixtureDef, 0, 0, .2f + (float) Math.random() * .8f, .2f + (float) Math.random() * .8f);
            else if (button == Input.Buttons.RIGHT)
                box2DFactory.createTriangle(body, fixtureDef, .2f + (float) Math.random() * .8f, .2f + (float) Math.random() * .8f);
            return super.touchDown(screenX, screenY, pointer, button);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public boolean keyUp(int keycode) {
            switch (keycode) {
                case CONTROL_MOVE_LEFT:
                    controlMoveLeft = false;
                    break;
                case CONTROL_MOVE_RIGHT:
                    controlMoveRight = false;
                    break;
                case CONTROL_JUMP:
                    controlJump = false;
                    break;
            }
            return super.keyUp(keycode);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }
}
