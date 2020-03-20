package main;

import java.util.ArrayList;

public class GameInstance {
	public Game game;
	public ArrayList<ObjectInstance> objects = new ArrayList<>();
	
	public GameInstance(Game game)
	{
		this.game = game;
	}
}
