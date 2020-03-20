package main;
import java.awt.Image;
import java.util.ArrayList;

import gameObjects.GameObject;

public class Game {
	public Image background;
	public ArrayList<GameObject> objects = new ArrayList<>();
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
