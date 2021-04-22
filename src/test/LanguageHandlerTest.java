package test;

import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import gui.language.Language;
import gui.language.Language.LanguageSummary;
import gui.language.LanguageHandler;
import gui.language.Words;

@RunWith(Parameterized.class)
public class LanguageHandlerTest {
    @Parameters
    public static List<String> params() {return Arrays.asList(new String[] {"de","en"});}   
    private final String lang;    
    public LanguageHandlerTest(String lang) {this.lang = lang;}

    @Test
    public void testLanguage()
    {
        LanguageHandler lh = new LanguageHandler(new LanguageSummary(lang, lang));
        Language l = lh.getCurrentLanguage();
        assertNotNull(l);
        for (Words word :  Words.values()) {
            assertNotNull("Word \"" + word + "\" Couln't be found in " + l.summary.name, l.getString(word));
        }
    }
}
