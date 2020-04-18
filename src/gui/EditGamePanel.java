package gui;

import javax.swing.JPanel;
import javax.swing.JTable;

import gameObjects.instance.GameInstance;

public class EditGamePanel extends JPanel{
	GameInstance gi;
	public EditGamePanel(GameInstance gi) {
		this.gi = gi;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 9089357847164495823L;

	public String name;
	
	JTable tableGameItems = new JTable();
	

}
