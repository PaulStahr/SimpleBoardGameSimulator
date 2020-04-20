package main.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import main.gameObjects.GameObjectInstanceColumnType;
import main.gameObjects.instance.GameInstance;
import main.gameObjects.instance.ObjectInstance;
import main.util.JFrameUtils;
import main.util.jframe.table.ButtonColumn;
import main.util.jframe.table.ColumnTypes;
import main.util.jframe.table.TableModel;

public class EditGamePanel extends JPanel implements ActionListener{
	GameInstance gi;
	private final DefaultTableModel tableModelGameObjects= new TableModel(ObjectInstance.TYPES.getTableColumnTypeList());
	private final JTable tableGameObjects = new JTable(tableModelGameObjects);
	public String name;
	private JScrollPane scrollPaneGameObjects = new JScrollPane(tableGameObjects);
	public EditGamePanel(GameInstance gi) {
		this.gi = gi;
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(scrollPaneGameObjects));
		layout.setVerticalGroup(layout.createParallelGroup().addComponent(scrollPaneGameObjects));
		setSize(500, 300);
		actionPerformed(null);
	}
    private final AbstractAction tableAction = new AbstractAction() {
    	private static final long serialVersionUID = 3980835476835695337L;
			@Override
			public void actionPerformed(ActionEvent e)
 	    {
				EditGamePanel.this.actionPerformed(e);
 	    }
    };
 	private final ButtonColumn deleteColumn = new ButtonColumn(tableGameObjects,tableAction, ObjectInstance.TYPES.getColumnNumber(GameObjectInstanceColumnType.DELETE));

    public static final void updateTable(JTable table, JScrollPane scrollPane, ArrayList<ObjectInstance> objectList, ColumnTypes types, DefaultTableModel tm, ButtonColumn ...buttonColumn)
    {
 		Object[][] rowData = new Object[objectList.size()][types.colsSize()];
     	for (int i = 0; i < rowData.length; ++i)
     	{
     		ObjectInstance obj = objectList.get(i);
     		for (int j = 0; j < types.colsSize();++j)
     		{
     			rowData[i][j] = JFrameUtils.toTableEntry(obj.getValue(types.getCol(j)));
     		}
     	}
     	JFrameUtils.updateTable(table, scrollPane, rowData, types.getColumnNames(), types.getList(), tm, buttonColumn);
 	}

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9089357847164495823L;

	@Override
	public void actionPerformed(ActionEvent arg0) {
		updateTable(tableGameObjects, scrollPaneGameObjects, gi.objects, ObjectInstance.TYPES, tableModelGameObjects, deleteColumn);
	}
	

}
