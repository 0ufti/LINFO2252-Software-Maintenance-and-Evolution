import controller.Controller;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SystemTest {
    @Test
    void premiumChangesLivesDuringExecution() {
        Controller c = new Controller();
        c.startLesson("EASY");
        c.answer(1); // wrong answer
        assertEquals(4, c.getModel().getProgress().getLives());

        c.activate(new String[0], new String[]{"PREMIUM"});
        c.startLesson("EASY");
        c.answer(1);
        assertEquals(4, c.getModel().getProgress().getLives()); // doesn't go down

        c.activate(new String[]{"PREMIUM"}, new String[0]);
        c.startLesson("EASY");
        c.answer(1);
        assertEquals(3, c.getModel().getProgress().getLives());
    }

    @Test
    void purchasedStatisticsSurvivePremiumDeactivation() {
        Controller c = new Controller();
        // 2 different lessons = 8 extra coins, giving 14 total.
        c.startLesson("EASY"); c.answer(0);
        c.startLesson("MEDIUM"); c.answer(1);
        assertTrue(c.buy("stats"));
        c.activate(new String[0], new String[]{"PREMIUM"});
        c.activate(new String[]{"PREMIUM"}, new String[0]);
        assertTrue(c.open("STATS"));
    }

    @Test
    void logsCorrespondToCurrentFeatures() {
        Controller c = new Controller();
        assertFalse(logs(c).contains("button|stats|Statistics|NAV"));
        c.activate(new String[0], new String[]{"PREMIUM"});
        assertTrue(logs(c).contains("button|stats|Statistics|NAV"));
        assertTrue(logs(c).contains("text|lives|Lives: unlimited|HEADER"));
        c.activate(new String[]{"PREMIUM"}, new String[0]);
        assertFalse(logs(c).contains("button|stats|Statistics|NAV"));
    }

    @Test
    void logDoesNotDependOnActivationOrder() {
        Controller a = new Controller();
        Controller b = new Controller();
        a.activate(new String[0], new String[]{"STREAKS"});
        a.activate(new String[0], new String[]{"PREMIUM"});
        b.activate(new String[0], new String[]{"PREMIUM"});
        b.activate(new String[0], new String[]{"STREAKS"});
        assertEquals(new java.util.HashSet<>(java.util.Arrays.asList(a.getStateAsLog())),
                     new java.util.HashSet<>(java.util.Arrays.asList(b.getStateAsLog())));
    }

    private java.util.Set<String> logs(Controller c) {
        return new java.util.HashSet<>(java.util.Arrays.asList(c.getStateAsLog()));
    }
}
