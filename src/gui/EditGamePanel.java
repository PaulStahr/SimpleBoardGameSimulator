package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import gameObjects.ColumnTypes;
import gameObjects.GameInstanceColumnType;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import util.JFrameUtils;
import util.jframe.table.ButtonColumn;
import util.jframe.table.TableModel;

public class EditGamePanel extends JPanel implements ActionListener{
	GameInstance gi;
	JTable tableGameObjects = new JTable();
	private final DefaultTableModel tableModelOpenGames = new TableModel(GameInstance.TYPES.getTableColumnTypeList());
	public EditGamePanel(GameInstance gi) {
		this.gi = gi;
	}
    private final AbstractAction tableAction = new AbstractAction() {
    	private static final long serialVersionUID = 3980835476835695337L;
			@Override
			public void actionPerformed(ActionEvent e)
 	    {
				EditGamePanel.this.actionPerformed(e);
 	    }
    };
 	private final ButtonColumn deleteColumn = new ButtonColumn(tableGameObjects,tableAction, GameInstance.TYPES.getVisibleColumnNumber(GameInstanceColumnType.DELETE));

    public static final void updateTable(JTable table, JScrollPane scrollPane, ArrayList<ObjectInstance> objectList, ColumnTypes types, DefaultTableModel tm, ButtonColumn ...buttonColumn)
    {
 		Object[][] rowData = new Object[objectList.size()][types.visibleColsSize()];
     	for (int i = 0; i < rowData.length; ++i)
     	{
     		ObjectInstance obj = objectList.get(i);
     		for (int j = 0; j < types.visibleColsSize();++j)
     		{
     			rowData[i][j] = JFrameUtils.toTableEntry(obj.getValue(types.getVisibleCol(j)));
     		}
     	}
     	JFrameUtils.updateTable(table, scrollPane, rowData, types.getVisibleColumnNames(), types.visibleList(), tm, buttonColumn);
 	}

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9089357847164495823L;

	public String name;
	
	JTable tableGameItems = new JTable();
	private JScrollPane scrollPaneGameObjects = new JScrollPane(tableGameItems);
	@Override
	public void actionPerformed(ActionEvent arg0) {
		updateTable(tableGameObjects, scrollPaneGameObjects, gi.objects, GameInstance.TYPES, tableModelOpenGames, deleteColumn);
	}
	

}
