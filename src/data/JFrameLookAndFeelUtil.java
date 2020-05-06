package data;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import util.ArrayTools;
import util.JFrameUtils;

public class JFrameLookAndFeelUtil {
    private static final ArrayList<WeakReference<JFrame> > updateUIList = new ArrayList<WeakReference<JFrame>>();
    private static final UIManager.LookAndFeelInfo lookAndFeelInfo[] = UIManager.getInstalledLookAndFeels();
	private static final Logger logger = LoggerFactory.getLogger(DataHandler.class);
	public static final List< UIManager.LookAndFeelInfo> installedLookAndFeels = ArrayTools.unmodifiableList(lookAndFeelInfo);
	
    private static String lookAndFeel = "";
    
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
	
    public static final void setLookAndFeel(String laf){
    	if (lookAndFeel.equals(laf))
    		return;
    	lookAndFeel = laf;
    	JFrameUtils.runByDispatcher(updateLookAndFeel);
    }
    

    
	public static void addToUpdateTree(JFrame frame) {
		updateUIList.add(new WeakReference<JFrame>(frame));
	}
}
