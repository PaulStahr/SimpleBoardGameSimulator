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
    	sclc.addPlayerToGameSession(player, gi.name, gi.password);
    	AsynchronousGameConnection connection = sclc.connectToGameSession(gi);
    	connection.start();
    	gi.addPlayer(player);
    	gw.setVisible(true);
    }
    
    public static GameWindow connectAndJoinGame(String address, int port, Player player, String gameInstanceId) throws UnknownHostException, IOException, JDOMException
    {
    	SynchronousGameClientLobbyConnection sclc = new SynchronousGameClientLobbyConnection(address,  port);
    	GameInstance gi = sclc.getGameInstance(gameInstanceId);
    	sclc.addPlayerToGameSession(player, gi.name, gi.password);
    	GameWindow gw = new GameWindow(gi, player);
    	AsynchronousGameConnection connection = sclc.connectToGameSession(gi);
    	//gi.addPlayer(player);
    	connection.syncPull();
    	connection.start();
    	gw.setVisible(true);
    	return gw;
    }
    
    public static void localTwoInstanceTest() throws IOException, JDOMException
    {
    	String address = "127.0.0.1";
    	int port = 8000 + (int)(Math.random() * 100);
    	{
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
    	}
    	{
    	   	Player player = new Player("Paul", 1);
    	   	FileInputStream fis = new FileInputStream("Doppelkopf.zip");
			GameInstance gi = GameIO.readSnapshotFromZip(fis);
			gi.name = "Testsession";
			gi.players.add(player);
	    	fis.close();
	    	connectAndStartGame(address, port, player, gi);
    	}
    	try {
    		Thread.sleep(100);
    	}catch(InterruptedException e) {}
    	
    	
	   	Player player = new Player("Florian", 1);
    	//FileInputStream fis = new FileInputStream("Doppelkopf.zip");
		//GameInstance gi = GameIO.readSnapshotFromZip(fis);
		//GameInstance gi = new GameInstance(new Game());
		//gi.name = "Testsession";
		//gi.players.add(player);
	    GameWindow gw = connectAndJoinGame(address, port, player, "Testsession");
    	try {
    		Thread.sleep(500);
    	}catch(InterruptedException e) {}
    	//gi.update(new GameObjectInstanceEditAction(0, gi.players.get(0), gi.objects.get(0)));    	
    }
}
