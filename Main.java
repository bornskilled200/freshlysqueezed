package com.unseen.wizard;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.unseen.wizard.screen.Level;

/**
 * Created with IntelliJ IDEA.
 * User: David
 * Date: 2/18/13
 * Time: 6:00 PM
 * To change this template use File | Settings | File Templates.
 */
public class Main extends Game {
    @Override
    public void create() {
        setScreen(new Level());
    }

    public static void main(String[] args)
    {
        new LwjglApplication(new Main());
    }
}
