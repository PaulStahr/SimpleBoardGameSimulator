package gui;

import java.util.ArrayList;

public class LanguageHandler {
    ArrayList<LanguageChangeListener> languageChangeListeners;

    Language getCurrentLanguage(){
        Language language = new Language();
        return language;
    }

    ArrayList<Object> getLanguages(){
        ArrayList<Object> languages = new ArrayList<>();
        return languages;
    }

    void addLanguageChangeListener(LanguageChangeListener languageChangeListener){
        languageChangeListeners.add(languageChangeListener);
    }
    void removeLanguageChangeListener(LanguageChangeListener languageChangeListener){
        languageChangeListeners.remove(languageChangeListener);
    }
}
