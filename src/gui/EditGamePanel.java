package gui;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;

import gameObjects.GameObjectColumnType;
import gameObjects.GameObjectInstanceColumnType;
import gameObjects.ImageColumnType;
import gameObjects.PlayerColumnType;
import gameObjects.action.GameAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.GameStructureEditAction;
import gameObjects.definition.GameObject;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import gameObjects.instance.ObjectInstance;
import main.Player;
import util.ArrayTools;
import util.JFrameUtils;
import util.jframe.table.ButtonColumn;
import util.jframe.table.TableColumnType;
import util.jframe.table.TableModel;

public class EditGamePanel extends JPanel implements ActionListener, GameChangeListener, Runnable{
	public static final List<TableColumnType> IMAGE_TYPES = ArrayTools.unmodifiableList(new TableColumnType[]{ImageColumnType.ID, ImageColumnType.WIDTH, ImageColumnType.HEIGHT, ImageColumnType.DELETE});
	GameInstance gi;
	private final DefaultTableModel tableModelGameObjectInstances= new TableModel(ObjectInstance.TYPES);
	private final DefaultTableModel tableModelGameObjects= new TableModel(GameObject.TYPES);
	private final DefaultTableModel tableModelImages= new TableModel(IMAGE_TYPES);
	private final DefaultTableModel tableModelPlayer = new TableModel(Player.TYPES);
	private final JTable tableGameObjectInstances = new JTable(tableModelGameObjectInstances);
	private final JTable tableGameObjects = new JTable(tableModelGameObjects);
	private final JTable tableImages = new JTable(tableModelImages);
	private final JTable tablePlayer = new JTable(tableModelPlayer);
	public String name;
	private final JScrollPane scrollPaneGameObjectInstances = new JScrollPane(tableGameObjectInstances);
	private final JScrollPane scrollPaneGameObjects = new JScrollPane(tableGameObjects);
	private final JScrollPane scrollPaneImages = new JScrollPane(tableImages);
	private final GeneralPanel panelGeneral = new GeneralPanel();	
	private final JTabbedPane tabPane = new JTabbedPane();
	public EditGamePanel(GameInstance gi) {
		this.gi = gi;
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(tabPane));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(tabPane));
		tabPane.addTab("Genenaral", panelGeneral);
		tabPane.addTab("GameObjects", scrollPaneGameObjects);
		tabPane.addTab("GameObjectInstances", scrollPaneGameObjectInstances);
		tabPane.addTab("Images", scrollPaneImages);
		tabPane.addTab("Player", tablePlayer);
		updateTables();
		gi.addChangeListener(this);
	}
    private final AbstractAction tableAction = new AbstractAction() {
    	private static final long serialVersionUID = 3980835476835695337L;
			@Override
			public void actionPerformed(ActionEvent e)
 	    {
				EditGamePanel.this.actionPerformed(e);
 	    }
    };
 	private final ButtonColumn deleteObjectColumn = new ButtonColumn(tableGameObjects,tableAction, GameObject.TYPES.indexOf(GameObjectColumnType.DELETE));
 	private final ButtonColumn deleteObjectInstanceColumn = new ButtonColumn(tableGameObjects,tableAction, ObjectInstance.TYPES.indexOf(GameObjectInstanceColumnType.DELETE));
 	private final ButtonColumn deleteImageColumn = new ButtonColumn(tableImages,tableAction, IMAGE_TYPES.indexOf(ImageColumnType.DELETE));
 	private final ButtonColumn deletePlayerColumn = new ButtonColumn(tablePlayer,tableAction, Player.TYPES.indexOf(PlayerColumnType.DELETE));
	private boolean isUpdating = false;
	 	
 	private class GeneralPanel extends JPanel implements ItemListener, DocumentListener
 	{
 		/**
		 * 
		 */
		private static final long serialVersionUID = 3667665407550359889L;
		private final JLabel labelName = new JLabel("Name");
 		private final JTextField textFieldName = new JTextField();
 		private final JLabel labelBackground = new JLabel("Background");
 		private final JComboBox<String> comboBoxBackground = new JComboBox<String>();
		
 		public GeneralPanel()
 		{
 			GroupLayout layout = new GroupLayout(this);
 			setLayout(layout);
 			
 			layout.setHorizontalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(labelBackground)).addGroup(layout.createParallelGroup().addComponent(textFieldName).addComponent(comboBoxBackground)));
 			layout.setVerticalGroup(layout.createSequentialGroup().addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(textFieldName)).addGroup(layout.createParallelGroup().addComponent(labelBackground).addComponent(comboBoxBackground)));
 		}

		public void update() {
			textFieldName.setText(gi.name);
			textFieldName.getDocument().addDocumentListener(this);
			JFrameUtils.updateComboBox(comboBoxBackground, gi.game.getImageKeys());
			comboBoxBackground.setSelectedItem(gi.game.getImageKey(gi.game.background));
			comboBoxBackground.addItemListener(this);
		}

		@Override
		public void itemStateChanged(ItemEvent arg0) {
			if (isUpdating)
			{
				return;
			}
			if (arg0.getStateChange() == ItemEvent.SELECTED) {
		        gi.game.background = gi.game.images.get(arg0.getItem());
		        gi.update(new GameStructureEditAction(0));
			}
		}
		
		
		public void update(DocumentEvent event)
		{
			if (isUpdating)
			{
				return;
			}
			gi.name = textFieldName.getText();
		}

		@Override
		public void changedUpdate(DocumentEvent arg0) {update(arg0);	}

		@Override
		public void insertUpdate(DocumentEvent arg0) {update(arg0);}

		@Override
		public void removeUpdate(DocumentEvent arg0) {update(arg0);}
 	}
 	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9089357847164495823L;

	private void updateTables()
	{
    	if (!EventQueue.isDispatchThread())
    	{
    		throw new RuntimeException("Game-Panel changes only allowed by dispatchment thread");
    	}
		isUpdating = true;
		JFrameUtils.updateTable(tableGameObjects, scrollPaneGameObjects, gi.game.objects, GameObject.TYPES, tableModelGameObjects, deleteObjectColumn);
		JFrameUtils.updateTable(tableGameObjectInstances, scrollPaneGameObjectInstances, gi.getObjectInstanceList(), ObjectInstance.TYPES, tableModelGameObjectInstances, deleteObjectInstanceColumn);
		JFrameUtils.updateTable(tableImages, scrollPaneImages, gi.game.images.entrySet().toArray(), IMAGE_TYPES, tableModelImages, deleteImageColumn);
		JFrameUtils.updateTable(tablePlayer, scrollPaneImages, gi.getPlayerList(), Player.TYPES, tableModelPlayer, deletePlayerColumn);
		panelGeneral.update();
		isUpdating = false;
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (isUpdating)
		{
			return;
		}
		if (ae instanceof ButtonColumn.TableButtonActionEvent)
		{
			ButtonColumn.TableButtonActionEvent event = (ButtonColumn.TableButtonActionEvent)ae;
			Object tableSource = event.getSource();
			//int col = event.getCol();
			int row = event.getRow();
			if (tableSource== tableModelGameObjectInstances)
			{
				gi.remove(gi.getObjectInstanceByIndex(row));
			}
			else if (tableSource == tableModelImages)
			{
				Set<Entry<String, BufferedImage>> entrySet = gi.game.images.entrySet();
				@SuppressWarnings("unchecked")
				Entry<String, BufferedImage> entry = entrySet.toArray(new Entry[entrySet.size()])[row];
				gi.game.images.remove(entry.getKey(), entry.getValue());
			}
			else if (tableSource == tableModelGameObjectInstances)
			{
				
			}
			else if (tableSource == tableModelPlayer)
			{
				
			}
		}
		updateTables();
	}
	
	@Override
	public void run()
	{
		updateTables();
	}

	@Override
	public void changeUpdate(GameAction action) {
		if (action instanceof GameObjectInstanceEditAction)
		{
			JFrameUtils.runByDispatcher(this);
		}
	}
	

}
