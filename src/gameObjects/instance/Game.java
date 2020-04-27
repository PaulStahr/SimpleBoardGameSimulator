package gameObjects.instance;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import gameObjects.definition.GameObject;

public class Game {
	public String name;
	public Image background;
	public ArrayList<GameObject> objects = new ArrayList<>();
	public final HashMap<String, BufferedImage> images = new HashMap<>();
	public GameObject getObject(String uniqueName) {
		for (int i = 0; i < objects.size(); ++i)
		{
			if (objects.get(i).uniqueName.equals(uniqueName))
			{
				return objects.get(i);
			}
		}
		return null;
	}

	/**
	 * Gets the initial image name from the images HashMap.
	 * The names function as keys in this array.
	 * @param image image for which the name is needed
	 * @return name of the image or null if image not found
	 */
	public String getImageKey(Image image)
	{
		for (Map.Entry<String, BufferedImage> mapEntry : images.entrySet())
		{
			if(mapEntry.getValue() == image)
			{
				return mapEntry.getKey();
			}
		}
		return null;
	}
	
	@Override
	public int hashCode()
	{
		int result = 0;
		for (int i = 0; i < objects.size(); ++i)
		{
			result += objects.get(i).hashCode();
		}
		return result;
	}
	
	public void clear()
	{
		images.clear();
		objects.clear();
	}

	public String[] getImageKeys() {
		return images.keySet().toArray(new String[images.size()]);
	}
}
