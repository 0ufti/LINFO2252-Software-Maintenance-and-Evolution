package be.uclouvain.sme33.model;

import be.uclouvain.sme33.model.UiElement.Area;
import be.uclouvain.sme33.model.UiElement.Kind;
import be.uclouvain.sme33.model.feature.FeatureCatalogue;
import be.uclouvain.sme33.model.feature.FeatureChangeCommand;
import be.uclouvain.sme33.model.feature.FeatureManager;
import be.uclouvain.sme33.model.lesson.Difficulty;
import be.uclouvain.sme33.model.lesson.LessonRepository;
import be.uclouvain.sme33.model.lesson.Question;
import be.uclouvain.sme33.model.lesson.Subject;
import be.uclouvain.sme33.model.progress.FreeLivesPolicy;
import be.uclouvain.sme33.model.progress.LivesPolicy;
import be.uclouvain.sme33.model.progress.PremiumLivesPolicy;
import be.uclouvain.sme33.model.progress.Progress;
import be.uclouvain.sme33.model.progress.ProgressSummary;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * MODEL: owns all game rules, progress, feature configuration, and current UI specifications.
 * No Swing or input parsing occurs in this class. All state-mutating methods are synchronized.
 */
public final class LearningModel {
    public enum Screen { HOME, QUESTION, RESULT, SHOP, PROFILE, STATS, FLASHCARDS, SETTINGS }

    public static final int STATS_PRICE = 12;
    public static final int PREMIUM_PRICE = 24;
    public static final int REFILL_PRICE = 5;

    private final Clock clock;
    private final FeatureManager features = new FeatureManager(new FeatureCatalogue());
    private final LessonRepository lessons = new LessonRepository();
    private final Progress progress = new Progress();
    private final UiStateLog log = new UiStateLog();
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<>();
    private final LivesPolicy freePolicy = new FreeLivesPolicy();
    private final LivesPolicy premiumPolicy = new PremiumLivesPolicy();

    private Screen screen = Screen.HOME;
    private Difficulty difficulty;
    private Question activeQuestion;
    private String notice = "Choose a subject and a difficulty to begin.";

    public LearningModel() { this(Clock.systemDefaultZone()); }
    public LearningModel(Clock clock) {
        this.clock = clock;
        updateChrome();
        updateMain();
    }

    public synchronized void addListener(ModelListener listener) {
        listeners.add(listener);
        listener.onStateChanged(log.elements());
    }
    public void removeListener(ModelListener listener) { listeners.remove(listener); }
    public synchronized List<UiElement> uiElements() { return log.elements(); }
    public synchronized String[] stateAsLog() { return log.stateAsLog(); }
    public synchronized ProgressSummary progressSummary() {
        return new ProgressSummary(progress.lives(), progress.coins(), progress.completedCount(),
                progress.correctAnswers(), progress.wrongAnswers(), progress.streak(), progress.purchasedStats());
    }
    public synchronized boolean isActive(String feature) { return features.isOn(feature); }
    public synchronized Subject subject() { return Subject.fromFeature(features.selectedSubject()); }
    public synchronized Screen screen() { return screen; }
    public synchronized Difficulty currentDifficulty() { return difficulty; }
    public synchronized String notice() { return notice; }
    private LivesPolicy livesPolicy() { return features.isOn(FeatureManager.PREMIUM) ? premiumPolicy : freePolicy; }
    private LocalDate today() { return LocalDate.now(clock); }

    /** All-or-nothing feature transaction; the existing UI log is patched after a successful commit. */
    public synchronized int changeFeatures(FeatureChangeCommand command) {
        String previousSubject = features.selectedSubject();
        int result = features.execute(command, progress.purchasedStats());
        if (result != 0) return result;
        if (!features.selectedSubject().equals(previousSubject)) {
            activeQuestion = null;
            screen = Screen.HOME;
        }
        if (screen == Screen.STATS && !features.isOn(FeatureManager.STATS)) screen = Screen.HOME;
        if (screen == Screen.FLASHCARDS && !features.isOn(FeatureManager.FLASH)) screen = Screen.HOME;
        if (screen == Screen.SETTINGS && !features.isOn(FeatureManager.SETTINGS)) screen = Screen.HOME;
        notice = "Features changed successfully.";
        refresh();
        return 0;
    }

    /** Exposes a single action for navigating between views; checks access in the model. */
    public synchronized boolean open(Screen requested) {
        if (requested == Screen.STATS && !features.isOn(FeatureManager.STATS)) return false;
        if (requested == Screen.FLASHCARDS && !features.isOn(FeatureManager.FLASH)) return false;
        if (requested == Screen.SETTINGS && !features.isOn(FeatureManager.SETTINGS)) return false;
        if (requested == Screen.QUESTION || requested == Screen.RESULT) return false;
        screen = requested;
        activeQuestion = null;
        notice = "";
        refresh();
        return true;
    }

