package wizard;

/**
 * Change this for per character and have it so it changeable
 * User: David Park
 * Date: 2/25/13
 * Time: 10:46 PM
 * To change this template use File | Settings | File Templates.
 */
public interface Constants {
    public final static String ASSETS_PATH = "/data/";

    // LETS HAVE A PLAYER THAT IS SIMILAR TO A AVERAGE (MALE?) BODY
    float PLAYER_BOUNDARY_WIDTH = .76f;
    float PLAYER_BOUNDARY_HEIGHT = 1.28f;

    float PLAYER_DENSITY = 1f;

    float PLAYER_WALK_SPEED = 150f;
    // Example of a defined variable but no number being assigned to, in this case it just assumes same as walk speed
    float PLAYER_RUN_SPEED = Float.NaN;
    float PLAYER_STOP_FRICTION = 5;

    float PLAYER_JUMP_START = 10f;
    float PLAYER_JUMP_CONSTANT = 100f;
    float PLAYER_JUMP_FLOAT_TIME = .25f;
    float PLAYER_MAX_SPEED = 8f;
    // CONSTANTS
    float GRAVITY_Y_DEFAULT = -9.8f;
}
