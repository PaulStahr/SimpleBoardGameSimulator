package gui;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JSplitPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gameObjects.instance.GameInstance;
import io.GameIO;
import main.Player;
import util.JFrameUtils;

public class GameWindow extends JFrame implements ActionListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1441104795154034811L;
	GameInstance gi;
	JSplitPane slider;
	public GamePanel gamePanel;
	public IngameChatPanel chatPanel;
	public final JMenuItem menuItemExit = new JMenuItem("Exit");
	private final JMenuItem menuItemEditGame = new JMenuItem("Edit");
	private final JMenuItem menuItemSaveGame = new JMenuItem("Save");
	
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private static final Logger logger = LoggerFactory.getLogger(GameWindow.class);
	
	public GameWindow(GameInstance gi)
	{
		this(gi, null);
	}
	
	public GameWindow(GameInstance gi, Player player)
	{
		this.gi = gi;
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu("File");
		menuBar.add(menuFile);
		setJMenuBar(menuBar);
		menuItemExit.addActionListener(this);
		menuItemEditGame.addActionListener(this);
		menuItemSaveGame.addActionListener(this);
		menuFile.add(menuItemExit);
		menuFile.add(menuItemEditGame);
		menuFile.add(menuItemSaveGame);
		gi.addPlayer(player);
		gamePanel = new GamePanel(gi);
		gamePanel.player = player;
		chatPanel = new IngameChatPanel(gi, player);
		slider = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,gamePanel, chatPanel);
		slider.setOneTouchExpandable(true);
		slider.setResizeWeight(1); // the chat panel will not be resized when resizing the window
		this.add(slider);

		setLayout(new GridLayout(1, 1));
		setSize((int) screenSize.getWidth(), (int) screenSize.getHeight());
		slider.setDividerLocation(0.5);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
		Object source = arg0.getSource();
		if(source == menuItemExit)
		{
			this.dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
		}
		else if (source == menuItemEditGame )
		{
			new EditGameWindow(gi).setVisible(true);
		}
		else if (source == menuItemSaveGame)
		{
			JFileChooser fileChooser = new JFileChooser();
			if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
			{
				try {
					FileOutputStream out = new FileOutputStream(fileChooser.getSelectedFile());
					GameIO.writeSnapshotToZip(gi, out);
					out.close();
				} catch (IOException e) {
					JFrameUtils.logErrorAndShow("Couldn't save game", e, logger );
				}
			}
		}
	}
	
}
