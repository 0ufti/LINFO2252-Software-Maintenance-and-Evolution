import java.util.Arrays;

// question record
public class Question {
    private String text;
    private String[] answers;
    private int correctAnswer;
    private String explanation;

    public Question(String text, String[] answers, int correctAnswer, String explanation) {
        if (answers.length != 4 || correctAnswer < 0 || correctAnswer >= answers.length) {
            throw new IllegalArgumentException("Question needs 4 answers and a valid correct index");
        }
        this.text = text;
        this.answers = Arrays.copyOf(answers, answers.length);
        this.correctAnswer = correctAnswer;
        this.explanation = explanation;
    }

    public String getText() { return text; }
    public String[] getAnswers() { return Arrays.copyOf(answers, answers.length); }
    public String getExplanation() { return explanation; }
    public boolean checkAnswer(int answer) { return answer == correctAnswer; }
    public String getCorrectAnswer() { return answers[correctAnswer]; }
}
