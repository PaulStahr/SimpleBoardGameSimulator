package gui;

import data.DataHandler;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import java.io.IOException;
import java.util.HashMap;

public class Language {

    enum LanguageWords {
        new_game,
        update,
    }
    HashMap<LanguageWords, String> wordsToString;

    public Language(Document document) {
        Element root = document.getRootElement();
        for (Element elem : root.getChildren())
        {
            wordsToString.put(LanguageWords.valueOf(elem.getName()), elem.getValue());
        }
    }

    String getString(LanguageWords languageWords){
        return wordsToString.get(languageWords);
    }
}
