package wizard.screen;

import box2D.Box2DFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.*;
import wizard.Constants;
import wizard.DataLoader;
import wizard.PlayerStats;
import wizard.box2D.WizardCategory;

import java.util.List;
import java.util.Map;

import static wizard.Constants.*;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 2/18/13
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class StaticLevel extends Level {
    private static final int playerFeetTouchingBoundary = WizardCategory.BOUNDARY.getID() | WizardCategory.PLAYER_FEET.getID();
    private static final int playerFeetTouchingDebris = WizardCategory.DEBRIS.getID() | WizardCategory.PLAYER_FEET.getID();

    // DRAWING OBJECTS
    private final Box2DDebugRenderer box2DDebugRenderer;
    private final ShapeRenderer renderer;
    private final SpriteBatch spritebatch;

    // INPUT HANDLING
    boolean controlMoveLeft = false;
    boolean controlMoveRight = false;
    boolean controlCrouch = false;
    private boolean controlJump = false;
    // LIBGDX OBJECTS
    private GL11 gl;
    private Fixture rightEdge;
    private Fixture leftEdge;
    private Fixture floorEdge;
    private Fixture playerCrouching;

    private final BitmapFont font;
    private String currentlyLoadedPlayer;

    public StaticLevel() {
        super();
        playerStats = DEFAULT_PLAYER_STATS;
        world = new World(new Vector2(0, GRAVITY_Y_DEFAULT), true);

        gl = Gdx.graphics.getGL11();

        World.setVelocityThreshold(.1f);
        box2DDebugRenderer = new Box2DDebugRenderer(true, true, true, true, true);
        renderer = new ShapeRenderer();
        spritebatch = new SpriteBatch();
        //spritebatch.enableBlending();

        font = new BitmapFont(Gdx.files.internal("com/badlogic/gdx/utils/arial-15.fnt"), false);
        //font.setScale(.5f, .5f);


        box2DFactory.begin();

        createPlayer(bodyDef, fixtureDef, box2DFactory);
        createPlatform(bodyDef, fixtureDef, box2DFactory);

        Gdx.input.setInputProcessor(new LevelInputProcessor());
    }

    public static void setFilter(Filter filter, Filter target) {
        target.categoryBits = filter.categoryBits;
        target.groupIndex = filter.groupIndex;
        target.maskBits = filter.maskBits;
    }

    private void createPlatform(BodyDef bodyDef, FixtureDef fixtureDef, Box2DFactory box2DFactory) {
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);
        levelBody = world.createBody(bodyDef);

        setFilter(WizardCategory.BOUNDARY.filter, fixtureDef.filter);
        fixtureDef.isSensor = false;
        fixtureDef.friction = .2f;
        floorEdge = box2DFactory.createEdge(levelBody, fixtureDef, 0, 0, Constants.LEVEL_WIDTH, 0);
        leftEdge = box2DFactory.createEdge(levelBody, fixtureDef, 0, 0, 0, Constants.LEVEL_HEIGHT);
        rightEdge = box2DFactory.createEdge(levelBody, fixtureDef, Constants.LEVEL_WIDTH, 0, Constants.LEVEL_WIDTH, Constants.LEVEL_HEIGHT);

        box2DFactory.createEdge(levelBody, fixtureDef, 0, 1, 5, 1);
        box2DFactory.createEdge(levelBody, fixtureDef, 7, 2, 9, 2);
        box2DFactory.createEdge(levelBody, fixtureDef, 10, 8f, 11, 8f);
        box2DFactory.createEdge(levelBody, fixtureDef, 10, 1, 11, 1);

        box2DFactory.createEdge(levelBody, fixtureDef, 15, 0f, 20, 2f);
    }

    private void createPlayer(BodyDef bodyDef, FixtureDef fixtureDef, Box2DFactory box2DFactory) {
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.bullet = true;
        bodyDef.position.set(2, 2);
        bodyDef.fixedRotation = true;
        player = world.createBody(bodyDef);
        setFilter(WizardCategory.PLAYER.filter, fixtureDef.filter);
        fixtureDef.density = playerStats.get(PlayerStats.DENSITY);
        fixtureDef.friction = 0f;
        playerBox = box2DFactory.createBox(player, fixtureDef, 0, 0, playerStats.get(PlayerStats.WIDTH), playerStats.get(PlayerStats.HEIGHT));
        fixtureDef.isSensor = true;
        //playerCrouching = box2DFactory.createBox(player,fixtureDef,0,0,PLAYER_BOUNDARY_WIDTH,PLAYER_BOUNDARY_HEIGHT/2f);

        fixtureDef.isSensor = false;
        fixtureDef.density = 0;
        setFilter(WizardCategory.PLAYER_FEET.filter, fixtureDef.filter);
        playerFeet = box2DFactory.createBox(player, fixtureDef, 0, -playerStats.get(PlayerStats.HEIGHT), playerStats.get(PlayerStats.WIDTH), .1f);

        player.resetMassData();//playerFeet = box2DFactory.createCircle(player, fixtureDef, 0, -PLAYER_BOUNDARY_HEIGHT, PLAYER_BOUNDARY_WIDTH);
    }

    @Override
    public void render(float delta) {
        // OUR OWN COLLISION NEEDS
        processGameCollisions(world.getContactList());

        // INPUT PROCESSING
        processInput(delta);


        // box2D PROCESSING
        world.step(delta, 8, 3); // RECOMMENDED IS EITHER 6,2 OR 8,3

        // RENDERING
        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        Vector2 position = player.getPosition();
        cam.position.set(position.x, position.y, 0);
        cam.update();
        cam.apply(gl);
        box2DDebugRenderer.render(world, cam.combined);

        renderDebugText(spritebatch);
    }

    private void renderDebugText(SpriteBatch spritebatch) {
        spritebatch.begin();
        spritebatch.setColor(Color.WHITE);
        font.drawMultiLine(spritebatch, "canJump=\t" + canJump + "\n" +
                "justKickedOff=\t" + justKickedOff + "\n" +
                "playerCanMoveUpwards = \t" + playerCanMoveUpwards + "\n" +
                "isFeetTouchingBoundary=\t" + isFeetTouchingBoundary + "\n" +
                "justJumped=\t" + justJumped, 0, 80);
        spritebatch.end();
    }

    private void processGameCollisions(List<Contact> contactList) {
        isFeetTouchingBoundary = false;
        for (Contact contact : contactList) {
            if (!contact.isTouching())
                continue;


            int collision = contact.getFixtureA().getFilterData().categoryBits | contact.getFixtureB().getFilterData().categoryBits;

            if (playerFeetTouchingBoundary == collision || playerFeetTouchingDebris == collision) {
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
        Vector2 worldCenter = player.getWorldCenter();
        Vector2 linearVelocity = player.getLinearVelocity();

        // JUMPING
        playerCanMoveUpwards -= delta;
        if (controlJump) {
            if (isFeetTouchingBoundary == true) {
                if (canJump == true) {
                    if (playerCanMoveUpwards <= 0) {
                        player.applyLinearImpulse(0, playerStats.get(PlayerStats.JUMP_START), worldCenter.x, worldCenter.y);
                        playerCanMoveUpwards = playerStats.get(PlayerStats.JUMP_HOLD_TIME);
                        //isFeetTouchingBoundary = false;
                        canJump = false;
                    }
                } else {
                    if (justKickedOff == false && linearVelocity.y > 0) {
                        player.applyLinearImpulse(0, playerStats.get(PlayerStats.JUMP_START), worldCenter.x, worldCenter.y);
                        justKickedOff = true;
                    }
                }
            } else {
                if (playerCanMoveUpwards > 0)
                    player.applyLinearImpulse(0, playerStats.get(PlayerStats.JUMP_HOLD_FORCE), worldCenter.x, worldCenter.y);
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
                player.applyForce(vx, 0, worldCenter.x, worldCenter.y);
            } else if (vx < 0 && linearVelocity.x > -playerStats.get(PlayerStats.MAX_SPEED)) {
                player.applyForce(vx, 0, worldCenter.x, worldCenter.y);
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
        setFilter(WizardCategory.BOUNDARY.filter, fixtureDef.filter);
        fixtureDef.friction = .2f;
        levelBody.destroyFixture(leftEdge);
        levelBody.destroyFixture(rightEdge);
        levelBody.destroyFixture(floorEdge);
        floorEdge = box2DFactory.createEdge(levelBody, fixtureDef, 0, 0, Constants.LEVEL_WIDTH, 0);
        leftEdge = box2DFactory.createEdge(levelBody, fixtureDef, 0, 0, 0, Constants.LEVEL_HEIGHT);
        rightEdge = box2DFactory.createEdge(levelBody, fixtureDef, Constants.LEVEL_WIDTH, 0, Constants.LEVEL_WIDTH, Constants.LEVEL_HEIGHT);
        float ladderX = Constants.LEVEL_WIDTH - 2;
        for (float i = 1; i < 20; i++) {
            box2DFactory.createEdge(levelBody, fixtureDef, ladderX, i, Constants.LEVEL_WIDTH, i);
        }
        cam.setToOrtho(false, 20 * ratio, 20);
        cam.update();
        renderer.setProjectionMatrix(cam.combined);
        //spritebatch.setProjectionMatrix(cam.combined);
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
        spritebatch.dispose();
        font.dispose();
        box2DFactory.end();
        world.dispose();
        renderer.dispose();
    }

    private class LevelInputProcessor extends InputAdapter {
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
            }
            return super.keyDown(keycode);    //To change body of overridden methods use File | Settings | File Templates.
        }

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
                case CONTROL_RELOAD_PLAYER:

                    if (currentlyLoadedPlayer == null) {
                        setStatsAndResetPlayer(DEFAULT_PLAYER_STATS);
                        break;
                    }

                    setStatsAndResetPlayer(DataLoader.loadPlayerObject(currentlyLoadedPlayer));
                    break;
                case CONTROL_LOAD_PLAYER:
                    //todo need to figure out how to ask for an input (jdialogbox equivalent)
                    break;
                case CONTROL_LOAD_LEVEL:
                    break;
                case CONTROL_RELOAD_LEVEL:
                    break;

            }
            return super.keyUp(keycode);    //To change body of overridden methods use File | Settings | File Templates.
        }
    }

    private void setStatsAndResetPlayer(Map<PlayerStats, Float> playerStats) {
        //todo set the stats of the player then reset the player(box2d) if needed
    }
}
