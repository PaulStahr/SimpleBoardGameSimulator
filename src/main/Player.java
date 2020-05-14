package main;

import java.awt.Color;
import java.awt.geom.AffineTransform;
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
	public int screenWidth = 0, screenHeight = 0;
	public final AffineTransform screenToBoardTransformation = new AffineTransform();
	public final AffineTransform playerAtTableTransform = new AffineTransform();
	public int playerAtTableRotation = 0;

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
	}
	
	public Player(String name, int id, Color color, int mouseX, int mouseY) {
		this.name = name;
		this.id = id;
		this.color = color;
		this.mouseXPos = mouseX;
		this.mouseYPos = mouseY;
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
