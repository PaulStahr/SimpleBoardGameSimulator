package test.functions;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.jdom2.JDOMException;
import org.junit.Test;

import data.DataHandler;
import gameObjects.action.player.PlayerAddAction;
import gameObjects.functions.MoveFunctions;
import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectState;
import io.GameIO;
import main.Player;
import util.data.IntegerArrayList;

public class MoveFunctionsTest {

    int id = (int)System.nanoTime();
    @Test
    public void moveStack() throws IOException, JDOMException
    {
        GameInstance gi = new GameInstance(new Game(), "Foo");
        GameIO.readSnapshotFromZip(DataHandler.getResourceAsStream("test/games/Test.zip"), gi);
        Player pl = new Player("Max", 4);
        gi.addPlayer(new PlayerAddAction(id, pl));
        int[] idList = new int[] {0, 1, 2, 3, 4};
        IntegerArrayList ial = new IntegerArrayList(idList);
        ObjectFunctions.makeStack(id, gi, pl, ial, null, null, ObjectFunctions.SIDE_TO_FRONT);
        MoveFunctions.moveStackTo(id, gi, pl, gi.getObjectInstanceById(0), 500, 1000);
        for (int id : idList){
            ObjectState state = gi.getObjectInstanceById(id).state;
            assertEquals(500, state.posX);
            assertEquals(1000, state.posY);
        }
    }
}
