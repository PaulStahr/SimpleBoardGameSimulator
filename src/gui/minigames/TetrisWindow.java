package gui.minigames;

import javax.swing.JFrame;

import data.JFrameLookAndFeelUtil;
import util.JFrameUtils;

public class TetrisWindow extends JFrame {
	private final TetrisPanel tp = new TetrisPanel(new TetrisGameInstance());
	public TetrisWindow()
	{
		setLayout(JFrameUtils.SINGLE_ROW_LAYOUT);
		add(tp);
		JFrameLookAndFeelUtil.addToUpdateTree(this);
		setSize(300, 300);
		tp.start();
	}
}
