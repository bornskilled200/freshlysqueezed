package wizard.screen;

import com.badlogic.gdx.Input;

import javax.script.ScriptException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;

/**
 * Created with IntelliJ IDEA.
 * User: David Park
 * Date: 3/31/13
 * Time: 3:20 PM
 * To change this template use File | Settings | File Templates.
 */
public abstract class DynamicLevel extends Level {
    protected static final int COMMAND_RESTART_LEVEL = Input.Keys.F1;
    protected static final int COMMAND_RELOAD_LEVEL = Input.Keys.F2;

    private String playerFile;
    private String levelFile;

    public DynamicLevel(String level, String player) {
        super();
        playerFile = player;
        levelFile = level;
    }

    public void setupWorld() {
        parsePlayer();

        loadWorld();
    }

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

}
