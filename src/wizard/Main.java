package wizard;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import wizard.screen.DebugLevel;
import wizard.screen.DynamicLevel;
import wizard.screen.StaticLevel;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 2/18/13
 * Time: 6:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main extends Game {

    @Override
    public void create() {
        setScreen(new DebugLevel(new DynamicLevel("./TrainingLevel.ini", "./TrainingPlayer.ini")));
    }

    public static void main(String[] args) {
        new LwjglApplication(new Main());
    }
}
