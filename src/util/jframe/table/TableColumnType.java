package util.jframe.table;

import java.util.List;

public interface TableColumnType {

	byte getOptionType();

	String[] getPossibleValues();
	
	String getName();

	public Object getValue(Object obj);

	public static String[] getColumnNames(TableColumnType[] ct2) {
		String names[] = new String[ct2.length];
		for (int i = 0; i < ct2.length; ++i)
		{
			names[i] = ct2[i].getName();
		}
		return names;
	}
	
	public static int getIndexByName(TableColumnType[] tct, String name)
	{
		for (int i = 0; i < tct.length; ++i)
		{
			if (name.equals(tct[i].getName()))
			{
				return i;
			}
		}
		return -1;
	}

	static TableColumnType getItemByName(TableColumnType[] tct, String name) {
		int index = getIndexByName(tct,  name);
		return index < 0 ? null : tct[index];
	}

	
	public static String[] getColumnNames(List<? extends TableColumnType> types) {
		String names[] = new String[types.size()];
		for (int i = 0; i < types.size(); ++i)
		{
			names[i] = types.get(i).getName();
		}
		return names;
	}

	Class<?> getCl();

	static Class<?> getColumnClass(byte optionType) {
		switch (optionType)
		{
			case ValueColumnTypes.TYPE_CHECKBOX:
				return Boolean.class;
			case ValueColumnTypes.TYPE_COLOR:
			case ValueColumnTypes.TYPE_TEXTFIELD:
			case ValueColumnTypes.TYPE_COMBOBOX:
			case ValueColumnTypes.TYPE_BUTTON:
				return String.class;
			default:
				throw new IllegalArgumentException();
		}
	}
}
