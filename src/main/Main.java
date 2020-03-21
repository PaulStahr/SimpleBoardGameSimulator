package main;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.jdom2.JDOMException;

import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gui.GameWindow;
import gui.ServerConnectionDialog;
import io.GameIO;

public class Main {
    public static final void main (String args[]){
    	FileInputStream fis;
		try {
			fis = new FileInputStream("SchreckenDesTempels.zip");
			GameInstance game = GameIO.readGame(fis);
	    	fis.close();
	    	GameWindow gw = new GameWindow(game);
	    	gw.setVisible(true);
	    	
			ServerConnectionDialog scd = new ServerConnectionDialog();
			scd.setVisible(true);
			FileOutputStream fos = new FileOutputStream("output.zip");
			GameIO.saveGame(game.game, fos);
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