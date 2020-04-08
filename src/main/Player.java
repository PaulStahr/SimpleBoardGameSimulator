package main;

import java.awt.*;
import java.util.Random;

public class Player {
	public String name;
	public final int id;
	public Color color;
	public int mouseXPos = 0;
	public int mouseYPos = 0;
	
	public Player(String name, int id)
	{
		this.name = name;
		this.id = id;
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

}
