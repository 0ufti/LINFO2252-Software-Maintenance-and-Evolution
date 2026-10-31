package be.uclouvain.sme33.model.lesson;

import java.util.EnumMap;
import java.util.Map;

/** In-memory Repository: replacing these samples by JSON or a database won't affect the UI. */
public final class LessonRepository {
    private final Map<Subject, Map<Difficulty, Question>> questions = new EnumMap<>(Subject.class);

    public LessonRepository() {
        register(Subject.ENGLISH, Difficulty.EASY,
                "Choose the correct translation of 'bonjour'.", "Hello", "Good night", "Thanks", "Sorry", 0,
                "'Bonjour' means 'hello' in English.");
        register(Subject.ENGLISH, Difficulty.MEDIUM,
                "Which sentence uses the present perfect?", "She went home.", "She has gone home.",
                "She goes home.", "She will go home.", 1, "Present perfect: has/have + past participle.");
        register(Subject.ENGLISH, Difficulty.HARD,
                "Complete: 'Had I known, I ___ earlier.'", "leave", "will leave", "had left", "would have left", 3,
                "Inverted third conditional: Had I known... I would have left.");
        register(Subject.PHYSICS, Difficulty.EASY,
                "Which SI unit measures force?", "Joule", "Newton", "Watt", "Pascal", 1,
                "Force is measured in newtons (N).");
        register(Subject.PHYSICS, Difficulty.MEDIUM,
                "A 2 kg object accelerates at 3 m/s². What is the net force?", "1.5 N", "5 N", "6 N", "9 N", 2,
                "Newton's second law: F = ma = 2 × 3 = 6 N.");
        register(Subject.PHYSICS, Difficulty.HARD,
                "If velocity doubles, kinetic energy (mass constant) becomes...", "2 times", "4 times", "8 times", "Unchanged", 1,
                "Kinetic energy is (1/2)mv², so doubling v multiplies energy by four.");
        register(Subject.GENERAL_KNOWLEDGE, Difficulty.EASY,
                "What is the capital of Belgium?", "Bruges", "Ghent", "Brussels", "Liège", 2,
                "Brussels is Belgium's capital.");
        register(Subject.GENERAL_KNOWLEDGE, Difficulty.MEDIUM,
                "Which planet has the most prominent ring system?", "Venus", "Mars", "Mercury", "Saturn", 3,
                "Saturn is famous for its large, bright ring system.");
        register(Subject.GENERAL_KNOWLEDGE, Difficulty.HARD,
                "Which scientist formulated the laws of planetary motion?", "Kepler", "Darwin", "Pasteur", "Faraday", 0,
                "Johannes Kepler formulated three laws of planetary motion.");
    }

    private void register(Subject subject, Difficulty difficulty, String prompt,
                          String a, String b, String c, String d, int correct, String explanation) {
        questions.computeIfAbsent(subject, key -> new EnumMap<>(Difficulty.class))
                .put(difficulty, new Question(prompt, java.util.List.of(a, b, c, d), correct, explanation));
    }

    public Question get(Subject subject, Difficulty difficulty) {
        Question question = questions.getOrDefault(subject, Map.of()).get(difficulty);
        if (question == null) throw new IllegalArgumentException("Missing " + subject + " / " + difficulty);
        return question;
    }
}
