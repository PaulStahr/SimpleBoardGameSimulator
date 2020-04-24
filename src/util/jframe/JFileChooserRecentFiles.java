package util.jframe;


import java.awt.Component;
import java.io.File;

import javax.swing.JComponent;
import javax.swing.JFileChooser;

import data.DataHandler;

public class JFileChooserRecentFiles  extends JFileChooser{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5511035325463286917L;
	public JFileChooserRecentFiles()
	{
		super.setAccessory(new RecentFileList(this, DataHandler.getRecentFiles()));
	}
	
	public JFileChooserRecentFiles(String filepath) {
		super(filepath);
		super.setAccessory(new RecentFileList(this, DataHandler.getRecentFiles()));
	}

	@Override
	public void setAccessory(JComponent component)
	{
		getAccessory().add(component);
	}
	
	@Override
	public int showSaveDialog(Component parent)
	{
		int ret = super.showSaveDialog(parent);
		if(ret == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = getSelectedFile();
			String filepath = selectedFile.getAbsolutePath();
			DataHandler.addRecentFile(filepath);
		}
		return ret;
	}
	
	@Override
	public int showOpenDialog(Component parent)
	{
		int ret = super.showOpenDialog(parent);
		if(ret == JFileChooser.APPROVE_OPTION)
		{
			File selectedFile = getSelectedFile();
			String filepath = selectedFile.getAbsolutePath();
			DataHandler.addRecentFile(filepath);
		}
		return ret;
	}
}
