package net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import gameObjects.definition.GameObject;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import io.GameIO;
import main.DataHandler;
import main.Player;
import util.StringUtils;

public class GameServer {
	public final ArrayList<GameInstance> gameInstances = new ArrayList<>();
	
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
				String player = split.get(0);
				int id = Integer.parseInt(split.get(1));
				split.clear();
				
			    line = in.nextLine();
			    StringUtils.split(line, ' ', split);
			    switch (split.get(0))
			    {
			    	case "list":
			    	{
			    		switch (split.get(1))
			    		{
			    			case "gameinstances": 
			    			{
					    	    PrintWriter out = new PrintWriter( output, true );
					    		for (int i = 0; i < gameInstances.size(); ++i)
					    		{
					    			out.write(gameInstances.get(i).name);
					    		}
					    		out.close();
			    			}
			    			case "player":
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
			    	case "hash":
			    	{
			    		switch (split.get(1))
			    		{
			    			
			    		}
			    	}
			    	case "push":
			    	{
			    		switch (split.get(1))
			    		{
			    			case "gameinstance":
			    				break;
			    			case "gameobject":
			    				break;
			    			case "gameobjectinstance":
			    				break;
			    			case "player":
			    				break;
			    			
			    		}
			    	}
			    	case "pull":
			    	{
			    		switch(split.get(1))
			    		{
			    			case "gameinstance":
			    			{
			    				GameInstance gi = getGameInstance(split.get(2));
			    				GameIO.saveGame(gi, output);
			    				break;
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
			    		GameInstance gi = getGameInstance(split.get(1));
			    		if (gi.password != null || gi.password.equals(split.get(2)))
			    		{
			    			gi.players.add(new Player("player", id));
			    		}
			    	}
			    	case "connect":
			    	{
			    		GameInstance gi = getGameInstance(split.get(1));
			    		AsynchronousGameConnection asc = new AsynchronousGameConnection(gi, input, output);
			    		asc.start();
			    	}
			    }
			} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
		}	
	}

	public void startGameServer(int port) throws IOException
    {
	    ServerSocket server = new ServerSocket( port );
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
	    	finally {
	    		if ( client != null )
	    		{
	    			try { client.close(); } catch ( IOException e ) { }
	    		}
	      }
	  }
    }
}
