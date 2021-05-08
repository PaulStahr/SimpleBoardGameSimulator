package test.functions;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.awt.image.BufferedImage;
import java.util.ArrayList;

import org.junit.Test;

import data.Texture;
import gameObjects.action.player.PlayerAddAction;
import gameObjects.definition.GameObjectToken;
import gameObjects.functions.CheckingFunctions;
import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import main.Player;
import util.data.IntegerArrayList;

public class CheckingFunctionTest {
    final int id = 4;
    @Test
    public void viceUpVersaInconsistencyTest() {
        GameInstance gi = createTestInstance();
        gi.getObjectInstanceByIndex(1).state.aboveInstanceId = -1;
        assertNotNull(CheckingFunctions.checkPlayerConsistency(-1, new ArrayList<>(), new ArrayList<>(), gi));
        gi.repairPlayerConsistency(-1, gi.getPlayerByIndex(0), new ArrayList<>());
        assertNull(CheckingFunctions.checkPlayerConsistency(-1, new ArrayList<>(), new ArrayList<>(), gi));
    }

    @Test
    public void viceDownVersaInconsistencyTest() {
        GameInstance gi = createTestInstance();
        gi.getObjectInstanceByIndex(1).state.belowInstanceId = -1;
        assertNotNull(CheckingFunctions.checkPlayerConsistency(-1, new ArrayList<>(), new ArrayList<>(), gi));
        gi.repairPlayerConsistency(-1, gi.getPlayerByIndex(0), new ArrayList<>());
        assertNull(CheckingFunctions.checkPlayerConsistency(-1, new ArrayList<>(), new ArrayList<>(), gi));
    }

    @Test
    public void circleInconsistencyTest() {
        GameInstance gi = createTestInstance();
        gi.getObjectInstanceByIndex(0).state.belowInstanceId = gi.getObjectInstanceByIndex(2).id;
        gi.getObjectInstanceByIndex(2).state.belowInstanceId = gi.getObjectInstanceByIndex(0).id;
        assertNotNull(CheckingFunctions.checkPlayerConsistency(-1, new ArrayList<>(), new ArrayList<>(), gi));
        gi.repairPlayerConsistency(-1, gi.getPlayerByIndex(0), new ArrayList<>());
        assertNull(CheckingFunctions.checkPlayerConsistency(-1, new ArrayList<>(), new ArrayList<>(), gi));
    }

    @Test
    public void ownerInconsistencyTest() {
        GameInstance gi = createTestInstance();
        Player pl = gi.getPlayerByIndex(0);
        gi.getObjectInstanceByIndex(1).state.owner_id = pl.id;
        assertNotNull(CheckingFunctions.checkPlayerConsistency(-1, new ArrayList<>(), new ArrayList<>(), gi));
        assertNotNull(CheckingFunctions.checkPlayerConsistency(pl.id, new ArrayList<>(), new ArrayList<>(), gi));
        gi.repairPlayerConsistency(-1, pl, new ArrayList<>());
        assertNull(CheckingFunctions.checkPlayerConsistency(-1, new ArrayList<>(), new ArrayList<>(), gi));
        gi.repairPlayerConsistency(pl.id, pl, new ArrayList<>());
        assertNull(CheckingFunctions.checkPlayerConsistency(pl.id, new ArrayList<>(), new ArrayList<>(), gi));
    }

    private GameInstance createTestInstance() {
        Game game = new Game("Testgame");
        game.images.add(new Texture(new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB), "white.png", "png"));
        game.addObject(new GameObjectToken("token", "card", 10, 10, game.getImage("white.png"), game.getImage("white.png"), 5, 5, 90, 0, false, -1));
        Player pl = new Player("Max", 4);
        GameInstance gi = new GameInstance(game, "Foobar");
        gi.addPlayer(new PlayerAddAction(id, pl));
        gi.addObjectInstance(new ObjectInstance(game.getGameObjectByIndex(0), 0));
        gi.addObjectInstance(new ObjectInstance(game.getGameObjectByIndex(0), 1));
        gi.addObjectInstance(new ObjectInstance(game.getGameObjectByIndex(0), 2));
        IntegerArrayList ial = new IntegerArrayList(new int[] {0,1,2});
        ObjectFunctions.makeStack(id, gi, pl, ial, null, ObjectFunctions.SIDE_TO_FRONT);
        assertNull(CheckingFunctions.checkPlayerConsistency(-1, new ArrayList<>(), new ArrayList<>(), gi));
        return gi;
    }
}
