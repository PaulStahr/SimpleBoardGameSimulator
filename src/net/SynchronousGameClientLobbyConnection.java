package net;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import gameObjects.UsertextMessageAction;
import gameObjects.instance.GameInstance;
import io.GameIO;
import util.StringUtils;

public class SynchronousGameClientLobbyConnection {
	String address;
	int port;

	public SynchronousGameClientLobbyConnection(String address, int port)
	{
		this.address = address;
		this.port = port;
	}
	
	public UsertextMessageAction getUserMessage(int index) throws IOException
	{
		Socket server = new Socket( address, port);
	    PrintWriter out = new PrintWriter( server.getOutputStream(), true );
	    out.write(NetworkString.PULL);
	    out.write(' ');
	    out.write(NetworkString.MESSAGE);
	    out.write(' ');
	    out.print(index);
	    out.flush();
	    Scanner scanner = new Scanner(server.getInputStream());
	    UsertextMessageAction result = new UsertextMessageAction(scanner.nextInt(), scanner.nextInt(), scanner.next());
	    scanner.close();
	    server.close();
	    return result;
	}
	
	public int getGameSessionHash(String gameInstanceName) throws IOException
	{
		Socket server = new Socket( address, port);
	    PrintWriter out = new PrintWriter( server.getOutputStream(), true );
	    out.write(NetworkString.HASH);
	    out.write(' ');
	    out.write(NetworkString.GAME_INSTANCE);
	    out.write(' ');
	    out.write(gameInstanceName);
	    out.flush();
	    Scanner scanner = new Scanner(server.getInputStream());
	    int result = scanner.nextInt();
	    scanner.close();
	    server.close();
	    return result;
	}
	
	public void createGameSession(String gameInstanceName) throws IOException
	{
		Socket server = new Socket( address, port);
	    PrintWriter out = new PrintWriter( server.getOutputStream(), true );
	    out.write(NetworkString.CREATE);
	    out.write(' ');
	    out.write(NetworkString.GAME_INSTANCE);
	    out.write(' ');
	    out.write(gameInstanceName);
	    out.flush();
	    server.close();
	}
	
	public void pushGameSession(GameInstance gi) throws UnknownHostException, IOException
	{
		Socket server = new Socket( address, port);
		OutputStream oStream = server.getOutputStream();
	    PrintWriter out = new PrintWriter( oStream, true );
	    out.write(NetworkString.CREATE);
	    out.write(' ');
	    out.write(NetworkString.GAME_INSTANCE);
	    out.flush();
	    GameIO.writeSnapshotToZip(gi, oStream);
	    server.close();
	}
	
	public void getPlayers(String gameInstanceName, ArrayList<String> result) throws UnknownHostException, IOException
	{
		Socket server = new Socket( address, port);
	    PrintWriter out = new PrintWriter( server.getOutputStream(), true );
	    out.write(NetworkString.LIST);
	    out.write(' ');
	    out.write((NetworkString.PLAYER));
	    out.write(' ');
	    out.write(gameInstanceName);
	    out.flush();
	    Scanner in = new Scanner(server.getInputStream());
	    String answer = in.nextLine();
	    StringUtils.split(answer, ' ', result);
	    in.close();
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
	    in.close();
	    server.close();
	}
}
