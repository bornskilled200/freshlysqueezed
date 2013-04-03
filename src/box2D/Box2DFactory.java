package box2D;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.*;

/**
 * Created with IntelliJ IDEA.
 * User: David Park
 * Date: 2/25/13
 * Time: 9:09 PM
 * To change this template use File | Settings | File Templates.
 */
public class Box2DFactory {
    private static float[] triangleVertices = new float[6];
    public EdgeShape edgeShape;
    public CircleShape circleShape;
    public PolygonShape polygonShape;
    public boolean isRunning;

    public Box2DFactory() {
        isRunning = false;
    }

    public void begin() {
        if (isRunning == true)
            throw new IllegalStateException("Your not supposed to call begin again when you already called this method!");
        edgeShape = new EdgeShape();
        circleShape = new CircleShape();
        polygonShape = new PolygonShape();
        isRunning = true;
    }

    public Fixture createEdge(Body body, FixtureDef fixtureDef, float x1, float y1, float x2, float y2) {
        return createEdge(edgeShape, body, fixtureDef, x1, y1, x2, y2);
    }

    private static final Vector2 temp = new Vector2();

    public Fixture createCircle(Body body, FixtureDef fixtureDef, float x, float y, float radius) {
        return createCircle(circleShape, body, fixtureDef, temp.set(x, y), radius);
    }

    public Fixture createBox(Body body, FixtureDef fixtureDef, float x, float y, float width, float height) {
        return createBox(polygonShape, body, fixtureDef, temp.set(x, y), width, height, 0);
    }

    public Fixture createBox(Body body, FixtureDef fixtureDef, float x, float y, float width, float height, float angle) {
        return createBox(polygonShape, body, fixtureDef, temp.set(x, y), width, height, angle);
    }

    public void end() {
        isRunning = false;
        edgeShape.dispose();
        edgeShape = null;
        circleShape.dispose();
        circleShape = null;
        polygonShape.dispose();
        polygonShape = null;
    }

    public void dispose() {
        if (isRunning)
            end();
    }

    //~~~~~~~~~~~~

    public static Fixture createEdge(EdgeShape edgeShape, Body body, FixtureDef fixtureDef, float x1, float y1, float x2, float y2) {
        edgeShape.set(x1, y1, x2, y2);
        return createFixture(edgeShape, body, fixtureDef);
    }

    public static Fixture createCircle(CircleShape circleShape, Body body, FixtureDef fixtureDef, Vector2 position, float radius) {
        circleShape.setPosition(position);
        circleShape.setRadius(radius);
        return createFixture(circleShape, body, fixtureDef);
    }

    public static Fixture createBox(PolygonShape polygonShape, Body body, FixtureDef fixtureDef, Vector2 position, float width, float height, float angle) {
        polygonShape.setAsBox(width, height, position, angle);
        return createFixture(polygonShape, body, fixtureDef);  //To change body of created methods use File | Settings | File Templates.
    }

    public static Fixture createTriangle(PolygonShape polygonShape, Body body, FixtureDef fixtureDef, float width, float height) {
        triangleVertices[0] = -width;
        triangleVertices[1] = -height;
        triangleVertices[2] = width;
        triangleVertices[3] = -height;
        triangleVertices[4] = 0;
        triangleVertices[5] = height;
        polygonShape.set(triangleVertices);
        return createFixture(polygonShape, body, fixtureDef);  //To change body of created methods use File | Settings | File Templates.
    }

    private static Fixture createFixture(Shape polygonShape, Body body, FixtureDef fixtureDef) {
        fixtureDef.shape = polygonShape;
        Fixture fixture = body.createFixture(fixtureDef);
        fixtureDef.shape = null;
        return fixture;
    }

    //~~

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

    public Fixture createTriangle(Body body, FixtureDef fixtureDef, float width, float height) {

        return createTriangle(polygonShape, body, fixtureDef, width, height);  //To change body of created methods use File | Settings | File Templates.
    }
}
