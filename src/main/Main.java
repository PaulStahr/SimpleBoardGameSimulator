package main;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
			GameInstance game = GameIO.readGame(fis);
	    	fis.close();
	    	GameWindow gw = new GameWindow(game);
	    	gw.setVisible(true);
	    	
			ServerConnectionDialog scd = new ServerConnectionDialog();
			scd.setVisible(true);
			FileOutputStream fos = new FileOutputStream("output.zip");
			GameIO.saveGame(game, fos);
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
    		FileInputStream fis = new FileInputStream("Doppelkopf.zip");
    		GameInstance game0 = GameIO.readGame(fis);
        	fis.close();
        	fis = new FileInputStream("Doppelkopf.zip");
        	GameInstance game1 = GameIO.readGame(fis);
	    	fis.close();
	    	GameServer server = new GameServer(1234);
	    	server.gameInstances.add(game0);
	    	server.start();
	    	
	        Socket socket = new Socket( "localhost", 1234 );
	        
	        AsynchronousGameConnection sgc = new AsynchronousGameConnection(game1, socket.getInputStream(), socket.getOutputStream());
	    	sgc.start();
	    	GameWindow gw0 = new GameWindow(game0);
	    	GameWindow gw1 = new GameWindow(game0);
	    	
	    	GameWindow gw2 = new GameWindow(game1);
	    	gw0.setVisible(true);
	    	gw1.setVisible(true);
	    	gw2.setVisible(true);
	    	FileOutputStream fos = new FileOutputStream("output.zip");
			GameIO.saveSnapshot(game0, fos);
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
