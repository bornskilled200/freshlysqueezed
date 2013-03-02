package wizard.box2D;

import box2D.FilterFactory;
import com.badlogic.gdx.physics.box2d.Filter;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 12/21/12
 * Time: 5:41 PM
 * To change this template use File | Settings | File Templates.
 */
public enum WizardCategory {
    PLAYER, PLAYER_FEET, DEBRIS, WIZARD, BOUNDARY;

    public final Filter filter;

    WizardCategory() {
        filter = FilterFactory.createFilter(ordinal());
    }

    public short getID() {
        return filter.categoryBits;
    }
}
