package gameObjects;

import java.util.List;

import util.ArrayTools;
import util.ArrayUtil;
import util.data.TableColumnType;

public class ColumnTypes
{
	GameInstanceColumnType cols[];
    private final String[] columnNames;
    private final String[] visibleColumnNames;
    private final List<GameInstanceColumnType> visibleColsList;
 	private final GameInstanceColumnType[] visibleCols;
	public ColumnTypes(GameInstanceColumnType cols[], GameInstanceColumnType visibleCols[])
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

	public int getVisibleColumnNumber(GameInstanceColumnType col) {
		return ArrayUtil.linearSearch(visibleCols, col);
	}
    
	public int getColumnNumber(GameInstanceColumnType col) {
		return ArrayUtil.linearSearch(cols, col);
	}
    
    public final int colSize()
    {
    	return cols.length;
    }
    
    public final GameInstanceColumnType getCol(int index)
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
	
	
	public GameInstanceColumnType[] getVisibleColList()
	{
		return visibleCols.clone();
	}
	
	public GameInstanceColumnType[] getVisibleCols()
	{
		return visibleCols.clone();
	}
    
    public final GameInstanceColumnType getVisibleCol(int index)
    {
    	return visibleCols[index];
    }

	public List<GameInstanceColumnType> visibleList() {
		return visibleColsList;
	}

	public List<? extends TableColumnType> getTableColumnTypeList() {
		return visibleColsList;
	} 
}
