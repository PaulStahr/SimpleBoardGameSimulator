package test.io;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import gameObjects.definition.GameObjectToken;
import gameObjects.instance.ObjectState;
import io.ObjectStateIO;

public class ObjectStateIOTest {
    @Test
    public void testSimulateRead() throws IOException {
        ObjectState state = new GameObjectToken.TokenState();
        state.aboveLyingObectIds.add(3);
        state.aboveLyingObectIds.add(6);
        state.aboveLyingObectIds.add(1);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(out);
        ObjectStateIO.writeStateToStreamObject(objOut, state);
        objOut.close();
        out.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objIn = new ObjectInputStream(in);
        ObjectStateIO.simulateStateFromStreamObject(objIn, state);
        assertEquals("Stream has still bytes which were not scipped", 0, objIn.available());
    }
}
