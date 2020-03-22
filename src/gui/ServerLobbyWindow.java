package gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import gameObjects.ColumnTypes;
import gameObjects.ObjectColumnType;
import gameObjects.ValueColumnTypes;
import gameObjects.instance.GameInstance;
import gui.util.ButtonColumn;
import net.SynchronousGameClientLobbyConnection;

public class ServerLobbyWindow extends JFrame implements ActionListener, ListSelectionListener, TableModelListener{
	public final SynchronousGameClientLobbyConnection client;
	
	JTable tableOpenGames = new JTable();
	
	/*TODOS here:
	show a list of the current running games
	show a list of lokally installed games
	Start a new game session
	Connect to an existing game session
	*/
	
	private static final class TableModel extends DefaultTableModel
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = -3379232940335572529L;
		private final ColumnTypes types;
		
		public TableModel(ColumnTypes types)
		{
			super(new Object[1][types.colSize()], types.getVisibleColumnNames());
			this.types = types;
			

			
		}
		
		@Override
        public final Class<?> getColumnClass(int columnIndex) {
            return types.getCol(columnIndex).cl;
        }
	}
	
	private final DefaultTableModel tableModelOpenGames = new TableModel(GameInstance.TYPES);
	private final JTable tableOperGames = new JTable(tableModelOpenGames);
	private final JScrollPane scrollPaneSurfaces = new JScrollPane(tableOperGames);
    private static final DefaultCellEditor checkBoxCellEditor = new DefaultCellEditor(new JCheckBox()); 
    private final JButton buttonPoll = new JButton("Aktualisiere");
	private boolean isUpdating = false;
    
	@Override
	public void actionPerformed(ActionEvent e)
    {
		Object source = e.getSource();
		
		ButtonColumn.TableButtonActionEvent event = (ButtonColumn.TableButtonActionEvent)e;
		Object tableSource = event.getSource();
		int col = event.getCol();
		int row = event.getRow();
		if (tableSource == tableModelOpenGames)
		{
			if (GameInstance.TYPES.getCol(col) == ObjectColumnType.CONNECT)
			{
				/*Connect*/
	 	    }
		}
    }
	
	public ServerLobbyWindow(SynchronousGameClientLobbyConnection client)
	{
		Container content = getContentPane();
		GroupLayout layout = new GroupLayout(content);
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(tableOpenGames).addGroup(layout.createSequentialGroup().addComponent(buttonPoll)));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(tableOpenGames).addGroup(layout.createParallelGroup().addComponent(buttonPoll)));
		setLayout(layout);
		buttonPoll.addActionListener(this);
		tableOperGames.getSelectionModel().addListSelectionListener(this);
		tableOperGames.getModel().addTableModelListener(this);
		this.client = client;
	}
	
    @Override
	public void tableChanged(TableModelEvent e) {
    	if (!EventQueue.isDispatchThread())
    	{
    		throw new RuntimeException("Table Changes only allowed by dispatchment thread");
    	}
       	if (!isUpdating )
		{
       		int rowBegin = e.getFirstRow();
        	if (rowBegin == TableModelEvent.HEADER_ROW)
        	{
        		return;
        	}
        	isUpdating = true;
 			Object source = e.getSource();
    		int colBegin = e.getColumn() == TableModelEvent.ALL_COLUMNS ? 0 : e.getColumn();
        	int rowEnd = e.getLastRow() + 1;
			if (source == tableModelOpenGames)
			{
				
				//tablechanged(e, tableOperGames, scene.surfaceObjectList, colBegin, rowBegin, rowEnd, tableModelOpenGames, GameInstance.TYPES);
			}

			isUpdating = false;
		}
    }
    
    private final <E extends GameInstance> void updateTable(JTable table, JScrollPane scrollPane, ArrayList<E> objectList, ColumnTypes types, DefaultTableModel tm, ButtonColumn ...buttonColumn)
    {
    	Object[][] rowData = new Object[objectList.size()][types.colSize()];
    	for (int i = 0; i < rowData.length; ++i)
    	{
    		GameInstance obj = objectList.get(i);
    		for (int j = 0; j < types.colSize();++j)
    		{
    			Object value = obj.getValue(types.getCol(j));
    			if (value instanceof Boolean)
    			{
    				rowData[i][j] = value;
    			}
    			else if (value == null)
    			{
    				rowData[i][j] = null;
    			}
    			else
    			{
    				rowData[i][j] = String.valueOf(value);
    			}
    		}
    	}

    	tm.setDataVector(rowData, types.getVisibleColumnNames());
		for (int i = 0; i < types.colSize(); ++i)
		{
			ObjectColumnType current = types.getCol(i); 
			TableColumn column = table.getColumnModel().getColumn(i);
		  	if (current.optionType == ValueColumnTypes.TYPE_COMBOBOX)
		 	{
		     	JComboBox<String> comboBox = new JComboBox<String>(current.possibleValues.toArray(new String[current.possibleValues.size()]));
		     	column.setCellEditor(new DefaultCellEditor(comboBox));
		 	}else if (current.optionType == ValueColumnTypes.TYPE_CHECKBOX)
		 	{
		 		column.setCellEditor(checkBoxCellEditor);
		 	}
		}

		for (ButtonColumn bc : buttonColumn)
		{
			bc.setTable(table);
		}
		Dimension dim = table.getPreferredSize();
		scrollPane.setPreferredSize(new Dimension(dim.width, dim.height + tableOperGames.getTableHeader().getPreferredSize().height + 8));
    }

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
    
}
