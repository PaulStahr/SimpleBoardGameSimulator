package main.gameObjects;

import java.awt.Color;
import java.util.Random;

public class Player {
	public String name;
	public final int id;
	public Color color;
	public int mouseXPos = 0;
	public int mouseYPos = 0;
	public String actionString = "";
	
	public Player(String name, int id)
	{
		this.name = name;
		this.id = id;
		this.color = getRandomColor();
	}

	public Player(String name, int id, int mouseXPos, int mouseYPos)
	{
		this.name = name;
		this.id = id;
		this.mouseXPos = mouseXPos;
		this.mouseYPos = mouseYPos;
		this.color = getRandomColor();
	}
	
	public Player(String name, int id, int mouseX, int mouseY, Color color)
	{
		this.name = name;
		this.id = id;
		this.mouseXPos = mouseX;
		this.mouseYPos = mouseY;
		this.color = color;
	}

	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("(").append(name).append(" ").append(id).append(")");
		return sb.toString();
	}

	public void setMousePos(int posX, int posY){
		this.mouseXPos = posX;
		this.mouseYPos = posY;
	}

	public void overwriteWith(Player player) {
		this.name = player.name;
		this.color = player.color;
		this.mouseXPos = player.mouseXPos;
		this.mouseYPos = player.mouseYPos;
	}

	public Color getRandomColor(){
		Random rand = new Random();
		float r = rand.nextFloat();
		float g = rand.nextFloat();
		float b = rand.nextFloat();
		return new Color(r, g, b);
	}
}
