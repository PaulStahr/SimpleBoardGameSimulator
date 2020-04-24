package util.jframe;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;

import util.JFrameUtils;

public class RecentFileList extends JPanel  implements ListSelectionListener{
	private static final long serialVersionUID = 5635823612008442053L;

	public static class FileListCellRenderer extends DefaultListCellRenderer{
		private static final long serialVersionUID = -3425324114285329122L;

		@Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            if (value instanceof File) {
                File file = (File) value;
                Icon ico = FileSystemView.getFileSystemView().getSystemIcon(file);
                setIcon(ico);
                setToolTipText(file.getParent());
                setText(file.getName());
            }
            return this;
        }

    }
	
	private static final FileListCellRenderer flcr = new FileListCellRenderer();
    private final FileListModel listModel = new FileListModel();;
    private final JList<File> list = new JList<>(listModel);
    private final JFileChooser fileChooser;
    
    public RecentFileList(JFileChooser chooser) {
        fileChooser = chooser;
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setCellRenderer(flcr);

        setLayout(JFrameUtils.SINGLE_COLUMN_LAYOUT);
        add(new JScrollPane(list));

        list.addListSelectionListener(this);
    }
    
    @Override
	public void valueChanged(ListSelectionEvent e) {
        if (!e.getValueIsAdjusting()) {
            fileChooser.setSelectedFile(list.getSelectedValue());
        }
    }

    public RecentFileList(JFileChooser chooser, String files[])
    {
    	this(chooser);
    	add(files);
    }

    public void clear() {
        listModel.clear();
    }

    public void add(File file) {
        listModel.add(file);
    }
    
    public void add(String files[])
    {
    	for (int i = 0; i < files.length; ++i)
    	{
    		add(new File(files[i]));
    	}
    }

    public static class FileListModel extends AbstractListModel<File> {
		private static final long serialVersionUID = -5883415822026866376L;
		private final List<File> files = new ArrayList<>();

        public FileListModel() {}

        public void add(File file) {
            if (!files.contains(file)) {
                files.add(0, file);
                fireIntervalAdded(this, 0, 0);
            }
        }

        public void clear() {
            int size = files.size() - 1;
            if (size >= 0) {
                files.clear();
                fireIntervalRemoved(this, 0, size);
            }
        }

        @Override
        public int getSize() {
            return files.size();
        }

        @Override
        public File getElementAt(int index) {
            return files.get(index);
        }
    }
}