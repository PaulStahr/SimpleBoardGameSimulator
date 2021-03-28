package test.io;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

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

}
