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

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

import static wizard.Constants.GRAVITY_Y_DEFAULT;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 3/12/13
 * Time: 8:10 PM
 * To change this template use File | Settings | File Templates.
 */
public class DynamicLevel extends Level {
    private static final int COMMAND_RESTART_LEVEL = Input.Keys.F1;
    private static final int COMMAND_RELOAD_LEVEL = Input.Keys.F2;
    protected final Box2DFactory box2DFactory;
    private String playerFile;
    private String levelFile;
    private String loadedLevel;
    private BodyDef bodyDef;
    private FixtureDef fixtureDef;

    public DynamicLevel(String level) {
        this(level, null);
    }

    public DynamicLevel(String level, String player) {
        super();
        playerFile = player;
        levelFile = level;

        bodyDef = new BodyDef();
        fixtureDef = new FixtureDef();
        box2DFactory = new Box2DFactory();

        box2DFactory.begin();

        setupWorld();

        Gdx.input.setInputProcessor(new CommandsInputProcessor(Gdx.input.getInputProcessor()));
    }

    private void setStatsAndResetPlayer(Map<PlayerStats, Float> playerStats) {
        //todo set the stats of the playerBody then reset the playerBody(box2d) if needed
    }

    private void setupWorld() {
        if (playerFile != null) {
            playerStats = DataLoader.loadPlayer(playerFile);
        }

        loadWorld();
    }

    private void restartWorld() {
        try {
            parseWorld(new Scanner(loadedLevel), false);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void loadWorld() {
        try {
            loadedLevel = "";
            parseWorld(new Scanner(new File(levelFile)), true);
        } catch (InvalidSyntaxException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    private void parseWorld(Scanner scanner, boolean concatenateLevelLoaded) throws InvalidSyntaxException {
        bodyDef.type = BodyDef.BodyType.StaticBody;
        bodyDef.position.set(0, 0);
        levelBody = world.createBody(bodyDef);
        setFilter(WizardCategory.BOUNDARY.filter, fixtureDef.filter);
        fixtureDef.isSensor = false;
        fixtureDef.friction = .2f;

        int line = 0;
        Map<String, Float> variables = new TreeMap<>();
        while (scanner.hasNextLine()) {
            line++;
            String nextLine = scanner.nextLine().toLowerCase();
            String[] split = nextLine.split("\\s");
            if (split.length == 0)
                continue;

            if (concatenateLevelLoaded == true)
                loadedLevel += nextLine + "\n";
            System.out.println(nextLine);
            System.out.println(Arrays.toString(split) + '\n');
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
                                throw new InvalidSyntaxException(line, "Invalid amount of arguments");
                            x2 = variables.containsKey(split[4]) ? variables.get(split[4]) : Float.parseFloat(split[4]);
                            y2 = variables.containsKey(split[5]) ? variables.get(split[5]) : Float.parseFloat(split[5]);
                            box2DFactory.createEdge(levelBody, fixtureDef, x1, y1, x2, y2);
                            break;
                        case "horizontal":
                            if (split.length != 5)
                                throw new InvalidSyntaxException(line, "Invalid amount of arguments");
                            x1 = variables.containsKey(split[2]) ? variables.get(split[2]) : Float.parseFloat(split[2]);
                            y1 = variables.containsKey(split[3]) ? variables.get(split[3]) : Float.parseFloat(split[3]);
                            width = variables.containsKey(split[4]) ? variables.get(split[4]) : Float.parseFloat(split[4]);
                            box2DFactory.createEdge(levelBody, fixtureDef, x1, y1, x1 + width, y1);
                            break;
                        case "vertical":
                            if (split.length != 5)
                                throw new InvalidSyntaxException(line, "Invalid amount of arguments");
                            x1 = variables.containsKey(split[2]) ? variables.get(split[2]) : Float.parseFloat(split[2]);
                            y1 = variables.containsKey(split[3]) ? variables.get(split[3]) : Float.parseFloat(split[3]);
                            height = variables.containsKey(split[4]) ? variables.get(split[4]) : Float.parseFloat(split[4]);
                            box2DFactory.createEdge(levelBody, fixtureDef, x1, y1, x1, y1 + height);
                            break;
                        default:
                            throw new InvalidSyntaxException(line, "Unknown Style");
                    }
                    break;
                default:
                    if (split.length != 3 || !split[1].equals("="))
                        throw new InvalidSyntaxException(line, "Unkown Function");
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
        createPlayer(bodyDef, fixtureDef, box2DFactory);
    }

    private void createPlayer(BodyDef bodyDef, FixtureDef fixtureDef, Box2DFactory box2DFactory) {
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.bullet = true;
        bodyDef.fixedRotation = true;
        playerBody = world.createBody(bodyDef);

        setFilter(WizardCategory.PLAYER.filter, fixtureDef.filter);
        fixtureDef.density = playerStats.get(PlayerStats.DENSITY);
        fixtureDef.friction = 0f;
        playerBox = box2DFactory.createBox(playerBody, fixtureDef, 0, 0, playerStats.get(PlayerStats.WIDTH), playerStats.get(PlayerStats.HEIGHT));
        //fixtureDef.isSensor = true;
        //playerCrouching = box2DFactory.createBox(playerBody,fixtureDef,0,0,PLAYER_BOUNDARY_WIDTH,PLAYER_BOUNDARY_HEIGHT/2f);

        fixtureDef.isSensor = false;
        fixtureDef.density = 0;
        setFilter(WizardCategory.PLAYER_FEET.filter, fixtureDef.filter);
        playerFeet = box2DFactory.createBox(playerBody, fixtureDef, 0, -playerStats.get(PlayerStats.HEIGHT), playerStats.get(PlayerStats.WIDTH), .2f);
        //playerFeet = box2DFactory.createCircle(playerBody, fixtureDef, 0, -PLAYER_BOUNDARY_HEIGHT, PLAYER_BOUNDARY_WIDTH);
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
