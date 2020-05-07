package test;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gui.GameWindow;
import gui.LanguageHandler;
import io.GameIO;
import main.Player;
import net.AsynchronousGameConnection;
import net.GameServer;
import net.SynchronousGameClientLobbyConnection;

public class SimpleNetworkServertest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleNetworkServertest.class);
    public static GameServer startNewServer(int port)
    {
    	GameServer gs = new GameServer(port);
    	gs.start();
    	return gs;
    }
    
    public static void connectAndStartGame(String address, int port, Player player, GameInstance gi, LanguageHandler lh) throws UnknownHostException, IOException
    {
    	GameWindow gw = new GameWindow(gi, player, lh);
    	SynchronousGameClientLobbyConnection sclc = new SynchronousGameClientLobbyConnection(address,  port);
    	sclc.pushGameSession(gi);
    	try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			logger.error("Unecpected interrupt", e);
		}
    	sclc.addPlayerToGameSession(player, gi.name, gi.password);
    	AsynchronousGameConnection connection = sclc.connectToGameSession(gi, null);
    	connection.start();
    	gi.addPlayer(player);
    	gw.setVisible(true);
    }
    
    public static GameWindow connectAndJoinGame(String address, int port, Player player, String gameInstanceId, LanguageHandler lh) throws UnknownHostException, IOException, JDOMException
    {
    	SynchronousGameClientLobbyConnection sclc = new SynchronousGameClientLobbyConnection(address,  port);
    	GameInstance gi = sclc.getGameInstance(gameInstanceId);
    	sclc.addPlayerToGameSession(player, gi.name, gi.password);
    	player = gi.addPlayer(player);
    	AsynchronousGameConnection connection = sclc.connectToGameSession(gi, null);
    	connection.syncPull();
    	connection.start();
    	Player pl = gi.getPlayerById(player.id);
    	if (pl == null)
    	{
    		logger.error("Player " + player.id+ " doesn't exist");
    	}
    	GameWindow gw = new GameWindow(gi, pl, lh);
    	gw.setVisible(true);
    	return gw;
    }
    
    public static void localTwoInstanceTest(int port, LanguageHandler lh) throws IOException, JDOMException
    {
    	String address = "127.0.0.1";
    	{
	    	GameServer gs = startNewServer(port);
	    	while (!gs.isReady())
	    	{
	    		try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					logger.error("Unecpected interrupt", e);
				}
	    	}
    	}
    	{
    	   	Player player = new Player("Paul", 1);
    	   	FileInputStream fis = new FileInputStream("Games/Doppelkopf.zip");
			GameInstance gi = new GameInstance(new Game(), null);
			GameIO.readSnapshotFromZip(fis, gi);
			gi.name = "Testsession";
			gi.addPlayer(player);
	    	fis.close();
	    	connectAndStartGame(address, port, player, gi, lh);
    	}
    	try {
    		Thread.sleep(300);
    	}catch(InterruptedException e) {}
    	
    	
	   	Player player = new Player("Florian", 2);
    	//FileInputStream fis = new FileInputStream("Doppelkopf.zip");
		//GameInstance gi = GameIO.readSnapshotFromZip(fis);
		//GameInstance gi = new GameInstance(new Game());
		//gi.name = "Testsession";
		//gi.players.add(player);
	    GameWindow gw = connectAndJoinGame(address, port, player, "Testsession", lh);
    	try {
    		Thread.sleep(500);
    	}catch(InterruptedException e) {
			logger.error("Unecpected interrupt", e);
    	}
    	//gi.update(new GameObjectInstanceEditAction(0, gi.players.get(0), gi.objects.get(0)));    	


		Player player2 = new Player("Melissa", 3);
    	//FileInputStream fis = new FileInputStream("Doppelkopf.zip");
		//GameInstance gi = GameIO.readSnapshotFromZip(fis);
		//GameInstance gi = new GameInstance(new Game());
		//gi.name = "Testsession";
		//gi.players.add(player);
	    GameWindow gw2 = connectAndJoinGame(address, port, player2, "Testsession", lh);
    	try {
    		Thread.sleep(500);
    	}catch(InterruptedException e) {
			logger.error("Unecpected interrupt", e);
    	}

	}
}
