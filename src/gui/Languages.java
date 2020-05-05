package gui;

import data.DataHandler;
import io.IOString;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

import java.io.File;
import java.io.IOException;

public class Languages {

    enum LanguageString {
        new_game,
        update,
    }

    SAXBuilder saxBuilder = new SAXBuilder();
    private String language = "en";
    Document document;

    {
        try {
            document = saxBuilder.build(DataHandler.getResourceAsStream("languages/" + language + ".xml"));
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    Element root = document.getRootElement();




    String getString(LanguageString languageString){
        for (Element elem : root.getChildren())
        {
            if (elem.getName() == languageString.toString()){
                return elem.getValue();
            }
        }
        return "";

    }

    void setLanguage(String language){
        this.language = language;
        try {
            document = saxBuilder.build(DataHandler.getResourceAsStream("languages/" + language + ".xml"));
        } catch (JDOMException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        root = document.getRootElement();
    }
}
