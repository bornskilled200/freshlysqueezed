package wizard.screen;

import box2D.Box2DFactory;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import wizard.DataLoader;
import wizard.PlayerStats;
import wizard.box2D.WizardCategory;

import javax.script.ScriptException;
import java.io.*;
import java.util.*;

import static wizard.Constants.GRAVITY_Y_DEFAULT;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 3/12/13
 * Time: 8:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class FakeScriptLevel extends DynamicLevel {
    protected final Box2DFactory box2DFactory;

    protected BodyDef bodyDef;
    protected FixtureDef fixtureDef;

    // SCRIPT VARIABLES
    private String loadedLevel; // Already loaded, for restarting the level

    public FakeScriptLevel(String level) {
        this(level, null);
    }

    public FakeScriptLevel(String level, String player) {
        super(player, level);

        bodyDef = new BodyDef();
        fixtureDef = new FixtureDef();
        box2DFactory = new Box2DFactory();

        box2DFactory.begin();

        setupWorld();

        Gdx.input.setInputProcessor(new CommandsInputProcessor(Gdx.input.getInputProcessor()));
    }

    @Override
    protected void parseWorld(Reader reader, boolean isNewWorld) throws ScriptException {
        if (isNewWorld == true)
            loadedLevel = "";

        Scanner scanner = new Scanner(reader);

        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);
        levelBody = world.createBody(bodyDef);
        setFilter(WizardCategory.BOUNDARY.filter, fixtureDef.filter);
        fixtureDef.isSensor = false;
        fixtureDef.friction = .2f;

        int line = 0;
        Map<String, Float> variables = new TreeMap<>();
        String levelFile = getLevelFile();
        while (scanner.hasNextLine()) {
            line++;
            String nextLine = scanner.nextLine().toLowerCase(Locale.ENGLISH);
            String[] split = nextLine.split("\\s");
            if (split.length == 0)
                continue;

            if (isNewWorld == true)
                loadedLevel += nextLine + "\n";
            float x1, y1, x2, y2, width, height, size;
            switch (split[0]) {
                case "":
                    //Tis a comment
                    break;
                case "line":
                    x1 = variables.containsKey(split[2]) ? variables.get(split[2]) : Float.parseFloat(split[2]);
                    y1 = variables.containsKey(split[3]) ? variables.get(split[3]) : Float.parseFloat(split[3]);
                    switch (split[1]) {
                        case "points":
                            if (split.length != 6)
                                throw new ScriptException("Invalid amount of arguments", levelFile, line);
                            x2 = variables.containsKey(split[4]) ? variables.get(split[4]) : Float.parseFloat(split[4]);
                            y2 = variables.containsKey(split[5]) ? variables.get(split[5]) : Float.parseFloat(split[5]);
                            box2DFactory.createEdge(levelBody, fixtureDef, x1, y1, x2, y2);
                            break;
                        case "horizontal":
                            if (split.length != 5)
                                throw new ScriptException("Invalid amount of arguments", levelFile, line);
                            x1 = variables.containsKey(split[2]) ? variables.get(split[2]) : Float.parseFloat(split[2]);
                            y1 = variables.containsKey(split[3]) ? variables.get(split[3]) : Float.parseFloat(split[3]);
                            width = variables.containsKey(split[4]) ? variables.get(split[4]) : Float.parseFloat(split[4]);
                            box2DFactory.createEdge(levelBody, fixtureDef, x1, y1, x1 + width, y1);
                            break;
                        case "vertical":
                            if (split.length != 5)
                                throw new ScriptException("Invalid amount of arguments", levelFile, line);
                            x1 = variables.containsKey(split[2]) ? variables.get(split[2]) : Float.parseFloat(split[2]);
                            y1 = variables.containsKey(split[3]) ? variables.get(split[3]) : Float.parseFloat(split[3]);
                            height = variables.containsKey(split[4]) ? variables.get(split[4]) : Float.parseFloat(split[4]);
                            box2DFactory.createEdge(levelBody, fixtureDef, x1, y1, x1, y1 + height);
                            break;
                        default:
                            throw new ScriptException("Unknown Style", levelFile, line);
                    }
                    break;
                default:
                    if (split.length != 3 || !split[1].equals("="))
                        throw new ScriptException("Unknown Function", levelFile, line);
                    // Otherwise it is a variable
                    float value = Float.parseFloat(split[2]);
                    variables.put(split[0], value);
            }
        }
        if (variables.containsKey("gravity_y"))
            world.setGravity(new Vector2(0, variables.get("gravity_y")));
        if (variables.containsKey("player_spawn_x") && variables.containsKey("player_spawn_y")) {
            bodyDef.position.set(variables.get("player_spawn_x"), variables.get("player_spawn_y"));
        }
        createPlayer(bodyDef, box2DFactory, fixtureDef);
    }

    @Override
    public void dispose() {
        box2DFactory.end();
        super.dispose();    //To change body of overridden methods use File | Settings | File Templates.
    }

    public void parsePlayer() {
        String playerFile = getPlayerFile();
        if (playerFile != null) {
            playerStats = DataLoader.loadPlayerIni(playerFile);
        }
    }

    public void restartWorld() {
        try {
            parseWorld(new StringReader(loadedLevel), false);
        } catch (ScriptException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public class CommandsInputProcessor extends InputAdapter {
        private static final int COMMAND_RELOAD_PLAYER = Input.Keys.F4;
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

            switch (keycode) {
                case COMMAND_RESTART_LEVEL:
                    world.dispose();
                    world = new World(new Vector2(0, GRAVITY_Y_DEFAULT), true);
                    restartWorld();
                    break;
                case COMMAND_RELOAD_LEVEL:
                    world.dispose();
                    world = new World(new Vector2(0, GRAVITY_Y_DEFAULT), true);
                    loadWorld();
                    break;
                case COMMAND_RELOAD_PLAYER:
                    if (playerStats == DEFAULT_PLAYER_STATS)
                        break;

                    Vector2 playerPosition = playerBody.getWorldCenter();
                    world.destroyBody(playerBody);
                    playerStats = DataLoader.loadPlayerIni(getPlayerFile());
                    bodyDef.position.set(playerPosition);
                    createPlayer(bodyDef, box2DFactory, fixtureDef);
                    break;
                default:
                    return false;
            }
            return true;    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public boolean keyTyped(char character) {
            return levelInputProcessor.keyTyped(character);
        }
    }
}
