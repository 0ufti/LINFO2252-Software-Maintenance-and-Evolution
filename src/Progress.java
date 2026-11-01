import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

// keeps state in memory during program life time
public class Progress {
    private int lives = 5;
    private int coins = 6;
    private int goodAnswers = 0;
    private int badAnswers = 0;
    private boolean statsBought = false;
    private Set<String> finished = new HashSet<>();
    private LocalDate lastDay;
    private LocalDate goalDay;
    private int streak = 0;
    private int todayDone = 0;
    private boolean goalRewarded = false;

    public int getLives() { return lives; }
    public int getCoins() { return coins; }
    public int getGoodAnswers() { return goodAnswers; }
    public int getBadAnswers() { return badAnswers; }
    public int getFinishedCount() { return finished.size(); }
    public boolean hasStats() { return statsBought; }
    public void buyStats() { statsBought = true; }
    public int getStreak() { return streak; }
    public int getTodayDone(LocalDate today) { return today.equals(goalDay) ? todayDone : 0; }
    public boolean hasFinished(String subject, String difficulty) {
        return finished.contains(subject + ":" + difficulty);
    }

    public void loseLife() { if (lives > 0) lives--; }
    public void refill() { lives = 5; }
    public void wrong() { badAnswers++; }
    public boolean spend(int amount) {
        if (amount < 0 || coins < amount) return false;
        coins -= amount;
        return true;
    }

    public boolean correct(String subject, String difficulty, LocalDate today, boolean dailyGoal) {
        goodAnswers++;
        
        // true if first completion
        boolean firstTime = finished.add(subject + ":" + difficulty);
        if (firstTime) coins += 4;

        if (!today.equals(goalDay)) {
            goalDay = today;
            todayDone = 0;
            goalRewarded = false;
        }
        todayDone++;

        if (lastDay == null || !today.equals(lastDay)) {
            if (today.minusDays(1).equals(lastDay)) streak++;
            else streak = 1;
            lastDay = today;
        }
        if (dailyGoal && !goalRewarded) {
            coins += 3;
            goalRewarded = true;
        }
        return firstTime;
    }
}
