package main.util.jframe.table;

import java.util.List;

import javax.swing.table.DefaultTableModel;

public final class TableModel extends DefaultTableModel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3379232940335572529L;
	private final List<? extends TableColumnType> types;
	
	public TableModel(List<? extends TableColumnType> types)
	{
		super(new Object[1][types.size()], TableColumnType.getColumnNames(types));
		this.types = types;
	}
	
	@Override
    public final Class<?> getColumnClass(int columnIndex) {
        return types.get(columnIndex).getCl();
    }
}
