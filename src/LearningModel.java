import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// MODEL of the application. No Scanner and no Swing here.
public class LearningModel {
    private FeatureManager features = new FeatureManager();
    private QuestionBank bank = new QuestionBank();
    private Progress progress = new Progress();
    private UiLog log = new UiLog();
    private List<Runnable> listeners = new ArrayList<>();
    private LivesPolicy freeLives = new FreeLives();
    private LivesPolicy premiumLives = new PremiumLives();
    private Clock clock;

    private String screen = "HOME";
    private String difficulty = "EASY";
    private Question question = null;
    private String message = "Choose a subject and difficulty.";

    public LearningModel() { this(Clock.systemDefaultZone()); }
    public LearningModel(Clock clock) {
        this.clock = clock;
        refresh();
    }

    public synchronized void addListener(Runnable listener) {
        listeners.add(listener);
        listener.run();
    }
    public synchronized void removeListener(Runnable listener) { listeners.remove(listener); }
    public synchronized String[] getLogs() { return log.getLines(); }
    public synchronized List<UiLog.Item> getItems() { return log.getItems(); }
    public synchronized String getSubject() { return features.getSubject(); }
    public synchronized String getScreen() { return screen; }
    public synchronized String getDifficulty() { return difficulty; }
    public synchronized Question getQuestion() { return question; }
    public synchronized String getMessage() { return message; }
    public synchronized Progress getProgress() { return progress; } // prototype: tests can inspect state
    public synchronized boolean isOn(String name) { return features.isOn(name); }
    public synchronized String getLivesText() { return policy().displayLives(progress); }

    private LivesPolicy policy() {
        if (features.isOn("PREMIUM")) return premiumLives;
        return freeLives;
    }
    private LocalDate today() { return LocalDate.now(clock); }

    public synchronized int changeFeatures(FeatureCommand command) {
        String previous = features.getSubject();
        int code = command.execute(features, progress.hasStats());
        if (code != 0) return code;
        if (!previous.equals(features.getSubject())) {
            screen = "HOME";
            question = null;
        }
        if (screen.equals("STATS") && !features.isOn("STATS")) screen = "HOME";
        if (screen.equals("FLASHCARDS") && !features.isOn("FLASH CARD")) screen = "HOME";
        if (screen.equals("SETTINGS") && !features.isOn("SETTING")) screen = "HOME";
        message = "Features changed.";
        refresh();
        return 0;
    }

    public synchronized boolean open(String page) {
        page = page.toUpperCase();
        if (!page.equals("HOME") && !page.equals("SHOP") && !page.equals("PROFILE")
            && !page.equals("STATS") && !page.equals("FLASHCARDS") && !page.equals("SETTINGS")) return false;
        if (page.equals("STATS") && !isOn("STATS")) return false;
        if (page.equals("FLASHCARDS") && !isOn("FLASH CARD")) return false;
        if (page.equals("SETTINGS") && !isOn("SETTING")) return false;
        screen = page;
        question = null;
        message = "";
        refresh();
        return true;
    }

    public synchronized boolean startLesson(String level) {
        level = level.toUpperCase();
        if (!level.equals("EASY") && !level.equals("MEDIUM") && !level.equals("HARD")) return false;
        if (!policy().canPlay(progress)) {
            message = "No lives left. Buy a refill or use Premium.";
            screen = "HOME";
            refresh();
            return false;
        }
        difficulty = level;
        question = bank.get(features.getSubject(), difficulty);
        if (question == null) return false;
        screen = "QUESTION";
        message = "";
        refresh();
        return true;
    }

    // Answer is 0..3 in Java. In terminal the player enters 1..4.
    public synchronized boolean answer(int answer) {
        if (!screen.equals("QUESTION") || question == null || answer < 0 || answer > 3) {
            message = "No question or invalid answer.";
            refresh();
            return false;
        }
        boolean good = question.checkAnswer(answer);
        if (good) {
            boolean first = progress.correct(features.getSubject(), difficulty, today(), features.isOn("DAILY GOAL"));
            if (first) message = "Correct! +4 coins. " + question.getExplanation();
            else message = "Correct! " + question.getExplanation();
        } else {
            progress.wrong();
            policy().wrongAnswer(progress);
            message = "Wrong. " + question.getExplanation();
        }
        question = null;
        screen = "RESULT";
        refresh();
        return good;
    }

    public synchronized boolean buy(String item) {
        item = item.toLowerCase();
        if (item.equals("stats")) {
            if (features.isOn("STATS")) { message = "Already unlocked."; refresh(); return false; }
            if (!progress.spend(12)) { message = "Not enough coins (12 needed)."; refresh(); return false; }
            progress.buyStats();
            features.change(new String[0], new String[]{"STATS"}, true);
            message = "Statistics unlocked!";
        } else if (item.equals("premium")) {
            if (features.isOn("PREMIUM")) { message = "Already premium."; refresh(); return false; }
            if (!progress.spend(24)) { message = "Not enough coins (24 needed)."; refresh(); return false; }
            features.change(new String[0], new String[]{"PREMIUM"}, progress.hasStats());
            message = "Premium unlocked!";
        } else if (item.equals("refill")) {
            if (progress.getLives() == 5 || !progress.spend(5)) {
                message = "Cannot refill now (5 coins, and you must be missing lives).";
                refresh();
                return false;
            }
            progress.refill();
            message = "Lives refilled!";
        } else return false;
        refresh();
        return true;
    }

