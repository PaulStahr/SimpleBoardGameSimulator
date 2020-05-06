package gui.minigames;

import java.util.ArrayList;

public class TetrisGameInstance {
	private byte gameWindow[];
	private int rows;
	private int cols;
	public final ArrayList<FallingObject> fallingObject = new ArrayList<>();
	
	public static class FallingObject{		
		byte type;
		int x;
		int y;
		
		public FallingObject(byte type, int x, int y)
		{
			this.x = x;
			this.y = y;
			this.type = type;
		}
	}
	
	private static class TetrisObjectType
	{
		public final int width;
		public final int height;
		public final boolean stencil[];
		public byte next;
		public final int id;
		
		public TetrisObjectType(int width, int height, int id, boolean ...stencil)
		{
			this.width = width;
			this.height = height;
			this.stencil = stencil;
			this.id = id;
		}

		public boolean get(int x, int y) {
			return stencil[x + y * width];
		}
	}
	
	private static final TetrisObjectType objectTypes[] = new TetrisObjectType[] {
			new TetrisObjectType(1, 4, 0, true, true, true, true),
			new TetrisObjectType(4, 1, 0, true, true, true, true),
			new TetrisObjectType(2, 2, 1, true, true, true, true),
			new TetrisObjectType(3,2, 2, false, true, false, true, true, true),
			new TetrisObjectType(2,3, 2, true, false, true, true, true, false),
			new TetrisObjectType(3,2, 2, true, true, true, false, true, false),
			new TetrisObjectType(2,3, 2, true, true, true, false, true, false),
			new TetrisObjectType(3,2, 3, true, true, true, false, false, true),
			new TetrisObjectType(3,2, 4, true, true, true, true, false, false),	
	};
	
	static {
		int first = 0;;
		for (int i = 1; i <= objectTypes.length; ++i)
		{
			if (i != objectTypes.length && objectTypes[i].id == objectTypes[i - 1].id)
			{
				objectTypes[i-1].next = (byte)i;
			}
			else
			{
				objectTypes[i - 1].next = (byte)first;
				first = i;
			}
		}
			
	}
	
	public byte getPixel(int x, int y)
	{
		return gameWindow[x + y * cols];
	}
	
	public void setPixel(int x, int y, byte value)
	{
		gameWindow[x + y * cols] = value;
	}
	
	public TetrisGameInstance()
	{
		rows = 20;
		cols = 10;
		gameWindow = new byte[rows * cols];
	}
	
	private final ArrayList<FallingObject> toRemove = new ArrayList<>();
	private int placedObjects;
	
	private void setPixels(FallingObject fo, byte multiply)
	{
		TetrisObjectType type = objectTypes[fo.type];
		int fox = fo.x;
		int foy = fo.y;
		byte pxvalue = (byte)(multiply * (fo.type + 1));
		for (int y = 0; y < type.height; ++y)
		{
			for (int x = 0; x < type.width; ++x)
			{
				if (type.get(x, y) && foy + y < rows)
				{
					setPixel(fox + x, foy + y, pxvalue);
				}
			}
		}
	}
	
	public byte isPlacable(FallingObject fo)
	{
		TetrisObjectType type = objectTypes[fo.type];
		int fox = fo.x;
		int foy = fo.y;
		byte ret = 0;
		for (int y = 0; y < type.height; ++y)
		{
			for (int x = 0; x < type.width; ++x)
			{
				if (type.get(x, y) && foy + y < rows)
				{
					byte pixel = getPixel(fox + x, foy + y);
					if (pixel > 0)
					{
						return 1;
					}
					else if (pixel < 0)
					{
						ret = -1;
					}
						
				}
			}
		}
		return ret;
	}
	
	public void logic_step()
	{
		for (int i = 0; i < fallingObject.size(); ++i)
		{
			FallingObject fo = fallingObject.get(i);
			int fox = fo.x;
			int foy = fo.y;
			TetrisObjectType type = objectTypes[fo.type];
			falling:
			{
				for (int y = 0; y < type.height; ++y)
				{
					for (int x = 0; x < type.width; ++x)
					{
						if (type.get(x, y) && foy + y < rows && (foy + y == 0 || 0 < getPixel(fox + x, foy + y - 1)))
						{
							toRemove.add(fo);
							setPixels(fo, (byte)1);
							++placedObjects;
							break falling;
						}
					}
				}
				setPixels(fo, (byte)0);
				--fo.y; 
				setPixels(fo, (byte)-1);
			}
		}
		fallingObject.removeAll(toRemove);
		toRemove.clear();
	}

	public int getRows() {
		return rows;
	}
	
	public int getCols() {
		return cols;
	}

	public int placedObjectCount() {
		return placedObjects;
	}

	public void moveRight(int i) {
		FallingObject fo = fallingObject.get(i);
		if (fo.x < cols - 1)
		{
			setPixels(fo, (byte)0);
			++fo.x;
			if (isPlacable(fo) != 0)
			{
				--fo.x;
			}
			setPixels(fo, (byte)-1);
		}
	}

	public void moveLeft(int i) {
		FallingObject fo = fallingObject.get(i);
		if (fo.x > 0)
		{
			setPixels(fo, (byte)0);
			--fo.x;
			if (isPlacable(fo) != 0)
			{
				++fo.x;
			}
			setPixels(fo, (byte)-1);		
		}
	}
	
	public void rotate(int index) {
		FallingObject fo = fallingObject.get(index);
		setPixels(fo, (byte)0);
		for (int i = 0; i < 4; ++i)
		{
			fo.type = objectTypes[fo.type].next;
			if (isPlacable(fo) == 0)
			{
				setPixels(fo, (byte)-1);
				return;
			}
		}
	}
}
