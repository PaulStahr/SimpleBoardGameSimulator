package test.gameObjects;

import main.gameObjects.Player;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class PlayerTest {

    @Test
    public void testConstructor_simplePlayer(){
        Player testPlayer = new Player("testPlayer", 0);
        assertEquals("testPlayer", testPlayer.name);
        assertEquals(0, testPlayer.id);
    }

    @Test
    public void testConstructor_positionedPlayerWithoutColor(){
        Player testPlayer = new Player("testPlayer", 0, 5, -5);
        assertEquals("testPlayer", testPlayer.name);
        assertEquals(0, testPlayer.id);
        assertEquals(5, testPlayer.mouseXPos);
        assertEquals(-5, testPlayer.mouseYPos);
        assertNotNull(testPlayer.color);
    }

    @Test
    public void testConstructor_positionedPlayerWithColor(){
        Player testPlayer = new Player("testPlayer", 0, 5, -5, Color.RED);
        assertEquals("testPlayer", testPlayer.name);
        assertEquals(0, testPlayer.id);
        assertEquals(5, testPlayer.mouseXPos);
        assertEquals(-5, testPlayer.mouseYPos);
        assertEquals(Color.RED, testPlayer.color);
    }

    @Test
    public void testToString(){
        Player testPlayer = new Player("testPlayer", 31415);
        assertEquals("(testPlayer 31415)", testPlayer.toString());
    }

    @Test
    public void testSetMousePosition(){
        Player testPlayer = new Player("testPlayer", 0, 5, 10);
        assertEquals(5, testPlayer.mouseXPos);
        assertEquals(10, testPlayer.mouseYPos);
        testPlayer.setMousePos(9, 19);
        assertEquals(9, testPlayer.mouseXPos);
        assertEquals(19, testPlayer.mouseYPos);
    }

    @Test
    public void testOverwriteWith(){
        Player initial = new Player("name", 0, 5, 10, Color.BLACK);
        Player update = new Player("updatedName", 1, 0, -5, Color.RED);
        initial.overwriteWith(update);
        assertEquals("updatedName", initial.name);
        assertEquals(0, initial.id);
        assertEquals(0, initial.mouseXPos);
        assertEquals(-5, initial.mouseYPos);
        assertEquals(Color.RED, initial.color);
    }
}
