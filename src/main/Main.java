package main;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gui.GameWindow;
import gui.Language.LanguageSummary;
import gui.LanguageHandler;
import gui.ServerLobbyWindow;
import io.GameIO;
import logging.LockbackUtil;
import net.AsynchronousGameConnection;
import net.GameServer;
import net.SynchronousGameClientLobbyConnection;


public class Main {
	public static final Logger logger = LoggerFactory.getLogger(Main.class);
	
	/* This is the main() function */
	public static final void main (String args[]){
		/*ServerLobbyWindow tmp = new ServerLobbyWindow(new SynchronousGameClientLobbyConnection("212.201.75.217", 20));
		tmp.setVisible(true);
    	tmp.setSize(500,200);*/
		LockbackUtil.setLoglevel("WARN");
		//LockbackUtil.setLoglevel("DEBUG");
		/*if (true)
		{
			return;
		}*/
		LanguageHandler lh = new LanguageHandler(new LanguageSummary("de", "de"));
		for (int i = 0; i < args.length; ++i)
    	{
    		if (args[i].equals("--loglevel"))
    		{
       			++i;
    			LockbackUtil.setLoglevel(args[i].toUpperCase());
    		}
    		else if (args[i].equals("--server"))
    		{
    			GameServer gs = new GameServer(Integer.parseInt(args[i + 1]));
    	    	gs.start();
    	    	return;
    		}
    		else if (args[i].equals("--join"))
    		{
    			if (args.length != 6)
    			{
    				System.out.println("Usage: <address> <port> <name> <id> <game>");
    			}
    			try {
					test.SimpleNetworkServertest.connectAndJoinGame(args[i + 1], Integer.parseInt(args[i + 2]), new Player(args[i + 3], Integer.parseInt(args[i + 4])), args[i + 5], lh);
				} catch (NumberFormatException e) {
					logger.error("Can't parse port", e);
				} catch (UnknownHostException e) {
					logger.error("Unknown host", e);
				} catch (IOException e) {
					logger.error("Error during join game", e);
				} catch (JDOMException e) {
					logger.error("Can't read", e);
				}
				return;
    		}
    		else if (args[i].equals("--create"))
    		{
				try {
	    			FileInputStream fis = new FileInputStream("Doppelkopf.zip");
			 		GameInstance game0 = new GameInstance(new Game(), null);
			 		GameIO.readSnapshotFromZip(fis, game0);
	            	fis.close();
	            	game0.name = "Testsession";
	            	game0.addPlayer(null, new Player(args[i + 3], Integer.parseInt(args[i + 4])));
	            	test.SimpleNetworkServertest.connectAndStartGame(args[i + 1], Integer.parseInt(args[i + 2]), game0.getPlayerById(Integer.parseInt(args[i + 4])), game0, lh);
	    		} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (NumberFormatException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnknownHostException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JDOMException e) {
					logger.error("Can't read", e);
				}
				return;
    		}
    	}
    	try {
    		int port = 8000 + (int)(Math.random() * 100);
    		ServerLobbyWindow slw = new ServerLobbyWindow(new SynchronousGameClientLobbyConnection("127.0.0.1", port), lh);
        	slw.setVisible(true);
        	slw.setSize(300,100);
        	test.SimpleNetworkServertest.localTwoInstanceTest(port, lh);
		} catch (IOException | JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//connectionTest();
    	/*
    	FileInputStream fis;
		try {
			fis = new FileInputStream("Doppelkopf.zip");
			GameInstance game = GameIO.readSnapshotFromStream(fis);
			fis.close();
	    	GameWindow gw = new GameWindow(game);
	    	gw.gamePanel.player = new Player("Paul", 0);
	    	gw.setVisible(true);
	    	
			ServerConnectionDialog scd = new ServerConnectionDialog("127.0.0.1", 1234);
			scd.setVisible(true);
			FileOutputStream fos = new FileOutputStream("output.zip");
			GameIO.writeSnapshotToZip(game, fos);
			fos.close();
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
    }

    
    public static void connectToServer(String address, int port)
    {
    	
    }
    
    public static void connectionTest()
    {
    	try {
    		LanguageHandler lh = null;
    		FileInputStream fis = new FileInputStream("Doppelkopf.zip");
    		GameInstance game0 = new GameInstance(new Game(), null);
    		GameIO.readSnapshotFromZip(fis, game0);
        	fis.close();
        	fis = new FileInputStream("Doppelkopf.zip");
    		GameInstance game1 = new GameInstance(new Game(), null);
        	GameIO.readSnapshotFromZip(fis, game1);
	    	fis.close();
	    	/*GameServer server = new GameServer(1234);
	    	server.gameInstances.add(game0);
	    	server.start();*/
	    	ServerSocket server = null;
	    	try
	    	{
	    		server = new ServerSocket(1234);
	    	}
	    	catch(Exception e)
	    	{
	    		e.printStackTrace();
	    	}
	    	final Socket socket[] = new Socket[1];
	        Thread th = new Thread()
			{
	    		@Override
				public void run()
	    		{
	    			try {
						socket[0] = new Socket( "localhost", 1234 );
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
			};
			th.start();
			Player player = new Player("Paul", 0);
			Socket socket2 = null;
			if (server != null)
			{
				do {
					socket2 = server.accept();
				}while(socket2 == null);	
		    	AsynchronousGameConnection sgc1 = new AsynchronousGameConnection(game1, socket2.getInputStream(), socket2.getOutputStream(), socket2);
		    	sgc1.start();
		    	GameWindow gw2 = new GameWindow(game1, lh);
		       	player = game1.addPlayer(null, player);    	
		       	gw2.gamePanel.setPlayer(player);
		       	gw2.setVisible(true);
		    }
			
			while (socket[0] == null);
	        AsynchronousGameConnection sgc = new AsynchronousGameConnection(game0, socket[0].getInputStream(), socket[0].getOutputStream(), socket[0]);
	    	sgc.start();
	    	GameWindow gw0 = new GameWindow(game0, lh);
	    	player = game0.addPlayer(null, player);
	    	gw0.gamePanel.setPlayer(player);
	    		//GameWindow gw1 = new GameWindow(game0);
	    	
	    	gw0.setVisible(true);
	    	//gw1.setVisible(true);
	    	FileOutputStream fos = new FileOutputStream("output.zip");
			GameIO.writeSnapshotToZip(game0, fos);
			fos.close();
    	} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JDOMException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
