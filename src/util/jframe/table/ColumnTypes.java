package util.jframe.table;

import java.util.List;

public class ColumnTypes
{
	public static final String[] getColumnNames(List<TableColumnType> list)
	{
		String result[] = new String[list.size()];
		for (int i = 0; i < result.length; ++i)
		{
			result[i] = list.get(i).getName();
		}
		return result;
	}
}
