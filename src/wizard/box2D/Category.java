package wizard.box2D;

import com.badlogic.gdx.physics.box2d.Filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 12/21/12
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
public enum Category {
    PLAYER, DEBRIS, WIZARD, BOUNDARY;

    public final Filter filter;

    Category() {
        int current = ordinal();
        if (current >= Short.SIZE)
            throw new IllegalStateException("Cannot make more than " + Short.SIZE + " id's");
        filter = new Filter();
        filter.categoryBits = (short) (1 << current);
    }

    private static Map<Short, Category> idToCategory;

    public short getID() {
        return filter.categoryBits;
    }

    static {
        Category[] values = values();
        idToCategory = new HashMap<>(values.length);
        for (Category category : values) {
            idToCategory.put(category.getID(), category);
        }
        idToCategory = Collections.unmodifiableMap(idToCategory);
    }

    public static Category getCategory(short id) {
        return idToCategory.get(id);
    }

    public static Category getCategory(Filter id) {
        return idToCategory.get(id.categoryBits);
    }
}
