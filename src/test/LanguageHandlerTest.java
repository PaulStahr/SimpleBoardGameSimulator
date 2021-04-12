package test;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import gui.Language.Language.LanguageSummary;
import gui.Language.LanguageHandler;

public class LanguageHandlerTest {
    @Test
    public void testGerman()
    {
        LanguageHandler lh = new LanguageHandler(new LanguageSummary("de", "de"));
        assertNotNull(lh.getCurrentLanguage());
    }

    @Test
    public void testEngish()
    {
        LanguageHandler lh = new LanguageHandler(new LanguageSummary("en", "en"));
        assertNotNull(lh.getCurrentLanguage());
    }
}
