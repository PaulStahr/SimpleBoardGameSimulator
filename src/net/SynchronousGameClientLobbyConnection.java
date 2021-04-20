package net;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.GameMetaInfo;
import gameObjects.action.message.UsertextMessageAction;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import io.GameIO;
import gui.game.Player;
import util.StringUtils;

public class SynchronousGameClientLobbyConnection {
	String address;
	int port;
	CommandEncoding ce = CommandEncoding.SERIALIZE;
	private static final Logger logger = LoggerFactory.getLogger(SynchronousGameClientLobbyConnection.class);

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
	    UsertextMessageAction result = new UsertextMessageAction(scanner.nextInt(), scanner.nextInt(), scanner.nextInt(), scanner.next());
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
	
	private final OutputStream writeCommand(StringBuilder strB, OutputStream oStream) throws IOException
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
	
	public void getGameInstanceMeta(ArrayList<GameMetaInfo> result) throws UnknownHostException, IOException, ClassNotFoundException
	{
		Socket server = new Socket( address, port);
	    StringBuilder strB = new StringBuilder();
	    strB.append(NetworkString.META).append(' ').append(NetworkString.GAME_INSTANCE);
	    writeCommand(strB, server.getOutputStream());
	    ObjectInputStream ois = new ObjectInputStream(server.getInputStream());
	    GameMetaInfo gmi[] = (GameMetaInfo[])ois.readObject();
	    for (GameMetaInfo tmp : gmi)
	    {
	    	result.add(tmp);
	    }
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
	    strB.append(NetworkString.JOIN).append(' ').append(player.getName()).append(' ').append(player.id).append(' ').append(name);
	    if (password != null)
	    {
	    	strB.append(' ').append(password);
	    }
	    writeCommand(strB, server.getOutputStream());
	    server.close();
	}

	public AsynchronousGameConnection connectToGameSession(GameInstance gi, String password) throws UnknownHostException, IOException
	{
		Socket server = new Socket( address, port);
		OutputStream output = server.getOutputStream();
	    StringBuilder strB = new StringBuilder();
	    strB.append(NetworkString.CONNECT).append(' ').append(gi.name);
	    if (password != null)
    	{
	    	strB.append(' ').append(password);
    	}
	    output = writeCommand(strB, output);

	    if (password != null && password.length() != 0)
	    {
			try {
				final KeyGenerator kg = KeyGenerator.getInstance("AES");
	            kg.init(new SecureRandom(password.getBytes()));
	            final SecretKey key = kg.generateKey();
	            final Cipher c = Cipher.getInstance("AES");
	            c.init(Cipher.ENCRYPT_MODE, key);
	            CipherInputStream input = new CipherInputStream(server.getInputStream(), c);
	    	    return new AsynchronousGameConnection(gi, input, output, server);
			} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
				logger.error("Can't create encrypted stream",e);
			}
	    }
	    return new AsynchronousGameConnection(gi, server.getInputStream(), output, server);
	}

	public GameInstance getGameInstance(String gameInstanceId) throws UnknownHostException, IOException, JDOMException
	{
		Socket server = new Socket( address, port);
		OutputStream output = server.getOutputStream();
	    StringBuilder strB = new StringBuilder();
	    strB.append(NetworkString.READ).append(' ').append(NetworkString.GAME_INSTANCE).append(' ').append(gameInstanceId);
	    output = writeCommand(strB, output);
	    GameInstance gi = new GameInstance(new Game(), null);
	    GameIO.readSnapshotFromZip(server.getInputStream(), gi);
	    logger.debug("Read successfull");
	    server.close();
	    return gi;
	}

	public void deleteGame(String gameInstanceId, String password) throws UnknownHostException, IOException {
		Socket server = new Socket(address, port);
		OutputStream output = server.getOutputStream();
		StringBuilder strB = new StringBuilder();
		strB.append(NetworkString.DELETE).append(' ').append(NetworkString.GAME_INSTANCE).append(' ').append(gameInstanceId);
		output = writeCommand(strB, output);
		server.close();
	}

	public int getPort() {
		return port;
	}

	public String getAddress() {
		return address;
	}
}
