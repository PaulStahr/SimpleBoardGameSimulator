package gui.create;

import java.awt.Cursor;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTextField;
import javax.swing.TransferHandler;

import util.ArrayUtil;

public class ImageTransferHandling extends TransferHandler {
    /**
     * 
     */
    private static final long serialVersionUID = 760200676505143156L;

    private static final DataFlavor FILE_FLAVOR = DataFlavor.javaFileListFlavor;

    private JTextField textField;

    public ImageTransferHandling(JTextField panel) {
        textField = panel;
    }

    /**
     * The method to handle the transfer between the source of data and the
     * destination, which is in our case the main panel.
     */
    @Override
    public boolean importData(JComponent c, Transferable t) {
        if (canImport(c, t.getTransferDataFlavors())) {
            if (transferFlavor(t.getTransferDataFlavors(), FILE_FLAVOR)) {
                try {
                    @SuppressWarnings("unchecked")
                    List<File> fileList = (List<File>) t.getTransferData(FILE_FLAVOR);
                    if (fileList != null && fileList.toArray() instanceof File[]) {
                        File[] files = fileList.toArray(new File[fileList.size()]);
                        //mainPanel.addFiles(files);
                    }
                    return true;
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (UnsupportedFlavorException ex) {
                    ex.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * Returns the type of transfer actions to be supported.
     */
    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    /**
     * Specifies the actions to be performed after the data has been exported.
     */
    @Override
    protected void exportDone(JComponent c, Transferable data, int action) {
        c.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
    }

    /**
     * Returns true if the specified flavor is contained in the flavors array,
     * false otherwise.
     */
    private boolean transferFlavor(DataFlavor[] flavors, DataFlavor flavor) {
        return ArrayUtil.linearSearchEqual(flavors, flavor) != -1;
    }

    /**
     * Returns true if the component can import the specified flavours, false
     * otherwise.
     */
    @Override
    public boolean canImport(JComponent c, DataFlavor[] flavors) {
        return ArrayUtil.linearSearchEqual(flavors, c) != -1;
    }
}
