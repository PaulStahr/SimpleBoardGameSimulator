package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import gameObjects.GameObjectInstanceColumnType;
import gameObjects.ImageColumnType;
import gameObjects.instance.GameInstance;
import gameObjects.instance.ObjectInstance;
import util.ArrayTools;
import util.JFrameUtils;
import util.jframe.table.ButtonColumn;
import util.jframe.table.TableColumnType;
import util.jframe.table.TableModel;

public class EditGamePanel extends JPanel implements ActionListener{
	public static final List<TableColumnType> IMAGE_TYPES = ArrayTools.unmodifiableList(new TableColumnType[]{ImageColumnType.ID, ImageColumnType.WIDTH, ImageColumnType.HEIGHT, ImageColumnType.DELETE});
	GameInstance gi;
	private final DefaultTableModel tableModelGameObjects= new TableModel(ObjectInstance.TYPES);
	private final DefaultTableModel tableModelImages= new TableModel(IMAGE_TYPES);
	private final JTable tableGameObjects = new JTable(tableModelGameObjects);
	private final JTable tableImages = new JTable(tableModelImages);
	public String name;
	private final JScrollPane scrollPaneGameObjects = new JScrollPane(tableGameObjects);
	private final JScrollPane scrollPaneImages = new JScrollPane(tableImages);
	public EditGamePanel(GameInstance gi) {
		this.gi = gi;
		GroupLayout layout = new GroupLayout(this);
		setLayout(layout);
		layout.setHorizontalGroup(layout.createParallelGroup().addComponent(scrollPaneGameObjects).addComponent(scrollPaneImages));
		layout.setVerticalGroup(layout.createSequentialGroup().addComponent(scrollPaneGameObjects).addComponent(scrollPaneImages));
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
 	private final ButtonColumn deleteImageColumn = new ButtonColumn(tableImages,tableAction, IMAGE_TYPES.indexOf(ImageColumnType.DELETE));
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 9089357847164495823L;

	@Override
	public void actionPerformed(ActionEvent arg0) {
		JFrameUtils.updateTable(tableGameObjects, scrollPaneGameObjects, gi.objects, ObjectInstance.TYPES, tableModelGameObjects, deleteObjectColumn);
		JFrameUtils.updateTable(tableImages, scrollPaneImages, gi.game.images.values().toArray(), IMAGE_TYPES, tableModelImages, deleteImageColumn);
	}
	

}
