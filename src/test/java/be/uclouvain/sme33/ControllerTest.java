import be.uclouvain.sme33.model.LearningModel;
import be.uclouvain.sme33.model.feature.FeatureManager;
import be.uclouvain.sme33.model.lesson.Difficulty;
import be.uclouvain.sme33.model.lesson.Subject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Unit suite to discuss in the M2 report: isolated Controller API, return codes and state. */
class ControllerTest {
    private Controller controller;
    private static final Clock FIXED = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC);

    @BeforeEach void setup() { controller = new Controller(new LearningModel(FIXED)); }

    @Test void rejectsUnknownFeatureWithoutPartialActivation() {
        int code = controller.activate(new String[0], new String[]{"PREMIUM", "FEATURE_NOT_IN_XML"});
        assertEquals(1, code);
        assertFalse(controller.isActive(FeatureManager.PREMIUM));
        assertFalse(controller.isActive(FeatureManager.STATS));
    }

    @Test void premiumAutomaticallyEnablesStatsAndSkin() {
        assertEquals(0, controller.activate(new String[0], new String[]{"PREMIUM"}));
        assertTrue(controller.isActive(FeatureManager.PREMIUM));
        assertTrue(controller.isActive(FeatureManager.STATS));
        assertTrue(controller.isActive(FeatureManager.SKIN));
        assertTrue(java.util.Arrays.asList(controller.getStateAsLog()).stream()
                .anyMatch(line -> line.contains("Lives: unlimited")));
    }

    @Test void cannotDeactivateLastSubjectOrMandatoryDifficulty() {
        assertEquals(3, controller.activate(new String[]{"ENGLISH"}, new String[0]));
        assertEquals(2, controller.activate(new String[]{"EASY"}, new String[0]));
        assertTrue(controller.isActive("ENGLISH"));
        assertTrue(controller.chooseSubject(Subject.PHYSICS));
        assertFalse(controller.isActive("ENGLISH"));
        assertTrue(controller.isActive("PHYSICS"));
    }

    @Test void freePlayerCanPurchaseStatsWithEarnedCurrency() {
        assertTrue(controller.startLesson(Difficulty.EASY));
        assertTrue(controller.submitAnswer(0)); // English EASY: Hello
        assertTrue(controller.startLesson(Difficulty.MEDIUM));
        assertTrue(controller.submitAnswer(1)); // English MEDIUM: has gone
        assertEquals(14, controller.progressSummary().coins());
        assertTrue(controller.buyStats());
        assertEquals(2, controller.progressSummary().coins());
        assertTrue(controller.isActive(FeatureManager.STATS));
        assertFalse(controller.isActive(FeatureManager.PREMIUM));
    }

    @Test void repeatingCompletedLevelDoesNotFarmCoins() {
        controller.startLesson(Difficulty.EASY);
        controller.submitAnswer(0);
        int coins = controller.progressSummary().coins();
        controller.startLesson(Difficulty.EASY);
        controller.submitAnswer(0);
        assertEquals(coins, controller.progressSummary().coins());
    }

    @Test void freePlayerCannotStartWithZeroLives() {
        for (int i = 0; i < 5; i++) {
            assertTrue(controller.startLesson(Difficulty.EASY));
            assertFalse(controller.submitAnswer(1));
        }
        assertEquals(0, controller.progressSummary().lives());
        assertFalse(controller.startLesson(Difficulty.EASY));
        assertEquals(LearningModel.Screen.HOME, controller.currentScreen());
    }

    @Test void declaredButNotImplementedFeatureHasExplicitError() {
        assertEquals(2, controller.activate(new String[0], new String[]{"LEADERBROAD"}));
    }
}
