package main.util.jframe.table;

import java.util.List;

import main.util.ArrayTools;
import main.util.ArrayUtil;

public class ColumnTypes
{
    private final String[] columnolumnNames;
    private final List<TableColumnType> colsList;
 	private final TableColumnType[] cols;
	public ColumnTypes(TableColumnType cols[], TableColumnType visibleCols[])
	{
		this.cols = visibleCols;
	    colsList = ArrayTools.unmodifiableList(visibleCols);
	    columnolumnNames = new String[visibleCols.length];
    	for (int i = 0; i < columnolumnNames.length; ++i)
    	{
    		columnolumnNames[i] = visibleCols[i].getName();
    	}
    }

	public int getColumnNumber(TableColumnType col) {
		return ArrayUtil.linearSearch(cols, col);
	}
    
    public final int colSize()
    {
    	return cols.length;
    }
    
    public final TableColumnType getCol(int index)
    {
    	return cols[index];
    }
    
    public final int colsSize()
    {
    	return cols.length;
    }
    
	public String[] getColumnNames() {
		return columnolumnNames.clone();
	}
	
	public TableColumnType[] getCols()
	{
		return cols.clone();
	}

	public List<TableColumnType> getList() {
		return colsList;
	}

	public List<? extends TableColumnType> getTableColumnTypeList() {
		return colsList;
	} 
}
