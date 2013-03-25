package wizard.screen;

import box2D.Box2DFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import wizard.GameState;
import wizard.PlayerStats;
import wizard.box2D.WizardCategory;

import java.util.*;

import static wizard.Constants.*;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 3/12/13
 * Time: 8:10 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Level implements Screen {
    public static final Map<PlayerStats, Float> DEFAULT_PLAYER_STATS;
    public static final int PLAYER_FEET_TOUCHING_BOUNDARY = WizardCategory.BOUNDARY.getID() | WizardCategory.PLAYER_FEET.getID();
    public static final int PLAYER_FEET_TOUCHING_DEBRIS = WizardCategory.DEBRIS.getID() | WizardCategory.PLAYER_FEET.getID();

    public World getWorld() {
        return world;
    }

    public void setWorld(World world) {
        this.world = world;
    }

    public void setGameState(GameState gameState) {
        this.gameState = gameState;
    }

    private LevelInputProcessor inputProcessor;

    public void setLevelInputProcessor(LevelInputProcessor levelInputProcessor) {
        this.inputProcessor = levelInputProcessor;
    }

    // LIBGDX OBJECTS
    protected final GL11 gl;
    protected final OrthographicCamera cam;
    protected final SpriteBatch spritebatch;
    protected final ShapeRenderer renderer;

    // BOX2D STUFF
    protected World world;
    protected Body playerBody;
    protected Fixture playerFeet;
    protected Fixture playerBox;
    protected Body levelBody;

    // GAME VARIABLES
    protected float playerCanMoveUpwards; //when it it >0 then it can still move upwards
    protected boolean justKickedOff = false;
    protected boolean justJumped = false;
    // todo turn these 3 boolean to a state variable
    protected boolean wasMoving = false;
    protected boolean isFeetTouchingBoundary = true;
    public boolean canJump = false;
    protected Map<PlayerStats, Float> playerStats;


    static {
        EnumMap<PlayerStats, Float> map = new EnumMap<PlayerStats, Float>(PlayerStats.class);
        map.put(PlayerStats.WIDTH, .76f);
        map.put(PlayerStats.HEIGHT, 1.28f);

        map.put(PlayerStats.DENSITY, 1f);

        map.put(PlayerStats.WALK_SPEED, 200f);
        map.put(PlayerStats.MAX_SPEED, 8f);
        map.put(PlayerStats.STOP_FRICTION, 5f);

        map.put(PlayerStats.JUMP_START, 18f);
        map.put(PlayerStats.JUMP_HOLD_FORCE, 200f);
        map.put(PlayerStats.JUMP_HOLD_TIME, .12f);

        map.put(PlayerStats.KICKOFF_START, 10f);

        EnumSet<PlayerStats> playerStatses = EnumSet.complementOf(EnumSet.copyOf(map.keySet()));
        for (PlayerStats playerStatse : playerStatses) {
            map.put(playerStatse, Float.NaN);
        }
        DEFAULT_PLAYER_STATS = Collections.unmodifiableMap(map);
    }

    //boolean controlCrouch = false;
    protected boolean controlJump = false;
    // INPUT HANDLING
    boolean controlMoveLeft = false;
    boolean controlMoveRight = false;
    private GameState gameState;

    public Level() {
        cam = new OrthographicCamera(20, 20);
        spritebatch = new SpriteBatch();
        renderer = new ShapeRenderer();

        playerStats = DEFAULT_PLAYER_STATS;
        World.setVelocityThreshold(.1f);
        world = new World(new Vector2(0, GRAVITY_Y_DEFAULT), true);
        gl = Gdx.graphics.getGL11();

        inputProcessor = new LevelInputProcessor();
        Gdx.input.setInputProcessor(inputProcessor);
        gameState = GameState.RUNNING;
    }

    public static void setFilter(Filter filter, Filter target) {
        target.categoryBits = filter.categoryBits;
        target.groupIndex = filter.groupIndex;
        target.maskBits = filter.maskBits;
    }

    @Override
    public void show() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void hide() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void pause() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resume() {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public LevelInputProcessor getInputProcessor() {
        return inputProcessor;
    }

    @Override
    public void dispose() {
        world.dispose();
        spritebatch.dispose();
    }

    @Override
    public void render(float delta) {
        if (gameState == GameState.RUNNING) {
            // OUR OWN COLLISION NEEDS
            processGameCollisions(world.getContactList());

            // INPUT PROCESSING
            processInput(delta);


            // box2D PROCESSING
            world.step(delta, 8, 3); // RECOMMENDED IS EITHER 6,2 OR 8,3
        }

        // RENDERING
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        Vector2 position = playerBody.getPosition();
        cam.position.set(position.x, position.y, 0);
        cam.update();
        cam.apply(gl);
    }

    private void processGameCollisions(List<Contact> contactList) {
        isFeetTouchingBoundary = false;
        for (Contact contact : contactList) {
            if (!contact.isTouching())
                continue;


            int collision = contact.getFixtureA().getFilterData().categoryBits | contact.getFixtureB().getFilterData().categoryBits;

            if (PLAYER_FEET_TOUCHING_BOUNDARY == collision || PLAYER_FEET_TOUCHING_DEBRIS == collision) {
                isFeetTouchingBoundary = true;

                Vector2 normal = contact.getWorldManifold().getNormal();
                if (normal.y != 0) {
                    canJump = true;
                    justKickedOff = false;
                }
            }
        }
        if (isFeetTouchingBoundary == false) {
            justKickedOff = false;
        }
    }

    private void processInput(float delta) {
        Vector2 worldCenter = playerBody.getWorldCenter();
        Vector2 linearVelocity = playerBody.getLinearVelocity();

        // JUMPING
        playerCanMoveUpwards -= delta;
        if (controlJump) {
            if (isFeetTouchingBoundary == true) {
                if (canJump == true) {
                    if (playerCanMoveUpwards <= 0) {
                        playerBody.applyLinearImpulse(0, playerStats.get(PlayerStats.JUMP_START), worldCenter.x, worldCenter.y);
                        playerCanMoveUpwards = playerStats.get(PlayerStats.JUMP_HOLD_TIME);
                        canJump = false;
                    }
                } else {
                    if (playerCanMoveUpwards > 0)
                        playerBody.applyForce(0, playerStats.get(PlayerStats.JUMP_HOLD_FORCE), worldCenter.x, worldCenter.y);
                    else if (justKickedOff == false && linearVelocity.y > 0) {
                        playerBody.applyLinearImpulse(0, playerStats.get(PlayerStats.KICKOFF_START), worldCenter.x, worldCenter.y);
                        justKickedOff = true;
                    }
                }
            } else {
                if (playerCanMoveUpwards > 0)
                    playerBody.applyForce(0, playerStats.get(PlayerStats.JUMP_HOLD_FORCE), worldCenter.x, worldCenter.y);
            }
        } else playerCanMoveUpwards = 0;

        // HORIZONTAL MOVEMENT
        float vx = 0;
        if (controlMoveLeft == true)
            vx += -playerStats.get(PlayerStats.WALK_SPEED);
        if (controlMoveRight == true)
            vx += playerStats.get(PlayerStats.WALK_SPEED);

        if (vx == 0) {
            playerFeet.setFriction(playerStats.get(PlayerStats.STOP_FRICTION));
            if (wasMoving == true) {
                resetContactsFriction();
                wasMoving = false;
            }
        } else {
            if (vx > 0 && linearVelocity.x < playerStats.get(PlayerStats.MAX_SPEED)) {
                playerBody.applyForce(vx, 0, worldCenter.x, worldCenter.y);
            } else if (vx < 0 && linearVelocity.x > -playerStats.get(PlayerStats.MAX_SPEED)) {
                playerBody.applyForce(vx, 0, worldCenter.x, worldCenter.y);
            }
            playerFeet.setFriction(0f);
            if (wasMoving == false) {
                resetContactsFriction();
                wasMoving = true;
            }
        }
    }

    private void resetContactsFriction() {
        for (Contact a : world.getContactList())
            a.resetFriction();
    }

    @Override
    public void resize(int width, int height) {
        float ratio = (float) width / height;
        cam.setToOrtho(false, 20 * ratio, 20);
        cam.update();
        renderer.setProjectionMatrix(cam.combined);
    }

    public GameState getGameState() {
        return gameState;
    }

    protected class LevelInputProcessor extends InputAdapter {
        // to unproject the screen coordinates to the camera/viewport
        Vector3 temp;

        public LevelInputProcessor() {
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
                default:
                    return false;
            }
            return true;    //To change body of overridden methods use File | Settings | File Templates.
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
                default:
                    return false;
            }
            return true;    //To change body of overridden methods use File | Settings | File Templates.
        }
    }
}
