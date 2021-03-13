/*******************************************************************************
 * Copyright (c) 2019 Paul Stahr
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 ******************************************************************************/
package util;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.swing.ButtonGroup;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import org.slf4j.Logger;

import util.jframe.table.ButtonColumn;
import util.jframe.table.ColumnTypes;
import util.jframe.table.TableColumnType;
import util.jframe.table.ValueColumnTypes;

public class JFrameUtils{
	public static final LayoutManager SINGLE_COLUMN_LAYOUT = new GridLayout(0, 1);
	public static final LayoutManager SINGLE_ROW_LAYOUT = new GridLayout(1, 0);
	public static final LayoutManager DOUBLE_COLUMN_LAUYOUT = new GridLayout(0, 2);
	public static final LayoutManager DOUBLE_ROW_LAUYOUT = new GridLayout(2, 0);
	public static final LayoutManager LEFT_FLOW_LAYOUT = new FlowLayout(FlowLayout.LEFT);

	public static final void runByDispatcher(Runnable runnable){
		if (EventQueue.isDispatchThread())
			runnable.run();
		else
			EventQueue.invokeLater(runnable);
	}
	
	public static final boolean contains(Container c, Component o)
	{
		for (int i = 0; i < c.getComponentCount(); ++i)
		{
			if (c.getComponent(i) == o)
			{
				return true;
			}
		}
		return false;
	}
	
	public static final void runByDispatcherAndWait(Runnable runnable){
		if (EventQueue.isDispatchThread()){
			runnable.run();
		}else{
    		try {
				EventQueue.invokeAndWait(runnable);
			}catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {}
		}
	}
	
	public static final Object runSupplierByDispatcherAndWait(final Supplier<?> sup){
		if (EventQueue.isDispatchThread()){
			return sup.get();
		}
		try {
			RunnableSupplier<Object> runnable = new RunnableSupplier<Object>() {
				@Override
				public void run()
				{
					set(sup.get());
				}
			};
			EventQueue.invokeAndWait(runnable);
			return runnable.get();
		}catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {}
		return null;
	}
	
	public static final ActionListener selectFileButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			JButton source = (JButton)arg0.getSource();
			JFileChooser fileChooser= new JFileChooser();
			//TODO: fileChooser.setAccessory(new RecentFileList(fileChooser, DataHandler.getRecentFiles()));
			
