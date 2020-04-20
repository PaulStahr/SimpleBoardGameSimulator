package main.util.jframe.table;

import java.util.List;

public interface TableColumnType {

	byte getOptionType();

	String[] getPossibleValues();
	
	String getName();

	public static Object[] getColumnNames(List<? extends TableColumnType> types) {
		String names[] = new String[types.size()];
		for (int i = 0; i < types.size(); ++i)
		{
			names[i] = types.get(i).getName();
		}
		return names;
	}

	Class<?> getCl();

}
