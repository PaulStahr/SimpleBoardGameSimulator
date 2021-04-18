package test.gui.game;

import static org.junit.Assert.assertEquals;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;

import org.jdom2.JDOMException;
import org.junit.Test;

import data.DataHandler;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.player.PlayerAddAction;
import gameObjects.functions.CheckingFunctions;
import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import gui.game.PrivateArea;
import io.GameIO;
import main.Player;
import util.data.IntegerArrayList;

public class PrivateAreaTest {
    int id = (int)System.nanoTime();
    @Test
    public void removeObjectFromPrivateArea() throws IOException, JDOMException
    {
        GameInstance gi = new GameInstance(new Game(), "Foo");
        GameIO.readSnapshotFromZip(DataHandler.getResourceAsStream("test/games/MinimalGame.zip"), gi);
        ObjectInstance oi = gi.getObjectInstanceByIndex(0);
        Player pl = new Player("Max", 4);
        gi.addPlayer(new PlayerAddAction(id, pl), pl);
        PrivateArea pa = new PrivateArea(new AffineTransform(), new AffineTransform());
        assertEquals(null, CheckingFunctions.checkPlayerConsistency(-1, new ArrayList<>(), new ArrayList<>(), gi));
        assertEquals(null, CheckingFunctions.checkPlayerConsistency(pl.id, new ArrayList<>(), new ArrayList<>(), gi));
        ObjectState st = oi.state.copy();
        st.owner_id = pl.id;
        st.aboveInstanceId = -1;
        st.belowInstanceId = -1;
        gi.update(new GameObjectInstanceEditAction(id, pl, oi, st));
        assertEquals(0, pa.privateObjectsPositions.size());
        ObjectFunctions.insertIntoOwnStack(id, pa, gi, pl, new Point2D.Double(), 0, oi, oi, new IntegerArrayList(new int[] {oi.id}), oi.id, 1);
        assertEquals(1, pa.privateObjectsPositions.size());
        ObjectFunctions.removeFromOwnStack(id, pa, gi, pl, oi);
        assertEquals(0, pa.privateObjectsPositions.size());
    }
}
