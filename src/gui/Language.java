package gui;

import java.util.HashMap;

import org.jdom2.Document;
import org.jdom2.Element;

public class Language {
    private final HashMap<Words, String> wordsToString = new HashMap<>();
    public final LanguageSummary summary;
    
    public Language(Document document, LanguageSummary summary) {
    	this.summary = summary;
        Element root = document.getRootElement();
        for (Element elem : root.getChildren())
        {
            String name = elem.getName();
            String value = elem.getValue();
            wordsToString.put(Words.valueOf(elem.getName()), elem.getValue());
        }
    }

    public final String getString(Words languageWords){
        return wordsToString.get(languageWords);
    }
    
    
    public static class LanguageSummary
    {
    	public final String name;
    	public final String internatialName;
    	
    	public LanguageSummary(String name, String internatialName)
    	{
    		this.name = name;
    		this.internatialName = internatialName;
    	}
    	
    	@Override
		public String toString() {
    		return name;
    	}
    }
}
