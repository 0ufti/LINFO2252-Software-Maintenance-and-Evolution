import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ControllerTest {
    @Test
    void canChangeSubject() {
        Controller c = new Controller();
        assertEquals(0, c.activate(new String[]{"ENGLISH"}, new String[]{"PHYSICS"}));
        assertEquals("PHYSICS", c.getSubject());
    }

    @Test
    void unknownFeatureDoesNotActivatePremium() {
        Controller c = new Controller();
        assertEquals(1, c.activate(new String[0], new String[]{"PREMIUM", "NOT A FEATURE"}));
        assertFalse(c.getModel().isOn("PREMIUM"));
    }

    @Test
    void cannotTurnOffTheOnlyCourse() {
        Controller c = new Controller();
        assertEquals(3, c.activate(new String[]{"ENGLISH"}, new String[0]));
        assertEquals("ENGLISH", c.getSubject());
    }

    @Test
    void cannotAccessStatsForFree() {
        Controller c = new Controller();
        assertEquals(4, c.activate(new String[0], new String[]{"STATS"}));
        assertFalse(c.open("STATS"));
    }

    @Test
    void declaredButNotImplementedFeatureHasOwnCode() {
        Controller c = new Controller();
        assertEquals(2, c.activate(new String[0], new String[]{"LEADERBROAD"}));
    }
}
