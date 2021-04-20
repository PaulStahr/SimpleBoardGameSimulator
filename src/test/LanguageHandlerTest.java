package test;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import gui.language.Language.LanguageSummary;
import gui.language.LanguageHandler;

@RunWith(Parameterized.class)
public class LanguageHandlerTest {
    @Parameters
    public static List<String> params() {return Arrays.asList(new String[] {"de","en"});}
    private final String lang;    
    public LanguageHandlerTest(String lang) {this.lang = lang;}

    @Test
    public void testGerman()
    {
        LanguageHandler lh = new LanguageHandler(new LanguageSummary(lang, lang));
        assertNotNull(lh.getCurrentLanguage());
    }
}
