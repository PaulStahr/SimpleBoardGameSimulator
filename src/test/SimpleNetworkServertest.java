package test;

import gameObjects.action.player.PlayerAddAction;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gui.game.GameWindow;
import gui.language.LanguageHandler;
import io.GameIO;
import main.Player;
import net.AsynchronousGameConnection;
import net.GameServer;
import net.SynchronousGameClientLobbyConnection;
import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.JFrameUtils;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class SimpleNetworkServertest {
    private static final Logger logger = LoggerFactory.getLogger(SimpleNetworkServertest.class);
    public static List<Player> PlayerList = new ArrayList<>();
    public static List<GameWindow> GameWindowList = new ArrayList<>();
    public static int AdditionalPlayers = 0;
    public static boolean startGame = true;
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
    	gi.addPlayer(new PlayerAddAction((int)System.nanoTime(), player));
    	JFrameUtils.runByDispatcherAndWaitNoExcept(
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
    	player = gi.addPlayer(new PlayerAddAction(-1, player));
    	GameWindow gw = new GameWindow(gi, player, lh);
    	JFrameUtils.runByDispatcherAndWaitNoExcept(new Runnable() {
			
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

    	if (startGame) {
			Player player = new Player("Paul", 8);
			FileInputStream fis = new FileInputStream("PrivateGames/Codenames.zip");
			GameInstance gi = new GameInstance(new Game(), null);
			GameIO.readSnapshotFromZip(fis, gi);
			gi.name = "Testsession";
			gi.begin_play();
			gi.addPlayer(new PlayerAddAction(-1, player));
			fis.close();
			connectAndStartGame(address, port, player, gi, lh);

			try {
				Thread.sleep(800);
			} catch (InterruptedException e) {}

			Player player1 = new Player("Florian", 2);
			GameWindow gw = connectAndJoinGame(address, port, player1, "Testsession", lh);
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				logger.error("Unecpected interrupt", e);
			}

			for (int i = 0; i < AdditionalPlayers; ++i) {
				PlayerList.add(new Player("TestPlayer" + (i + 1), 3 + i));
				GameWindowList.add(connectAndJoinGame(address, port, PlayerList.get(PlayerList.size() - 1), "Testsession", lh));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					logger.error("Unecpected interrupt", e);
				}
			}
		}
	}
}
