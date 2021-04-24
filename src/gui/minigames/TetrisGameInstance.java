package gui.minigames;

import java.util.ArrayList;
import java.util.Arrays;

import gameObjects.action.GameAction;
import gameObjects.instance.GameInstance.GameChangeListener;
import util.ArrayUtil;
import util.data.ByteArrayList;
import util.data.IntegerArrayList;

public class TetrisGameInstance implements GameChangeListener {
	public final ArrayList<TetrisGameListener> gameListener = new ArrayList<>();
	private final IntegerArrayList removeRowList = new IntegerArrayList();
	public final int source = (int)System.nanoTime() * Integer.MAX_VALUE;
	
	public static interface TetrisGameListener{
		public void actionPerformed(TetrisGameEvent event);
	};
	
	public static class TetrisGameEvent extends GameAction
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -1533769225129432885L;

		public TetrisGameEvent(int source){
			super(source);
		}
	}
	
	public static class TetrisGameResetEvent extends TetrisGameEvent{

		public TetrisGameResetEvent(int source) {
			super(source);
		}

		/**
		 * 
		 */
		private static final long serialVersionUID = 329308199229617837L;}
	
	public static class TetrisGameStateEvent extends TetrisGameEvent{
		/**
		 * 
		 */
		private static final long serialVersionUID = -5170488736137761342L;
		private final byte values[];
		public TetrisGameStateEvent(int source, byte values[])
		{
			super(source);
			this.values = values.clone();
		}
	}
	
	public static class TetrisRequestStateEvent extends TetrisGameEvent{

		public TetrisRequestStateEvent(int source) {super(source);}

		/**
		 * 
		 */
		private static final long serialVersionUID = 4660642893285105594L;}
	
	public static class TetrisGameChangePixelEvent extends TetrisGameEvent{
		/**
		 * 
		 */
		private static final long serialVersionUID = -8420972247190408813L;
		private final int indices[];
		private final byte values[];
		public TetrisGameChangePixelEvent(int source, int indices[], byte values[])
		{
			super(source);
			this.indices = indices;
			this.values = values;
		}
	}

	public static class TetrisGameRemoveRowsEvent extends TetrisGameEvent{
		/**
		 * 
		 */
		private static final long serialVersionUID = -3616384518013910773L;
		public final int rows[];
		
		public TetrisGameRemoveRowsEvent(int source, int rows[])
		{
			super(source);
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

		private FallingObject(byte type, int x, int y)
		{
            this.type = type;
			this.x = x;
			this.y = y;
		}

        public FallingObject(byte type) {this(type, Integer.MIN_VALUE, Integer.MIN_VALUE);}

		public int getX() {return x;}

		public int getY() {return y;}

        public boolean isPlaced() {return x != Integer.MIN_VALUE && y != Integer.MIN_VALUE;}
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
		actionPerformed(new TetrisRequestStateEvent(source));
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
	
	private void setPixels(FallingObject fo, byte multiply, IntegerArrayList indices, ByteArrayList values)
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
					indices.add(fox + x + (foy + y) * cols);
					values.add(pxvalue);
				}
			}
		}
	}

	public byte isPlacable(FallingObject fo, int fox, int foy, byte tp)
	{
		if (foy < 0){return 1;}
		TetrisObjectType type = objectTypes[tp];
        if (fo.isPlaced()) {setPixels(fo, (byte)0);}
		byte res = 0;
		for (int y = 0; y < type.height; ++y)
		{
			for (int x = 0; x < type.width; ++x)
			{
				if (type.get(x, y) && foy + y < rows)
				{
					byte pixel = getPixel(fox + x, foy + y);
					if (pixel > 0){return 1;}
					if (pixel < 0){res = -1;}
				}
			}
		}
		if (fo.isPlaced()) {setPixels(fo, (byte)-1);}
		return res;
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

	private final IntegerArrayList indices = new IntegerArrayList();
	private final ByteArrayList values = new ByteArrayList();

	public void logic_step()
	{
		for (int i = 0; i < fallingObject.size(); ++i)
		{
			FallingObject fo = fallingObject.get(i);
			int placeable = isPlacable(fo, fo.x, fo.y - 1, fo.type);
			if (placeable > 0)
			{
				toRemove.add(fo);
				++placedObjects;
				setPixels(fo, (byte)1, indices, values);
				
			}
			else if (placeable == 0)
			{
				setPixels(fo, (byte)0, indices, values);
				--fo.y; 
				setPixels(fo, (byte)-1, indices, values);
			}
		}
		actionPerformed(new TetrisGameChangePixelEvent(source, indices.toArrayI(), values.toArrayB()));
		indices.clear();
		values.clear();
		if (toRemove.size() != 0)
		{
			for (int y = 0; y < rows; ++y)
			{
				if (row_filled(y))
				{
					removeRowList.add(y);
				}
			}
		}
		if (removeRowList.size() != 0)
		{
			actionPerformed(new TetrisGameRemoveRowsEvent(source, removeRowList.toArrayI()));
			removeRowList.clear();
		}
		fallingObject.removeAll(toRemove);
		toRemove.clear();
	}

	public int getRows() {return rows;}

	public int getCols() {return cols;}

	public int placedObjectCount() {return placedObjects;}

	public void moveRight(int i) {
		FallingObject fo = fallingObject.get(i);
		if (fo.x < cols - 1)
		{
			if (isPlacable(fo, fo.x + 1, fo.y, fo.type) == 0)
			{
				setPixels(fo, (byte)0, indices, values);
				++fo.x;
				setPixels(fo, (byte)-1, indices, values);
				actionPerformed(new TetrisGameChangePixelEvent(source, indices.toArrayI(), values.toArrayB()));				
				indices.clear();
				values.clear();
			}
		}
	}

	public void moveLeft(int i) {
		FallingObject fo = fallingObject.get(i);
		if (fo.x > 0)
		{
			if (isPlacable(fo, fo.x - 1, fo.y, fo.type) == 0)
			{
				setPixels(fo, (byte)0, indices, values);
				--fo.x;
				setPixels(fo, (byte)-1, indices, values);
				actionPerformed(new TetrisGameChangePixelEvent(source, indices.toArrayI(), values.toArrayB()));
				indices.clear();
				values.clear();
			}	
		}
	}
	
	public void rotate(int index) {
		FallingObject fo = fallingObject.get(index);
		setPixels(fo, (byte)0);
		byte type = fo.type;
		for (int i = 0; i < 4; ++i)
		{
			type = objectTypes[type].next;
			if (isPlacable(fo, fo.x, fo.y, type) == 0)
			{
				setPixels(fo, (byte)0, indices, values);
				fo.type = type;
				setPixels(fo, (byte)-1, indices, values);
				actionPerformed(new TetrisGameChangePixelEvent(source, indices.toArrayI(), values.toArrayB()));
				indices.clear();
				values.clear();
				break;
			}	
		}
	}

	public byte add(FallingObject fo, int x, int y) {
		byte placable = isPlacable(fo, x, y, fo.type);
		if (placable == 0)
		{
		    fo.x = x;
		    fo.y = y;
			setPixels(fo, (byte)-1);
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
		else if (event instanceof TetrisGameStateEvent)
		{
			System.arraycopy(((TetrisGameStateEvent) event).values, 0, gameWindow, 0, gameWindow.length);
		}
		else if (event instanceof TetrisRequestStateEvent)
		{
			actionPerformed(new TetrisGameStateEvent(source, gameWindow));
		}
		else if (event instanceof TetrisGameChangePixelEvent)
		{
			TetrisGameChangePixelEvent pixelEvent = (TetrisGameChangePixelEvent)event;
			ArrayUtil.setLementsAt(gameWindow, pixelEvent.indices, pixelEvent.values);
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

	public void addGameListener(TetrisGameListener tetrisGameListener) {
		gameListener.add(tetrisGameListener);
	}

	@Override
	public void changeUpdate(GameAction action) {
		if (action instanceof TetrisGameEvent && action.source != source)
		{
			actionPerformed((TetrisGameEvent)action);
		}
	}

	public void reset() {actionPerformed(new TetrisGameResetEvent(source));    }
}
