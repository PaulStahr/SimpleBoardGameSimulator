package gameObjects;

import gameObjects.instance.ObjectInstance;
import util.ArrayTools;
import util.ArrayTools.UnmodifiableArrayList;
import util.ArrayUtil;
import util.data.UniqueObjects;
import util.jframe.table.TableColumnType;
import util.jframe.table.ValueColumnTypes;

public enum GameObjectInstanceColumnType implements TableColumnType{
	ID("id", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	NAME("name", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	OWNER("owner", ValueColumnTypes.TYPE_COMBOBOX, "Unnamed", null),
	ABOVE("above", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	BELOW("below", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	RESET("Reset", ValueColumnTypes.TYPE_TEXTFIELD, "Delete", null),
	DELETE("Delete", ValueColumnTypes.TYPE_BUTTON, "Delete", null);
	
    private static final GameObjectInstanceColumnType ct[] = GameObjectInstanceColumnType.values();
    private static final String[] columnNames = TableColumnType.getColumnNames(ct);
    
    public static final int size()
    {
    	return ct.length;
    }
    
    public static final GameObjectInstanceColumnType get(int index)
    {
    	return ct[index];
    }
    
	public final String name;
	public final Class<?> cl;
	public final byte optionType;
	public final Object defaultValue;
	public final UnmodifiableArrayList<String> possibleValues;
	
	private GameObjectInstanceColumnType(String name, byte optionType, Object defaultValue, String possibleValues[]) {
		this.name = name;
		this.optionType = optionType;
		this.cl = TableColumnType.getColumnClass(optionType);
		this.possibleValues = possibleValues == null || possibleValues.length == 0 ? UniqueObjects.EMPTY_STRING_LIST : ArrayTools.unmodifiableList(possibleValues);
		this.defaultValue = defaultValue;
	}
	
	public static GameObjectInstanceColumnType getByName(String name) {
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
		ObjectInstance gi = (ObjectInstance)obj;
		switch (this)
		{
			case DELETE:	return "Delete";
			case ID:		return gi.id;
			case NAME:		return gi.go.uniqueObjectName;
			case ABOVE:		return gi.state.aboveInstanceId;
			case BELOW:		return gi.state.belowInstanceId;
			case OWNER:		return gi.owner_id();
			case RESET:		return "Reset";
			default:throw new IllegalArgumentException(getName());
		}
	}
};
