package gui.game;

import java.awt.Color;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
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

import data.DataHandler;
import data.Texture;
import gameObjects.action.AddObjectAction;
import gameObjects.action.GameAction;
import gameObjects.action.GameObjectEditAction;
import gameObjects.action.GameObjectInstanceEditAction;
import gameObjects.action.player.PlayerAddAction;
import gameObjects.action.player.PlayerEditAction;
import gameObjects.action.player.PlayerRemoveAction;
import gameObjects.action.structure.GameStructureEditAction;
import gameObjects.action.structure.GameTextureRemoveAction;
import gameObjects.columnTypes.GameObjectBooksColumnType;
import gameObjects.columnTypes.GameObjectColumnType;
import gameObjects.columnTypes.GameObjectDicesColumnType;
import gameObjects.columnTypes.GameObjectFiguresColumnType;
import gameObjects.columnTypes.GameObjectInstanceColumnType;
import gameObjects.columnTypes.GameObjectTokenColumnType;
import gameObjects.columnTypes.ImageColumnType;
import gameObjects.columnTypes.PlayerColumnType;
import gameObjects.definition.GameObject;
import gameObjects.definition.GameObjectBook;
import gameObjects.definition.GameObjectDice;
import gameObjects.definition.GameObjectFigure;
import gameObjects.definition.GameObjectToken;
import gameObjects.functions.CheckingFunctions;
import gameObjects.functions.ObjectFunctions;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import gameObjects.instance.ObjectInstance;
import gameObjects.instance.ObjectState;
import gui.game.edit.ObjectEditPanel;
import gui.language.Language;
import gui.language.LanguageChangeListener;
import gui.language.LanguageHandler;
import gui.language.Words;
import main.Player;
import util.ArrayTools;
import util.JFrameUtils;
import util.StringUtils;
import util.jframe.table.ButtonColumn;
import util.jframe.table.TableColumnType;
import util.jframe.table.TableModel;

public class EditGamePanel extends JPanel implements ActionListener, GameChangeListener, Runnable, MouseListener, TableModelListener, LanguageChangeListener {
	public static final List<TableColumnType> IMAGE_TYPES = ArrayTools.unmodifiableList(new TableColumnType[]{ImageColumnType.ID, ImageColumnType.WIDTH, ImageColumnType.HEIGHT, ImageColumnType.DELETE});
	private final GameInstance gi;
	private final DefaultTableModel tableModelGameObjectInstances= new TableModel(ObjectInstance.TYPES);
	private final DefaultTableModel tableModelGameObjects= new TableModel(GameObject.TYPES);

	private final DefaultTableModel tableModelCards = new TableModel(GameObjectToken.TOKEN_ATTRIBUTES);
	private final DefaultTableModel tableModelFigures = new TableModel(GameObjectFigure.FIGURE_ATTRIBUTES);
	private final DefaultTableModel tableModelDices = new TableModel(GameObjectDice.DICE_ATTRIBUTES);
	private final DefaultTableModel tableModelBooks = new TableModel(GameObjectBook.BOOK_ATTRIBUTES);
	private final DefaultTableModel tableModelImages= new TableModel(IMAGE_TYPES);
	private final DefaultTableModel tableModelPlayer = new TableModel(Player.TYPES);
	private final JTable tableGameObjectInstances = new JTable(tableModelGameObjectInstances);
	private final JTable tableGameObjects = new JTable(tableModelGameObjects);
	//Tables for Objects and Images
	private final JTable tableCards = new JTable(tableModelCards);
	private final JTable tableFigures = new JTable(tableModelFigures);
	private final JTable tableDices = new JTable(tableModelDices);
	private final JTable tableBooks = new JTable(tableModelBooks);
	private final JTable tableImages = new JTable(tableModelImages);
	private final JTable tablePlayer = new JTable(tableModelPlayer);

