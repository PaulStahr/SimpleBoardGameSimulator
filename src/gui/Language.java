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

    SAXBuilder saxBuilder = new SAXBuilder();
    private String language = "en";
    HashMap<LanguageWords, String> wordsToString;

    public Language(String language) {
        Document document = null;
        this.language = language;
        try {
            document = saxBuilder.build(DataHandler.getResourceAsStream("languages/" + language + ".xml"));
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
