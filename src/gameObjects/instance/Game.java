package gameObjects.instance;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import data.Texture;
import gameObjects.definition.GameObject;

public class Game {
	public String name;
	public Texture background;
	private ArrayList<GameObject> objects = new ArrayList<>();
	public final HashMap<String, Texture> images = new HashMap<>();
	public Game(Game other) {
        this.name = other.name;
        this.background = background == null ? null : new Texture(other.background);
        for (int i = 0; i < other.objects.size(); ++i)
        {
            objects.add(other.objects.get(i).copy());
        }
        for (Entry<String, Texture> entry : other.images.entrySet())
        {
            images.put(entry.getKey(), entry.getValue().copy());
        }
    }
	
	public Game copy() {return new Game(this);}

    public Game(String name) {this.name = name;}

    public Game() {}

    public GameObject getObject(String uniqueObjectName) {
		for (int i = 0; i < objects.size(); ++i)
		{
			if (objects.get(i).uniqueObjectName.equals(uniqueObjectName))
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
     * @throws IOException 
     */
    public String getImageKey(Texture image)
    {
        for (Map.Entry<String, Texture> mapEntry : images.entrySet())
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
		result += name.hashCode();
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

	public Map.Entry<String, Texture> getImage(int hash) {
		for (Map.Entry<String, Texture> mapEntry : images.entrySet())
		{
			if(mapEntry.getKey().hashCode() == hash)
			{
				return mapEntry;
			}
		}
		return null;
	}

	public GameObject getObjectByIndex(int index) {
		return objects.get(index);
	}

	public Texture getImage(String str) {
		return images.get(str);
	}

    public List<GameObject> getObjects() {return objects;}

    public int getGameObjectCount() {return objects.size();}

    public void removeObject(GameObject object) {objects.remove(object);}

    public GameObject addObject(GameObject obj) {
        GameObject res = getObejctByUniqueName(obj.uniqueObjectName);
        if (res != null) {
            res.set(obj);
        }else {
            objects.add(res = obj);
        }
        return res;
    }

    public GameObject getObejctByUniqueName(String uniqueObjectName) {
        for (int i = 0; i < objects.size(); ++i)
        {
            GameObject current = objects.get(i);
            if (uniqueObjectName.equals(current.uniqueObjectName))
            {
                return current;
            }
        }
        return null;
    }

    public GameObject getGameObjectByIndex(int i) {
        return objects.get(i);
    }
}
