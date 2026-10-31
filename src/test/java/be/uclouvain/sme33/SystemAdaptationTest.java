import be.uclouvain.sme33.model.LearningModel;
import be.uclouvain.sme33.model.lesson.Difficulty;
import be.uclouvain.sme33.model.lesson.Subject;
import java.util.Arrays;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** System-level tests vary the active configuration between user actions. */
class SystemAdaptationTest {
    @Test void wrongAnswerChangesPolicyImmediatelyAfterPremiumActivation() {
        Controller controller = new Controller();
        controller.startLesson(Difficulty.EASY);
        controller.submitAnswer(1); // English EASY wrong
        assertEquals(4, controller.progressSummary().lives());
        assertEquals(0, controller.activate(new String[0], new String[]{"PREMIUM"}));
        controller.startLesson(Difficulty.EASY);
        controller.submitAnswer(1);
        assertEquals(4, controller.progressSummary().lives());
        assertTrue(Arrays.stream(controller.getStateAsLog()).anyMatch(s -> s.contains("Lives: unlimited")));
        assertEquals(0, controller.activate(new String[]{"PREMIUM"}, new String[0]));
        controller.startLesson(Difficulty.EASY);
        controller.submitAnswer(1);
        assertEquals(3, controller.progressSummary().lives());
    }

    @Test void switchingSubjectAbortsQuestionAndUsesNewCourse() {
        Controller controller = new Controller();
        controller.startLesson(Difficulty.MEDIUM);
        assertTrue(Arrays.stream(controller.getStateAsLog()).anyMatch(s -> s.contains("present perfect")));
        controller.chooseSubject(Subject.PHYSICS);
        assertEquals(LearningModel.Screen.HOME, controller.currentScreen());
        assertFalse(controller.submitAnswer(1)); // cannot answer orphaned English question
        controller.startLesson(Difficulty.MEDIUM);
        assertTrue(Arrays.stream(controller.getStateAsLog()).anyMatch(s -> s.contains("2 kg object")));
    }

    @Test void activateAndDeactivateStreaksDuringUse() {
        Controller controller = new Controller();
        assertFalse(Arrays.stream(controller.getStateAsLog()).anyMatch(s -> s.contains("app.streaks")));
        assertEquals(0, controller.activate(new String[0], new String[]{"STREAKS"}));
        controller.startLesson(Difficulty.EASY);
        controller.submitAnswer(0);
        assertTrue(Arrays.stream(controller.getStateAsLog()).anyMatch(s -> s.contains("Streak: 1 days")));
        assertEquals(0, controller.activate(new String[]{"STREAKS"}, new String[0]));
        assertFalse(Arrays.stream(controller.getStateAsLog()).anyMatch(s -> s.contains("app.streaks")));
        assertEquals(1, controller.progressSummary().streak()); // progress survives feature deactivation
    }
}
