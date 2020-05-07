package gui.minigames;

import java.util.ArrayList;
import java.util.Arrays;

import util.data.IntegerArrayList;

public class TetrisGameInstance {
	public final ArrayList<TetrisGameListener> gameListener = new ArrayList<>();
	private final IntegerArrayList ial = new IntegerArrayList();
	
	public static interface TetrisGameListener{
		public void actionPerformed(TetrisGameEvent event);
	};
	
	public static class TetrisGameEvent
	{
		public TetrisGameEvent(){}
	}
	
	public static class TetrisGameResetEvent extends TetrisGameEvent{}
	
	public static class TetrisGameChangePixelEvent extends TetrisGameEvent{}

	public static class TetrisGameRemoveRowsEvent extends TetrisGameEvent{
		public final int rows[];
		
		public TetrisGameRemoveRowsEvent(int rows[])
		{
			this.rows = rows;
		}
	}
	
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
			new TetrisObjectType(2,3, 2, false, true, true, true, false, true),
			new TetrisObjectType(3,2, 3, true, true, true, false, false, true),
			new TetrisObjectType(2,3, 3, true, true, true, false, true, false),
			new TetrisObjectType(3,2, 3, true, false, false, true, true, true),
			new TetrisObjectType(2,3, 3, false, true, false, true, true, true),
			new TetrisObjectType(3,2, 4, true, true, true, true, false, false),	
			new TetrisObjectType(2,3, 4, true, true, false, true, false, true),	
			new TetrisObjectType(3,2, 4, false, false, true, true, true, true),	
			new TetrisObjectType(2,3, 4, true, false, true, false, true, true),	
			new TetrisObjectType(3,2, 5, true, true, false, false, true, true),
			new TetrisObjectType(2,3, 5, false, true, true, true, true, false),
			new TetrisObjectType(3,2, 6, false, true, true, true, true, false),
			new TetrisObjectType(2,3, 6, true, false, true, true, false, true),
	};
	
	static {
		int first = 0;;
		for (int i = 0; i < objectTypes.length; ++i)
		{
			if (i+1 < objectTypes.length && objectTypes[i+1].id == objectTypes[i].id)
			{
				objectTypes[i].next = (byte)(i+1);
			}
			else
			{
				objectTypes[i].next = (byte)first;
				first = i+1;
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
		byte pxvalue = (byte)(multiply * (objectTypes[fo.type].id + 1));
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
	
	public boolean row_filled(int row)
	{
		int offset = row * cols;
		for (int y = offset; y < offset + cols; ++y)
		{
			if (gameWindow[y] <= 0)
			{
				return false;
			}
		}
		return true;
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
				setPixels(fo, (byte)0);
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
				--fo.y; 
				setPixels(fo, (byte)-1);
			}
		}
		if (toRemove.size() != 0)
		{
			for (int y = 0; y < rows; ++y)
			{
				if (row_filled(y))
				{
					ial.add(y);
				}
			}
		}
		if (ial.size() != 0)
		{
			actionPerformed(new TetrisGameRemoveRowsEvent(ial.toArrayI()));
			ial.clear();
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

	public byte add(FallingObject fo) {
		byte placable = isPlacable(fo);
		if (placable == 0)
		{
			setPixels(fo, (byte)1);
			fallingObject.add(fo);
		}
		return placable;		
	}
	
	public void actionPerformed(TetrisGameEvent event)
	{
		if (event instanceof TetrisGameResetEvent)
		{
			placedObjects = 0;
			fallingObject.clear();
			Arrays.fill(gameWindow, (byte)0);			
		}
		else if (event instanceof TetrisGameRemoveRowsEvent)
		{
			int deletedRows[] = ((TetrisGameRemoveRowsEvent) event).rows;
			int outy = 0;
			for (int y = 0; y < rows; ++y)
			{
				if (0 > Arrays.binarySearch(deletedRows, y))
				{
					System.arraycopy(gameWindow, y * cols, gameWindow, outy * cols, cols);
					++outy;
				}
			}
			Arrays.fill(gameWindow, outy * cols, gameWindow.length, (byte)0);
		}
		for (int i = 0; i < gameListener.size(); ++i)
		{
			gameListener.get(i).actionPerformed(event);
		}
	}
}