	private final JScrollPane scrollPaneGameObjectInstances = new JScrollPane(tableGameObjectInstances);
	private final JScrollPane scrollPaneGameObjects = new JScrollPane(tableGameObjects);
	private final JScrollPane scrollPaneCards = new JScrollPane(tableCards);
	private final JScrollPane scrollPaneFigures = new JScrollPane(tableFigures);
	private final JScrollPane scrollPaneDices = new JScrollPane(tableDices);
	private final JScrollPane scrollPaneBooks = new JScrollPane(tableBooks);
	private final JScrollPane scrollPaneImages = new JScrollPane(tableImages);
	private final JScrollPane scrollPanePlayer = new JScrollPane(tablePlayer);
	private final GeneralPanel panelGeneral;
	private final JTabbedPane tabPane = new JTabbedPane();
	public int id = (int)(Math.random() * Integer.MAX_VALUE);
	private boolean isUpdating = false;
	private Texture[] imageArray;
	private final LanguageHandler lh;
	private final Player player;

	public EditGamePanel(GameInstance gi, LanguageHandler lh, Player player) {
		this.gi = gi;
		this.lh = lh;
		panelGeneral = new GeneralPanel(lh);
		this.player = player;
		Language language = lh.getCurrentLanguage();
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(tabPane));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(tabPane));
		tabPane.addTab(language.getString(Words.general), panelGeneral);
		tabPane.addTab(language.getString(Words.game_objects), scrollPaneGameObjects);
		tabPane.addTab(language.getString(Words.game_object_instances), scrollPaneGameObjectInstances);
		tabPane.addTab(language.getString(Words.cards), scrollPaneCards);
		tabPane.addTab(language.getString(Words.figures), scrollPaneFigures);
		tabPane.addTab(language.getString(Words.dices), scrollPaneDices);
		tabPane.addTab(language.getString(Words.books), scrollPaneBooks);
		tabPane.addTab(language.getString(Words.images), scrollPaneImages);
		tabPane.addTab(language.getString(Words.player), scrollPanePlayer);
        languageChanged(lh.getCurrentLanguage());
        lh.addLanguageChangeListener(this);
		tableGameObjects.addMouseListener(this);
		JFrameUtils.runByDispatcher(this);
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
		            	gi.game.images.add(new Texture(Files.readAllBytes(file.toPath()), file.getName(), StringUtils.getFileType(file.getName())));
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

		tabPane.setTitleAt(tabPane.indexOfComponent(scrollPaneCards), 				language.getString(Words.cards));
		tabPane.setTitleAt(tabPane.indexOfComponent(scrollPaneFigures), 			language.getString(Words.figures));
		tabPane.setTitleAt(tabPane.indexOfComponent(scrollPaneDices), 				language.getString(Words.dices));
		tabPane.setTitleAt(tabPane.indexOfComponent(scrollPaneBooks), 				language.getString(Words.books));

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

 	private final ButtonColumn resetCardColumn = new ButtonColumn(tableCards,tableAction, GameObjectToken.TOKEN_ATTRIBUTES.indexOf(GameObjectTokenColumnType.RESET));
	private final ButtonColumn resetFiguresColumn = new ButtonColumn(tableFigures,tableAction, GameObjectFigure.FIGURE_ATTRIBUTES.indexOf(GameObjectFiguresColumnType.RESET));
	private final ButtonColumn resetDiceColumn = new ButtonColumn(tableDices,tableAction, GameObjectDice.DICE_ATTRIBUTES.indexOf(GameObjectDicesColumnType.RESET));
	private final ButtonColumn resetBookColumn = new ButtonColumn(tableBooks,tableAction, GameObjectBook.BOOK_ATTRIBUTES.indexOf(GameObjectBooksColumnType.RESET));

 	private final ButtonColumn deleteObjectInstanceColumn = new ButtonColumn(tableGameObjectInstances,tableAction, ObjectInstance.TYPES.indexOf(GameObjectInstanceColumnType.DELETE));

 	private final ButtonColumn deleteCardColumn = new ButtonColumn(tableCards, tableAction, GameObjectToken.TOKEN_ATTRIBUTES.indexOf(GameObjectTokenColumnType.DELETE));
	private final ButtonColumn deleteFigureColumn = new ButtonColumn(tableFigures, tableAction, GameObjectFigure.FIGURE_ATTRIBUTES.indexOf(GameObjectFiguresColumnType.DELETE));
	private final ButtonColumn deleteDiceColumn = new ButtonColumn(tableDices, tableAction, GameObjectDice.DICE_ATTRIBUTES.indexOf(GameObjectDicesColumnType.DELETE));
	private final ButtonColumn deleteBookColumn = new ButtonColumn(tableBooks, tableAction, GameObjectBook.BOOK_ATTRIBUTES.indexOf(GameObjectBooksColumnType.DELETE));

 	private final ButtonColumn deleteImageColumn = new ButtonColumn(tableImages,tableAction, IMAGE_TYPES.indexOf(ImageColumnType.DELETE));
    private final ButtonColumn playerSelectColorColumn = new ButtonColumn(tablePlayer, tableAction, Player.TYPES.indexOf(PlayerColumnType.COLOR)) {
        /**
         *
         */
		private static final long serialVersionUID = -6972127406930221792L;

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            if (row < gi.getPlayerCount())
            {
                comp.setBackground(gi.getPlayerByIndex(row).color);
            }
            return comp;
        }
    };
 	private final ButtonColumn deletePlayerColumn = new ButtonColumn(tablePlayer,tableAction, Player.TYPES.indexOf(PlayerColumnType.DELETE));
	private final ButtonColumn repairPlayerColumn = new ButtonColumn(tablePlayer, tableAction, Player.TYPES.indexOf(PlayerColumnType.REPAIR)) {
	    /**
         *
         */
		private static final long serialVersionUID = 1L;
        private final ArrayList<ObjectInstance> tmp = new ArrayList<>();
        private final ArrayList<ObjectInstance> tmp2 = new ArrayList<>();

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            try {
            comp.setForeground(CheckingFunctions.checkPlayerConsistency(gi.getPlayerByIndex(row).id, tmp, tmp2, gi) == null ? Color.BLACK : Color.RED);
            }catch(Exception e) {}
            return comp;
        }
	};

 	private class GeneralPanel extends JPanel implements ItemListener, DocumentListener, LanguageChangeListener, ComponentListener
 	{
 		/**
		 *
		 */
		private static final long serialVersionUID = 3667665407550359889L;
		private final JLabel labelName = new JLabel();
 		private final JTextField textFieldName = new JTextField();
 		private final JLabel labelBackground = new JLabel();
 		private final JComboBox<String> comboBoxBackground = new JComboBox<>();
 		private final JLabel labelTableRadius = new JLabel("Table Radius");
 		private final JTextField textFieldTableRadius = new JTextField();
 		private final JLabel labelPassword = new JLabel();
 		private final JTextField textFieldPassword = new JTextField();
 		private final JLabel labelSeats = new JLabel();
 		private final JTextField textFieldSeats = new JTextField();
		//private final JButton buttonResetAll = new JButton("Reset All");

 		public GeneralPanel(LanguageHandler lh)
 		{
 			GroupLayout layout = new GroupLayout(this);
 			setLayout(layout);

 			layout.setHorizontalGroup(
 					layout.createSequentialGroup()
 					.addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(labelBackground).addComponent(labelTableRadius).addComponent(labelPassword).addComponent(labelSeats))
 					.addGroup(layout.createParallelGroup().addComponent(textFieldName).addComponent(comboBoxBackground).addComponent(textFieldTableRadius).addComponent(textFieldPassword).addComponent(textFieldSeats)));
 			layout.setVerticalGroup(
 					layout.createSequentialGroup()
 					.addGroup(layout.createParallelGroup().addComponent(labelName).addComponent(textFieldName))
 					.addGroup(layout.createParallelGroup().addComponent(labelBackground).addComponent(comboBoxBackground))
 					.addGroup(layout.createParallelGroup().addComponent(labelTableRadius).addComponent(textFieldTableRadius))
 					.addGroup(layout.createParallelGroup().addComponent(labelPassword).addComponent(textFieldPassword))
 					.addGroup(layout.createParallelGroup().addComponent(labelSeats).addComponent(textFieldSeats)));
 			textFieldName.getDocument().addDocumentListener(this);
			comboBoxBackground.addItemListener(this);
			textFieldPassword.getDocument().addDocumentListener(this);
			textFieldTableRadius.getDocument().addDocumentListener(this);
			tableModelImages.addTableModelListener(EditGamePanel.this);
			tableModelGameObjects.addTableModelListener(EditGamePanel.this);
			tableModelGameObjectInstances.addTableModelListener(EditGamePanel.this);
			tableModelCards.addTableModelListener(EditGamePanel.this);
			tableModelFigures.addTableModelListener(EditGamePanel.this);
			tableModelDices.addTableModelListener(EditGamePanel.this);
			tableModelBooks.addTableModelListener(EditGamePanel.this);
			languageChanged(lh.getCurrentLanguage());
			lh.addLanguageChangeListener(this);
			addComponentListener(this);
 		}

		public void update() {
	    	if (!EventQueue.isDispatchThread()){throw new RuntimeException("Game-Panel changes only allowed by dispatchment thread");}
			if (!isVisible() || isUpdating)  {return;}
			isUpdating = true;
			try {
    			textFieldName.setText(gi.name);
    			textFieldTableRadius.setText(Integer.toString(gi.tableRadius));
    		    JFrameUtils.updateComboBox(comboBoxBackground, gi.game.getTextureNames());
    			comboBoxBackground.setSelectedItem(gi.game.background.getId());
                textFieldPassword.setText(gi.password);
    			textFieldSeats.setText(Integer.toString(gi.seats));
			}finally {
			    isUpdating = false;
			}
		}

		@Override
		public void itemStateChanged(ItemEvent arg0) {
	    	if (!EventQueue.isDispatchThread()){throw new RuntimeException("Game-Panel changes only allowed by dispatchment thread");}
			if (isUpdating){return;}
			isUpdating = true;
			try {
    			if (arg0.getStateChange() == ItemEvent.SELECTED) {
    	            gi.game.background = gi.game.getImage((String)arg0.getItem());
                    gi.update(new GameStructureEditAction(id, GameStructureEditAction.EDIT_BACKGROUND));
    			}
			}finally {
			    isUpdating = false;
			}
		}

		public void update(DocumentEvent event)
		{
	    	if (!EventQueue.isDispatchThread()){throw new RuntimeException("Game-Panel changes only allowed by dispatchment thread");}
			if (isUpdating){return;}
            isUpdating = true;
			Document source = event.getDocument();
			if (source == textFieldName.getDocument())
			{
				gi.name = textFieldName.getText();
				gi.update(new GameStructureEditAction(id, GameStructureEditAction.EDIT_SESSION_NAME));
			}
			else if (source == textFieldTableRadius.getDocument())
			{
				if(!textFieldTableRadius.getText().equals("")) {
					gi.tableRadius = Integer.parseInt(textFieldTableRadius.getText());
					gi.update(new GameStructureEditAction(id, GameStructureEditAction.EDIT_TABLE_RADIUS));
				}
			}
			else if (source == textFieldPassword.getDocument())
			{
				gi.password = textFieldPassword.getText();
				gi.update(new GameStructureEditAction(id, GameStructureEditAction.EDIT_SESSION_PASSWORD));
			}
            isUpdating = false;
		}

		@Override
		public void changedUpdate(DocumentEvent arg0) {update(arg0);	}

		@Override
		public void insertUpdate(DocumentEvent arg0) {update(arg0);}

		@Override
		public void removeUpdate(DocumentEvent arg0) {update(arg0);}

		@Override
		public void languageChanged(Language language) {
            labelName.setText(language.getString(Words.name));
		    labelBackground.setText(language.getString(Words.background));
		    labelPassword.setText(language.getString(Words.password));
		    labelSeats.setText(language.getString(Words.seats));
		}

        @Override
        public void componentHidden(ComponentEvent e) {}

        @Override
        public void componentMoved(ComponentEvent e) {}

        @Override
        public void componentResized(ComponentEvent e) {}

        @Override
        public void componentShown(ComponentEvent e) {
            update();
        }
 	}

	/**
	 *
	 */
	private static final long serialVersionUID = 9089357847164495823L;

	private final Function<TableColumnType, String[]> comboBoxOverrides = new Function<TableColumnType, String[]>() {

        @Override
        public String[] apply(TableColumnType t) {
            if (t == GameObjectInstanceColumnType.OWNER)
            {
                int count = gi.getPlayerCount();
                String[] result = new String[count + 1];
                result[0] = "-1";
                for (int i = 0; i < count; ++i)
                {
                    result[i + 1] = String.valueOf(gi.getPlayerByIndex(i).id);
                }
                return result;
            }
            return null;
        }
    };

    @SuppressWarnings("unchecked")
    private void updateTables()
	{
    	if (!EventQueue.isDispatchThread()){throw new RuntimeException("Game-Panel changes only allowed by dispatchment thread");}
    	if (isUpdating){return;}
		isUpdating = true;
		JFrameUtils.updateTable(tableGameObjects, scrollPaneGameObjects, gi.game.getObjects(), GameObject.TYPES, tableModelGameObjects, null, deleteObjectColumn);
		JFrameUtils.updateTable(tableGameObjectInstances, scrollPaneGameObjectInstances, gi.getObjectInstanceList(), ObjectInstance.TYPES, tableModelGameObjectInstances, comboBoxOverrides, resetObjectInstanceColumn, deleteObjectInstanceColumn);
		JFrameUtils.updateTable(tableCards, scrollPaneCards, gi.getTokenList(), GameObjectToken.TOKEN_ATTRIBUTES, tableModelCards, comboBoxOverrides, resetCardColumn, deleteCardColumn);
		JFrameUtils.updateTable(tableFigures, scrollPaneFigures, gi.getFigureList(), GameObjectFigure.FIGURE_ATTRIBUTES, tableModelFigures, null, resetFiguresColumn, deleteFigureColumn);
		JFrameUtils.updateTable(tableDices, scrollPaneDices, gi.getDiceList(), GameObjectDice.DICE_ATTRIBUTES, tableModelDices, null, resetDiceColumn, deleteDiceColumn);
		JFrameUtils.updateTable(tableBooks, scrollPaneBooks, gi.getBookList(), GameObjectBook.BOOK_ATTRIBUTES, tableModelBooks, null, resetBookColumn, deleteBookColumn);
		JFrameUtils.updateTable(tableImages, scrollPaneImages, imageArray=gi.game.images.toArray(new Texture[gi.game.images.size()]), IMAGE_TYPES, tableModelImages, null, deleteImageColumn);
		JFrameUtils.updateTable(tablePlayer, scrollPaneImages, gi.getPlayerList(true), Player.TYPES, tableModelPlayer, null, playerSelectColorColumn, deletePlayerColumn, repairPlayerColumn);
		panelGeneral.update();
		isUpdating = false;
	}

	@Override
	public void actionPerformed(ActionEvent ae) {
        if (!EventQueue.isDispatchThread()){throw new RuntimeException("Game-Panel changes only allowed by dispatchment thread");}
		if (isUpdating){return;}
		isUpdating = true;
		try {
    		if (ae instanceof ButtonColumn.TableButtonActionEvent)
    		{
    			ButtonColumn.TableButtonActionEvent event = (ButtonColumn.TableButtonActionEvent)ae;
    			Object tableSource = event.getSource();
    			int row = event.getRow();
    			ButtonColumn button = event.getButton();
    			//TODO add other tabs actions
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
    				gi.update(new GameTextureRemoveAction(id, gi.game.images.get(row).getId()));
    			}
    			else if (tableSource == tableModelGameObjects)
    			{
    				gi.remove(id, gi.game.getObjectByIndex(row));
    			}
    			else if (tableSource == tableModelPlayer)
    			{
    			    if (button == playerSelectColorColumn)  {
    			        final Color c = JColorChooser.showDialog(null, "Farbe w\u00E4hlen", getBackground());
    			        if (c == null)
    			            return;
    			        Player pl = gi.getPlayerByIndex(row);
    			        pl.color = c;
    			        gi.update(new PlayerEditAction(id, player, pl));
    			     }
    			    else if (button == deletePlayerColumn)  {gi.update(new PlayerRemoveAction(id, player, gi.getPlayerByIndex(row)));}
    				else if (button == repairPlayerColumn)	{gi.repairPlayerConsistency(gi.getPlayerByIndex(row).id, player, new ArrayList<>());}
    			}
    		}
    		updateTables();
		}finally {
	        isUpdating = false;
		}
	}

	@Override
	public void run()
	{
		updateTables();
		panelGeneral.update();
	}

	private final Runnable triggerUpdateRunnable = new Runnable(){
	    @Override
        public void run() {
	        JFrameUtils.runByDispatcher(EditGamePanel.this);
	    }
	};

	@Override
	public void changeUpdate(GameAction action) {
		if (action instanceof GameObjectInstanceEditAction || action instanceof GameStructureEditAction || action instanceof PlayerAddAction || action instanceof PlayerRemoveAction)
		{
		    DataHandler.hs.enqueue(triggerUpdateRunnable, System.nanoTime() + 100000000, false);
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
	            int[] selectedRows = table.getSelectedRows();
	            JFrame frame = new JFrame();
	            frame.setLayout(JFrameUtils.SINGLE_COLUMN_LAYOUT);
	            final ArrayList<GameObject> go = new ArrayList<>();
				for (int selectedRow : selectedRows) {
					go.add(gi.game.getGameObjectByIndex(selectedRow));
				}
	            go.add(gi.game.getGameObjectByIndex(row));
	            GameObject gocp = ObjectEditPanel.reduce(go);
	            frame.add(new ObjectEditPanel(gocp, gi, lh));
	            JButton buttonOk = new JButton("Ok");
	            buttonOk.addActionListener(new ActionListener() {

                    @Override
                    public void actionPerformed(ActionEvent e) {
						for (GameObject current : go) {
							ObjectEditPanel.setValid(gocp, current);
							gi.update(new GameObjectEditAction(id, current));
						}
                    }
                });
	            frame.add(buttonOk);
	            frame.setSize(300,300);
	            frame.setVisible(true);
	        }
		}
    }

	@Override
	public void mouseReleased(MouseEvent arg0) {}

	@Override
	public void tableChanged(TableModelEvent event) {
    	if (!EventQueue.isDispatchThread()){throw new RuntimeException("Game-Panel changes only allowed by dispatchment thread");}
		if (isUpdating){return;}
		int rowBegin = event.getFirstRow();
    	if (rowBegin == TableModelEvent.HEADER_ROW){return;}
    	isUpdating = true;
    	try {
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
    						gi.update(new PlayerEditAction(id, pl, pl));
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
    						gi.game.images.remove(imageArray[row]);
    						gi.game.images.add(imageArray[row]);
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
    						instance.go.uniqueObjectName = tableModelGameObjectInstances.getValueAt(row, col).toString();
    						gi.update(new GameObjectEditAction(-1, instance.go));
    					}
    					else
    					{
    						ObjectState state = instance.state.copy();
    						switch (type) {
    							case OWNER:
    								int NewOwerId = Integer.parseInt(tableModelGameObjectInstances.getValueAt(row, col).toString());
    								if (state.owner_id != -1){
    									Player owner = gi.getPlayerById(state.owner_id);
    									ObjectFunctions.dropObject(id, gi, owner, instance);
    								}
    								if (NewOwerId != -1){
    									Player newOwner = gi.getPlayerById(NewOwerId);
    									ObjectFunctions.removeObject(id, gi, newOwner, instance);
    									//ObjectFunctions.takeObjects(id, gi, newOwner, instance);
    								}
    								break;
    							case ABOVE:
    								int NewAboveId = Integer.parseInt(tableModelGameObjectInstances.getValueAt(row, col).toString());
    								state.aboveInstanceId = NewAboveId;
    								break;
    							case BELOW:
    								int NewBelowId = Integer.parseInt(tableModelGameObjectInstances.getValueAt(row, col).toString());
    								state.belowInstanceId = NewBelowId;
    							case POSX:
    								int NewXPos = Integer.parseInt(tableModelGameObjectInstances.getValueAt(row, col).toString());
    								state.posX = NewXPos;
    								gi.update(new GameObjectInstanceEditAction(this.id, player, instance, state));
    							case POSY:
    								int NewYPos = Integer.parseInt(tableModelGameObjectInstances.getValueAt(row, col).toString());
    								state.posY = NewYPos;
    								gi.update(new GameObjectInstanceEditAction(this.id, player, instance, state));
    							break;
    							default:break;
    						}
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
    						case NAME: go.uniqueObjectName = tableModelGameObjects.getValueAt(row, col).toString(); break;
    						default: break;
    					}
    				}
    			}
    		}
    	}finally {
    	    isUpdating = false;
    	}
	}
}
