package test.main;

import java.io.IOException;

import org.jdom2.JDOMException;
import org.junit.Test;

import gameObjects.action.player.PlayerAddAction;
import gameObjects.action.player.PlayerEditAction;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import main.Player;

import static org.junit.Assert.*;

public class PlayerTest {
    int id = (int)System.nanoTime();
    @Test
    public void AddPlayerTest() throws IOException, JDOMException {
        GameInstance gi = new GameInstance(new Game(), "Foo");
        Player pl = new Player("Max", 4);
        pl = gi.addPlayer(new PlayerAddAction(id, pl));
        int playerNum = gi.getPlayerList().size();
        int playerIndex = gi.getPlayerList().indexOf(pl);
        assertEquals(1, gi.getPlayerCount(true));
        Player pl1 = gi.getPlayerById(pl.id);
        assertNotNull(pl1);
        assertEquals(pl.id, pl1.id);
        assertEquals(1, playerNum);
        assertEquals(0, playerIndex);
    }

    @Test
    public void PlayerColorTest() throws IOException, JDOMException {
        GameInstance gi = new GameInstance(new Game(), "Foo");
        Player pl = new Player("Max1", 4);
        gi.addPlayer(new PlayerAddAction(id, pl));
        gi.getPlayerList().indexOf(pl);
        if (pl.seatNum == -1) {
            pl.seatNum = gi.getPlayerList().indexOf(pl);
        }
        Player pl1 = new Player("Max2", 5);
        gi.addPlayer(new PlayerAddAction(id, pl1));

        if (pl1.seatNum == -1) {
            pl1.seatNum = gi.getPlayerList().indexOf(pl1);
        }
        assertFalse(pl.seatNum == -1);
        assertFalse(pl1.seatNum == -1);
        assertEquals(0, pl.seatNum);
        assertEquals(1, pl1.seatNum);
    }
    @Test
    public void UpdatePlayerTest() throws IOException, JDOMException {
        GameInstance gi = new GameInstance(new Game(), "Foo");
        Player pl = new Player("Max1", 4);
        gi.addPlayer(new PlayerAddAction(id, pl));
        gi.getPlayerList().indexOf(pl);

        Player pl1 = new Player("Max2", 5);
        gi.addPlayer(new PlayerAddAction(id, pl1));
        pl1.trickNum = 1;
        gi.update(new PlayerEditAction(id, pl, pl1));
        assertEquals(1, gi.getPlayerById(pl1.id).trickNum);
    }
}
