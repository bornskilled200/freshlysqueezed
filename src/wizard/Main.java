package wizard;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import wizard.screen.StaticLevel;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 2/18/13
 * Time: 6:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main extends Game {

    public static StaticLevel SCREEN;

    @Override
    public void create() {
        setScreen(SCREEN = new StaticLevel());
    }

    public static void main(String[] args) {
        new LwjglApplication(new Main());
    }
}
