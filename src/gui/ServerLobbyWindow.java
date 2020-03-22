package gui;

import javax.swing.JFrame;

import net.SynchronousGameClientLobbyConnection;
import net.ServerConnection;

public class ServerLobbyWindow extends JFrame{
	public final SynchronousGameClientLobbyConnection client;
	
	/*TODOS here:
	show a list of the current running games
	show a list of lokally installed games
	Start a new game session
	Connect to an existing game session
	*/
	
	public ServerLobbyWindow(SynchronousGameClientLobbyConnection client)
	{
		this.client = client;
	}
	
	
}
