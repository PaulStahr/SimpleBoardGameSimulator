package gameObjects.instance;
import java.util.ArrayList;
import java.util.List;

import data.Texture;
import gameObjects.definition.GameObject;

public class Game {
	public String name;
	public Texture background;
	private ArrayList<GameObject> objects = new ArrayList<>();
	public final ArrayList<Texture> images = new ArrayList<>();
	public Game(Game other) {
        this.name = other.name;
        this.background = background == null ? null : new Texture(other.background);
        for (int i = 0; i < other.objects.size(); ++i)
        {
            objects.add(other.objects.get(i).copy());
        }
        for (Texture entry : other.images)
        {
            images.add(entry);
        }
    }

	public String[] getTextureNames() {
	    String result[] = new String[images.size()];
	    for (int i = 0; i < images.size(); ++i)
	    {
	        result[i] = images.get(i).getId();
	    }
	    return result;
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

	public GameObject getObjectByIndex(int index) {
		return objects.get(index);
	}

	public Texture getImage(String str) {
	    for (int i = 0; i < images.size(); ++i)
	    {
	        if (str.equals(images.get(i).getId()))
	        {
	            return images.get(i);
	        }
	    }
	    return null;
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
