package test.io;

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.junit.Test;

import gameObjects.definition.GameObjectBook;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.ObjectState;
import io.ObjectStateIO;

public class ObjectStateIOTest {
    private static void test(ObjectState state) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(out);
        ObjectStateIO.writeStateToStreamObject(objOut, state);
        objOut.close();
        out.close();
        ByteArrayInputStream in = new ByteArrayInputStream(out.toByteArray());
        ObjectInputStream objIn = new ObjectInputStream(in);
        ObjectStateIO.simulateStateFromStreamObject(objIn, state);
        assertEquals("Stream has still bytes which were not skipped", 0, objIn.available());
    }
    
    @Test
    public void testSimulateTokenRead() throws IOException {
        ObjectState state = new GameObjectToken.TokenState();
        state.aboveLyingObectIds.add(3);
        state.aboveLyingObectIds.add(6);
        state.aboveLyingObectIds.add(1);
        test(state);
    }

    @Test
    public void testSimulateFigureRead() throws IOException {
        test(new GameObjectBook.BookState());
    }
    
    @Test
    public void testSimulateBookRead() throws IOException {
        test(new GameObjectBook.BookState());
    }
    
    @Test
    public void testSimulateDiceRead() throws IOException {
        test(new GameObjectBook.BookState());
    }
}
