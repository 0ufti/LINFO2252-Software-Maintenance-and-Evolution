import be.uclouvain.sme33.model.LearningModel;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** The logs are the same UI specification the Swing renderer receives, not event history. */
class LogTest {
    private static Set<String> state(Controller controller) {
        return new HashSet<>(Arrays.asList(controller.getStateAsLog()));
    }

    @Test void independentFeatureActivationOrderGivesSameLogSet() {
        Controller a = new Controller();
        Controller b = new Controller();
        assertEquals(0, a.activate(new String[0], new String[]{"STREAKS"}));
        assertEquals(0, a.activate(new String[0], new String[]{"PREMIUM"}));
        assertEquals(0, b.activate(new String[0], new String[]{"PREMIUM"}));
        assertEquals(0, b.activate(new String[0], new String[]{"STREAKS"}));
        assertEquals(state(a), state(b));
    }

    @Test void logsRemoveDeactivatedElementsAndMatchViewSpecification() {
        LearningModel model = new LearningModel();
        Controller controller = new Controller(model);
        controller.activate(new String[0], new String[]{"PREMIUM", "FLASH CARD"});
        assertTrue(state(controller).stream().anyMatch(s -> s.startsWith("button|nav.flashcards|")));
        controller.activate(new String[]{"PREMIUM"}, new String[0]);
        assertFalse(state(controller).stream().anyMatch(s -> s.startsWith("button|nav.flashcards|")));
        assertFalse(state(controller).stream().anyMatch(s -> s.startsWith("button|nav.stats|")));
        Set<String> viewElements = model.uiElements().stream().map(e -> e.toLog()).collect(Collectors.toSet());
        assertEquals(viewElements, state(controller));
    }
}