			fileChooser.setCurrentDirectory(new File(source.getText()));
	        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
	        {
	        	String filePath = fileChooser.getSelectedFile().getAbsolutePath();
	        	//TODO add recent File
	        	source.setText(filePath);
	        }
	   	}
	};
	
	public static final ActionListener selectFolderButtonActionListener = new ActionListener() {
		@Override
		public void actionPerformed(ActionEvent arg0) {
			JButton source = (JButton)arg0.getSource();
			JFileChooser fileChooser= new JFileChooser();
			fileChooser.setCurrentDirectory(new File(source.getText()));
			fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	        if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION)
	        {
	        	source.setText(fileChooser.getSelectedFile().getAbsolutePath());
	        }
	   	}
	};
	
	public static abstract class DocumentChangeListener implements DocumentListener{
		public abstract void update(DocumentEvent de);
		
		
		@Override
		public final void changedUpdate(DocumentEvent de) {
			update(de);
		}

		
		@Override
		public final void insertUpdate(DocumentEvent de) {
			update(de);
		}

		
		@Override
		public final void removeUpdate(DocumentEvent de) {
			update(de);
		}		
	}
		
	public static class OpenWindowListener implements ActionListener{
		final JFrame frame;
		public OpenWindowListener(JFrame frame){
			this.frame = frame;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			frame.setVisible(true);
		}
	}

	public static class CloseWindowListener implements ActionListener{
		private final JFrame frame;
		public CloseWindowListener(JFrame frame){
			this.frame = frame;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			frame.dispose();
		}
	}

	public static final ActionListener closeParentWindowListener = new ActionListener(){
		@Override
		public void actionPerformed(ActionEvent e) {
			Object source = e.getSource();
			if (source instanceof JComponent)
			{
				while (!(source instanceof JFrame))
				{
					source = ((JComponent)source).getParent();
					if (source == null)
					{
						return;
					}
				}
				((JFrame)source).dispose();
			}
		}
	};

	public static class PropertyChangeUpdateCompTreeListener implements PropertyChangeListener{
		private final JFrame frame;
		private final boolean pack;
		public PropertyChangeUpdateCompTreeListener(JFrame frame, boolean pack){
			this.frame = frame;
			this.pack = pack;
		}
		
		
		@Override
		public void propertyChange(PropertyChangeEvent arg0) {
			SwingUtilities.updateComponentTreeUI(frame);
			if (pack)
				frame.pack();
		}
	}
	
	public static final PropertyChangeListener sourceRedrawMenuItemPropertyListener = new RedrawMenuItemPropertyListener(null);
	
	public static final class RedrawMenuItemPropertyListener implements PropertyChangeListener {
		private final JComponent menu;
		
		public RedrawMenuItemPropertyListener(JComponent menu){
			this.menu = menu;
		}
		
		@Override
		public final void propertyChange(PropertyChangeEvent arg0) {
			if (arg0.getPropertyName().equals("ancestor"))
			{	
				if (menu == null)
				{
					Object source = arg0.getSource();
					if (source instanceof JComponent)
					{
						((JComponent)source).revalidate();
					}
				}
				else
				{
					menu.revalidate();
				}
			}
		}
	}

	public static int getFirstSelected(JCheckBoxMenuItem[] parametrizations) {
		for (int i = 0; i < parametrizations.length; ++i)
		{
			if (parametrizations[i].isSelected())
			{
				return i;
			}
		}
		return -1;
	}

	public static void addItemListener(JCheckBoxMenuItem[] parametrizations, ItemListener listener) {
		for (int i = 0; i < parametrizations.length; ++i)
		{
			parametrizations[i].addItemListener(listener);
		}
	}

	public static void add(JComponent comp, JComponent[] items) {
		for (int i = 0; i < items.length; ++i) 
		{
			comp.add(items[i]);
		}
	}

	public static void add(ButtonGroup group, JCheckBoxMenuItem[] comp) {
		for (int i = 0; i < comp.length; ++i) 
		{
			group.add(comp[i]);
		}
	}
	
	private static class ShowMessageClass implements Runnable
	{
		private final String message;

		public ShowMessageClass(String message)
		{
			this.message = message;
		}
		
		@Override
		public void run() {
			JOptionPane.showMessageDialog(null, message);
		}
		
	}

	public static void showInfoMessage(String info, Logger logger){
		logger.info(info);
		JFrameUtils.runByDispatcher(new ShowMessageClass(info));
	}
	
	public static void logErrorAndShow(String error, Exception ex, Logger logger) {
		if (ex == null)
		{
			logger.error(error);
			JFrameUtils.runByDispatcher(new ShowMessageClass(error));
		}
		else
		{
			logger.error(error, ex);
			JFrameUtils.runByDispatcher(new ShowMessageClass(error + ' ' + ex.toString()));
		}
	}

	private static final DefaultCellEditor checkBoxCellEditor = new DefaultCellEditor(new JCheckBox()); 


    public static final void updateTable(JTable table, JScrollPane scrollPane, Object objectList[], List<TableColumnType> types, DefaultTableModel tm, Function<TableColumnType, String[]> possibleValueOverride, ButtonColumn ...buttonColumn)
    {
 		Object[][] rowData = new Object[objectList.length][types.size()];
     	for (int i = 0; i < rowData.length; ++i)
     	{
     		Object obj = objectList[i];
     		for (int j = 0; j < types.size();++j)
     		{
     			rowData[i][j] = JFrameUtils.toTableEntry(types.get(j).getValue(obj));
     		}
     	}
     	JFrameUtils.updateTable(table, scrollPane, rowData, ColumnTypes.getColumnNames(types), types, tm, possibleValueOverride, buttonColumn);
 	}
	
    public static final void updateTable(
            JTable table,
            JScrollPane scrollPane,
            List<? extends Object> objectList,
            List<TableColumnType> types,
            DefaultTableModel tm,
            Function<TableColumnType, String[]> possibleValueOverride,
            ButtonColumn ...buttonColumn)
    {
 		Object[][] rowData = new Object[objectList.size()][types.size()];
     	for (int i = 0; i < rowData.length; ++i)
     	{
     		Object obj = objectList.get(i);
     		for (int j = 0; j < types.size();++j)
     		{
     			rowData[i][j] = JFrameUtils.toTableEntry(types.get(j).getValue(obj));
     		}
     	}
     	JFrameUtils.updateTable(table, scrollPane, rowData, ColumnTypes.getColumnNames(types), types, tm, possibleValueOverride, buttonColumn);
 	}
	
    public static final void updateTable(
            JTable table,
            JScrollPane scrollPane,
            Object[][] rowData,
            String names[],
            List<? extends TableColumnType> types,
            DefaultTableModel tm,
            Function<TableColumnType, String[]> possibleValueOverride,
            ButtonColumn ...buttonColumn)
    {
    	tm.setDataVector(rowData,names);
    	for (int i = 0; i < types.size(); ++i)
		{
			TableColumnType current = types.get(i); 
			TableColumn column = table.getColumnModel().getColumn(i);
		  	if (current.getOptionType() == ValueColumnTypes.TYPE_COMBOBOX)
		 	{
		  	    String possibleValues[] = null;
		  	    if (possibleValueOverride != null){possibleValues = possibleValueOverride.apply(current);}
		  	    if (possibleValues == null){possibleValues = current.getPossibleValues();}
		     	JComboBox<String> comboBox = new JComboBox<String>(possibleValues);
		     	column.setCellEditor(new DefaultCellEditor(comboBox));
		 	}else if (current.getOptionType() == ValueColumnTypes.TYPE_CHECKBOX)
		 	{
		 		column.setCellEditor(checkBoxCellEditor);
		 	}
		}

		for (ButtonColumn bc : buttonColumn)
		{
			bc.setTable(table);
		}
		Dimension dim = table.getPreferredSize();
		scrollPane.setPreferredSize(new Dimension(dim.width, dim.height + table.getTableHeader().getPreferredSize().height + 8));
    }

	public static Object toTableEntry(Object value) {
		if (value instanceof Boolean) {return value;}
		else if (value == null)       {return null;}
		else                          {return String.valueOf(value);}
	}

	public static <E>void updateComboBox(JComboBox<E> sendTo, E[] sendToNames) {
		Object selected = sendTo.getSelectedItem();
		sendTo.setModel(new DefaultComboBoxModel<E>(sendToNames));
		sendTo.setSelectedItem(selected);
	}
    

}
