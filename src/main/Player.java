package main;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.Random;

import gameObjects.PlayerColumnType;
import util.ArrayTools;
import util.jframe.table.TableColumnType;

public class Player {
	public static final List<TableColumnType> TYPES = ArrayTools.unmodifiableList(new TableColumnType[]{PlayerColumnType.ID, PlayerColumnType.NAME, PlayerColumnType.DELETE});
	private String name;
	public final int id;
	public Color color;
	public int mouseXPos = 0;
	public int mouseYPos = 0;

	public Point2D[] screenToBoardPos = new Point2D[4];

	public String actionString = "";
	private transient int nameModCount = 0;
	
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
		initPlayerStartPosition();
	}
	
	public Player(String name, int id, Color color, int mouseX, int mouseY, double bl_x, double bl_y, double br_x, double br_y, double tr_x, double tr_y, double tl_x, double tl_y) {
		this.name = name;
		this.id = id;
		this.color = color;
		this.mouseXPos = mouseX;
		this.mouseYPos = mouseY;
		this.screenToBoardPos[0].setLocation(bl_x, bl_y);
		this.screenToBoardPos[0].setLocation(br_x, br_y);
		this.screenToBoardPos[0].setLocation(tr_x, tr_y);
		this.screenToBoardPos[0].setLocation(tl_x, tl_y);
		setPlayerColor();	
	}

	@Override
	public String toString() {
		return "(" + name + " " + id + ")";
	}

	public void setPlayerColor(){
		Random rand = new Random();
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();

		this.color = new Color(r, g, b);
	}

	public void initPlayerStartPosition(){
		Point2D bl = new Point2D.Double(0,0);
		Point2D br = new Point2D.Double(0,0);
		Point2D tr = new Point2D.Double(0,0);
		Point2D tl = new Point2D.Double(0,0);
		screenToBoardPos[0] = bl;
		screenToBoardPos[1] = br;
		screenToBoardPos[2] = tr;
		screenToBoardPos[3] = tl;
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
		System.out.println(this.mouseXPos);
	}

}
