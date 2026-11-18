package model;

import java.util.HashMap;
import java.util.Map;

public class QuestionBank {
    private Map<String, Question> questions = new HashMap<>();

    public QuestionBank() {
        add("ENGLISH", "EASY", "Choose the correct translation of 'bonjour'.",
            "Hello", "Good night", "Thanks", "Sorry", 0, "'Bonjour' means hello in English.");
        add("ENGLISH", "MEDIUM", "Which sentence uses the present perfect?",
            "She went home.", "She has gone home.", "She goes home.", "She will go home.", 1,
            "Present perfect: has/have + past participle.");
        add("ENGLISH", "HARD", "Complete: 'Had I known, I ___ earlier.'",
            "leave", "will leave", "had left", "would have left", 3,
            "Had I known... I would have left.");

        add("PHYSICS", "EASY", "Which SI unit measures force?",
            "Joule", "Newton", "Watt", "Pascal", 1, "Force is measured in newtons (N).");
        add("PHYSICS", "MEDIUM", "A 2 kg object accelerates at 3 m/s². What is the net force?",
            "1.5 N", "5 N", "6 N", "9 N", 2, "F = ma = 2 x 3 = 6 N.");
        add("PHYSICS", "HARD", "If velocity doubles, kinetic energy becomes...",
            "2 times", "4 times", "8 times", "Unchanged", 1,
            "Kinetic energy is 1/2 mv², so it becomes four times bigger.");

        add("GENERAL KNOWLEDGE", "EASY", "What is the capital of Belgium?",
            "Bruges", "Ghent", "Brussels", "Liège", 2, "Brussels is Belgium's capital.");
        add("GENERAL KNOWLEDGE", "MEDIUM", "Which planet has the most prominent ring system?",
            "Venus", "Mars", "Mercury", "Saturn", 3, "Saturn is famous for its rings.");
        add("GENERAL KNOWLEDGE", "HARD", "Who formulated the laws of planetary motion?",
            "Kepler", "Darwin", "Pasteur", "Faraday", 0, "Johannes Kepler formulated these laws.");
    }

    private void add(String subject, String difficulty, String text,
                     String a, String b, String c, String d, int answer, String explanation) {
        questions.put(subject + ":" + difficulty,
            new Question(text, new String[]{a, b, c, d}, answer, explanation));
    }

    public Question get(String subject, String difficulty) {
        return questions.get(subject + ":" + difficulty);
    }
}
