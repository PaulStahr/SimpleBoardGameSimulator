package net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.UsertextMessageAction;
import gameObjects.instance.GameInstance;
import io.GameIO;
import main.Player;
import util.StringUtils;

public class SynchronousGameClientLobbyConnection {
	String address;
	int port;
	CommandEncoding ce = CommandEncoding.SERIALIZE;
	Logger logger = LoggerFactory.getLogger(SynchronousGameClientLobbyConnection.class);

	public void setAdress(String address)
	{
		this.address = address;
	}
	
	public void setPort(int port)
	{
		this.port = port;
	}

	public SynchronousGameClientLobbyConnection(String address, int port)
	{
		this.address = address;
		this.port = port;
	}
	
	public UsertextMessageAction getUserMessage(int index) throws IOException
	{
		Socket server = new Socket( address, port);
		StringBuilder strB = new StringBuilder();
	    strB.append(NetworkString.PULL).append(' ').append(NetworkString.MESSAGE).append(index);
	    writeCommand(strB, server.getOutputStream());
	    Scanner scanner = new Scanner(server.getInputStream());
	    UsertextMessageAction result = new UsertextMessageAction(scanner.nextInt(), scanner.nextInt(), scanner.next());
	    scanner.close();
	    server.close();
	    return result;
	}
	
	public int getGameSessionHash(String gameInstanceName) throws IOException
	{
		Socket server = new Socket( address, port);
	    StringBuilder strB = new StringBuilder();
	    strB.append(NetworkString.HASH).append(' ').append(NetworkString.GAME_INSTANCE).append(' ').append(gameInstanceName);
	    writeCommand(strB, server.getOutputStream());
	    Scanner scanner = new Scanner(server.getInputStream());
	    int result = scanner.nextInt();
	    scanner.close();
	    server.close();
	    return result;
	}
	
	OutputStream writeCommand(StringBuilder strB, OutputStream oStream) throws IOException
	{
		logger.debug("Write command " + strB.toString());
		switch (ce)
		{
			case PLAIN: oStream.write(strB.append('\n').toString().getBytes());break;
			case SERIALIZE:
			{
				if (!(oStream instanceof ObjectOutputStream))
				{
					oStream = new ObjectOutputStream(oStream);
				}
				((ObjectOutputStream)oStream).writeObject(strB.toString());
			}
		}
		return oStream;
	}
	
	public void createGameSession(String gameInstanceName) throws IOException
	{
		Socket server = new Socket( address, port);
	    StringBuilder strB = new StringBuilder();
		strB.append(NetworkString.CREATE).append(' ').append(NetworkString.GAME_INSTANCE).append(' ').append(gameInstanceName);
		writeCommand(strB, server.getOutputStream());
	    server.close();
	}
	
	public void pushGameSession(GameInstance gi) throws UnknownHostException, IOException
	{
		Socket server = new Socket( address, port);
		OutputStream oStream = server.getOutputStream();
		StringBuilder strB = new StringBuilder();
		strB.append(NetworkString.PUSH).append(' ').append(NetworkString.GAME_INSTANCE);
		oStream = writeCommand(strB, oStream);
	    //GameIO.writeSnapshotToZip(gi, oStream);
	    ByteArrayOutputStream bos = new ByteArrayOutputStream();
	    GameIO.writeSnapshotToZip(gi, bos);
	    if (ce == CommandEncoding.SERIALIZE)
	    {
	    	((ObjectOutputStream)oStream).writeObject(bos.toByteArray());
	    }
	    else
	    {
	    	bos.writeTo(oStream);	    	
	    }
	    server.close();
	    logger.debug("successfull");
	}
	
	public void getPlayers(String gameInstanceName, ArrayList<String> result) throws UnknownHostException, IOException
	{
		Socket server = new Socket( address, port);
	    StringBuilder strB = new StringBuilder();
	    strB.append(NetworkString.LIST).append(' ').append(NetworkString.PLAYER).append(' ').append(gameInstanceName);
	    writeCommand(strB, server.getOutputStream());
	    Scanner in = new Scanner(server.getInputStream());
	    String answer = in.nextLine();
	    StringUtils.split(answer, ' ', result);
	    in.close();
	    server.close();
	}
	
	public void getGameInstances(ArrayList<String> result) throws UnknownHostException, IOException
	{
		Socket server = new Socket( address, port);
	    StringBuilder strB = new StringBuilder();
	    strB.append(NetworkString.LIST).append(' ').append(NetworkString.GAME_INSTANCE);
	    writeCommand(strB, server.getOutputStream());
	    Scanner in = new Scanner(server.getInputStream());
	    String answer = in.nextLine();
	    StringUtils.split(answer, ' ', result);		
	    in.close();
	    server.close();
	}

	public void addPlayerToGameSession(Player player, String name, String password) throws UnknownHostException, IOException
	{
		Socket server = new Socket( address, port);
	    StringBuilder strB = new StringBuilder();
	    strB.append(NetworkString.JOIN).append(' ').append(player.name).append(' ').append(player.id).append(' ').append(name);
	    if (password != null)
	    {
	    	strB.append(' ').append(password);
	    }
	    writeCommand(strB, server.getOutputStream());
	    server.close();
	}

	public AsynchronousGameConnection connectToGameSession(GameInstance gi) throws UnknownHostException, IOException
	{
		Socket server = new Socket( address, port);
		OutputStream output = server.getOutputStream();
	    StringBuilder strB = new StringBuilder();
	    strB.append(NetworkString.CONNECT).append(' ').append(gi.name);
	    output = writeCommand(strB, output);
	    return new AsynchronousGameConnection(gi, server.getInputStream(), output);
	}

	public GameInstance getGameInstance(String gameInstanceId) throws UnknownHostException, IOException, JDOMException
	{
		Socket server = new Socket( address, port);
		OutputStream output = server.getOutputStream();
	    StringBuilder strB = new StringBuilder();
	    strB.append(NetworkString.READ).append(' ').append(NetworkString.GAME_INSTANCE).append(' ').append(gameInstanceId);
	    output = writeCommand(strB, output);
	    GameInstance gi = GameIO.readSnapshotFromZip(server.getInputStream());
	    logger.debug("Read successfull");
	    server.close();
	    return gi;
	}
}
