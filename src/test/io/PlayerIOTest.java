package test.io;

import static org.junit.Assert.assertEquals;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.jdom2.JDOMException;
import org.junit.Test;

import io.PlayerIO;
import main.Player;

public class PlayerIOTest {
    @Test
    public void testSimulateRead() throws IOException, ClassNotFoundException {
        Player player = new Player("Max", 53);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(out);
        PlayerIO.writePlayerToStreamObject(objOut, player);
        objOut.close();
        out.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objIn = new ObjectInputStream(in);
        PlayerIO.simulateEditPlayerFromObject(objIn);
        assertEquals("Stream has still bytes which were not scipped", 0, objIn.available());
    }

    @Test
    public void testXmlPlayer()
    {
        Player pl = new Player("Player", 42);
        pl.color = Color.CYAN;
        pl.mouseXPos = 43;
        pl.mouseYPos = 44;
        pl.playerAtTableTransform.setTransform(1,2,3,4,5,6);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        Player res;
        try {
            PlayerIO.writePlayerToStream(pl, bos);
            res = PlayerIO.readPlayerFromStream(new ByteArrayInputStream(bos.toByteArray()));
        } catch (IOException e) {
            throw new AssertionError(e);
        } catch (JDOMException e) {
            throw new AssertionError(e);
        }
        assertEquals(pl.toStringAdvanced() + "!=" + res.toStringAdvanced(), res,pl);
    }
}
