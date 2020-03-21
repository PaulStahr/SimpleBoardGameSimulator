package net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

import gameObjects.instance.GameInstance;
import main.DataHandler;

public class GameServer {
	public final ArrayList<GameInstance> gameInstances = new ArrayList<>();
	
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
			    PrintWriter out = new PrintWriter( client.getOutputStream(), true );

			    String command = in.nextLine();
			    switch (command)
			    {
			    	case "list games":
			    	{
			    		for (int i = 0; i < gameInstances.size(); ++i)
			    		{
			    			out.write(gameInstances.get(i).name);
			    		}
			    	}
			    	case "list players":
			    	{
			    		
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
