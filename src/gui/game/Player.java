package gui.game;

import java.awt.Color;
import java.awt.geom.AffineTransform;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

import gameObjects.columnTypes.PlayerColumnType;
import gameObjects.instance.GameInstance;
import util.ArrayTools;
import util.jframe.table.TableColumnType;

public class Player implements Comparable<Object> {
	public static final List<TableColumnType> TYPES = ArrayTools.unmodifiableList(new TableColumnType[]{PlayerColumnType.ID, PlayerColumnType.NAME, PlayerColumnType.COLOR, PlayerColumnType.REPAIR, PlayerColumnType.DELETE});
    public int trickNum = 0;
    private String name;
	public final int id;
	public Color color;
	public int seatNum = -1;
	public int mouseXPos = 0;
	public int mouseYPos = 0;
	public int screenWidth = 0, screenHeight = 0;
	public final AffineTransform screenToBoardTransformation = new AffineTransform();
	public final AffineTransform playerAtTableTransform = new AffineTransform();

	public String actionString = "";
	private transient int nameModCount = 0;

	public boolean visitor = false;
    public volatile long lastReceivedSignal;
	
    public final Predicate<Player> sameSeatPredicate = new Predicate<Player>() {
        @Override
        public boolean test(Player arg0) {return arg0.seatNum == Player.this.seatNum;}
    };

    public final Predicate<Player> sameIdPredicate = new Predicate<Player>() {
        @Override
        public boolean test(Player arg0) {return arg0.id == Player.this.id;}
    };

    public final Predicate<Player> sameNamePredicate = new Predicate<Player>() {
        @Override
        public boolean test(Player arg0) {return arg0.name.equals(Player.this);}
    };

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
		setPlayerColor(null);
	}
	public Player(String name, int id, boolean visitor)
	{
		this.name = name;
		this.id = id;
		this.visitor = visitor;
		setPlayerColor(null);
	}
	
	public Player(String name, int id, Color color, int seatNum, int trickNum, int mouseX, int mouseY) {
		this.name = name;
		this.id = id;
		this.color = color;
		this.seatNum = seatNum;
		this.trickNum = trickNum;
		this.mouseXPos = mouseX;
		this.mouseYPos = mouseY;
	}

	public Player(Player other) {
        this.name = other.name;
        this.id = other.id;
        this.color = other.color;
        this.seatNum = other.seatNum;
        this.trickNum = other.trickNum;
        this.mouseXPos = other.mouseXPos;
        this.mouseYPos = other.mouseYPos;
        this.screenWidth = other.screenWidth;
        this.screenHeight = other.screenHeight;
        this.screenToBoardTransformation.setTransform(other.screenToBoardTransformation);
        this.playerAtTableTransform.setTransform(other.playerAtTableTransform);
    }

    @Override
	public String toString() {
		return "(" + name + " " + id + ")";
	}

	public void setPlayerColor(Color color){
        if (color == null) {
            color = new Color(new Random().nextInt() & 0xFFFFFF);
        }
		this.color = color;
	}

	public void setMousePos(int posX, int posY){
		this.mouseXPos = posX;
		this.mouseYPos = posY;
	}

	public void set(Player player) {
		this.name = player.name;
		this.color = player.color;
		this.seatNum = player.seatNum;
		this.trickNum = player.trickNum;
		this.mouseXPos = player.mouseXPos;
		this.mouseYPos = player.mouseYPos;
	}

	public void beginPlay(GamePanel gamePanel, GameInstance gameInstance){
		if (gamePanel != null && gameInstance != null){
			if (this.seatNum == -1 && !this.visitor){
				this.seatNum = gameInstance.getPlayerList().indexOf(this);
			}
			gamePanel.sitDown(this, this.seatNum, false);
		}
	}

	@Override
	public int compareTo(Object o) {
		int compareId = ((Player)o).id;
		return this.id - compareId;
	}

	public String toStringAdvanced()
	{
	    return name + " " + id + " " + color + " " + seatNum + " " + trickNum + " " + mouseXPos + " " + mouseYPos + " " + screenToBoardTransformation;
	}

	@Override
	public boolean equals(Object oth)
	{
	    if (oth == this) {return true;}
	    if (!(oth instanceof Player)){return false;}
        Player other = (Player)oth;
        return name.equals(other.name)
                && id == other.id
                && color.equals(other.color)
				&& seatNum == other.seatNum
				&& trickNum == other.trickNum
                && mouseXPos == other.mouseXPos
                && mouseYPos == other.mouseYPos
                && screenToBoardTransformation.equals(other.screenToBoardTransformation);
	}

    public Player copy() {return new Player(this);}
}
