package wizard.screen;

import box2D.Box2DFactory;
import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.utils.Clipboard;
import wizard.GameState;
import wizard.box2D.WizardCategory;

import javax.script.*;
import java.util.EnumMap;

/**
 * Created with IntelliJ IDEA.
 * User: David Park
 * Date: 3/20/13
 * Time: 8:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class DebugLevel implements Screen {
    public static final int DEBUG_NOW = Input.Keys.F1;
    public static final int DEBUG_ON_MOUSE_MOVED = Input.Keys.F2;
    public static final int DEBUG_ON_TOUCH_DRAGGED = Input.Keys.F3;
    public static final int DEBUG_ON_TOUCH_UP = Input.Keys.F4;
    public static final int DEBUG_ON_TOUCH_DOWN = Input.Keys.F5;
    public static final int DEBUG_ON_KEY_UP = Input.Keys.F6;
    public static final int DEBUG_ON_KEY_DOWN = Input.Keys.F7;
    public static final int DEBUG_AFTER_RENDER = Input.Keys.F8;


    private Level level;

    private final Box2DDebugRenderer box2DDebugRenderer;
    private Box2DFactory box2DFactory;
    private BodyDef bodyDef;
    private FixtureDef fixtureDef;
    private final BitmapFont font;
    private Matrix4 ortho2DMatrix;

    private final DebugKeysInputProcessor inputProcessor;

    private final Clipboard clipboard;

    private final ScriptEngine scriptEngine;
    private String debugScript;
    private Hook debugHook;
    private EnumMap<Hook, CompiledScript> hookedScipts;


    public DebugLevel(Level level) {
        this.level = level;
        box2DDebugRenderer = new Box2DDebugRenderer(true, true, true, true, true);
        ortho2DMatrix = new Matrix4();
        font = new BitmapFont(Gdx.files.internal("com/badlogic/gdx/utils/arial-15.fnt"), false);

        bodyDef = new BodyDef();
        fixtureDef = new FixtureDef();
        box2DFactory = new Box2DFactory();
        box2DFactory.begin();
        ScriptEngineManager scriptEngineManager = new ScriptEngineManager();
        scriptEngine = scriptEngineManager.getEngineByExtension(".lua");
        scriptEngine.put("level", level.getClass().cast(level));
        scriptEngine.put("GameState", GameState.class);
        scriptEngine.put("box2DFactory", box2DFactory);
        scriptEngine.put("box2DFactory:end", null);
        scriptEngine.put("bodyDef", bodyDef);
        scriptEngine.put("fixtureDef", fixtureDef);
        scriptEngine.put("WizardCategory", WizardCategory.class);
        scriptEngine.put("BodyType", BodyDef.BodyType.class);

        debugScript = "";
        debugHook = null;
        hookedScipts = new EnumMap<Hook, CompiledScript>(Hook.class);
        clipboard = Gdx.app.getClipboard();

        inputProcessor = new DebugKeysInputProcessor(level.getInputProcessor());
        Gdx.input.setInputProcessor(inputProcessor);
    }

    @Override
    public void render(float delta) {
        level.render(delta);
        box2DDebugRenderer.render(level.getWorld(), level.getCamera().combined);
        renderDebugText(level.spritebatch);
        if (hookedScipts.containsKey(Hook.afterRender))
            try {
                scriptEngine.put("spriteBatch", level.spritebatch);
                hookedScipts.get(DEBUG_AFTER_RENDER).eval();
                scriptEngine.put("spriteBatch", null);
            } catch (ScriptException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
    }

    private void renderDebugText(SpriteBatch spritebatch) {
        String levelText = level.toString();

        int place = -1, amount = 1;
        while ((place = levelText.indexOf('\n', place + 1)) > 0) amount++;

        spritebatch.setProjectionMatrix(ortho2DMatrix);
        spritebatch.begin();
        spritebatch.setColor(Color.WHITE);
        font.drawMultiLine(spritebatch, "canJump=\t" + level.canJump + "\n" +
                "justKickedOff=\t" + level.justKickedOff + "\n" +
                "playerCanMoveUpwards = \t" + level.playerCanMoveUpwards + "\n" +
                "isFeetTouchingBoundary=\t" + level.isFeetTouchingBoundary + "\n" +
                levelText, 0, font.getLineHeight() * (4 + amount));
        if (level.getGameState() == GameState.PAUSED) {
            font.draw(spritebatch, "PAUSED", Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2);
            font.draw(spritebatch, "Execute " + (debugHook == null ? "now" : debugHook), 0, Gdx.graphics.getHeight());
            font.drawMultiLine(spritebatch, '>' + debugScript.replaceAll("\\n", "\n "), 0, Gdx.graphics.getHeight() - font.getLineHeight());
        }
        spritebatch.end();
        spritebatch.setProjectionMatrix(level.getCamera().combined);
    }

    @Override
    public void resize(int width, int height) {
        level.resize(width, height);
        ortho2DMatrix.setToOrtho2D(0, 0, width, height);
    }

    @Override
    public void show() {
        level.show();
    }

    @Override
    public void hide() {
        level.hide();
    }

    @Override
    public void pause() {
        level.pause();
    }

    @Override
    public void resume() {
        level.resume();
    }

    @Override
    public void dispose() {
        level.dispose();
        box2DDebugRenderer.dispose();
        box2DFactory.dispose();
        font.dispose();
        //inputProcessor.dispose();
    }

    public class DebugKeysInputProcessor extends InputAdapter {
        private InputProcessor levelInputProcessor;
        private final Vector3 tempVector = new Vector3();

        public DebugKeysInputProcessor(InputProcessor levelInputProcessor) {
            this.levelInputProcessor = levelInputProcessor;
        }

        @Override
        public boolean keyDown(int keycode) {
            if (keycode == Input.Keys.ESCAPE) {
                level.setGameState(level.getGameState() == GameState.PAUSED ? GameState.RUNNING : GameState.PAUSED);
                debugScript = "";
            } else if (level.getGameState() == GameState.PAUSED) {
                switch (keycode) {
                    case Input.Keys.INSERT:
                        if (debugScript.isEmpty())
                            break;
                        try {
                            if (debugHook == null)
                                scriptEngine.eval(debugScript);
                            else {
                                CompiledScript compile = ((Compilable) scriptEngine).compile(debugScript);
                                //if (!hookedScipts.containsKey(debugHook))
                                hookedScipts.put(debugHook, compile);
                            }
                        } catch (ScriptException e) {
                            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                        }
                        debugScript = "";
                        break;
                    case DEBUG_NOW:
                        debugHook = null;
                        break;
                    case DEBUG_ON_KEY_UP:
                        debugHook = Hook.onKeyUp;
                        break;
                    case DEBUG_ON_KEY_DOWN:
                        debugHook = Hook.onKeyDown;
                        break;
                    case DEBUG_ON_TOUCH_UP:
                        debugHook = Hook.onTouchUp;
                        break;
                    case DEBUG_ON_TOUCH_DOWN:
                        debugHook = Hook.onTouchDown;
                        break;
                    case DEBUG_AFTER_RENDER:
                        debugHook = Hook.afterRender;
                        break;
                }
            } else if (level.getGameState() == GameState.RUNNING)
                if (hookedScipts.containsKey(Hook.onKeyDown))
                    try {
                        scriptEngine.put("keyCode", keycode);
                        hookedScipts.get(Hook.onKeyDown).eval();
                    } catch (ScriptException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }

            return levelInputProcessor.keyDown(keycode);
        }

        @Override
        public boolean keyUp(int keycode) {
            if (level.getGameState() == GameState.RUNNING)
                if (hookedScipts.containsKey(Hook.onKeyUp))
                    try {
                        scriptEngine.put("keyCode", keycode);
                        hookedScipts.get(Hook.onKeyUp).eval();
                    } catch (ScriptException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
            return levelInputProcessor.keyUp(keycode);
        }

        @Override
        public boolean keyTyped(char character) {
            if (level.getGameState()==GameState.PAUSED)
            {
                switch (character) {
                    case '\b':
                        if (!debugScript.isEmpty())
                            debugScript = debugScript.substring(0, debugScript.length() - 1);
                        break;
                    case 3:
                        clipboard.setContents(debugScript);
                        break;
                    case 22:
                        debugScript = clipboard.getContents();
                        break;
                    default:
                        if (Character.getType(character) != Character.CONTROL) //space character, we are maing sure we do not add in a control character
                            debugScript += (character == '\r') ? '\n' : character;
                        break;
                }
            }
            //System.out.println(character + " " + ((int)character));
            return levelInputProcessor.keyTyped(character);
        }

        @Override
        public boolean touchDown(int screenX, int screenY, int pointer, int button) {
            if (level.getGameState() == GameState.RUNNING)
                if (hookedScipts.containsKey(Hook.onTouchDown))
                    try {
                        tempVector.set(screenX,screenY,0);
                        level.getCamera().unproject(tempVector);
                        CompiledScript hookedScript = hookedScipts.get(Hook.onTouchDown);
                        //ScriptEngine engine = hookedScript.getEngine();
                        scriptEngine.put("worldX", tempVector.x);
                        scriptEngine.put("worldY", tempVector.y);
                        scriptEngine.put("screenX", screenX);
                        scriptEngine.put("screenY", screenY);
                        scriptEngine.put("pointer", pointer);
                        scriptEngine.put("button", button);
                        hookedScript.eval();
                    } catch (ScriptException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
            return levelInputProcessor.touchDown(screenX, screenY, pointer, button);    //To change body of overridden methods use File | Settings | File Templates.
        }

        @Override
        public boolean touchUp(int screenX, int screenY, int pointer, int button) {
            if (level.getGameState() == GameState.RUNNING)
                if (hookedScipts.containsKey(Hook.onTouchUp))
                    try {
                        tempVector.set(screenX,screenY,0);
                        level.getCamera().unproject(tempVector);
                        CompiledScript hookedScript = hookedScipts.get(Hook.onTouchUp);
                        //ScriptEngine engine = hookedScript.getEngine();
                        scriptEngine.put("worldX", tempVector.x);
                        scriptEngine.put("worldY", tempVector.y);
                        scriptEngine.put("screenX", screenX);
                        scriptEngine.put("screenY", screenY);
                        scriptEngine.put("pointer", pointer);
                        scriptEngine.put("button", button);
                        hookedScript.eval();
                    } catch (ScriptException e) {
                        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                    }
            return levelInputProcessor.touchUp(screenX, screenY, pointer, button);
        }

        @Override
        public boolean touchDragged(int screenX, int screenY, int pointer) {
            return levelInputProcessor.touchDragged(screenX, screenY, pointer);
        }

        @Override
        public boolean mouseMoved(int screenX, int screenY) {
            return levelInputProcessor.mouseMoved(screenX, screenY);
        }

        @Override
        public boolean scrolled(int amount) {
            return levelInputProcessor.scrolled(amount);
        }
    }

    private enum Hook {
        onMouseMoved, onTouchDragged, onTouchUp, onTouchDown, onKeyUp, onKeyDown, afterRender;
    }
}
