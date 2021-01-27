package test;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

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
import util.JFrameUtils;

public class SimpleNetworkServertest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleNetworkServertest.class);
    public static List<Player> PlayerList = new ArrayList<>();
    public static List<GameWindow> GameWindowList = new ArrayList<>();
    public static int AdditionalPlayers = 2;
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
    	AsynchronousGameConnection connection = sclc.connectToGameSession(gi, null);
    	connection.start();
    	gi.addPlayer(null, player);
    	JFrameUtils.runByDispatcherAndWait(
    			new Runnable() {
					
					@Override
					public void run() {
				    	gw.setVisible(true);
					}
				}
			);
    }
    
    public static GameWindow connectAndJoinGame(String address, int port, Player player, String gameInstanceId, LanguageHandler lh) throws UnknownHostException, IOException, JDOMException
    {
    	SynchronousGameClientLobbyConnection sclc = new SynchronousGameClientLobbyConnection(address,  port);
    	GameInstance gi = sclc.getGameInstance(gameInstanceId);
    	sclc.addPlayerToGameSession(player, gi.name, gi.password);
    	AsynchronousGameConnection connection = sclc.connectToGameSession(gi, null);
    	connection.syncPull();
    	connection.start();
    	player = gi.addPlayer(null, player);
    	GameWindow gw = new GameWindow(gi, player, lh);
    	JFrameUtils.runByDispatcherAndWait(new Runnable() {
			
			@Override
			public void run() {
		    	gw.setVisible(true);
			}
		});
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
    	   	Player player = new Player("Paul", 5);
    	   	FileInputStream fis = new FileInputStream("PrivateGames/Crew.zip");
			GameInstance gi = new GameInstance(new Game(), null);
			GameIO.readSnapshotFromZip(fis, gi);
			gi.name = "Testsession";
			gi.addPlayer(null, player);
	    	fis.close();
	    	connectAndStartGame(address, port, player, gi, lh);
    	}
    	try {
    		Thread.sleep(800);
    	}catch(InterruptedException e) {}
    	


	   	Player player = new Player("Florian", 2);
	    GameWindow gw = connectAndJoinGame(address, port, player, "Testsession", lh);
    	try {
    		Thread.sleep(1000);
    	}catch(InterruptedException e) {
			logger.error("Unecpected interrupt", e);
    	}

		for(int i=0; i<AdditionalPlayers; ++i)
		{
			PlayerList.add(new Player("TestPlayer" + (i+1), 3+i));
			GameWindowList.add(connectAndJoinGame(address, port, PlayerList.get(PlayerList.size()-1), "Testsession", lh));
			try {
				Thread.sleep(1000);
			}catch(InterruptedException e) {
				logger.error("Unecpected interrupt", e);
			}
		}
	}
}
