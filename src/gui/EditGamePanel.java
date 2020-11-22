package gui;

import java.awt.EventQueue;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.Document;

import gameObjects.GameObjectColumnType;
import gameObjects.GameObjectInstanceColumnType;
import gameObjects.ImageColumnType;
import gameObjects.PlayerColumnType;
import gameObjects.action.AddObjectAction;
import gameObjects.action.GameAction;
import gameObjects.action.GameObjectEditAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.GamePlayerEditAction;
import gameObjects.action.GameStructureEditAction;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectToken;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import main.Player;
import util.ArrayTools;
import util.JFrameUtils;
import util.jframe.table.ButtonColumn;
import util.jframe.table.TableColumnType;
import util.jframe.table.TableModel;

public class EditGamePanel extends JPanel implements ActionListener, GameChangeListener, Runnable, MouseListener, TableModelListener, LanguageChangeListener{
	public static final List<TableColumnType> IMAGE_TYPES = ArrayTools.unmodifiableList(new TableColumnType[]{ImageColumnType.ID, ImageColumnType.WIDTH, ImageColumnType.HEIGHT, ImageColumnType.DELETE});
	private final GameInstance gi;
	private final DefaultTableModel tableModelGameObjectInstances= new TableModel(ObjectInstance.TYPES);
	private final DefaultTableModel tableModelGameObjects= new TableModel(GameObject.TYPES);
	private final DefaultTableModel tableModelImages= new TableModel(IMAGE_TYPES);
	private final DefaultTableModel tableModelPlayer = new TableModel(Player.TYPES);
	private final JTable tableGameObjectInstances = new JTable(tableModelGameObjectInstances);
	private final JTable tableGameObjects = new JTable(tableModelGameObjects);
	private final JTable tableImages = new JTable(tableModelImages);
	private final JTable tablePlayer = new JTable(tableModelPlayer);
	private final JScrollPane scrollPaneGameObjectInstances = new JScrollPane(tableGameObjectInstances);
	private final JScrollPane scrollPaneGameObjects = new JScrollPane(tableGameObjects);
	private final JScrollPane scrollPaneImages = new JScrollPane(tableImages);
	private final JScrollPane scrollPanePlayer = new JScrollPane(tablePlayer);
	private final GeneralPanel panelGeneral = new GeneralPanel();	
	private final JTabbedPane tabPane = new JTabbedPane();
	public int id = (int)(Math.random() * Integer.MAX_VALUE);
	private boolean isUpdating = false;
	private Entry<String, BufferedImage>[] imageArray;
	private final LanguageHandler lh;
	private final Player player;

