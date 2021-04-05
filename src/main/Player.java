package main;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Random;

import gameObjects.columnTypes.PlayerColumnType;
import gameObjects.instance.GameInstance;
import util.ArrayTools;
import util.jframe.table.TableColumnType;

public class Player implements Comparable<Object> {
	public static final List<TableColumnType> TYPES = ArrayTools.unmodifiableList(new TableColumnType[]{PlayerColumnType.ID, PlayerColumnType.NAME, PlayerColumnType.REPAIR, PlayerColumnType.DELETE});
    public int trickNum = 0;
    private String name;
	public final int id;
	public Color color;
	public int mouseXPos = 0;
	public int mouseYPos = 0;
	public int screenWidth = 0, screenHeight = 0;
	public final AffineTransform screenToBoardTransformation = new AffineTransform();
	public final AffineTransform playerAtTableTransform = new AffineTransform();
	public int playerAtTableRotation = 0;
	public int playerAtTablePosition = -1;

	public String actionString = "";
	private transient int nameModCount = 0;

	public boolean visitor = false;
    public volatile long lastReceivedSignal;
	
	public String getName()
	{
		return name;
	}
	
	public int getNameModCount()
	{
		return nameModCount;
	}
	
	public void setName(String name)
	{
		if (!this.name.equals(name))
		{
			++nameModCount;
		}
		this.name = name;
	}
	
	public Player(String name, int id)
	{
		this.name = name;
		this.id = id;
		setPlayerColor();
	}
	public Player(String name, int id, boolean visitor)
	{
		this.name = name;
		this.id = id;
		this.visitor = visitor;
		setPlayerColor();
	}
	
	public Player(String name, int id, Color color, int mouseX, int mouseY) {
		this.name = name;
		this.id = id;
		this.color = color;
		this.mouseXPos = mouseX;
		this.mouseYPos = mouseY;
		setPlayerColor();
	}

	public Player(Player other) {
        this.name = other.name;
        this.id = other.id;
        this.color = other.color;
        this.mouseXPos = other.mouseXPos;
        this.mouseYPos = other.mouseYPos;
        this.screenWidth = other.screenWidth;
        this.screenHeight = other.screenHeight;
        this.screenToBoardTransformation.setTransform(other.screenToBoardTransformation);
        this.playerAtTableTransform.setTransform(other.playerAtTableTransform);
        this.playerAtTableRotation = other.playerAtTableRotation;
        this.playerAtTablePosition = other.playerAtTablePosition;
    }

    @Override
	public String toString() {
		return "(" + name + " " + id + ")";
	}



	public void setPlayerColor(GameInstance gameInstance){
		int posPlayer = -1;
		if (gameInstance != null) {
			++posPlayer;
			for (int i = 0; i < gameInstance.getPlayerNumber(); ++i) {
				if (gameInstance.getPlayerByIndex(i).id < this.id) {
					++posPlayer;
				}
			}
		}
		if (posPlayer != -1 && posPlayer < gameInstance.seatColors.size())
		{
			this.color = gameInstance.seatColors.get(posPlayer);
		}
		else {
            this.color = new Color(new Random().nextInt() & 0xFFFFFF);
        }
	}

	public void setPlayerColor(){
		if (this.color == null) {
            this.color = new Color(new Random().nextInt() & 0xFFFFFF);
		}
	}

	public void setMousePos(int posX, int posY){
		this.mouseXPos = posX;
		this.mouseYPos = posY;
	}

	public void set(Player player) {
		this.name = player.name;
		this.color = player.color;
		this.mouseXPos = player.mouseXPos;
		this.mouseYPos = player.mouseYPos;
	}

	@Override
	public int compareTo(Object o) {
		int compareId = ((Player)o).id;
		return this.id - compareId;
	}

	public String toStringAdvanced()
	{
	    return name + " " + id + " " + color + " " + mouseXPos + " " + mouseYPos + " " + screenToBoardTransformation + " " + playerAtTableRotation + " " + playerAtTablePosition;
	}

	@Override
	public boolean equals(Object oth)
	{
	    if (oth == this) {return true;}
	    if (oth instanceof Player)
	    {
	        Player other = (Player)oth;
	        return name.equals(other.name)
	                && id== other.id
	                && color.equals(other.color)
	                && mouseXPos == other.mouseXPos
	                && mouseYPos == other.mouseYPos
	                && screenToBoardTransformation.equals(other.screenToBoardTransformation)
	                && playerAtTableRotation == other.playerAtTableRotation
	                && playerAtTablePosition == other.playerAtTablePosition;
	    }
	    return false;
	}

    public Player copy() {return new Player(this);}
}
