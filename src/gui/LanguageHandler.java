package gui;

import java.awt.Component;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JTextField;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import data.DataHandler;
import gui.Language.LanguageSummary;
import util.ListTools;

public class LanguageHandler {
	private static final Logger logger = LoggerFactory.getLogger(LanguageHandler.class);
    private final ArrayList<WeakReference<LanguageChangeListener> > languageChangeListeners = new ArrayList<>();
    private final ArrayList<ComponentUpdater> updateComponents = new ArrayList<>();
    private Language currentLanguage;
    
    private static class ComponentUpdater extends WeakReference<JComponent>
    {
    	private final Words word;
		public ComponentUpdater(JComponent component, Words word) {
			super(component);
			this.word = word;
		}
		public boolean update(Language lang) {
			Component component = get();
        	if (component instanceof JLabel)
        	{
        		((JLabel)component).setText(lang.getString(word));
        	}
        	else if (component instanceof JTextField)
        	{
        		((JTextField)component).setText(lang.getString(word));
        	}
           	else if (component instanceof JMenuItem)
        	{
        		((JMenuItem)component).setText(lang.getString(word));
        	}
           	else if (component instanceof JMenu)
        	{
        		((JMenu)component).setText(lang.getString(word));
        	}
           	else
           	{
           		return false;
           	}
        	return true;
		}
    }

    public LanguageHandler(LanguageSummary object){
        setCurrentLanguage(object);
    }

    Language getCurrentLanguage(){
        return currentLanguage;
    }

	public LanguageSummary getCurrentSummary() {
		return currentLanguage.summary;
	}

    public LanguageSummary[] getLanguages(){
        ArrayList<LanguageSummary> languages = new ArrayList<>();
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = null;
        try {
            document = saxBuilder.build(DataHandler.getResourceAsStream("languages/languages.xml"));
        } catch (JDOMException | IOException e) {
            logger.error("Can't read language file");
        }
        Element root = document.getRootElement();
        for (Element elem : root.getChildren())
        {
            languages.add(new LanguageSummary(elem.getValue(), elem.getValue()));
        }
        return languages.toArray(new LanguageSummary[languages.size()]);
    }
    
    public void addItem(JComponent component, Words word)
    {
    	updateComponents.add(new ComponentUpdater(component, word));
    }

    void setCurrentLanguage(LanguageSummary summary){
        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            currentLanguage = new Language(saxBuilder.build(DataHandler.getResourceAsStream("languages/" + summary + ".xml")), summary);
        } catch (JDOMException | IOException e) {
        	logger.error("Can't read Language-File", e);
        } 
        {
	        int write = 0;
	        for (int read = 0; read < languageChangeListeners.size(); ++read)
	        {
	        	WeakReference<LanguageChangeListener> ref = languageChangeListeners.get(read);
	        	LanguageChangeListener l = ref.get();
	        	if (l != null)
	        	{
	        		l.languageChanged(currentLanguage);
	        		languageChangeListeners.set(write++, ref);
	        	}
	        }
	        ListTools.removeRange(languageChangeListeners, write, languageChangeListeners.size());
        }
        {
	        int write = 0;
	        for (int read = 0; read < updateComponents.size(); ++read)
	        {
	        	ComponentUpdater updater = updateComponents.get(read);
	        	if (updater.update(currentLanguage))
	        	{
	            	updateComponents.set(write++, updater);        		
	        	}
	        }
	        ListTools.removeRange(updateComponents, write, updateComponents.size());
        }
    }

    void addLanguageChangeListener(LanguageChangeListener languageChangeListener){
        languageChangeListeners.add(new WeakReference<LanguageChangeListener>(languageChangeListener));
    }
    void removeLanguageChangeListener(LanguageChangeListener l){
    	ListTools.removeReference(languageChangeListeners, l);
    }
}