    private UiLog.Item item(String type, String id, String text, String place) {
        return new UiLog.Item(type, id, text, place);
    }

    // Converts the current Model into a list of items that a UI could display.
    // We update the groups of the current logs rather than resetting the log object.
    private void refresh() {
        List<UiLog.Item> header = new ArrayList<>();
        header.add(item("text", "title", "Smart Learning - SME33", "HEADER"));
        header.add(item("text", "coins", "Coins: " + progress.getCoins(), "HEADER"));
        header.add(item("text", "lives", policy().displayLives(progress), "HEADER"));
        if (features.isOn("PREMIUM")) header.add(item("text", "premium", "Premium", "HEADER"));
        if (features.isOn("STREAKS")) header.add(item("text", "streak", "Streak: " + progress.getStreak(), "HEADER"));
        if (features.isOn("DAILY GOAL")) header.add(item("text", "goal", "Daily goal: " + progress.getTodayDone(today()) + "/1", "HEADER"));
        log.update("header", header);

        List<UiLog.Item> nav = new ArrayList<>();
        nav.add(item("button", "home", "Home", "NAV"));
        nav.add(item("button", "shop", "Shop", "NAV"));
        nav.add(item("button", "profile", "Profile", "NAV"));
        if (features.isOn("STATS")) nav.add(item("button", "stats", "Statistics", "NAV"));
        if (features.isOn("FLASH CARD")) nav.add(item("button", "flashcards", "Flash cards", "NAV"));
        if (features.isOn("SETTING")) nav.add(item("button", "settings", "Settings", "NAV"));
        log.update("nav", nav);

        List<UiLog.Item> main = new ArrayList<>();
        if (screen.equals("HOME")) {
            main.add(item("title", "page", "Learning: " + features.getSubject(), "MAIN"));
            main.add(item("button", "subject.english", "English", "MAIN"));
            main.add(item("button", "subject.physics", "Physics", "MAIN"));
            main.add(item("button", "subject.general", "General Knowledge", "MAIN"));
            for (String d : new String[]{"EASY", "MEDIUM", "HARD"}) {
                String label = d + (progress.hasFinished(features.getSubject(), d) ? " (done)" : "");
                main.add(item("button", "lesson." + d.toLowerCase(), label, "MAIN"));
            }
        } else if (screen.equals("QUESTION") && question != null) {
            main.add(item("title", "page", features.getSubject() + " / " + difficulty, "MAIN"));
            main.add(item("text", "question", question.getText(), "MAIN"));
            String[] answers = question.getAnswers();
            for (int i = 0; i < answers.length; i++) {
                main.add(item("button", "answer." + i, (i + 1) + ". " + answers[i], "MAIN"));
            }
        } else if (screen.equals("RESULT")) {
            main.add(item("title", "page", "Lesson finished", "MAIN"));
            main.add(item("button", "retry", "Try again", "MAIN"));
        } else if (screen.equals("SHOP")) {
            main.add(item("title", "page", "Shop", "MAIN"));
            if (!features.isOn("STATS")) main.add(item("button", "buy.stats", "Statistics - 12 coins", "MAIN"));
            if (!features.isOn("PREMIUM")) main.add(item("button", "buy.premium", "Premium - 24 coins", "MAIN"));
            if (progress.getLives() < 5) main.add(item("button", "buy.refill", "Refill lives - 5 coins", "MAIN"));
        } else if (screen.equals("PROFILE")) {
            main.add(item("title", "page", "Profile", "MAIN"));
            main.add(item("text", "user", "Demo learner", "MAIN"));
            main.add(item("text", "completed", "Completed: " + progress.getFinishedCount() + "/9", "MAIN"));
            if (features.isOn("PROFILE SKIN")) main.add(item("text", "skin", "Premium skin enabled", "MAIN"));
        } else if (screen.equals("STATS")) {
            main.add(item("title", "page", "Statistics", "MAIN"));
            main.add(item("text", "correct", "Correct: " + progress.getGoodAnswers(), "MAIN"));
            main.add(item("text", "wrong", "Wrong: " + progress.getBadAnswers(), "MAIN"));
            main.add(item("text", "completed", "Completed: " + progress.getFinishedCount() + "/9", "MAIN"));
        } else if (screen.equals("FLASHCARDS")) {
            Question q = bank.get(features.getSubject(), "EASY");
            main.add(item("title", "page", "Flash card: " + features.getSubject(), "MAIN"));
            main.add(item("text", "flash.question", q.getText(), "MAIN"));
            main.add(item("text", "flash.answer", q.getCorrectAnswer(), "MAIN"));
        } else if (screen.equals("SETTINGS")) {
            main.add(item("title", "page", "Settings", "MAIN"));
            main.add(item("text", "theme", "Theme: " + (features.isOn("THEME") ? "Dark" : "Light"), "MAIN"));
        }
        if (!message.isEmpty()) main.add(item("text", "notice", message, "MAIN"));
        log.update("main", main);
        for (Runnable r : listeners) r.run();
    }
}