    public synchronized boolean startLesson(Difficulty requestedDifficulty) {
        if (requestedDifficulty == null) return false;
        if (!livesPolicy().canStart(progress)) {
            notice = "No lives remain. Use the shop to refill, or activate Premium.";
            screen = Screen.HOME;
            refresh();
            return false;
        }
        difficulty = requestedDifficulty;
        activeQuestion = lessons.get(subject(), difficulty);
        screen = Screen.QUESTION;
        notice = "";
        refresh();
        return true;
    }

    public synchronized boolean answer(int index) {
        if (screen != Screen.QUESTION || activeQuestion == null ||
                index < 0 || index >= activeQuestion.choices().size()) return false;
        boolean correct = activeQuestion.isCorrect(index);
        if (correct) {
            boolean firstTime = progress.recordCorrect(subject(), difficulty, today(),
                    features.isOn(FeatureManager.GOAL));
            notice = (firstTime ? "Correct! +4 coins (first completion). " : "Correct! Practice completed. ")
                    + activeQuestion.explanation();
        } else {
            progress.recordWrong();
            livesPolicy().wrongAnswer(progress);
            notice = "Not quite. " + activeQuestion.explanation();
        }
        activeQuestion = null;
        screen = Screen.RESULT;
        refresh();
        return correct;
    }

    /** Free-to-play users may buy one premium feature without buying the whole package. */
    public synchronized boolean buyStats() {
        if (features.isOn(FeatureManager.STATS)) {
            notice = "Statistics already available.";
            refresh();
            return false;
        }
        if (!progress.spendCoins(STATS_PRICE)) {
            notice = "Not enough coins for Statistics.";
            refresh();
            return false;
        }
        progress.unlockStats();
        int code = features.execute(new FeatureChangeCommand(List.of(), List.of(FeatureManager.STATS)), true);
        if (code != 0) throw new IllegalStateException("Purchased statistics could not activate: " + code);
        notice = "Statistics unlocked using earned coins.";
        refresh();
        return true;
    }

    public synchronized boolean buyPremium() {
        if (features.isOn(FeatureManager.PREMIUM)) return false;
        if (!progress.spendCoins(PREMIUM_PRICE)) {
            notice = "Not enough coins for Premium.";
            refresh();
            return false;
        }
        int code = features.execute(new FeatureChangeCommand(List.of(), List.of(FeatureManager.PREMIUM)),
                progress.purchasedStats());
        if (code != 0) throw new IllegalStateException("Premium could not activate: " + code);
        notice = "Premium active: infinite lives, statistics and profile skin.";
        refresh();
        return true;
    }

    public synchronized boolean refillLives() {
        if (progress.lives() == Progress.MAX_LIVES || !progress.spendCoins(REFILL_PRICE)) {
            notice = "Refill unavailable: you need missing lives and at least 5 coins.";
            refresh();
            return false;
        }
        progress.refillLives();
        notice = "Lives refilled!";
        refresh();
        return true;
    }

    private void refresh() {
        updateChrome(); // small regions are diffed in-place, not cleared
        updateMain();
        List<UiElement> state = log.elements();
        for (ModelListener listener : listeners) listener.onStateChanged(state);
    }

    private static UiElement element(Kind kind, String id, String text, Area area) {
        return new UiElement(kind, id, text, area);
    }
    private static UiElement button(String id, String text, Area area) {
        return element(Kind.BUTTON, id, text, area);
    }
    private static UiElement text(String id, String text) {
        return element(Kind.TEXT, id, text, Area.MAIN);
    }

    private void updateChrome() {
        List<UiElement> header = new ArrayList<>();
        header.add(element(Kind.HEADING, "app.title", "Smart Learning · SME-33", Area.HEADER));
        header.add(element(Kind.BADGE, "app.coins", "Coins: " + progress.coins(), Area.HEADER));
        header.add(element(Kind.BADGE, "app.lives", livesPolicy().label(progress), Area.HEADER));
        if (features.isOn(FeatureManager.PREMIUM))
            header.add(element(Kind.BADGE, "app.premium", "Premium", Area.HEADER));
        if (features.isOn(FeatureManager.STREAKS))
            header.add(element(Kind.BADGE, "app.streaks", "Streak: " + progress.streak() + " days", Area.HEADER));
        if (features.isOn(FeatureManager.GOAL))
            header.add(element(Kind.BADGE, "app.goal", "Daily goal: " + progress.todayCompletions(today()) + "/1", Area.HEADER));
        log.updateGroup("header", header);

        List<UiElement> navigation = new ArrayList<>();
        navigation.add(button("nav.home", "Home", Area.NAVIGATION));
        for (Subject subject : Subject.values()) {
            String marker = subject.featureName().equals(features.selectedSubject()) ? "● " : "○ ";
            navigation.add(button("nav.subject." + subject.name(), marker + subject.displayName(), Area.NAVIGATION));
        }
        navigation.add(button("nav.shop", "Shop", Area.NAVIGATION));
        navigation.add(button("nav.profile", "Profile", Area.NAVIGATION));
        if (features.isOn(FeatureManager.STATS)) navigation.add(button("nav.stats", "Statistics", Area.NAVIGATION));
        if (features.isOn(FeatureManager.FLASH)) navigation.add(button("nav.flashcards", "Flash cards", Area.NAVIGATION));
        if (features.isOn(FeatureManager.SETTINGS)) navigation.add(button("nav.settings", "Settings", Area.NAVIGATION));
        log.updateGroup("navigation", navigation);
    }

