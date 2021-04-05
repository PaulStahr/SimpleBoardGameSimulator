package gameObjects.columnTypes;

import main.Player;
import util.ArrayTools;
import util.ArrayTools.UnmodifiableArrayList;
import util.ArrayUtil;
import util.data.UniqueObjects;
import util.jframe.table.TableColumnType;
import util.jframe.table.ValueColumnTypes;

public enum PlayerColumnType implements TableColumnType{
	ID("id", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	NAME("name", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	DELETE("Delete", ValueColumnTypes.TYPE_BUTTON, "Delete", null),
	REPAIR("Repair", ValueColumnTypes.TYPE_BUTTON, "Repair", null),
	COLOR("", ValueColumnTypes.TYPE_BUTTON, "Color", null);
	
    private static final PlayerColumnType ct[] = PlayerColumnType.values();
    private static final String[] columnNames = TableColumnType.getColumnNames(ct);
    
    public static final int size(){return ct.length;}
    
    public static final PlayerColumnType get(int index){return ct[index];}
    
	public final String name;
	public final Class<?> cl;
	public final byte optionType;
	public final Object defaultValue;
	public final UnmodifiableArrayList<String> possibleValues;
	
	private PlayerColumnType(String name, byte optionType, Object defaultValue, String possibleValues[]) {
		this.name = name;
		this.optionType = optionType;
		this.cl = TableColumnType.getColumnClass(optionType);
		this.possibleValues = possibleValues == null || possibleValues.length == 0 ? UniqueObjects.EMPTY_STRING_LIST : ArrayTools.unmodifiableList(possibleValues);
		this.defaultValue = defaultValue;
	}
	
	public static PlayerColumnType getByName(String name) {
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
		Player gi = (Player)obj;
		switch (this)
		{
			case DELETE:	return "Delete";
			case REPAIR:	return "Repair";
			case ID:		return gi.id;
			case NAME:		return gi.getName();
			case COLOR:     return "";
			default:throw new IllegalArgumentException(getName());
		}
	}
};
