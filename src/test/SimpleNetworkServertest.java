package test;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;

import org.jdom2.JDOMException;

import gameObjects.instance.GameInstance;
import gui.GameWindow;
import io.GameIO;
import main.Player;
import net.AsynchronousGameConnection;
import net.GameServer;
import net.SynchronousGameClientLobbyConnection;

public class SimpleNetworkServertest {
    
    public static GameServer startNewServer(int port)
    {
    	GameServer gs = new GameServer(port);
    	gs.start();
    	return gs;
    }
    
    public static void connectAndStartGame(String address, int port, Player player, GameInstance gi) throws UnknownHostException, IOException
    {
    	GameWindow gw = new GameWindow(gi, player);
    	SynchronousGameClientLobbyConnection sclc = new SynchronousGameClientLobbyConnection(address,  port);
    	sclc.pushGameSession(gi);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	sclc.joinToGameSession(player, gi.name, gi.password);
    	AsynchronousGameConnection connection = sclc.connectToGameSession(gi);
    	gw.gamePanel.player = player;
    	connection.start();
    	gw.setVisible(true);
    }
    
    public static void connectAndJoinGame(String address, int port, Player player, GameInstance gi) throws UnknownHostException, IOException
    {
    	GameWindow gw = new GameWindow(gi, player);
    	SynchronousGameClientLobbyConnection sclc = new SynchronousGameClientLobbyConnection(address,  port);
    	sclc.joinToGameSession(player, gi.name, gi.password);
    	AsynchronousGameConnection connection = sclc.connectToGameSession(gi);
    	connection.start();
    	gw.setVisible(true);
    }
    
    public static void localTwoInstanceTest() throws IOException, JDOMException
    {
    	String address = "127.0.0.1";
    	int port = 8000 + (int)(Math.random() * 100);
    	GameServer gs = startNewServer(port);
    	while (!gs.isReady())
    	{
    		try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
    	}
    	Player player = new Player("Player1", 1);
    	FileInputStream fis = new FileInputStream("Doppelkopf.zip");
		GameInstance gi = GameIO.readSnapshotFromZip(fis);
		gi.name = "Testsession";
    	fis.close();
    	connectAndStartGame(address, port, player, gi);
    	try {
    		Thread.sleep(100);
    	}catch(InterruptedException e) {}
    	connectAndJoinGame(address, port, player, gi);
    }
}
