package test.io;

import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;

import org.jdom2.Element;
import org.junit.Test;

import data.Texture;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.Game;
import io.GameIO;

public class GameIOTest {
    @Test
    public void testXmlObject()
    {
        Game game = new Game();
        Texture foo = new Texture(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "png");
        Texture bar = new Texture(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB), "png");
        game.images.put("foo.png",foo);
        game.images.put("bar.png",bar);
        GameObjectToken token = new GameObjectToken("baz", "card", 10, 2, foo, bar, 1, 2, 3, 4);
        GameObject res;
        Element elem = GameIO.createElementFromGameObject(token, game);
        res = GameIO.createGameObjectFromElement(elem, game.images);
        assertEquals(token.toStringAdvanced() + "!=" + res.toStringAdvanced(), res,token);
    }
}