	public EditGamePanel(GameInstance gi, LanguageHandler lh, Player player) {
		this.gi = gi;
		this.lh = lh;
		this.player = player;
		Language language = lh.getCurrentLanguage();
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(tabPane));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(tabPane));
		lh.addLanguageChangeListener(this);
		tabPane.addTab(language.getString(Words.general), panelGeneral);
		tabPane.addTab(language.getString(Words.game_objects), scrollPaneGameObjects);
		tabPane.addTab(language.getString(Words.game_object_instances), scrollPaneGameObjectInstances);
		tabPane.addTab(language.getString(Words.images), scrollPaneImages);
		tabPane.addTab(language.getString(Words.player), scrollPanePlayer);
		tableGameObjects.addMouseListener(this);
		updateTables();
		gi.addChangeListener(this);
		tableModelPlayer.addTableModelListener(this);
		
		scrollPaneImages.setDropTarget(new DropTarget() {
		    /**
			 * 
			 */
			private static final long serialVersionUID = 8601119174600655506L;

			@SuppressWarnings("unchecked")
			@Override
			public synchronized void drop(DropTargetDropEvent evt) {
		        try {
		            evt.acceptDrop(DnDConstants.ACTION_COPY);
		            List<? extends File> droppedFiles = (List<? extends File>)
		                evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
		            for (File file : droppedFiles) {
		            	gi.game.images.put(file.getName(), ImageIO.read(file));
		            	gi.update(new AddObjectAction(id, AddObjectAction.ADD_IMAGE, file.getName().hashCode()));
		            }
		        } catch (Exception ex) {
		            ex.printStackTrace();
		        }
		        updateTables();
		    }
		});
	}
	
	@Override
	public void languageChanged(Language language) {
		tabPane.setTitleAt(tabPane.indexOfComponent(panelGeneral), 					language.getString(Words.general));
		tabPane.setTitleAt(tabPane.indexOfComponent(scrollPaneGameObjects), 		language.getString(Words.game_objects));
		tabPane.setTitleAt(tabPane.indexOfComponent(scrollPaneGameObjectInstances), language.getString(Words.game_object_instances));
		tabPane.setTitleAt(tabPane.indexOfComponent(scrollPaneImages), 				language.getString(Words.images));
		tabPane.setTitleAt(tabPane.indexOfComponent(scrollPanePlayer), 				language.getString(Words.player));
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
 	private final ButtonColumn resetObjectInstanceColumn = new ButtonColumn(tableGameObjectInstances,tableAction, ObjectInstance.TYPES.indexOf(GameObjectInstanceColumnType.RESET));
 	private final ButtonColumn deleteObjectInstanceColumn = new ButtonColumn(tableGameObjectInstances,tableAction, ObjectInstance.TYPES.indexOf(GameObjectInstanceColumnType.DELETE));
 	private final ButtonColumn deleteImageColumn = new ButtonColumn(tableImages,tableAction, IMAGE_TYPES.indexOf(ImageColumnType.DELETE));
 	private final ButtonColumn deletePlayerColumn = new ButtonColumn(tablePlayer,tableAction, Player.TYPES.indexOf(PlayerColumnType.DELETE));
	private final ButtonColumn repairPlayerColumn = new ButtonColumn(tablePlayer, tableAction, Player.TYPES.indexOf(PlayerColumnType.REPAIR)); 	
 	
 	private class GeneralPanel extends JPanel implements ItemListener, DocumentListener, LanguageChangeListener
 	{
 		/**
		 * 
		 */
		private static final long serialVersionUID = 3667665407550359889L;
		private final JLabel labelName = new JLabel();
 		private final JTextField textFieldName = new JTextField();
 		private final JLabel labelBackground = new JLabel("Background");
 		private final JComboBox<String> comboBoxBackground = new JComboBox<String>();
 		private final JLabel labelTableRadius = new JLabel("Table Radius");
 		private final JTextField textFieldTableRadius = new JTextField();
 		private final JLabel labelPassword = new JLabel("Password");
 		private final JTextField textFieldPassword = new JTextField();
		//private final JButton buttonResetAll = new JButton("Reset All");

 		public GeneralPanel()
 		{
 			GroupLayout layout = new GroupLayout(this);
 			setLayout(layout);
 			
 			layout.setHorizontalGroup(
 					layout.createSequentialGroup()
 					.addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(labelBackground).addComponent(labelTableRadius).addComponent(labelPassword))
 					.addGroup(layout.createParallelGroup().addComponent(textFieldName).addComponent(comboBoxBackground).addComponent(textFieldTableRadius).addComponent(textFieldPassword)));
 			layout.setVerticalGroup(
 					layout.createSequentialGroup()
 					.addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(textFieldName))
 					.addGroup(layout.createParallelGroup().addComponent(labelBackground).addComponent(comboBoxBackground))
 					.addGroup(layout.createParallelGroup().addComponent(labelTableRadius).addComponent(textFieldTableRadius))
 					.addGroup(layout.createParallelGroup().addComponent(labelPassword).addComponent(textFieldPassword)));
 			
 			textFieldName.getDocument().addDocumentListener(this);
			comboBoxBackground.addItemListener(this);
			textFieldPassword.getDocument().addDocumentListener(this);
			textFieldTableRadius.getDocument().addDocumentListener(this);
			tableModelImages.addTableModelListener(EditGamePanel.this);
			tableModelGameObjects.addTableModelListener(EditGamePanel.this);
			tableModelGameObjectInstances.addTableModelListener(EditGamePanel.this);
			
 		}

		public void update() {
	    	if (!EventQueue.isDispatchThread())
	    	{
	    		throw new RuntimeException("Game-Panel changes only allowed by dispatchment thread");
	    	}
			if (isUpdating)
			{
				return;
			}
			isUpdating = true;
			textFieldName.setText(gi.name);
			textFieldTableRadius.setText(Float.toString(gi.tableRadius));
			JFrameUtils.updateComboBox(comboBoxBackground, gi.game.getImageKeys());
			comboBoxBackground.setSelectedItem(gi.game.getImageKey(gi.game.background));
			textFieldPassword.setText(gi.password);
			isUpdating = false;
		}

		@Override
		public void itemStateChanged(ItemEvent arg0) {
	    	if (!EventQueue.isDispatchThread())
	    	{
	    		throw new RuntimeException("Game-Panel changes only allowed by dispatchment thread");
	    	}
			if (isUpdating)
			{
				return;
			}
			isUpdating = true;
			if (arg0.getStateChange() == ItemEvent.SELECTED) {
		        gi.game.background = gi.game.images.get(arg0.getItem());
		        gi.update(new GameStructureEditAction(id, GameStructureEditAction.EDIT_BACKGROUND));
			}
			isUpdating = false;
		}
		
		
		public void update(DocumentEvent event)
		{
	    	if (!EventQueue.isDispatchThread())
	    	{
	    		throw new RuntimeException("Game-Panel changes only allowed by dispatchment thread");
	    	}
			if (isUpdating)
			{
				return;
			}
			Document source = event.getDocument();
			if (source == textFieldName.getDocument())
			{
				gi.name = textFieldName.getText();
				isUpdating = true;
				gi.update(new GameStructureEditAction(id, GameStructureEditAction.EDIT_SESSION_NAME));
				isUpdating = false;
			}
			else if (source == textFieldTableRadius.getDocument())
			{
				gi.tableRadius = Integer.parseInt(textFieldTableRadius.getText());
				isUpdating = true;
				gi.update(new GameStructureEditAction(id, GameStructureEditAction.EDIT_TABLE_RADIUS));
				isUpdating = false;
			}
			else if (source == textFieldPassword.getDocument())
			{
				gi.password = textFieldPassword.getText();
				isUpdating = true;
				gi.update(new GameStructureEditAction(id, GameStructureEditAction.EDIT_SESSION_PASSWORD));
				isUpdating = false;
			}
		}

		@Override
		public void changedUpdate(DocumentEvent arg0) {update(arg0);	}

		@Override
		public void insertUpdate(DocumentEvent arg0) {update(arg0);}

		@Override
		public void removeUpdate(DocumentEvent arg0) {update(arg0);}

		@Override
		public void languageChanged(Language language) {
			
		}
		
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
    	if (isUpdating)
    	{
    		return;
    	}
		isUpdating = true;
		JFrameUtils.updateTable(tableGameObjects, scrollPaneGameObjects, gi.game.objects, GameObject.TYPES, tableModelGameObjects, deleteObjectColumn);
		JFrameUtils.updateTable(tableGameObjectInstances, scrollPaneGameObjectInstances, gi.getObjectInstanceList(), ObjectInstance.TYPES, tableModelGameObjectInstances, resetObjectInstanceColumn, deleteObjectInstanceColumn);
		JFrameUtils.updateTable(tableImages, scrollPaneImages, imageArray=gi.game.images.entrySet().toArray(new Entry[gi.game.images.size()]), IMAGE_TYPES, tableModelImages, deleteImageColumn);
		JFrameUtils.updateTable(tablePlayer, scrollPaneImages, gi.getPlayerList(), Player.TYPES, tableModelPlayer, deletePlayerColumn, repairPlayerColumn);
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
			int row = event.getRow();
			ButtonColumn button = event.getButton();
			if (tableSource== tableModelGameObjectInstances)
			{
				
				if (button == resetObjectInstanceColumn)
				{
					ObjectInstance oi = gi.getObjectInstanceByIndex(row);
					ObjectState state = oi.state.copy();
					state.reset();
					gi.update(new GameObjectInstanceEditAction(id, player, oi, state));
				}
				else if (button == deleteObjectInstanceColumn)
				{
					gi.remove(id, gi.getObjectInstanceByIndex(row));				
				}
				else
				{
					throw new RuntimeException("Unknown Button");
				}
			}
			else if (tableSource == tableModelImages)
			{
				Set<Entry<String, BufferedImage>> entrySet = gi.game.images.entrySet();
				@SuppressWarnings("unchecked")
				Entry<String, BufferedImage> entry = entrySet.toArray(new Entry[entrySet.size()])[row];
				gi.game.images.remove(entry.getKey(), entry.getValue());
			}
			else if (tableSource == tableModelGameObjects)
			{
				gi.remove(id, gi.game.getObjectByIndex(row));
			}
			else if (tableSource == tableModelPlayer)
			{
				if (button == deletePlayerColumn)
				{
					gi.remove(id, gi.getPlayerByIndex(row));
				}
				else if (button == repairPlayerColumn)
				{
					gi.repairPlayerConsistency(gi.getPlayerByIndex(row).id, player, new ArrayList<>());
				}
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
		if (action instanceof GameObjectInstanceEditAction || action instanceof GameStructureEditAction)
		{
			JFrameUtils.runByDispatcher(this);
		}
	}
	
	public class ObjectEditPanel extends JPanel implements DocumentListener, ItemListener, LanguageChangeListener{
		/**
		 * 
		 */
		private static final long serialVersionUID = -1555265079401333395L;
		private final JLabel labelName = new JLabel();
		private final JTextField textFieldName = new JTextField();
		private final JLabel labelWidth = new JLabel();
		private final JTextField textFieldWidth = new JTextField();
		private final JLabel labelHeight = new JLabel();
		private final JTextField textFieldHeight = new JTextField();
		private final GameObject go;
		private final GameInstance gi;
		boolean updating = false;
		private final ArrayList<JComboBox> imageComboBoxes = new ArrayList<>();
		private final JComboBox<String> comboBoxFrontImage;
		private final JComboBox<String> comboBoxBackImage;
		private final LanguageHandler lh;
		
		private void updateImages()
		{
			String imageNames[] = gi.game.images.keySet().toArray(new String[gi.game.images.size()]);
			for (int i = 0; i < imageComboBoxes.size(); ++i)
			{
				JFrameUtils.updateComboBox(imageComboBoxes.get(i), imageNames);
			}
		}
		
		public ObjectEditPanel(GameObject go, GameInstance gi, LanguageHandler lh)
		{
			this.gi = gi;
			this.lh = lh;
			//GroupLayout layout = new GroupLayout(this);
			//setLayout(layout);YES_NO_OPTION
			setLayout(JFrameUtils.DOUBLE_COLUMN_LAUYOUT);
			this.go = go;
			textFieldName.setText(go.uniqueName);
			textFieldWidth.setText(Integer.toString(go.widthInMM));
			textFieldHeight.setText(Integer.toString(go.heightInMM));
			textFieldWidth.getDocument().addDocumentListener(this);
			textFieldHeight.getDocument().addDocumentListener(this);
			
			add(labelName);
			add(textFieldName);
			add(labelWidth);
			add(textFieldWidth);
			add(labelHeight);
			add(textFieldHeight);
			
			/*layout.setHorizontalGroup(
 					layout.createSequentialGroup()
 					.addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(labelWidth).addComponent(labelHeight))
 					.addGroup(layout.createParallelGroup().addComponent(textFieldName).addComponent(textFieldWidth).addComponent(textFieldHeight)));
 			layout.setVerticalGroup(
 					layout.createSequentialGroup()
 					.addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(textFieldName))
 					.addGroup(layout.createParallelGroup().addComponent(labelWidth).addComponent(textFieldWidth))
 					.addGroup(layout.createParallelGroup().addComponent(labelHeight).addComponent(textFieldHeight)));*/
 			
			if(go instanceof GameObjectToken)
			{
				JLabel labelFrontImage = new JLabel("Front Image");
				JLabel labelBackImage = new JLabel("Back Image");
				comboBoxFrontImage = new JComboBox<String>();
				comboBoxBackImage = new JComboBox<String>();
				imageComboBoxes.add(comboBoxFrontImage);
				imageComboBoxes.add(comboBoxBackImage);
				updateImages();
				GameObjectToken token = (GameObjectToken)go;
				comboBoxFrontImage.setSelectedItem(gi.game.getImageKey(token.getUpsideLook()));
				comboBoxBackImage.setSelectedItem(gi.game.getImageKey(token.getDownsideLook()));
				comboBoxFrontImage.addItemListener(this);
				add(labelFrontImage);
				add(comboBoxFrontImage);
				add(labelBackImage);
				add(comboBoxBackImage);
			}
			else
			{
				comboBoxFrontImage = null;
				comboBoxBackImage = null;
			}
			languageChanged(lh.getCurrentLanguage());
		}
		@Override
		public void changedUpdate(DocumentEvent e) {
			Document source = e.getDocument();
			if (updating)
			{
				return;
			}
			if (source == textFieldWidth.getDocument())
			{
				go.widthInMM = Integer.parseInt(textFieldWidth.getText());
			}
			else if (source == textFieldHeight.getDocument())
			{
				go.heightInMM = Integer.parseInt(textFieldHeight.getText());
			}
			isUpdating = true;
			gi.update(new GameObjectEditAction(id, go.uniqueName));
			isUpdating = false;
		}
		@Override
		public void insertUpdate(DocumentEvent e) {
			changedUpdate(e);
		}
		@Override
		public void removeUpdate(DocumentEvent e) {
			changedUpdate(e);
		}

		@Override
		public void itemStateChanged(ItemEvent event) {
			Object source = event.getSource();
			if (go instanceof GameObjectToken)
			{
				GameObjectToken got = (GameObjectToken)go;
				if (source == comboBoxFrontImage)
				{
					got.setUpsideLook((String)comboBoxFrontImage.getSelectedItem());
				}
				if (source == comboBoxFrontImage)
				{
					got.setDownsideLook((String)comboBoxBackImage.getSelectedItem());
				}
				isUpdating = true;
				gi.update(new GameObjectEditAction(id, got.uniqueName));
				isUpdating = false;
			}
		}

		@Override
		public void languageChanged(Language language) {
			labelName.setText(language.getString(Words.name));
			labelWidth.setText(language.getString(Words.width));
			labelHeight.setText(language.getString(Words.height));
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {}

	@Override
	public void mouseEntered(MouseEvent arg0) {}

	@Override
	public void mouseExited(MouseEvent arg0) {}

	@Override
	public void mousePressed(MouseEvent mouseEvent) {
		Object source = mouseEvent.getSource();
		if (source == tableGameObjects)
		{
	        JTable table =(JTable) source;
	        Point point = mouseEvent.getPoint();
	        int row = table.rowAtPoint(point);
	        if (mouseEvent.getClickCount() == 2 && table.getSelectedRow() != -1) {
	            JFrame frame = new JFrame();
	            frame.setLayout(JFrameUtils.SINGLE_COLUMN_LAYOUT);
	            frame.add(new ObjectEditPanel(gi.game.objects.get(row), gi, lh));
	            frame.setSize(300,300);
	            frame.setVisible(true);
	        }
		}
    }

	@Override
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void tableChanged(TableModelEvent event) {
    	if (!EventQueue.isDispatchThread())
    	{
    		throw new RuntimeException("Game-Panel changes only allowed by dispatchment thread");
    	}
		if (isUpdating)
		{
			return;
		}
		int rowBegin = event.getFirstRow();
    	if (rowBegin == TableModelEvent.HEADER_ROW)
    	{
    		return;
    	}
    	isUpdating = true;
		Object source = event.getSource();
		int colBegin = event.getColumn() == TableModelEvent.ALL_COLUMNS ? 0 : event.getColumn();
    	int rowEnd = event.getLastRow() + 1;
    	TableModel model = (TableModel)source;
    	int colEnd = event.getColumn() == TableModelEvent.ALL_COLUMNS ? model.getColumnCount() : (event.getColumn() + 1);
		if (source == tableModelPlayer)
		{
			for (int col = colBegin; col < colEnd; ++col)
			{
				for (int row = rowBegin; row < rowEnd; ++row)
				{
					if (Player.TYPES.get(col) == PlayerColumnType.NAME)
					{
						Player pl = gi.getPlayerByIndex(row);
						pl.setName((String)tableModelPlayer.getValueAt(row, col));
						gi.update(new GamePlayerEditAction(id, pl, pl));
					}
				}
			}
		}
		else if (source == tableModelImages)
		{
			for (int col = colBegin; col < colEnd; ++col)
			{
				for (int row = rowBegin; row < rowEnd; ++row)
				{
					if (ImageColumnType.get(col) == ImageColumnType.ID)
					{
						gi.game.images.remove(imageArray[row].getKey());
						gi.game.images.put((String)tableModelPlayer.getValueAt(row, col), imageArray[row].getValue());
						//TODO update game
					}
				}
			}
		}
		else if (source == tableModelGameObjectInstances)
		{
			for (int col = colBegin; col < colEnd; ++col)
			{
				GameObjectInstanceColumnType type = GameObjectInstanceColumnType.get(col);
				for (int row = rowBegin; row < rowEnd; ++row)
				{
					ObjectInstance instance = gi.getObjectInstanceByIndex(row);
					if (type == GameObjectInstanceColumnType.NAME)
					{
						instance.go.uniqueName = tableModelGameObjectInstances.getValueAt(row, col).toString();
						gi.update(new GameObjectEditAction(-1, instance.go));
					}
					else
					{
						ObjectState state = instance.state.copy();
						switch (type) {
							case OWNER:state.owner_id = Integer.parseInt(tableModelGameObjectInstances.getValueAt(row, col).toString());break;
							case ABOVE:state.aboveInstanceId = Integer.parseInt(tableModelGameObjectInstances.getValueAt(row, col).toString());break;
							case BELOW:state.belowInstanceId = Integer.parseInt(tableModelGameObjectInstances.getValueAt(row, col).toString());break;
							default:break;
						}
						gi.update(new GameObjectInstanceEditAction(-1, player, instance, state));
					}
				}
			}
		}
		else if (source == tableModelGameObjects)
		{
			for (int col = colBegin; col < colEnd; ++col)
			{
				GameObjectColumnType type = GameObjectColumnType.get(col);
				for (int row = rowBegin; row < rowEnd; ++row)
				{
					GameObject go = gi.getObjectByIndex(row);
					switch (type)
					{
						case NAME: go.uniqueName = tableModelGameObjects.getValueAt(row, col).toString(); break;
						default: break;
					}
				}
			}
		}
		isUpdating = false;
	}
}
