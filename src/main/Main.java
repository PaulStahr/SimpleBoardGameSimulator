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
    	//connectionTest();
    	
    	FileInputStream fis;
		try {
			fis = new FileInputStream("Doppelkopf.zip");
			GameInstance game = GameIO.readSnapshotFromStream(fis);
			fis.close();
	    	GameWindow gw = new GameWindow(game);
	    	gw.gamePanel.player = new Player("Paul", 0);
	    	gw.setVisible(true);
	    	
			ServerConnectionDialog scd = new ServerConnectionDialog();
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
		}
    }
    
    public static void connectionTest()
    {
    	try {
    		
    		FileInputStream fis = new FileInputStream("SchreckenDesTempels.zip");
    		GameInstance game0 = GameIO.readSnapshotFromStream(fis);
        	fis.close();
        	fis = new FileInputStream("SchreckenDesTempels.zip");
        	GameInstance game1 = GameIO.readSnapshotFromStream(fis);
	    	fis.close();
	    	/*GameServer server = new GameServer(1234);
	    	server.gameInstances.add(game0);
	    	server.start();*/
	    	
	    	ServerSocket server = new ServerSocket(1234);
	    	final Socket socket[] = new Socket[1];
	        Thread th = new Thread()
			{
	    		public void run()
	    		{
	    			try {
						socket[0] = new Socket( "localhost", 1234 );
						System.out.println("hu");
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	    		}
			};
			th.start();
			Socket socket2 = null;
			do {
				socket2 = server.accept();
			}while(socket2 == null);
	    	System.out.println("hi");
	        System.out.println("ho");
	        
	        AsynchronousGameConnection sgc = new AsynchronousGameConnection(game0, socket[0].getInputStream(), socket[0].getOutputStream());
	    	sgc.start();
	    	AsynchronousGameConnection sgc1 = new AsynchronousGameConnection(game1, socket2.getInputStream(), socket2.getOutputStream());
	    	sgc1.start();
	    	GameWindow gw0 = new GameWindow(game0);
	    	gw0.gamePanel.player = new Player("Paul", 0);
	    	game0.players.add(gw0.gamePanel.player);
		    	//GameWindow gw1 = new GameWindow(game0);
	    	
	    	GameWindow gw2 = new GameWindow(game1);
	       	gw2.gamePanel.player = new Player("Paul", 0);
	       	game1.players.add(gw0.gamePanel.player);
		    gw0.setVisible(true);
	    	//gw1.setVisible(true);
	    	gw2.setVisible(true);
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
