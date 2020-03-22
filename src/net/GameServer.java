package net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.UsertextMessageAction;
import gameObjects.definition.GameObject;
import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
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
	private Thread th;
	
	private GameInstance getGameInstance(String name)
	{
		for (int i = 0; i < gameInstances.size(); ++i)
		{
			if (gameInstances.get(i).name.equals(name))
			{
				return gameInstances.get(i);
			}
		}
		return null;
	}
	
	class ConnectionHandle implements Runnable
	{
		Socket client;
		public ConnectionHandle(Socket client)
		{
			this.client = client;
		}
		
		public void run()
		{
		    Scanner in;
			try {
				InputStream input = client.getInputStream();
				OutputStream output = client.getOutputStream();
				in = new Scanner( input);
				String line = in.nextLine();
				ArrayList<String> split = new ArrayList<>();
				StringUtils.split(line, ' ', split);
				split.clear();
				
			    line = in.nextLine();
			    StringUtils.split(line, ' ', split);
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
			    			case "gameobject":
			    				break;
			    			case "gameobjectinstance":
			    				break;
			    			case "player":
			    				break;
			    			
			    		}
			    	}
			    	case NetworkString.PUSH:
			    	{
			    		switch (split.get(1))
			    		{
			    			case NetworkString.GAME_INSTANCE:
								try {
									GameInstance gi = GameIO.readSnapshotFromStream(input);
									synchronized(gameInstances)
				    				{
				    					gameInstances.add(gi);
				    				}
								} catch (JDOMException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
			    				
			    				break;
			    			case NetworkString.MESSAGE:
			    				userMessageChatHistory.add(new UsertextMessageAction(Integer.parseInt(split.get(2)), Integer.parseInt(split.get(3)), split.get(4)));
			    			case "gameobject":
			    				break;
			    			case "gameobjectinstance":
			    				break;
			    			case "player":
			    				break;
			    			
			    		}
			    	}
			    	case NetworkString.PULL:
			    	{
			    		switch(split.get(1))
			    		{
			    			case NetworkString.GAME_INSTANCE:
			    			{
			    				GameInstance gi = getGameInstance(split.get(2));
			    				GameIO.saveSnapshotToZip(gi, output);
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
			    			case "gameobject":
			    			{
			    				GameInstance gi = getGameInstance(split.get(2));
			    				GameObject go = gi.game.getObject(split.get(3));
			    				//GameIO.saveGameObject(go, output);
			    				break;
			    			}
			    			case "gameobjectinstance":
			    			{
			    				GameInstance gi = getGameInstance(split.get(2));
			    				ObjectInstance oi = gi.getObjectInstance(Integer.parseInt(split.get(2)));
			    				//GameIO.saveGameObjectInstance(go, output);
			    				break;
			    			}
			    		}
			    	}
			    	case "join":
			    	{
			    		String player = split.get(2);
						int id = Integer.parseInt(split.get(3));
						GameInstance gi = getGameInstance(split.get(4));
			    		if (gi.password != null || gi.password.equals(split.get(5)))
			    		{
			    			gi.players.add(new Player(player, id));
			    		}
			    	}
			    	case NetworkString.CONNECT:
			    	{
			    		GameInstance gi = getGameInstance(split.get(1));
			    		AsynchronousGameConnection asc = new AsynchronousGameConnection(gi, input, output);
			    		asc.start();
			    	}
			    }
			} catch (IOException e) {
				logger.error("Networking error", e);
			}
			try { client.close(); } catch ( IOException e ) { }
		}	
    }
	
	public void run()
	{
		ServerSocket server;
		try {
			server = new ServerSocket( port );
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
		    	if (client != null)
	    		{
	    			client.close();
	    		}
		    }
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void start() throws IOException
    {
		 if (th == null)
		 {
			 th = new Thread(this);
			 th.start();
		 }
    }
}
