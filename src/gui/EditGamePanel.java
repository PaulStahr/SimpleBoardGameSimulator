package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import gameObjects.GameAction;
import gameObjects.GameObjectColumnType;
import gameObjects.GameObjectInstanceColumnType;
import gameObjects.GameObjectInstanceEditAction;
import gameObjects.ImageColumnType;
import gameObjects.definition.GameObject;
import gameObjects.instance.GameInstance;
import gameObjects.instance.GameInstance.GameChangeListener;
import gameObjects.instance.ObjectInstance;
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
	private final JTable tableGameObjectInstances = new JTable(tableModelGameObjectInstances);
	private final JTable tableGameObjects = new JTable(tableModelGameObjects);
	private final JTable tableImages = new JTable(tableModelImages);
	public String name;
	private final JScrollPane scrollPaneGameObjectInstances = new JScrollPane(tableGameObjectInstances);
	private final JScrollPane scrollPaneGameObjects = new JScrollPane(tableGameObjects);
	private final JScrollPane scrollPaneImages = new JScrollPane(tableImages);
	private final JTabbedPane tabPane = new JTabbedPane();
	public EditGamePanel(GameInstance gi) {
		this.gi = gi;
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(tabPane));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(tabPane));
		tabPane.addTab("GameObjects", scrollPaneGameObjects);
		tabPane.addTab("GameObjectInstances", scrollPaneGameObjectInstances);
		tabPane.addTab("Images", scrollPaneImages);
		updateTables();
		gi.changeListener.add(this);
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
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9089357847164495823L;

	private void updateTables()
	{
		JFrameUtils.updateTable(tableGameObjects, scrollPaneGameObjects, gi.game.objects, GameObject.TYPES, tableModelGameObjects, deleteObjectColumn);
		JFrameUtils.updateTable(tableGameObjectInstances, scrollPaneGameObjectInstances, gi.objects, ObjectInstance.TYPES, tableModelGameObjectInstances, deleteObjectInstanceColumn);
		JFrameUtils.updateTable(tableImages, scrollPaneImages, gi.game.images.entrySet().toArray(), IMAGE_TYPES, tableModelImages, deleteImageColumn);
	}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae instanceof ButtonColumn.TableButtonActionEvent)
		{
			ButtonColumn.TableButtonActionEvent event = (ButtonColumn.TableButtonActionEvent)ae;
			Object tableSource = event.getSource();
			int col = event.getCol();
			int row = event.getRow();
			if (tableSource== tableModelGameObjectInstances)
			{
				gi.remove(gi.objects.get(row));
			}
			else if (tableSource == tableModelImages)
			{
				Entry<String, BufferedImage> entry = (Entry<String, BufferedImage>)gi.game.images.entrySet().toArray()[row];
				gi.game.images.remove(entry.getKey(), entry.getValue());
			}
			else if (tableSource == tableModelGameObjectInstances)
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
