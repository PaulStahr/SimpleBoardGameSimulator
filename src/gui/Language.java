package gui;

import data.DataHandler;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import java.io.IOException;
import java.util.HashMap;

public class Language {
    private final HashMap<Words, String> wordsToString = new HashMap<>();

    public Language(Document document) {
        Element root = document.getRootElement();
        for (Element elem : root.getChildren())
        {
            String name = elem.getName();
            String value = elem.getValue();
            wordsToString.put(Words.valueOf(elem.getName()), elem.getValue());
        }
    }

    String getString(Words languageWords){
        return wordsToString.get(languageWords);
    }
}
