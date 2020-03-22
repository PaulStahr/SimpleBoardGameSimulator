package gui;

import javax.swing.JFrame;

public class ServerLobbyWindow extends JFrame{
	public final String address;
	public final int port;
	
	/*TODOS here:
	show a list of the current running games
	show a list of lokally installed games
	Start a new game session
	Connect to an existing game session
	*/
	
	public ServerLobbyWindow(String address, int port)
	{
		this.address = address;
		this.port = port;
	}
	
	
}
