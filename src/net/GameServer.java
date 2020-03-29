package net;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.UsertextMessageAction;
import gameObjects.definition.GameObject;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import io.GameIO;
import main.DataHandler;
import main.Player;
import util.StringUtils;

public class GameServer implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(GameServer.class);
	private int port;
	public GameServer(int port)
	{
		this.port = port;
	}
	
	public final ArrayList<GameInstance> gameInstances = new ArrayList<>();
	public final ArrayList<UsertextMessageAction> userMessageChatHistory = new ArrayList<>();
	volatile boolean isReady;
	private Thread th;
	CommandEncoding ce = CommandEncoding.SERIALIZE;
	private final int id = (int)(Integer.MAX_VALUE * Math.random());
	
	public boolean isReady()
	{
		return isReady;
	}
	
	private GameInstance getGameInstance(String name)
	{
		logger.debug("get instance (" + id + ")" + name);
		for (int i = 0; i < gameInstances.size(); ++i)
		{
			System.out.print("\"" + gameInstances.get(i).name + "\" ");
			if (gameInstances.get(i).name.equals(name))
			{
				System.out.println("found");
				return gameInstances.get(i);
			}
		}
		System.out.println("Not found");
		return null;
	}
	
	public static String readLine(InputStream is) throws IOException
	{
		StringBuilder strB = new StringBuilder();
		int value;
		while ((value = is.read()) != '\n')
		{
			strB.append((char)value);
		}
		return strB.toString();
	}
	
	class ConnectionHandle implements Runnable
	{
		Socket client;
		public ConnectionHandle(Socket client)
		{
			this.client = client;
		}
		
		@Override
		public void run()
		{
			try {
				InputStream input = client.getInputStream();
				OutputStream output = client.getOutputStream();
				//in = new Scanner( input);
				//String line = in.nextLine();
				String line = null;
				if (ce == CommandEncoding.SERIALIZE)
				{
					ObjectInputStream objIn = new ObjectInputStream(input);
					try {
						line = (String)objIn.readObject();
					} catch (ClassNotFoundException e) {
						logger.error("Unknonwn input class", e);
					}
					input = objIn;
				}
				else if (ce == CommandEncoding.PLAIN)
				{
					line = readLine(input);
				}
				ArrayList<String> split = new ArrayList<>();
				StringUtils.split(line, ' ', split);
				logger.debug(line);
			    switch (split.get(0))
			    {
			    	case NetworkString.LIST:
			    	{
			    		switch (split.get(1))
			    		{
			    			case NetworkString.GAME_INSTANCE: 
			    			{
					    	    PrintWriter out = new PrintWriter( output, true );
					    		for (int i = 0; i < gameInstances.size(); ++i)
					    		{
					    			out.write(gameInstances.get(i).name);
					    			out.write(' ');
					    		}
					    		out.close();
			    			}
			    			case NetworkString.PLAYER:
			    			{
			    				PrintWriter out = new PrintWriter(output, true);
			    				String gameinstanceName = split.get(2);
			    				GameInstance gi = getGameInstance(gameinstanceName);
			    				for (int i = 0; i < gi.players.size(); ++i)
			    				{
			    					out.write(gi.players.get(i).name);
			    				}
			    				out.close();
			    			}
			    		}
			    		break;
			    	}
			    	case NetworkString.HASH:
			    	{
			    		switch (split.get(1))
			    		{
			    			case NetworkString.GAME_INSTANCE:
			    			{
			    				GameInstance gi = getGameInstance(split.get(2));
			    				PrintWriter out = new PrintWriter(output, true);
			    				out.write(gi.hashCode());
			    				out.flush();
			    				break;
			    			}
			    		}
			    		break;
			    	}
			    	case NetworkString.CREATE:
			    	{
			    		switch (split.get(1))
			    		{
			    			case NetworkString.GAME_INSTANCE:
								GameInstance gi = new GameInstance(new Game());
								gi.name = split.get(2);
								synchronized(gameInstances)
			    				{
			    					gameInstances.add(gi);
			    				}
							
			    				break;
			    			case NetworkString.GAME_OBJECT:
			    				break;
			    			case NetworkString.GAME_OBJECT_INSTANCE:
			    				break;
			    			case NetworkString.PLAYER:
			    				break;
			    			
			    		}
			    		break;
			    	}
			    	case NetworkString.PUSH:
			    	{
			    		switch (split.get(1))
			    		{
			    			case NetworkString.GAME_INSTANCE:
			    				logger.debug("do push (" + id + ")");
								try {
									GameInstance gi;
									if (ce == CommandEncoding.SERIALIZE)
									{
										gi = GameIO.readSnapshotFromZip(new ByteArrayInputStream((byte[])((ObjectInputStream)input).readObject()));
									}
									else
									{
										gi = GameIO.readSnapshotFromZip(input);		
									}
									logger.debug("read successfull");
									synchronized(gameInstances)
				    				{
				    					gameInstances.add(gi);
				    				}
								} catch (JDOMException e) {
									logger.error("Can't read instance ", e);
								} catch (ClassNotFoundException e) {
									logger.error("Can't read instance ", e);
								}
			    				
			    				break;
			    			case NetworkString.MESSAGE:
			    				userMessageChatHistory.add(new UsertextMessageAction(Integer.parseInt(split.get(2)), Integer.parseInt(split.get(3)), split.get(4)));
			    				break;
			    			case NetworkString.GAME_OBJECT:
			    				break;
			    			case NetworkString.GAME_OBJECT_INSTANCE:
			    				break;
			    			case NetworkString.PLAYER:
			    				break;
			    			
			    		}
			    		break;
			    	}
			    	case NetworkString.PULL:
			    	{
			    		switch(split.get(1))
			    		{
			    			case NetworkString.GAME_INSTANCE:
			    			{
			    				GameInstance gi = getGameInstance(split.get(2));
			    				GameIO.writeSnapshotToZip(gi, output);
			    				break;
			    			}
			    			case NetworkString.MESSAGE:
			    			{
			    				int index = Integer.parseInt(split.get(2));
			    				if (index < userMessageChatHistory.size())
			    				{
				    				UsertextMessageAction message = userMessageChatHistory.get(index);
				    				PrintWriter printer = new PrintWriter(output);
				    				printer.print(message.source);
				    				printer.print(' ');
				    				printer.print(message.player);
				    				printer.print(' ');
				    				printer.print(message.message);
				    				printer.close();
			    				}
			    			}
			    			case NetworkString.GAME_OBJECT:
			    			{
			    				GameInstance gi = getGameInstance(split.get(2));
			    				GameObject go = gi.game.getObject(split.get(3));
			    				//GameIO.saveGameObject(go, output);
			    				break;
			    			}
			    			case NetworkString.GAME_OBJECT_INSTANCE:
			    			{
			    				GameInstance gi = getGameInstance(split.get(2));
			    				ObjectInstance oi = gi.getObjectInstance(Integer.parseInt(split.get(2)));
			    				//GameIO.saveGameObjectInstance(go, output);
			    				break;
			    			}
			    		}
			    		break;
			    	}
			    	case NetworkString.JOIN:
			    	{
			    		String player = split.get(1);
						int id = Integer.parseInt(split.get(2));
						GameInstance gi = getGameInstance(split.get(3));
						if (gi == null)
						{
							throw new NullPointerException("Can't find game instance " + split.get(3));
						}
			    		if (gi.password == null || gi.password.equals(split.get(4)))
			    		{
			    			gi.addPlayer(new Player(player, id));
			    		}
			    		break;
			    	}
			    	case NetworkString.CONNECT:
			    	{
			    		GameInstance gi = getGameInstance(split.get(1));
			    		AsynchronousGameConnection asc;
			    		if (input instanceof ObjectInputStream)
			    		{
			    			logger.debug("Connect to Object Input");
			    			asc = new AsynchronousGameConnection(gi, (ObjectInputStream)input, output);
			    		}
			    		else
			    		{
			    			logger.debug("Connect to Stream Input");
			    			asc = new AsynchronousGameConnection(gi, input, output);
				    	}
			    		input = null;
			    		output = null;
			    		client = null;
			    		asc.start();
			    		break;
			    	}
			    	default:
			    		logger.error("Unknown command");
			    }
			} catch (IOException e) {
				logger.error("Networking error", e);
			}
			try { if (client != null) {client.close();} } catch ( IOException e ) { }
			client = null;
		}	
    }
	
	@Override
	public void run()
	{
		ServerSocket server;
		try {
			server = new ServerSocket( port );
			isReady = true;
			while ( true )
		    {
		    	Socket client = null;
		    	try
		    	{
		    		client = server.accept();
		    		DataHandler.tp.run(new ConnectionHandle(client), "Server Connection");
		    	}
		    	catch ( IOException e ) {
		    		e.printStackTrace();
		    		
		    	}
		    	
		    }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void start()
    {
		 if (th == null)
		 {
			 th = new Thread(this);
			 th.start();
		 }
    }
}
