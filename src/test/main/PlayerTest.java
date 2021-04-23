package test.main;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;

import org.jdom2.JDOMException;
import org.junit.Test;

import gameObjects.action.player.PlayerAddAction;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import main.Player;

public class PlayerTest {
    int id = (int)System.nanoTime();
    @Test
    public void AddPlayerTest() throws IOException, JDOMException {
        GameInstance gi = new GameInstance(new Game(), "Foo");
        Player pl = new Player("Max1", 4);

        pl = gi.addPlayer(new PlayerAddAction(id, pl));
        assertEquals(1, gi.getPlayerCount(true));
        Player pl1 = gi.getPlayerById(pl.id);
        assertNotNull(pl1);
        assertEquals(pl.id, pl1.id);
    }
}
