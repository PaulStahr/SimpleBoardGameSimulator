package test;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;

public class GameActionTest {
    @Test
    public void ObjectStateEvent()
    {
        Game game = new Game();
        game.objects.add(new GameObjectToken("token", "card", 10, 10, null, null, 5, 5, 90, 0, false, -1));
        GameInstance gi = new GameInstance(game, "Foobar");
        gi.addObjectInstance(new ObjectInstance(game.objects.get(0), 4));
        ObjectState state = gi.getObjectInstanceById(4).copyState();
        state.posX = 4;
        state.posY = 2;
        gi.update(new GameObjectInstanceEditAction(-1, -1, 4, state));
        assertTrue(state.equals(gi.getObjectInstanceById(4).state));
    }
}
