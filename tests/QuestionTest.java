import model.Progress;
import model.Question;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class QuestionTest {
    @Test
    void correctAnswer() {
        Question q = new Question("2+2?", new String[]{"2", "3", "4", "5"}, 2, "four");
        assertTrue(q.checkAnswer(2));
        assertFalse(q.checkAnswer(0));
    }

    @Test
    void invalidCorrectIndex() {
        assertThrows(IllegalArgumentException.class,
            () -> new Question("test", new String[]{"a", "b", "c", "d"}, 6, ""));
    }

    @Test
    void sameLessonIsOnlyPaidOnce() {
        Progress p = new Progress();
        java.time.LocalDate date = java.time.LocalDate.of(2026, 10, 30);
        assertTrue(p.correct("ENGLISH", "EASY", date, false));
        assertFalse(p.correct("ENGLISH", "EASY", date, false));
        assertEquals(10, p.getCoins());
    }
}
