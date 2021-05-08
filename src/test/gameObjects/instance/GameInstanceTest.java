package test.gameObjects.instance;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;

import org.junit.Test;

import data.Texture;
import gameObjects.action.player.PlayerAddAction;
import gameObjects.action.player.PlayerRemoveAction;
import gameObjects.definition.GameObjectToken;
import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import main.Player;
import util.data.IntegerArrayList;

public class GameInstanceTest {
    int id = 4;

    @Test
    public void deletePlayerTest() {
        Game game = new Game("Testgame");
        game.images.add(new Texture(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "white.png", "png"));
        game.addObject(new GameObjectToken("token", "card", 10, 10, game.getImage("white.png"), game.getImage("white.png"), 5, 5, 90, 0, false, -1));
        Player pl = new Player("Max", 4);
        GameInstance gi = new GameInstance(game, "Foobar");
        gi.addPlayer(new PlayerAddAction(id, pl));
        for (int i = 0; i < 3; ++i)
        {
            gi.addObjectInstance(new ObjectInstance(game.getGameObjectByIndex(0), i));
        }
        IntegerArrayList ial = new IntegerArrayList(new int[] {0,1,2});
        ObjectFunctions.makeStack(id, gi, pl, ial, null, ObjectFunctions.SIDE_TO_FRONT);
        for (int i = 0; i < ial.size(); ++i)
        {
            gi.getObjectInstanceByIndex(i).state.owner_id = pl.id;
        }
        gi.update(new PlayerRemoveAction(id, pl, pl));
        assertEquals(0, gi.getPlayerCount());
        for (int i = 0; i < ial.size(); ++i)
        {
            assertEquals(-1, gi.getObjectInstanceByIndex(i).state.owner_id);
        }
    }
}
