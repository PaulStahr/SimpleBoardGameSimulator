package main;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;

import gameObjects.GameObject;

public class Game {
	public Image background;
	public ArrayList<GameObject> objects = new ArrayList<>();
	public final HashMap<String, BufferedImage> images = new HashMap<>();
	public GameObject getObject(String id) {
		for (int i = 0; i < objects.size(); ++i)
		{
			if (objects.get(i).id.equals(id))
			{
				return objects.get(i);
			}
		}
		return null;
	}
}
