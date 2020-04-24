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
package data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.JFrameUtils;
import util.StringUtils;
import util.data.UniqueObjects;
import util.io.IOUtil;
import util.io.StreamUtil;

/**
 * Write a description of class DataHandler here.
 * 
 * @author  Paul Stahr
 * @version 04.02.2012
 */
public abstract class DataHandler
{
    public static final UIManager.LookAndFeelInfo lookAndFeelInfo[] = UIManager.getInstalledLookAndFeels();
	private static final Logger logger = LoggerFactory.getLogger(DataHandler.class);
    private static final String resourceFolder = "/resources/";
    private static final ArrayList<WeakReference<JFrame> > updateUIList = new ArrayList<WeakReference<JFrame>>();
    
    public static volatile int openWindows = 0;

	private static String lookAndFeel = "";
    private static final Runnable updateLookAndFeel = new Runnable(){
		@Override
		public void run() {
    		try{
   				UIManager.setLookAndFeel(lookAndFeel);
  			}catch (UnsupportedLookAndFeelException e) {
  				logger.error("UIManager not supported");
   			}catch (ClassNotFoundException e) {
   				logger.error("UIManager not found");
   			}catch (InstantiationException e) {
   				logger.error("Can't set UIManager:", e);
   			}catch (IllegalAccessException e) {
   				logger.error("Can't set UIManager:", e);
   			}      			
		}        			
	};
	
	
	public static void loadLib(String file) throws IOException {
    	InputStream in = DataHandler.getResourceAsStream(file);
    	String name = file.substring(Math.max(0,file.lastIndexOf('/')));
        File fileOut = new File(System.getProperty("java.io.tmpdir") + 	'/' + name);
        logger.info("Writing dll to: " + fileOut.getAbsolutePath());
        OutputStream out = new FileOutputStream(fileOut);
        IOUtil.copy(in, out);
        in.close();
        out.close();
        System.load(fileOut.toString());
	}
	
	static{
        UIManager.addPropertyChangeListener(new PropertyChangeListener() {
    		@Override
    		public void propertyChange(PropertyChangeEvent evt) {
    			for (int i=updateUIList.size()-1;i>=0;--i){
    				try{
    					JFrame frame = updateUIList.get(i).get();
    					if (frame == null){
    						updateUIList.remove(i);
    					}else{
    						SwingUtilities.updateComponentTreeUI(frame);
    					}
    				}catch(Exception e){
    					logger.error("Error at updating Component tree", e);
    				}
    			}
    		}
    	});
              
	}    

    private DataHandler(){}
    
    public static final URL getResource(String resource)
    {
    	return DataHandler.class.getResource(DataHandler.getResourceFolder().concat(resource));
    }
    
    public static final InputStream getResourceAsStream(String file)
    {
    	return DataHandler.class.getResourceAsStream(getResourceFolder().concat(file));
    }
    
    public static final String getResourceAsString(String file) throws IOException
    {
		InputStream stream = DataHandler.getResourceAsStream(file);
		String res = StreamUtil.readStreamToString(stream);
		stream.close();
		return res;
    }

    public static final String getResourceFolder(){
    	return resourceFolder;
    }
    
    public static final void setLookAndFeel(final Object lafi){
    	if (lafi instanceof LookAndFeelInfo){
    		if (UIManager.getLookAndFeel().getName().equals(((LookAndFeelInfo)lafi).getName()))
    			return;
    		setLookAndFeel(((LookAndFeelInfo)lafi).getClassName());
    	}else if (lafi instanceof String){
    		setLookAndFeel((String)lafi);
    	}else{
    		throw new IllegalArgumentException();
    	}
    }
    
    public static final void setLookAndFeel(String laf){
    	if (lookAndFeel.equals(laf))
    		return;
    	lookAndFeel = laf;
    	JFrameUtils.runByDispatcher(updateLookAndFeel);
    }
    
    public static final List<String> getRecentFiles(ArrayList<String> list){
      	String value = Options.getString("recent_files");
      	if (value.length() <= 2 || value.charAt(0) != '{' || value.charAt(value.length()-1) != '}')
      		return list;
      	if (list == null)
      		list = new ArrayList<String>();
      	StringUtils.split(value, 1, value.length() - 1, ',', false, list);
    	return list;
    	
    }
    
    public static final String[] getRecentFiles(){
    	List<String> l = getRecentFiles(null);
    	if (l == null)
    		return UniqueObjects.EMPTY_STRING_ARRAY;
    	return l.toArray(new String[l.size()]);
    }
    
    public synchronized static void addRecentFile (String file){
    	if (file == null)
    		throw new NullPointerException();
    	try{
    		List<String> recentFiles = getRecentFiles(new ArrayList<String>());
    		int index = -1;
	    	for (int i=0;i<recentFiles.size();i++){
	    		String str = recentFiles.get(i);
	    		if (file.equals(str)){
	    			index = i;
	    			break;
	    		}
	    	}
    		if (index != -1)
     			recentFiles.remove(index);
 			else if (recentFiles.size() == 20)
				recentFiles.remove(19);
     		recentFiles.add(0, file);
	    	
     		StringBuilder strB = new StringBuilder();
	        strB.append('{').append(recentFiles.get(0));
	        for (int i=1;i<recentFiles.size();i++)
	            strB.append(',').append(recentFiles.get(i));
	        strB.append('}');
	        Options.set("recent_files", strB.toString());
	        Options.triggerUpdates();
    	}catch(Exception e){
    		logger.error("Error at adding recent file: ", e);
    	}
    }

	public static void addToUpdateTree(JFrame frame) {
		updateUIList.add(new WeakReference<JFrame>(frame));
	}
}
