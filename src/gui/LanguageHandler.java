package gui;

import java.io.IOException;
import java.util.ArrayList;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import data.DataHandler;
import gui.Language.LanguageSummary;

public class LanguageHandler {
    private final ArrayList<LanguageChangeListener> languageChangeListeners = new ArrayList<>();
    private Language currentLanguage;

    public LanguageHandler(LanguageSummary object){
        setCurrentLanguage(object);
    }

    Language getCurrentLanguage(){
        return currentLanguage;
    }

	public Object getCurrentSummary() {
		return currentLanguage.summary;
	}

    public LanguageSummary[] getLanguages(){
        ArrayList<LanguageSummary> languages = new ArrayList<>();
        SAXBuilder saxBuilder = new SAXBuilder();
        Document document = null;
        try {
            document = saxBuilder.build(DataHandler.getResourceAsStream("languages/languages.xml"));
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Element root = document.getRootElement();
        for (Element elem : root.getChildren())
        {
            languages.add(new LanguageSummary(elem.getValue(), elem.getValue()));
        }
        return languages.toArray(new LanguageSummary[languages.size()]);
    }

    void setCurrentLanguage(LanguageSummary summary){
        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            currentLanguage = new Language(saxBuilder.build(DataHandler.getResourceAsStream("languages/" + summary + ".xml")), summary);
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < languageChangeListeners.size(); ++i)
        {
        	languageChangeListeners.get(i).languageChanged(currentLanguage);
        }
    }

    void addLanguageChangeListener(LanguageChangeListener languageChangeListener){
        languageChangeListeners.add(languageChangeListener);
    }
    void removeLanguageChangeListener(LanguageChangeListener languageChangeListener){
        languageChangeListeners.remove(languageChangeListener);
    }
}
