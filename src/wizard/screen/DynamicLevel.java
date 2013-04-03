package wizard.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.World;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

import static wizard.Constants.*;

/**
 * Created with IntelliJ IDEA.
 * User: David Park
 * Date: 3/31/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class DynamicLevel extends Level {
    private CommandsInputProcessor commandsInputProcessor;

    private String playerFile;
    private String levelFile;

    public DynamicLevel(String level, String player) {
        super();
        playerFile = player;
        levelFile = level;

        commandsInputProcessor = new CommandsInputProcessor(super.getInputProcessor());
        Gdx.input.setInputProcessor(commandsInputProcessor);
    }

    public void setupWorld() {
        parsePlayer();

        loadWorld();
    }

    public abstract void createPlayer(float x, float y);

    public abstract void parsePlayer();

    public abstract void restartWorld();

    public void loadWorld() {
        try {
            parseWorld(new FileReader(new File(getLevelFile())), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ScriptException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    protected abstract void parseWorld(Reader reader, boolean isNewWorld) throws ScriptException;

    public String getPlayerFile() {
        return playerFile;
    }

    public void setPlayerFile(String playerFile) {
        this.playerFile = playerFile;
    }

    public String getLevelFile() {
        return levelFile;
    }

    public void setLevelFile(String levelFile) {
        this.levelFile = levelFile;
    }

    @Override
    public String toString() {
        return "playerFile='" + ((playerFile == null) ? "null" : new File(playerFile).getAbsolutePath()) + '\n' +
                "levelFile='" + new File(levelFile).getAbsolutePath();
    }

    @Override
    public InputProcessor getInputProcessor() {
        return commandsInputProcessor;    //To change body of overridden methods use File | Settings | File Templates.
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
                    newWorld();
                    restartWorld();
                    break;
                case COMMAND_RELOAD_LEVEL:
                    newWorld();
                    loadWorld();
                    break;
                case COMMAND_RELOAD_PLAYER:
                    if (playerStats == DEFAULT_PLAYER_STATS)
                        break;

                    Vector2 playerPosition = playerBody.getWorldCenter();
                    getWorld().destroyBody(playerBody);
                    parsePlayer();
                    //playerStats = DataLoader.loadPlayerIni(getPlayerFile());
                    createPlayer(playerPosition.x, playerPosition.y);
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
