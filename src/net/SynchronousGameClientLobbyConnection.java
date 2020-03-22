package net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import util.StringUtils;

public class SynchronousGameClientLobbyConnection {
	String address;
	int port;

	public SynchronousGameClientLobbyConnection(String address, int port)
	{
		this.address = address;
		this.port = port;
	}
	public void getPlayers(String gameInstanceName, ArrayList<String> result) throws UnknownHostException, IOException
	{
		Socket server = new Socket( address, port);
	    PrintWriter out = new PrintWriter( server.getOutputStream(), true );
	    out.write("list player ");
	    out.write(gameInstanceName);
	    out.flush();
	    Scanner in = new Scanner(server.getInputStream());
	    String answer = in.nextLine();
	    StringUtils.split(answer, ' ', result);
	    server.close();
	}
	
	public void getGameInstances(ArrayList<String> result) throws UnknownHostException, IOException
	{
		Socket server = new Socket( address, port);
	    PrintWriter out = new PrintWriter( server.getOutputStream(), true );
	    out.write("list gameinstances");
	    out.flush();
	    Scanner in = new Scanner(server.getInputStream());
	    String answer = in.nextLine();
	    StringUtils.split(answer, ' ', result);		
	    server.close();
	}
}
