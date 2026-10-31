package be.uclouvain.sme33.model.lesson;

import java.util.List;

/** Immutable lesson content; UI code never decides which choice is correct. */
public record Question(String prompt, List<String> choices, int correctIndex, String explanation) {
    public Question {
        choices = List.copyOf(choices);
        if (choices.size() < 2 || correctIndex < 0 || correctIndex >= choices.size()) {
            throw new IllegalArgumentException("A question needs valid multiple-choice answers");
        }
    }
    public boolean isCorrect(int index) { return index == correctIndex; }
}
