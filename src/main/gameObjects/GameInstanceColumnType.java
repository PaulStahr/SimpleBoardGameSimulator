package main.gameObjects;

import main.util.ArrayTools;
import main.util.ArrayTools.UnmodifiableArrayList;
import main.util.ArrayUtil;
import main.util.data.UniqueObjects;
import main.util.jframe.table.TableColumnType;
import main.util.jframe.table.ValueColumnTypes;

public enum GameInstanceColumnType implements TableColumnType{
	ID("id", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	NAME("name", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	CONNECT("Connect", ValueColumnTypes.TYPE_BUTTON, "Connect", null),
	DELETE("Delete", ValueColumnTypes.TYPE_BUTTON, "Delete", null);
	
    private static final GameInstanceColumnType ct[] = GameInstanceColumnType.values();
    private static final String[] columnNames = new String[GameInstanceColumnType.ct.length];
    
    public static final int size()
    {
    	return ct.length;
    }
    
    public static final GameInstanceColumnType get(int index)
    {
    	return ct[index];
    }
    
	public final String name;
	public final Class<?> cl;
	public final byte optionType;
	public final Object defaultValue;
	public final UnmodifiableArrayList<String> possibleValues;
	
	private GameInstanceColumnType(String name, byte optionType, Object defaultValue, String possibleValues[]) {
		this.name = name;
		this.optionType = optionType;
		switch (optionType)
		{
			case ValueColumnTypes.TYPE_CHECKBOX:
				this.cl = Boolean.class;
				break;
			case ValueColumnTypes.TYPE_COLOR:
			case ValueColumnTypes.TYPE_TEXTFIELD:
			case ValueColumnTypes.TYPE_COMBOBOX:
			case ValueColumnTypes.TYPE_BUTTON:
				this.cl = String.class;
				break;
			default:
				throw new IllegalArgumentException();
		}
		this.possibleValues = possibleValues == null || possibleValues.length == 0 ? UniqueObjects.EMPTY_STRING_LIST : ArrayTools.unmodifiableList(possibleValues);
		this.defaultValue = defaultValue;
	}
	static {
		for (int i = 0; i < ct.length; ++i)
    	{
    		columnNames[i] = ct[i].name;
    	}
	}
	
	public static GameInstanceColumnType getByName(String name) {
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
};
