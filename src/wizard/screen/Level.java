package wizard.screen;

import box2D.Box2DFactory;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.physics.box2d.*;
import wizard.PlayerStats;

import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 3/12/13
 * Time: 8:10 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class Level implements Screen {

    protected final OrthographicCamera cam;
    protected final Box2DFactory box2DFactory;
    protected final BodyDef bodyDef;
    protected final FixtureDef fixtureDef;
    public boolean canJump = false;
    // BOX2D STUFF
    protected World world;
    protected Body player;
    protected Fixture playerFeet;
    protected Fixture playerBox;
    protected Body levelBody;
    // GAME VARIABLES
    protected boolean isFeetTouchingBoundary = true;
    protected float playerCanMoveUpwards; //when it it >0 then it can still move upwards
    protected boolean justKickedOff = false;
    protected boolean justJumped = false;
    protected boolean wasMoving = false;
    protected Map<PlayerStats, Float> playerStats;

    public Level() {
        box2DFactory = new Box2DFactory();
        bodyDef = new BodyDef();
        cam = new OrthographicCamera(20, 20);
        fixtureDef = new FixtureDef();
    }

    @Override
    public void render(float delta) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void resize(int width, int height) {
        //To change body of implemented methods use File | Settings | File Templates.
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

    @Override
    public void dispose() {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
