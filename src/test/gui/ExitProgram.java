package test.gui;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicBoolean;

import main.Player;
import org.jdom2.JDOMException;
import org.junit.Test;

import data.DataHandler;
import gameObjects.action.player.PlayerAddAction;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gui.game.GameWindow;
import gui.language.Language.LanguageSummary;
import gui.language.LanguageHandler;
import io.GameIO;
import util.JFrameUtils;

public class ExitProgram {
    private final int id = (int)System.nanoTime();
    @Test
    public void testNoRemainingWindow() throws Throwable {
        final AtomicBoolean exitCalled = new AtomicBoolean(false);
        final AtomicBoolean awtMethodRan = new AtomicBoolean(false);
        Runnable origExitRunnable = DataHandler.swapExitRunnable(new Runnable() {
            @Override
            public void run() {
                synchronized(exitCalled)
                {
                    exitCalled.set(true);
                    exitCalled.notifyAll();
                }
            }
        });
        try {
            LanguageHandler lh = new LanguageHandler(new LanguageSummary("de", "de"));
            GameInstance gi = new GameInstance(new Game(), "Foo");
            GameIO.readSnapshotFromZip(DataHandler.getResourceAsStream("test/games/MinimalGame.zip"), gi);
            Player pl = new Player("Max", 4);
            gi.addPlayer(new PlayerAddAction(id, pl), pl);
            JFrameUtils.runByDispatcherAndWait(new Runnable() {
                @Override
                public void run() {
                    GameWindow gw = new GameWindow(gi, pl, lh);
                    gw.setVisible(true);
                    gw.dispatchEvent(new WindowEvent(gw, WindowEvent.WINDOW_CLOSING));
                    WindowEvent windowClosedEvent = new WindowEvent(gw, WindowEvent.WINDOW_CLOSED);
                    for (WindowListener wl : gw.getWindowListeners())
                    {
                        wl.windowClosed(windowClosedEvent);
                    }
                    awtMethodRan.set(true);
                }
            });
            assert(awtMethodRan.get());
            synchronized(exitCalled)
            {
                if (!exitCalled.get())
                {
                    try {
                        exitCalled.wait(100);
                    }catch(InterruptedException e) {
                        e.printStackTrace();
                    }
                    assert(exitCalled.get());
                }
            }
        }catch(IOException| JDOMException e) {
            throw e;
        }catch(InvocationTargetException e) {
            throw e.getTargetException();
        }finally {
            DataHandler.swapExitRunnable(origExitRunnable);
        }
    }
}
