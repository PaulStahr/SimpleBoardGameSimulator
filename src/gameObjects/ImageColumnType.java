package gameObjects;

import java.awt.image.BufferedImage;
import java.util.Map.Entry;

import util.ArrayTools;
import util.ArrayTools.UnmodifiableArrayList;
import util.ArrayUtil;
import util.data.UniqueObjects;
import util.jframe.table.TableColumnType;
import util.jframe.table.ValueColumnTypes;

public enum ImageColumnType implements TableColumnType{
	ID("id", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	WIDTH("width", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	HEIGHT("height", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	DELETE("Delete", ValueColumnTypes.TYPE_BUTTON, "Delete", null);
	
    private static final ImageColumnType ct[] = ImageColumnType.values();
    private static final String[] columnNames = TableColumnType.getColumnNames(ct);
    
    public static final int size()
    {
    	return ct.length;
    }
    
    public static final ImageColumnType get(int index)
    {
    	return ct[index];
    }
    
	public final String name;
	public final Class<?> cl;
	public final byte optionType;
	public final Object defaultValue;
	public final UnmodifiableArrayList<String> possibleValues;
	
	private ImageColumnType(String name, byte optionType, Object defaultValue, String possibleValues[]) {
		this.name = name;
		this.optionType = optionType;
		this.cl = TableColumnType.getColumnClass(optionType);
		this.possibleValues = possibleValues == null || possibleValues.length == 0 ? UniqueObjects.EMPTY_STRING_LIST : ArrayTools.unmodifiableList(possibleValues);
		this.defaultValue = defaultValue;
	}
	
	public static ImageColumnType getByName(String name) {
		int index = ArrayUtil.firstEqualIndex(columnNames, name);
		return index < 0 ? null : ct[index];
	}

	@Override
	public byte getOptionType() {
		return optionType;
	}

	@Override
	public String[] getPossibleValues() {
		return possibleValues.toArray(new String[possibleValues.size()]);
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public Class<?> getCl() {
		return cl;
	}
	
	@Override
	public Object getValue(Object obj) {
		if (obj instanceof BufferedImage)
		{
			BufferedImage gi = (BufferedImage)obj;
			switch (this)
			{
				case DELETE:	return "Delete";
				case WIDTH:		return gi.getWidth();
				case HEIGHT:	return gi.getHeight();
				case ID:		return "id";
				default:throw new IllegalArgumentException(getName());
			}
		}
		else if (obj instanceof Entry<?, ?>)
		{
			@SuppressWarnings("unchecked")
			Entry<String, BufferedImage> entry = (Entry<String, BufferedImage>)obj;
			switch (this)
			{
				case DELETE:	return "Delete";
				case WIDTH:		return entry.getValue().getWidth();
				case HEIGHT:	return entry.getValue().getHeight();
				case ID:		return entry.getKey();
				default:throw new IllegalArgumentException(getName());
			}
		}
		else
		{
			throw new IllegalArgumentException();
		}
	}
};
