import be.uclouvain.sme33.model.LearningModel;
import be.uclouvain.sme33.model.lesson.Difficulty;
import be.uclouvain.sme33.model.lesson.Subject;
import java.util.Arrays;

/** Self-contained smoke checks without JUnit; run via scripts/run-smoke.sh. */
public final class SmokeCheck {
    private static int checks;
    private static void require(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        Controller controller = new Controller();
        require(controller.getStateAsLog().length > 10, "initial log");
        require(controller.activate(new String[0], new String[]{"NOT_A_FEATURE"}) == 1, "unknown feature");
        require(controller.activate(new String[]{"ENGLISH"}, new String[0]) == 3, "ALT group invariant");
        require(controller.chooseSubject(Subject.PHYSICS), "subject switch");
        require(controller.startLesson(Difficulty.EASY), "start physics");
        require(controller.submitAnswer(1), "physics easy correct answer");
        require(controller.progressSummary().coins() == 10, "4-coin reward");
        require(controller.activate(new String[0], new String[]{"PREMIUM", "FLASH CARD"}) == 0,
                "atomic activation");
        require(controller.isActive("STATS") && controller.isActive("PROFILE SKIN"), "M1 implications");
        require(Arrays.stream(controller.getStateAsLog()).anyMatch(s -> s.startsWith("button|nav.flashcards|")),
                "flashcard present in logs");
        require(controller.activate(new String[]{"PREMIUM"}, new String[0]) == 0, "deactivation");
        require(!controller.isActive("FLASH CARD"), "flashcard cascaded off");
        require(Arrays.stream(controller.getStateAsLog()).noneMatch(s -> s.startsWith("button|nav.flashcards|")),
                "flashcard removed from current UI logs");
        require(controller.startLesson(Difficulty.EASY), "repeat physics");
        require(!controller.submitAnswer(0), "wrong answer");
        require(controller.progressSummary().lives() == 4, "free policy deducts life");
        require(controller.chooseSubject(Subject.ENGLISH), "return English");
        require(controller.startLesson(Difficulty.EASY), "English start");
        require(controller.submitAnswer(0), "English answer");
        require(controller.startLesson(Difficulty.MEDIUM), "English medium start");
        require(controller.submitAnswer(1), "English medium answer");
        require(controller.buyStats(), "stats purchasable without premium");
        require(!controller.isActive("PREMIUM") && controller.isActive("STATS"), "free stats unlock");
        require(controller.activate(new String[]{"STATS"}, new String[]{"PREMIUM", "NOT_A_FEATURE"}) == 1,
                "transaction rejected without side effects");
        require(controller.isActive("STATS") && !controller.isActive("PREMIUM"), "rollback preserved previous state");
        require(controller.currentScreen() == LearningModel.Screen.RESULT, "screen remains responsive");
        System.out.println("PASS: " + checks + " smoke assertions");
    }
}
