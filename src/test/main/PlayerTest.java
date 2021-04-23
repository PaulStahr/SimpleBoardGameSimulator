package test.main;

import gameObjects.action.player.PlayerAddAction;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import main.Player;
import org.jdom2.JDOMException;
import org.junit.Test;

import java.io.IOException;

public class PlayerTest {
    int id = (int)System.nanoTime();
    @Test
    public void PlayerColorTest() throws IOException, JDOMException {
        GameInstance gi = new GameInstance(new Game(), "Foo");
        Player pl = new Player("Max", 4);
        gi.addPlayer(new PlayerAddAction(id, pl), pl);
    }
}
