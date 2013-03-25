package wizard;

import com.badlogic.gdx.Input;

/**
 * Change this for per character and have it so it changeable
 * User: David Park
 * Date: 2/25/13
 * Time: 10:46 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Constants {
    public final static String ASSETS_PATH = "/data/";


    // CONSTANTS
    float GRAVITY_Y_DEFAULT = -9.8f;
    int CONTROL_MOVE_LEFT = Input.Keys.A;
    int CONTROL_MOVE_RIGHT = Input.Keys.D;
    int CONTROL_JUMP = Input.Keys.W;
    int CONTROL_CROUCH = Input.Keys.S;
    int CONTROL_RELOAD_PLAYER = Input.Keys.F5;
    int CONTROL_LOAD_PLAYER = Input.Keys.F6;
    int CONTROL_RELOAD_LEVEL = Input.Keys.F2;
    int CONTROL_LOAD_LEVEL = Input.Keys.F1;
    float LEVEL_WIDTH = 30;
    float LEVEL_HEIGHT = 80;
}
