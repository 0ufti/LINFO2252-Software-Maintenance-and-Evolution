import be.uclouvain.sme33.model.LearningModel;
import be.uclouvain.sme33.model.lesson.Difficulty;
import be.uclouvain.sme33.model.lesson.Subject;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.*;

/** Each test establishes one valid configuration and keeps it fixed during the lesson. */
class ConfigurationTest {
    static Stream<Arguments> configurations() {
        return Stream.of(
            Arguments.of(Subject.ENGLISH, List.of()),
            Arguments.of(Subject.PHYSICS, List.of()),
            Arguments.of(Subject.GENERAL_KNOWLEDGE, List.of()),
            Arguments.of(Subject.ENGLISH, List.of("STREAKS")),
            Arguments.of(Subject.PHYSICS, List.of("DAILY GOAL")),
            Arguments.of(Subject.GENERAL_KNOWLEDGE, List.of("THEME")),
            Arguments.of(Subject.ENGLISH, List.of("PREMIUM")),
            Arguments.of(Subject.PHYSICS, List.of("PREMIUM", "FLASH CARD")),
            Arguments.of(Subject.GENERAL_KNOWLEDGE, List.of("PREMIUM", "STREAKS")),
            Arguments.of(Subject.ENGLISH, List.of("PREMIUM", "DAILY GOAL")),
            Arguments.of(Subject.PHYSICS, List.of("PREMIUM", "THEME")),
            Arguments.of(Subject.GENERAL_KNOWLEDGE, List.of("PREMIUM", "FLASH CARD"))
        );
    }

    @ParameterizedTest(name = "subject={0}, features={1}")
    @MethodSource("configurations")
    void fixedConfigurationSupportsLesson(Subject subject, List<String> enabled) {
        Clock fixed = Clock.fixed(Instant.parse("2026-10-30T12:00:00Z"), ZoneOffset.UTC);
        Controller controller = new Controller(new LearningModel(fixed));
        assertTrue(controller.chooseSubject(subject));
        assertEquals(0, controller.activate(new String[0], enabled.toArray(String[]::new)));
        assertTrue(controller.startLesson(Difficulty.EASY));
        String[] state = controller.getStateAsLog();
        assertTrue(java.util.Arrays.stream(state).anyMatch(line -> line.startsWith("text|question.prompt|")));
        assertTrue(java.util.Arrays.stream(state).anyMatch(line -> line.startsWith("button|answer.0|")));
        assertTrue(controller.submitAnswer(0) || controller.currentScreen() == LearningModel.Screen.RESULT);
        assertEquals(1, controller.progressSummary().correctAnswers() + controller.progressSummary().wrongAnswers());
        if (enabled.contains("PREMIUM")) {
            assertTrue(controller.isActive("STATS"));
            assertEquals(5, controller.progressSummary().lives());
        }
        if (enabled.contains("THEME")) assertTrue(controller.isActive("SETTING"));
    }
}
