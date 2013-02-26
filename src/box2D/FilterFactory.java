package box2D;

import com.badlogic.gdx.physics.box2d.Filter;

/**
 * Created with IntelliJ IDEA.
 * User: David Park
 * Date: 2/25/13
 * Time: 9:44 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilterFactory {

    public static Filter createFilter(int id) {
        if (id >= Short.SIZE)
            throw new IllegalStateException("Cannot make more than " + Short.SIZE + " id's");
        Filter filter = new Filter();
        filter.categoryBits = (short) (1 << id);
        return filter;
    }

    public static Filter createFilter(short id) {
        Filter filter = new Filter();
        filter.categoryBits = (short) (1 << id);
        return filter;
    }
}
