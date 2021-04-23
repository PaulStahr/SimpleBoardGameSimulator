package test.main;

import gameObjects.action.player.PlayerAddAction;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import main.Player;
import org.jdom2.JDOMException;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class PlayerTest {
    int id = (int)System.nanoTime();
    @Test
    public void AddPlayerTest() throws IOException, JDOMException {
        GameInstance gi = new GameInstance(new Game(), "Foo");
        Player pl = new Player("Max1", 4);

        gi.addPlayer(new PlayerAddAction(id, pl), pl);
        int playerNum = gi.getPlayerList().size();
        Player pl1 = gi.getPlayerById(pl.id);
        assertFalse(pl1 == null);
        assertEquals(pl.id, pl1.id);
        assertEquals(1, playerNum);
    }
}
