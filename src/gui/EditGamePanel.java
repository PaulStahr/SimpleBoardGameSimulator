package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import gameObjects.GameObjectInstanceColumnType;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import util.ArrayTools;
import util.JFrameUtils;
import util.jframe.table.ButtonColumn;
import util.jframe.table.ColumnTypes;
import util.jframe.table.TableColumnType;
import util.jframe.table.TableModel;

public class EditGamePanel extends JPanel implements ActionListener{
	public static final List<TableColumnType> IMAGE_TYPES = ArrayTools.unmodifiableList(new TableColumnType[]{GameObjectInstanceColumnType.ID, GameObjectInstanceColumnType.NAME, GameObjectInstanceColumnType.DELETE});

	GameInstance gi;
	private final DefaultTableModel tableModelGameObjects= new TableModel(ObjectInstance.TYPES);
	private final DefaultTableModel tableModelImages= new TableModel(ObjectInstance.TYPES);
	private final JTable tableGameObjects = new JTable(tableModelGameObjects);
	private final JTable tableImages = new JTable(tableModelImages);
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
 	private final ButtonColumn deleteObjectColumn = new ButtonColumn(tableGameObjects,tableAction, ObjectInstance.TYPES.indexOf(GameObjectInstanceColumnType.DELETE));
 	private final ButtonColumn deleteImageColumn = new ButtonColumn(tableGameObjects,tableAction, ObjectInstance.TYPES.indexOf(GameObjectInstanceColumnType.DELETE));

    public static final void updateTable(JTable table, JScrollPane scrollPane, ArrayList<ObjectInstance> objectList, List<TableColumnType> types, DefaultTableModel tm, ButtonColumn ...buttonColumn)
    {
 		Object[][] rowData = new Object[objectList.size()][types.size()];
     	for (int i = 0; i < rowData.length; ++i)
     	{
     		ObjectInstance obj = objectList.get(i);
     		for (int j = 0; j < types.size();++j)
     		{
     			rowData[i][j] = JFrameUtils.toTableEntry(obj.getValue(types.get(j)));
     		}
     	}
     	JFrameUtils.updateTable(table, scrollPane, rowData, ColumnTypes.getColumnNames(types), types, tm, buttonColumn);
 	}

    public static final void updateImageTable(JTable table, JScrollPane scrollPane, ArrayList<ObjectInstance> objectList, List<TableColumnType> types, DefaultTableModel tm, ButtonColumn ...buttonColumn)
    {
 		Object[][] rowData = new Object[objectList.size()][types.size()];
     	for (int i = 0; i < rowData.length; ++i)
     	{
     		ObjectInstance obj = objectList.get(i);
     		for (int j = 0; j < types.size();++j)
     		{
     			rowData[i][j] = JFrameUtils.toTableEntry(obj.getValue(types.get(j)));
     		}
     	}
     	JFrameUtils.updateTable(table, scrollPane, rowData, ColumnTypes.getColumnNames(types), types, tm, buttonColumn);
 	}

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9089357847164495823L;

	@Override
	public void actionPerformed(ActionEvent arg0) {
		updateTable(tableGameObjects, scrollPaneGameObjects, gi.objects, ObjectInstance.TYPES, tableModelGameObjects, deleteObjectColumn);
	}
	

}
