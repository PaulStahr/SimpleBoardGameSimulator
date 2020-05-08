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

import data.JFrameLookAndFeelUtil;
import gameObjects.instance.GameInstance;
import gui.minigames.TetrisWindow;
import io.GameIO;
import main.Player;
import util.JFrameUtils;

public class GameWindow extends JFrame implements ActionListener, LanguageChangeListener{
	/**
	 * 
	 */
	private static final long serialVersionUID = -1441104795154034811L;
	GameInstance gi;
	private final JSplitPane slider;
	public GamePanel gamePanel;
	public IngameChatPanel chatPanel;
	private final JMenuItem menuItemExit = new JMenuItem();
	private final JMenuItem menuItemEditGame = new JMenuItem();
	private final JMenuItem menuItemSaveGame = new JMenuItem();
	private final JMenuItem menuItemSettings = new JMenuItem();
	private final JMenuItem menuItemAbout = new JMenuItem();
	private final JMenuItem menuItemControls = new JMenuItem();
	private final JMenuItem menuItemTetris = new JMenuItem();
	private final JMenu menuFile = new JMenu();
	private final JMenu menuExtras = new JMenu();
	private final JMenu menuControls = new JMenu();

	private final LanguageHandler lh;
	
	Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
	private static final Logger logger = LoggerFactory.getLogger(GameWindow.class);
	
	public GameWindow(GameInstance gi, LanguageHandler lh)
	{
		this(gi, null, lh);
	}
	
	public GameWindow(GameInstance gi, Player player, LanguageHandler lh)
	{
		this.gi = gi;
		this.lh = lh;
		JMenuBar menuBar = new JMenuBar();
		menuBar.add(menuFile);
		menuBar.add(menuExtras);
		menuBar.add(menuControls);
		setJMenuBar(menuBar);
		menuItemExit.addActionListener(this);
		menuItemEditGame.addActionListener(this);
		menuItemSaveGame.addActionListener(this);
		menuItemAbout.addActionListener(Credits.getOpenWindowListener());
		menuItemSettings.addActionListener(this);
		menuItemControls.addActionListener(this);
		menuItemTetris.addActionListener(this);
		menuFile.add(menuItemExit);
		menuFile.add(menuItemEditGame);
		menuFile.add(menuItemSaveGame);
		menuExtras.add(menuItemSettings);
		menuExtras.add(menuItemTetris);
		menuExtras.add(menuItemAbout);
		menuControls.add(menuItemControls);
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
		JFrameLookAndFeelUtil.addToUpdateTree(this);
		languageChanged(lh.getCurrentLanguage());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
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
			new EditGameWindow(gi, lh).setVisible(true);
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
		else if (source == menuItemSettings)
		{
			OptionWindow ow = new OptionWindow(lh);
			ow.setVisible(true);
		}
		else if (source == menuItemControls){
			ControlWindow cw = new ControlWindow(gi, lh);
			cw.setVisible(true);
		}
		else if (source == menuItemTetris)
		{
			TetrisWindow tw = new TetrisWindow();
			tw.setVisible(true);
		}
	}

	@Override
	public void languageChanged(Language language) {
		menuItemExit.setText(language.getString(Words.exit));
		menuItemEditGame.setText(language.getString(Words.edit));
		menuItemSaveGame.setText(language.getString(Words.save));
		menuItemSettings.setText(language.getString(Words.settings));
		menuItemAbout.setText(language.getString(Words.about));
		menuItemControls.setText(language.getString(Words.controls));
		menuItemTetris.setText(language.getString(Words.tetris));
		menuFile.setText(language.getString(Words.files));
		menuExtras.setText(language.getString(Words.extras));
		menuControls.setText(language.getString(Words.tetris));
	}
	
}
