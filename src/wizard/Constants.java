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

    float GRAVITY_Y_DEFAULT = -32f;

    int CONTROL_MOVE_LEFT = Input.Keys.A;
    int CONTROL_MOVE_RIGHT = Input.Keys.D;
    int CONTROL_JUMP = Input.Keys.W;
    int CONTROL_CROUCH = Input.Keys.S;
    int COMMAND_RELOAD_PLAYER = Input.Keys.NUM_4;
    int COMMAND_RESTART_LEVEL = Input.Keys.NUM_1;
    int COMMAND_RELOAD_LEVEL = Input.Keys.NUM_2;
}
