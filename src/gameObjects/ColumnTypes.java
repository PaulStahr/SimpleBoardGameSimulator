package gameObjects;

import java.util.List;

import util.ArrayTools;
import util.ArrayUtil;

public class ColumnTypes
{
	ObjectColumnType cols[];
    private final String[] columnNames;
    private final String[] visibleColumnNames;
    private final List visibleColsList;
 	private final ObjectColumnType[] visibleCols;
	public ColumnTypes(ObjectColumnType cols[], ObjectColumnType visibleCols[])
	{
		this.cols = cols;
		columnNames = new String[cols.length];
		this.visibleCols = visibleCols;
	    visibleColsList = ArrayTools.unmodifiableList(visibleCols);
	    visibleColumnNames = new String[visibleCols.length];
		for (int i = 0; i < cols.length; ++i)
    	{
    		columnNames[i] = cols[i].name;
    	}
    	for (int i = 0; i < visibleColumnNames.length; ++i)
    	{
    		visibleColumnNames[i] = visibleCols[i].name;
    	}
    }

	public int getVisibleColumnNumber(ObjectColumnType col) {
		return ArrayUtil.linearSearch(visibleCols, col);
	}
    
	public int getColumnNumber(ObjectColumnType col) {
		return ArrayUtil.linearSearch(cols, col);
	}
    
    public final int colSize()
    {
    	return cols.length;
    }
    
    public final ObjectColumnType getCol(int index)
    {
    	return cols[index];
    }
    
    public final int visibleColsSize()
    {
    	return visibleCols.length;
    }
    
	public String[] getVisibleColumnNames() {
		return visibleColumnNames.clone();
	}
	
	public ObjectColumnType[] getVisibleCols()
	{
		return visibleCols.clone();
	}
    
    public final ObjectColumnType getVisibleCol(int index)
    {
    	return visibleCols[index];
    }

	public List<ObjectColumnType> visibleList() {
		return visibleColsList;
	} 
}