    private void updateMain() {
        List<UiElement> main = new ArrayList<>();
        switch (screen) {
            case HOME -> {
                main.add(element(Kind.HEADING, "screen.title", "Learn " + subject().displayName(), Area.MAIN));
                main.add(text("home.intro", "One short sample lesson per difficulty. Complete levels to earn coins."));
                for (Difficulty level : Difficulty.values()) {
                    String completed = progress.completed(subject(), level) ? " ✓" : "";
                    main.add(button("lesson." + level, level.name().charAt(0) +
                            level.name().substring(1).toLowerCase() + completed, Area.MAIN));
                }
            }
            case QUESTION -> {
                main.add(element(Kind.HEADING, "screen.title", subject().displayName() + " · " + difficulty, Area.MAIN));
                main.add(text("question.prompt", activeQuestion.prompt()));
                for (int i = 0; i < activeQuestion.choices().size(); i++)
                    main.add(button("answer." + i, (i + 1) + ". " + activeQuestion.choices().get(i), Area.MAIN));
            }
            case RESULT -> {
                main.add(element(Kind.HEADING, "screen.title", "Lesson result", Area.MAIN));
                main.add(button("lesson.retry", "Try this difficulty again", Area.MAIN));
                main.add(button("nav.home", "Back to lessons", Area.MAIN));
            }
            case SHOP -> {
                main.add(element(Kind.HEADING, "screen.title", "Shop", Area.MAIN));
                main.add(text("shop.intro", "Earn 4 coins on the first completion of each question."));
                if (!features.isOn(FeatureManager.STATS))
                    main.add(button("shop.stats", "Unlock statistics · 12 coins", Area.MAIN));
                if (!features.isOn(FeatureManager.PREMIUM))
                    main.add(button("shop.premium", "Unlock Premium · 24 coins", Area.MAIN));
                if (progress.lives() < Progress.MAX_LIVES)
                    main.add(button("shop.refill", "Refill lives · 5 coins", Area.MAIN));
                if (features.isOn(FeatureManager.PREMIUM)) main.add(text("shop.premiumowned", "Premium already active."));
            }
            case PROFILE -> {
                main.add(element(Kind.HEADING, "screen.title", "Your profile", Area.MAIN));
                main.add(text("profile.username", "User: Demo learner"));
                main.add(text("profile.completed", "Completed levels: " + progress.completedCount() + "/9"));
                if (features.isOn(FeatureManager.SKIN))
                    main.add(element(Kind.BADGE, "profile.skin", "Premium skin unlocked ★", Area.MAIN));
                main.add(text("profile.demo", "Feature toggles below are development controls for the M2 demonstration."));
                main.add(button("feature.premium", "Toggle Premium (developer demo)", Area.MAIN));
                main.add(button("feature.streaks", "Toggle streak counter", Area.MAIN));
                main.add(button("feature.daily", "Toggle daily goal", Area.MAIN));
                main.add(button("feature.theme", "Toggle dark theme", Area.MAIN));
                if (features.isOn(FeatureManager.PREMIUM))
                    main.add(button("feature.flash", "Toggle flash cards", Area.MAIN));
            }
            case STATS -> {
                main.add(element(Kind.HEADING, "screen.title", "Learning statistics", Area.MAIN));
                main.add(text("stats.correct", "Correct answers: " + progress.correctAnswers()));
                main.add(text("stats.wrong", "Incorrect answers: " + progress.wrongAnswers()));
                main.add(text("stats.completed", "Unique levels completed: " + progress.completedCount() + "/9"));
            }
            case FLASHCARDS -> {
                Question question = lessons.get(subject(), Difficulty.EASY);
                main.add(element(Kind.HEADING, "screen.title", "Flash card · " + subject().displayName(), Area.MAIN));
                main.add(text("flash.question", question.prompt()));
                main.add(text("flash.answer", "Answer: " + question.choices().get(question.correctIndex())));
                main.add(text("flash.explanation", question.explanation()));
            }
            case SETTINGS -> {
                main.add(element(Kind.HEADING, "screen.title", "Settings", Area.MAIN));
                main.add(text("settings.theme", "Theme: " + (features.isOn(FeatureManager.THEME) ? "Dark" : "Light")));
                main.add(button("feature.theme", "Toggle theme", Area.MAIN));
            }
        }
        if (!notice.isBlank()) main.add(text("system.notice", notice));
        log.updateGroup("main", main);
        log.updateGroup("footer", List.of(element(Kind.TEXT, "app.footer", "Prototype · no server or database",
                Area.FOOTER)));
    }
}
