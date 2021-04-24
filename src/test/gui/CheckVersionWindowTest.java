package test.gui;

import org.junit.Test;

import gui.CheckVersionWindow;
import gui.language.Language.LanguageSummary;
import gui.language.LanguageHandler;

public class CheckVersionWindowTest {
    @Test
    public void openVersionWindowTest() {
        LanguageHandler lh = new LanguageHandler(new LanguageSummary("de", "de"));
        CheckVersionWindow.setLanguageHandler(lh);
        CheckVersionWindow.getInstance();
    }
}
