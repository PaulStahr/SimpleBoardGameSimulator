package gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.DefaultCellEditor;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.jdom2.JDOMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.ColumnTypes;
import gameObjects.GameMetaInfo;
import gameObjects.ObjectColumnType;
import gameObjects.ValueColumnTypes;
import gameObjects.instance.GameInstance;
import gui.util.ButtonColumn;
import io.GameIO;
import main.Player;
import net.AsynchronousGameConnection;
import net.SynchronousGameClientLobbyConnection;
import util.JFrameUtils;

public class ServerLobbyWindow extends JFrame implements ActionListener, ListSelectionListener, TableModelListener{
	/**
	 * 
	 */
	private static final Logger logger = LoggerFactory.getLogger(ServerLobbyWindow.class);
	private static final long serialVersionUID = 6569919447688866509L;
	public final SynchronousGameClientLobbyConnection client;
	public final JTextField textFieldName = new JTextField();
	private final JTextField textFieldChat = new JTextField();
	private final JTextArea textAreaChat = new JTextArea();
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
	private final JTable tableOpenGames = new JTable(tableModelOpenGames);
	private final JScrollPane scrollPaneOpenGames = new JScrollPane(tableOpenGames);
    private static final DefaultCellEditor checkBoxCellEditor = new DefaultCellEditor(new JCheckBox()); 
    private final JButton buttonPoll = new JButton("Aktualisiere");
    private final JButton buttonCreateGame = new JButton("CreateGame");
	private boolean isUpdating = false;
	
    private final AbstractAction tableAction = new AbstractAction() {
    	private static final long serialVersionUID = 3980835476835695337L;
			@Override
			public void actionPerformed(ActionEvent e)
 	    {
			ServerLobbyWindow.this.actionPerformed(e);
 	    }
    };
 	private final ButtonColumn connectColumn = new ButtonColumn(tableOpenGames,tableAction, GameInstance.TYPES.getVisibleColumnNumber(ObjectColumnType.CONNECT));

    private final void updateTable(JTable table, JScrollPane scrollPane, ArrayList<GameMetaInfo> objectList, ColumnTypes types, DefaultTableModel tm, ButtonColumn ...buttonColumn)
    {
    	Object[][] rowData = new Object[objectList.size()][types.visibleColsSize()];
    	for (int i = 0; i < rowData.length; ++i)
    	{
    		GameMetaInfo obj = objectList.get(i);
    		for (int j = 0; j < types.visibleColsSize();++j)
    		{
    			Object value = obj.getValue(types.getVisibleCol(j));
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
		for (int i = 0; i < types.visibleColsSize(); ++i)
		{
			ObjectColumnType current = types.getVisibleCol(i); 
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
		scrollPane.setPreferredSize(new Dimension(dim.width, dim.height + table.getTableHeader().getPreferredSize().height + 8));
    }
    
	@Override
	public void actionPerformed(ActionEvent e)
    {
		Object source = e.getSource();
		if (source == buttonPoll)
		{
			ArrayList<String> al = new ArrayList<>();
			try {
				client.getGameInstances(al);
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			ArrayList<GameMetaInfo> gmi = new ArrayList<>();
			for (int i = 0; i < al.size(); ++i) {
				gmi.add(new GameMetaInfo(al.get(i)));
				gmi.get(i).name = al.get(i);
			}
			updateTable(tableOpenGames, scrollPaneOpenGames, gmi, GameInstance.TYPES, tableModelOpenGames, connectColumn);
			
		}
		else if (source == buttonCreateGame)
		{
			JFileChooser fileChooser = new JFileChooser();
			if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION)
			{
				try
				{
					File file = fileChooser.getSelectedFile();
					GameInstance gi = GameIO.readSnapshotFromZip(new FileInputStream(file));
					Player player = new Player("Florian", 1);
					GameWindow gw = new GameWindow(gi, new Player("Florian", 1));
			    	client.pushGameSession(gi);
			    	try {
						Thread.sleep(1000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
			    	client.addPlayerToGameSession(player, gi.name, gi.password);
			    	AsynchronousGameConnection connection = client.connectToGameSession(gi);
			    	connection.start();
			    	gi.addPlayer(player);
			    	gw.setVisible(true);
				}catch(IOException | JDOMException ex)
				{
					JFrameUtils.logErrorAndShow("Can't connect to Server", ex, logger);
				}
			}
			
		}
		else if (e instanceof ButtonColumn.TableButtonActionEvent)
		{
			ButtonColumn.TableButtonActionEvent event = (ButtonColumn.TableButtonActionEvent)e;
			Object tableSource = event.getSource();
			int col = event.getCol();
			int row = event.getRow();
			if (tableSource == tableModelOpenGames)
			{
				if (GameInstance.TYPES.getCol(col) == ObjectColumnType.CONNECT)
				{
					try
					{
						Player player = new Player("Florian", 1);
						GameInstance gi = client.getGameInstance((String)tableModelOpenGames.getValueAt(GameInstance.TYPES.getColumnNumber(ObjectColumnType.ID), row));
				    	client.addPlayerToGameSession(player, gi.name, gi.password);
				    	GameWindow gw = new GameWindow(gi, player);
				    	AsynchronousGameConnection connection = client.connectToGameSession(gi);
				    	//gi.addPlayer(player);
				    	connection.syncPull();
				    	connection.start();
				    	gw.setVisible(true);
					} catch (IOException | JDOMException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
		 	    }
			}
		}
    }
	
	public ServerLobbyWindow(SynchronousGameClientLobbyConnection client)
	{
		Container content = getContentPane();
		GroupLayout layout = new GroupLayout(content);
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(scrollPaneOpenGames).addGroup(layout.createSequentialGroup().addComponent(buttonPoll).addComponent(buttonCreateGame)));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(scrollPaneOpenGames).addGroup(layout.createParallelGroup().addComponent(buttonPoll).addComponent(buttonCreateGame)));
		setLayout(layout);
		buttonPoll.addActionListener(this);
		tableOpenGames.getSelectionModel().addListSelectionListener(this);
		tableOpenGames.getModel().addTableModelListener(this);
		this.client = client;
		buttonCreateGame.addActionListener(this);
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
    

	@Override
	public void valueChanged(ListSelectionEvent e) {
		// TODO Auto-generated method stub
		
	}
}
