package test.gui;

import org.junit.Test;

import gameObjects.instance.Game;
import gameObjects.instance.GameInstance;
import gui.EditGamePanel;
import gui.Language.LanguageSummary;
import gui.LanguageHandler;
import main.Player;

public class EditGamePanelTest {
    @Test
    public void testCreateEditPanel() {
        GameInstance gi = new GameInstance(new Game(),"foobar");
        LanguageHandler lh = new LanguageHandler(new LanguageSummary("de", "de"));
        Player pl = new Player("Max",3);
        new EditGamePanel(gi, lh, pl);
    }
 }
