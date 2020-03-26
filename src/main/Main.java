package main;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.jdom2.JDOMException;

import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gui.GameWindow;
import gui.ServerConnectionDialog;
import io.GameIO;
import net.AsynchronousGameConnection;
import net.GameServer;

public class Main {
    public static final void main (String args[]){
    	for (int i = 0; i < args.length; ++i)
    	{
    		if (args[i].equals("--server"))
    		{
    			GameServer gs = test.SimpleNetworkServertest.startNewServer(Integer.parseInt(args[i + 1]));
    			return;
    		}
    	}
    	try {
			test.SimpleNetworkServertest.localTwoInstanceTest();
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
    		
    		FileInputStream fis = new FileInputStream("Doppelkopf.zip");
    		GameInstance game0 = GameIO.readSnapshotFromZip(fis);
        	fis.close();
        	fis = new FileInputStream("Doppelkopf.zip");
        	GameInstance game1 = GameIO.readSnapshotFromZip(fis);
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
		    	AsynchronousGameConnection sgc1 = new AsynchronousGameConnection(game1, socket2.getInputStream(), socket2.getOutputStream());
		    	sgc1.start();
		    	GameWindow gw2 = new GameWindow(game1);
		       	gw2.gamePanel.player = player;
		       	game1.players.add(player);    	
		       	gw2.setVisible(true);
		    }
			
			while (socket[0] == null);
	        AsynchronousGameConnection sgc = new AsynchronousGameConnection(game0, socket[0].getInputStream(), socket[0].getOutputStream());
	    	sgc.start();
	    	GameWindow gw0 = new GameWindow(game0);
	    	gw0.gamePanel.player = player;
	    	game0.players.add(player);
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
