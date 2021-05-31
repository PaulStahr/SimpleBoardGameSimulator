package test.gui.minigames;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import gui.minigames.TetrisGameInstance;
import gui.minigames.TetrisGameInstance.FallingObject;

public class TetrisGameInstanceTest {
    @Test
    public void testPlacable() {
        TetrisGameInstance tgi = new TetrisGameInstance();
        assertEquals(0, tgi.fallingObject.size());
        assertEquals(0, tgi.add(new FallingObject((byte)1), 4, 18));
        assertEquals(1, tgi.fallingObject.size());
        assertEquals(-1, tgi.add(new FallingObject((byte)1), 4, 18));
        assertEquals(1, tgi.fallingObject.size());
    }
}
