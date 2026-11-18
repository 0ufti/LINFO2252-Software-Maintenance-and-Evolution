import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ConfigurationTest {
    @Test
    void englishWithNoExtraFeatures() {
        Controller c = new Controller();
        assertTrue(c.startLesson("EASY"));
        assertTrue(c.answer(0));
        assertEquals(10, c.getModel().getProgress().getCoins());
        assertFalse(c.open("STATS"));
    }

    @Test
    void physicsWithDailyGoal() {
        Controller c = new Controller();
        c.chooseSubject("PHYSICS");
        assertEquals(0, c.activate(new String[0], new String[]{"DAILY GOAL"}));
        c.startLesson("EASY");
        c.answer(1);
        assertEquals(13, c.getModel().getProgress().getCoins()); // +4 question, +3 daily
    }

    @Test
    void generalKnowledgeWithPremiumFlashCards() {
        Controller c = new Controller();
        c.chooseSubject("GENERAL KNOWLEDGE");
        assertEquals(0, c.activate(new String[0], new String[]{"PREMIUM", "FLASH CARD"}));
        assertTrue(c.open("FLASHCARDS"));
        assertTrue(c.getModel().isOn("STATS"));
        assertTrue(c.getModel().isOn("PROFILE SKIN"));
    }

    @Test
    void graphicsDependOnSettings() {
        Controller c = new Controller();
        c.activate(new String[0], new String[]{"THEME"});
        assertTrue(c.getModel().isOn("SETTING"));
        assertTrue(c.getModel().isOn("GRAFICS"));
        c.activate(new String[]{"SETTING"}, new String[0]);
        assertFalse(c.getModel().isOn("THEME"));
        assertFalse(c.getModel().isOn("GRAFICS"));
    }
}
