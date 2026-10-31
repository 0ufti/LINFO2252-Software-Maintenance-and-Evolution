package be.uclouvain.sme33.model.progress;

import be.uclouvain.sme33.model.lesson.Difficulty;
import be.uclouvain.sme33.model.lesson.Subject;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Player state, deliberately in-memory for a prototype; easy to replace with persistence. */
public final class Progress {
    public static final int MAX_LIVES = 5;
    private final Map<Subject, Set<Difficulty>> completed = new EnumMap<>(Subject.class);
    private int lives = MAX_LIVES;
    private int coins = 6;
    private int correctAnswers;
    private int wrongAnswers;
    private LocalDate lastCompletion;
    private int consecutiveDays;
    private LocalDate dailyDate;
    private int todayCompletions;
    private boolean goalRewarded;
    private boolean purchasedStats;

    public int lives() { return lives; }
    public void loseLife() { lives = Math.max(0, lives - 1); }
    public void refillLives() { lives = MAX_LIVES; }
    public int coins() { return coins; }
    public boolean spendCoins(int amount) {
        if (amount < 0 || coins < amount) return false;
        coins -= amount;
        return true;
    }
    public void earnCoins(int amount) {
        if (amount < 0) throw new IllegalArgumentException("Negative reward");
        coins += amount;
    }
    public boolean purchasedStats() { return purchasedStats; }
    public void unlockStats() { purchasedStats = true; }
    public int correctAnswers() { return correctAnswers; }
    public int wrongAnswers() { return wrongAnswers; }
    public void recordWrong() { wrongAnswers++; }
    public int completedCount() { return completed.values().stream().mapToInt(Set::size).sum(); }
    public boolean completed(Subject subject, Difficulty difficulty) {
        return completed.getOrDefault(subject, Set.of()).contains(difficulty);
    }
    public int streak() { return consecutiveDays; }
    public int todayCompletions(LocalDate date) { return date.equals(dailyDate) ? todayCompletions : 0; }

    /** Completing an already-solved question remains practice, not an infinite coin farm. */
    public boolean recordCorrect(Subject subject, Difficulty difficulty, LocalDate date, boolean dailyGoalEnabled) {
        correctAnswers++;
        boolean firstTime = completed.computeIfAbsent(subject, key -> EnumSet.noneOf(Difficulty.class))
                .add(difficulty);
        if (firstTime) earnCoins(4);
        if (!date.equals(dailyDate)) {
            dailyDate = date;
            todayCompletions = 0;
            goalRewarded = false;
        }
        todayCompletions++;
        if (lastCompletion == null || !date.equals(lastCompletion)) {
            consecutiveDays = date.minusDays(1).equals(lastCompletion) ? consecutiveDays + 1 : 1;
            lastCompletion = date;
        }
        if (dailyGoalEnabled && !goalRewarded) {
            earnCoins(3);
            goalRewarded = true;
        }
        return firstTime;
    }
}
