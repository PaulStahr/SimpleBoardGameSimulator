package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.Test;

import gameObjects.action.GameAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import net.AsynchronousGameConnection;

public class AsynchronousGameConnectionTest {
    private Game game = new Game("Testgame");
    private GameInstance gi0, gi1;
    private Object block0 = new Object(), block1 = new Object();
    private AsynchronousGameConnection connection0;
    private AsynchronousGameConnection connection1;

    public AsynchronousGameConnectionTest() throws IOException {
        game.objects.add(new GameObjectToken("token", "card", 10, 10, null, null, 5, 5, 90, 0, false, -1));
        gi0 = new GameInstance(game, "Foobar");
        gi0.addObjectInstance(new ObjectInstance(game.objects.get(0), 4));
        gi1 = new GameInstance(gi0);
        assertEquals(gi0.hashCode(), gi1.hashCode());
        PipedInputStream pis0 = new PipedInputStream();
        PipedOutputStream pos0 = new PipedOutputStream(pis0);
        PipedInputStream pis1 = new PipedInputStream();
        PipedOutputStream pos1 = new PipedOutputStream(pis1);

        connection0 = new AsynchronousGameConnection(gi0, pis0, pos1, null);
        connection1 = new AsynchronousGameConnection(gi1, pis1, pos0, null);
        connection0.start();
        connection1.start();
        gi0.addChangeListener(new GameChangeListener() {
            @Override
            public void changeUpdate(GameAction action) {
                if (action.source == 42)
                {
                    synchronized (block0) {
                        block0.notifyAll();                    
                    }
                }
            }
        });
        gi1.addChangeListener(new GameChangeListener() {
            @Override
            public void changeUpdate(GameAction action) {
                if (action.source == 42)
                {
                    synchronized (block1) {
                        block1.notifyAll();                    
                    }
                }
            }
        });
    }   

    @Test
    public void simpleConnectionTest() throws IOException
    {
        assertEquals (gi0.hashCode(), gi1.hashCode());
        ObjectInstance oi = gi0.getObjectInstanceById(4);
        ObjectState state = oi.copyState();
        state.posX = 4;
        state.posY = 2;
        gi0.update(new GameObjectInstanceEditAction(42, null, oi, state));
        try {
            synchronized(block1){block1.wait(1000);}
        } catch (InterruptedException e) {}
        assertEquals(state + "!=" + gi1.getObjectInstanceById(4).state, state, gi1.getObjectInstanceById(4).state);
    }

    @Test
    public void syncPullTest() throws IOException
    {
        assertEquals (gi0.hashCode(), gi1.hashCode());
        ObjectInstance oi = gi0.getObjectInstanceById(4);
        ObjectState state = oi.state;
        state.posX = 4;
        state.posY = 2;
        assertNotEquals(gi0.hashCode(), gi1.hashCode());
        connection1.syncPull();
        for (int i = 0; i < 100; ++i) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {}
            if (gi0.hashCode() == gi1.hashCode()) {break;}
        }
        assertEquals (gi0.hashCode(), gi1.hashCode());
    }
}
