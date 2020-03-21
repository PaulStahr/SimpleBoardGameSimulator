package net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import gameObjects.instance.GameInstance;
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
				in = new Scanner( client.getInputStream() );
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
					    	    PrintWriter out = new PrintWriter( client.getOutputStream(), true );
					    		for (int i = 0; i < gameInstances.size(); ++i)
					    		{
					    			out.write(gameInstances.get(i).name);
					    		}
			    			}
			    			case "game":
			    			{
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
			    				GameIO.saveGame(gi, client.getOutputStream());
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
			    }
			} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
			}
		}	
	}

	public void main( String[] args ) throws IOException
    {
	    ServerSocket server = new ServerSocket( 3141 );
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
