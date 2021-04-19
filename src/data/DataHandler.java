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

import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.HeapSceduler;
import util.StringUtils;
import util.ThreadPool;
import util.TimedUpdater;
import util.data.UniqueObjects;
import util.io.StreamUtil;

/**
 * Write a description of class DataHandler here.
 * 
 * @author  Paul Stahr
 * @version 04.02.2012
 */
public abstract class DataHandler
{
	private static final Logger logger = LoggerFactory.getLogger(DataHandler.class);
    private static final String resourceFolder = "/resources/";
    public static final TimedUpdater timedUpdater = new TimedUpdater(10);
    public static final ThreadPool tp = new ThreadPool();
	public static final HeapSceduler hs = new HeapSceduler();
	private static final ArrayList<BooleanSupplier> exitProgramPredicates = new ArrayList<>();
	public static final WindowListener closeProgramWindowListener = new WindowListener() {
        
	    @Override
	    public void windowActivated(WindowEvent arg0) {}

	    @Override
	    public void windowClosed(WindowEvent e) {DataHandler.closeWindow(e.getWindow());}

	    @Override
	    public void windowClosing(WindowEvent e) {}

	    @Override
	    public void windowDeactivated(WindowEvent e) {}

	    @Override
	    public void windowDeiconified(WindowEvent e) {}

	    @Override
	    public void windowIconified(WindowEvent e) {}

	    @Override
	    public void windowOpened(WindowEvent e) {}
    };
    private static Runnable exitRunnable = new Runnable() {
        @Override
        public void run() {
            System.exit(0);
        }
    };

	public static void closeWindow(Window window) {
	    java.awt.Window win[] = java.awt.Window.getWindows();
	    for (int i = 0; i < win.length; ++i)
	    {
	        if (win[i].isVisible()) {return;}
	    }
	    for (int i = 0; i < exitProgramPredicates.size(); ++i)
	    {
	        if (exitProgramPredicates.get(i).getAsBoolean()){return;}
	    }
	    exitRunnable.run();
	}

	public static void addKeepProgramPredicate(BooleanSupplier pred) {
	    exitProgramPredicates.add(pred);
    }

	static {
	    hs.start();
	}

	public static void loadLib(String file) throws IOException {
    	InputStream in = DataHandler.getResourceAsStream(file);
    	String name = file.substring(Math.max(0,file.lastIndexOf('/')));
        File fileOut = new File(System.getProperty("java.io.tmpdir") + 	'/' + name);
        logger.info("Writing dll to: " + fileOut.getAbsolutePath());
        OutputStream out = new FileOutputStream(fileOut);
        StreamUtil.copy(in, out);
        in.close();
        out.close();
        System.load(fileOut.toString());
	}
	
    private DataHandler(){}
    
    public static final URL getResource(String resource){return DataHandler.class.getResource(DataHandler.getResourceFolder().concat(resource));}
    
    public static final InputStream getResourceAsStream(String file)
    {
    	if (logger.isDebugEnabled()) {logger.debug("Read File: " + getResourceFolder().concat(file));}
    	return DataHandler.class.getResourceAsStream(getResourceFolder().concat(file));
    }
    
    public static final String getResourceAsString(String file) throws IOException
    {
		InputStream stream = DataHandler.getResourceAsStream(file);
		String res = StreamUtil.readStreamToString(stream);
		stream.close();
		return res;
    }

    public static final String getResourceFolder(){return resourceFolder;}
    
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

    public static Runnable swapExitRunnable(Runnable runnable) {
        Runnable res = exitRunnable ;
        exitRunnable = runnable;
        return res;
    }
}
