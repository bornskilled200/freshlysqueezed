package wizard.screen;

/**
 * Created with IntelliJ IDEA.
 * User: David Park
 * Date: 3/24/13
 * Time: 7:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class InvalidSyntaxException extends Exception {
    public InvalidSyntaxException() {
    }

    public InvalidSyntaxException(int line, String message) {
        super("Error at line " + line + ": " + message);
    }
}
