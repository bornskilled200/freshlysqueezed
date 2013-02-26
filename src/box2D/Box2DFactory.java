package box2D;

import com.badlogic.gdx.physics.box2d.*;

/**
 * Created with IntelliJ IDEA.
 * User: David Park
 * Date: 2/25/13
 * Time: 9:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Box2DFactory {
    public static FixtureDef resetFixtureDef(FixtureDef fixtureDef) {
        fixtureDef.shape = null;
        fixtureDef.friction = 0.2f;
        fixtureDef.restitution = 0;
        fixtureDef.density = 0;
        fixtureDef.isSensor = false;
        fixtureDef.filter.categoryBits = 0;
        fixtureDef.filter.maskBits = 0;
        fixtureDef.filter.groupIndex = 0;
        return fixtureDef;
    }

    public static BodyDef resetBodyDef(BodyDef bodyDef) {
        bodyDef.position.set(0, 0);
        bodyDef.angle = 0;
        bodyDef.linearVelocity.set(0, 0);
        bodyDef.angularVelocity = 0;
        bodyDef.linearDamping = 0;
        bodyDef.angularDamping = 0;
        bodyDef.allowSleep = true;
        bodyDef.awake = true;
        bodyDef.fixedRotation = false;
        bodyDef.bullet = false;
        bodyDef.active = true;
        bodyDef.gravityScale = 1;
        return bodyDef;
    }
}
