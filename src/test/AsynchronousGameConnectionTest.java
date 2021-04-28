package test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import gameObjects.action.player.PlayerAddAction;
import gameObjects.action.player.PlayerEditAction;
import main.Player;
import org.junit.Test;

import data.Texture;
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
    final int id = 4;

    public AsynchronousGameConnectionTest() throws IOException {
        game.images.put("white.png", new Texture(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "png"));
        game.addObject(new GameObjectToken("token", "card", 10, 10, game.images.get("white.png"), game.images.get("white.png"), 5, 5, 90, 0, false, -1));
        gi0 = new GameInstance(game, "Foobar");
        gi0.addObjectInstance(new ObjectInstance(game.getGameObjectByIndex(0), id));
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
        ObjectInstance oi = gi0.getObjectInstanceById(id);
        ObjectState state = oi.copyState();
        state.posX = 4;
        state.posY = 2;
        gi0.update(new GameObjectInstanceEditAction(42, null, oi, state));
        try {
            synchronized(block1){block1.wait(1000);}
        } catch (InterruptedException e) {}
        assertEquals(state + "!=" + gi1.getObjectInstanceById(id).state, state, gi1.getObjectInstanceById(id).state);
    }

    @Test
    public void simplePlayerTest() throws IOException
    {
        Player pl = new Player("Max1", 4);
        gi0.addPlayer(new PlayerAddAction(id, pl));

        Player pl1 = new Player("Max2", 5);
        gi1.addPlayer(new PlayerAddAction(id, pl1));
        try {
            synchronized(block1){block1.wait(1000);}
        } catch (InterruptedException e) {}
        assertEquals(gi0.getPlayerById(4), gi1.getPlayerById(4));
        assertEquals(2, gi0.getPlayerCount());
        assertEquals(2, gi1.getPlayerCount());
    }

    @Test
    public void simplePlayerEditTest() throws IOException
    {
        Player pl1 = new Player("Max1", 4);
        gi0.addPlayer(new PlayerAddAction(id, pl1));
        Player pl2 = new Player("Max2", 5);
        gi1.addPlayer(new PlayerAddAction(id, pl2));
        try {
            synchronized(block1){block1.wait(1000);}
        } catch (InterruptedException e) {}

        Player p11 = gi1.getPlayerById(4);
        Player p12 = gi1.getPlayerById(5);
        p11.seatNum = 1;
        gi1.update(new PlayerEditAction(id, p11, p11));
        try {
            synchronized(block1){block1.wait(1000);}
        } catch (InterruptedException e) {}

        Player p01 = gi0.getPlayerById(4);
        Player p02 = gi0.getPlayerById(5);
        assertEquals(1, p01.seatNum);
    }

    @Test
    public void syncPullTest() throws IOException
    {
        assertEquals (gi0.hashCode(), gi1.hashCode());
        ObjectInstance oi = gi0.getObjectInstanceById(id);
        ObjectState state = oi.state;
        state.posX = 8;
        state.posY = 5;
        assertNotEquals(gi0.hashCode(), gi1.hashCode());
        connection1.syncPull();
        for (int i = 0; i < 1000; ++i) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {}
            if (gi0.hashCode() == gi1.hashCode()) {break;}
        }
        for (int i = 0; i < gi0.getObjectInstanceCount(); ++i)
        {
            ObjectInstance oi0 = gi0.getObjectInstanceByIndex(i);
            ObjectInstance oi1 = gi1.getObjectInstanceById(oi0.id);
            assertEquals(oi0.state, oi1.state);
            
        }
        assertEquals (gi0.hashCode(), gi1.hashCode());
    }
}
