package gameObjects.instance;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import gameObjects.GameAction;
import main.Player;

public class GameInstance {
	public Game game;
	public String password;
	public String name;
	public boolean hidden = false;
	public final ArrayList<ObjectInstance> objects = new ArrayList<>();
	public final ArrayList<Player> players = new ArrayList<>();
	public final ArrayList<GameAction> actions = new ArrayList<>();
	
	public GameInstance(Game game)
	{
		this.game = game;
	}
	
	public int getHash()
	{//TODO
		return 0;
	}
}
