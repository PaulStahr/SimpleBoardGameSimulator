package gameObjects;

import util.ArrayTools;
import util.ArrayTools.UnmodifiableArrayList;
import util.data.UniqueObjects;

public enum ObjectColumnType{
	ID("id", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	NAME("name", ValueColumnTypes.TYPE_TEXTFIELD, "Unnamed", null),
	CONNECT("Connect", ValueColumnTypes.TYPE_BUTTON, "Connect", null),
	DELETE("Delete", ValueColumnTypes.TYPE_BUTTON, "Delete", null);
	
    private static final ObjectColumnType ct[] = ObjectColumnType.values();
    private static final String[] columnNames = new String[ObjectColumnType.ct.length];
    
    public static final int size()
    {
    	return ct.length;
    }
    
    public static final ObjectColumnType get(int index)
    {
    	return ct[index];
    }
    
	public final String name;
	public final Class<?> cl;
	public final byte optionType;
	public final Object defaultValue;
	public final UnmodifiableArrayList<String> possibleValues;
	
	private ObjectColumnType(String name, byte optionType, Object defaultValue, String possibleValues[]) {
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
	
	public static ObjectColumnType getByName(String name) {
		for (int i = 0; i < columnNames.length; ++i)
		{
			if (columnNames[i].equals(name))
			{
				return ct[i];
			}
		}
		return null;
	}
};
