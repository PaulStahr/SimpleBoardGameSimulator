package gui.minigames;

import javax.swing.JFrame;

import data.JFrameLookAndFeelUtil;
import util.JFrameUtils;

public class TetrisWindow extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6147856397016097419L;
	private final TetrisPanel tp = new TetrisPanel(new TetrisGameInstance());
	
	public TetrisGameInstance getGameInstance()
	{
		return tp.getGameInstance();
	}
	
	public TetrisWindow()
	{
		setLayout(JFrameUtils.SINGLE_ROW_LAYOUT);
		add(tp);
		JFrameLookAndFeelUtil.addToUpdateTree(this);
		setSize(300, 300);
		tp.start();
	}
}
