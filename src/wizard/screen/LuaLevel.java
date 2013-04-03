package wizard.screen;

import box2D.Box2DFactory;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import wizard.DataLoader;
import wizard.box2D.WizardCategory;

import javax.script.*;
import java.io.Reader;

import static wizard.Constants.GRAVITY_Y_DEFAULT;

/**
 * Created with IntelliJ IDEA.
 * User: David Park
 * Date: 3/31/13
 * Time: 4:12 AM
 * To change this template use File | Settings | File Templates.
 */
public class LuaLevel extends DynamicLevel {
    private BodyDef bodyDef;
    private FixtureDef fixtureDef;
    private Box2DFactory box2DFactory;

    //private ScriptEngine scriptEngine;
    private CompiledScript loadedLevel;
    private Bindings bindings;
    private ScriptEngineFactory factory;

    public LuaLevel(String level) {
        this(level, null);
    }

    public LuaLevel(String level, String player) {
        super(level, player);

        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        ScriptEngine scriptEngine = scriptEngineManager.getEngineByExtension(".lua");
        factory = scriptEngine.getFactory();

        bodyDef = new BodyDef();
        fixtureDef = new FixtureDef();
        box2DFactory = new Box2DFactory();

        box2DFactory.begin();

        bindings = new SimpleBindings();
        bindings.put("box2DFactory", box2DFactory);
        bindings.put("fixtureDef", fixtureDef);
        bindings.put("bodyDef", bodyDef);
        setupWorld();
    }

    @Override
    public void createPlayer(float x, float y) {
        bodyDef.position.set(x,y);
        createPlayer(bodyDef, box2DFactory, fixtureDef);
    }

    @Override
    public void parsePlayer() {
        String playerFile = getPlayerFile();
        if (playerFile != null) {
            playerStats = DataLoader.loadPlayerLua(playerFile);
        }
    }

    @Override
    public void dispose() {
        super.dispose();    //To change body of overridden methods use File | Settings | File Templates.
        box2DFactory.end();
    }

    @Override
    public void restartWorld() {
        World world = getWorld();
        bindings.put("world", world);

        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);
        setFilter(WizardCategory.BOUNDARY.filter, fixtureDef.filter);
        fixtureDef.isSensor = false;
        fixtureDef.friction = .2f;

        levelBody = world.createBody(bodyDef);
        bindings.put("levelBody", levelBody);

        try {
            loadedLevel.eval(bindings);
        } catch (ScriptException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        createPlayer(bodyDef, box2DFactory, fixtureDef);
    }

    @Override
    protected void parseWorld(Reader reader, boolean isNewWorld) throws ScriptException {
        ScriptEngine scriptEngine = factory.getScriptEngine();
        loadedLevel = ((Compilable) scriptEngine).compile(reader);
        restartWorld();
    }
}
