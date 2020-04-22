package gameObjects;

import gameObjects.instance.GameInstance;
import util.ArrayTools;
import util.ArrayTools.UnmodifiableArrayList;
import util.ArrayUtil;
import util.data.UniqueObjects;
import util.jframe.table.TableColumnType;
import util.jframe.table.ValueColumnTypes;

public enum GameInstanceColumnType implements TableColumnType{
	ID("id", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	NAME("name", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	CONNECT("Connect", ValueColumnTypes.TYPE_BUTTON, "Connect", null),
	DELETE("Delete", ValueColumnTypes.TYPE_BUTTON, "Delete", null), 
	NUM_PLAYERS("Players", ValueColumnTypes.TYPE_TEXTFIELD, "0", null);
	
    private static final GameInstanceColumnType ct[] = GameInstanceColumnType.values();
    private static final String[] columnNames = TableColumnType.getColumnNames(ct);
    
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
		this.cl = TableColumnType.getColumnClass(optionType);
		this.possibleValues = possibleValues == null || possibleValues.length == 0 ? UniqueObjects.EMPTY_STRING_LIST : ArrayTools.unmodifiableList(possibleValues);
		this.defaultValue = defaultValue;
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
	
	
	@Override
	public Object getValue(Object obj) {
		GameInstance gi = (GameInstance)obj;
		switch (this)
		{
			case CONNECT: 		return "Connect";
			case DELETE:		return "Delete";
			case ID:			return gi.name;
			case NAME:			return gi.name;
			case NUM_PLAYERS:	return gi.players.size();
			default:throw new IllegalArgumentException(getName());
		}
	}
};