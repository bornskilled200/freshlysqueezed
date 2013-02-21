package wizard.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.GL11;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;
import wizard.box2D.Category;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 2/18/13
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class Level implements Screen {
    private final float GRAVITY_X_DEFAULT = -.98f;
    private final Box2DDebugRenderer box2DDebugRenderer;
    private final ShapeRenderer renderer;
    private final OrthographicCamera cam;
    private final Body player;
    private final Fixture playerFeet;
    private final Fixture playerBox;
    private World world;
    private GL11 gl;
    private Input input;
    private boolean canPlayerJump;
    private boolean isPlayerTouchingBoundary;

    public Level() {
        world = new World(new Vector2(0, GRAVITY_X_DEFAULT), true);

        gl = Gdx.graphics.getGL11();
        cam = new OrthographicCamera();

        input = Gdx.input;
        World.setVelocityThreshold(.1f);
        box2DDebugRenderer = new Box2DDebugRenderer(true, true, true, true, true);
        renderer = new ShapeRenderer();


        //Player
        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.position.set(2, 2);
        bodyDef.fixedRotation = true;
        player = world.createBody(bodyDef);
        FixtureDef fixtureDef = new FixtureDef();
        setFilter(Category.PLAYER.filter, fixtureDef.filter);
        PolygonShape polygonShape = new PolygonShape();
        polygonShape.setAsBox(.2f, .3f);
        fixtureDef.shape = polygonShape;
        fixtureDef.density = 2f;
        fixtureDef.friction = 0;
        playerBox = player.createFixture(fixtureDef);
        CircleShape circleShape = new CircleShape();
        circleShape.setPosition(new Vector2(0, -.15f));
        circleShape.setRadius(.2f);
        fixtureDef.shape = circleShape;
        fixtureDef.isSensor = true;
        playerFeet = player.createFixture(fixtureDef);

        //Platform
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);
        Body body = world.createBody(bodyDef);
        setFilter(Category.BOUNDARY.filter, fixtureDef.filter);
        EdgeShape edgeShape = new EdgeShape();
        edgeShape.set(0, 0, 5, 0);
        fixtureDef.shape = edgeShape;
        fixtureDef.isSensor = false;
        body.createFixture(fixtureDef);
        edgeShape.set(0, 0, 0, 5);
        body.createFixture(fixtureDef);
        edgeShape.set(5, 0, 0, 5);
        body.createFixture(fixtureDef);


        world.setContactListener(new ContactListener() {
            @Override
            public void beginContact(Contact contact) {
                int collision = contact.getFixtureA().getFilterData().categoryBits | contact.getFixtureB().getFilterData().categoryBits;
                int playerTouchingBoundary = Category.BOUNDARY.getID() | Category.PLAYER.getID();

                if (playerTouchingBoundary == collision && (contact.getFixtureA() == playerFeet || contact.getFixtureB() == playerFeet))
                    canPlayerJump = true;

                if (playerTouchingBoundary == collision && (contact.getFixtureA() == playerBox || contact.getFixtureB() == playerBox))
                    isPlayerTouchingBoundary = true;

            }

            @Override
            public void endContact(Contact contact) {
                int collision = contact.getFixtureA().getFilterData().categoryBits | contact.getFixtureB().getFilterData().categoryBits;
                int playerTouchingBoundary = Category.BOUNDARY.getID() | Category.PLAYER.getID();

                if (playerTouchingBoundary == collision && (contact.getFixtureA() == playerFeet || contact.getFixtureB() == playerFeet))
                    canPlayerJump = false;

                if (playerTouchingBoundary == collision && (contact.getFixtureA() == playerBox || contact.getFixtureB() == playerBox))
                    isPlayerTouchingBoundary = false;
            }

            @Override
            public void preSolve(Contact contact, Manifold oldManifold) {
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            public void postSolve(Contact contact, ContactImpulse impulse) {
                //To change body of implemented methods use File | Settings | File Templates.
            }
        });

    }

    public static void setFilter(Filter filter, Filter target) {
        target.categoryBits = filter.categoryBits;
        target.groupIndex = filter.groupIndex;
        target.maskBits = filter.maskBits;
    }

    @Override
    public void render(float delta) {
        Vector2 worldCenter = player.getWorldCenter();
        //player.applyLinearImpulse(0,.1f, worldCenter.x,worldCenter.y);
        if (input.isKeyPressed(Input.Keys.W) && isPlayerTouchingBoundary && canPlayerJump)
            player.applyLinearImpulse(0, .2f, worldCenter.x, worldCenter.y);
        if (input.isKeyPressed(Input.Keys.A))
            player.applyForce(-.5f, 0, worldCenter.x, worldCenter.y);
        if (input.isKeyPressed(Input.Keys.D))
            player.applyForce(.5f, 0, worldCenter.x, worldCenter.y);
        world.step(delta, 3, 3);

        gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
        cam.apply(gl);
        box2DDebugRenderer.render(world, cam.combined);
    }

    @Override
    public void resize(int width, int height) {
        float ratio = (float) width / height;
        cam.setToOrtho(false, 5 * ratio, 5);
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
        //To change player of implemented methods use File | Settings | File Templates.
    }
}
