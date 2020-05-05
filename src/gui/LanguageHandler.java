package gui;

import data.DataHandler;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.IOException;
import java.util.ArrayList;

public class LanguageHandler {
    ArrayList<LanguageChangeListener> languageChangeListeners;
    Language currentLanguage;

    Language getCurrentLanguage(){
        return currentLanguage;
    }

    ArrayList<Object> getLanguages(){
        ArrayList<Object> languages = new ArrayList<>();
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
            languages.add(elem.getValue());
        }
        return languages;
    }

    void setCurrentLanguage(Object object){
        SAXBuilder saxBuilder = new SAXBuilder();
        try {
            currentLanguage = new Language(saxBuilder.build(DataHandler.getResourceAsStream("languages/" + object + ".xml")));
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void addLanguageChangeListener(LanguageChangeListener languageChangeListener){
        languageChangeListeners.add(languageChangeListener);
    }
    void removeLanguageChangeListener(LanguageChangeListener languageChangeListener){
        languageChangeListeners.remove(languageChangeListener);
    }
}
