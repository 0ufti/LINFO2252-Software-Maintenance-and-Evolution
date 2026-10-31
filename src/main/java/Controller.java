import be.uclouvain.sme33.model.LearningModel;
import be.uclouvain.sme33.model.feature.FeatureChangeCommand;
import be.uclouvain.sme33.model.feature.FeatureManager;
import be.uclouvain.sme33.model.lesson.Difficulty;
import be.uclouvain.sme33.model.lesson.Subject;
import be.uclouvain.sme33.model.progress.ProgressSummary;
import be.uclouvain.sme33.view.SwingView;
import java.awt.GraphicsEnvironment;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import javax.swing.SwingUtilities;

/**
 * CONTROLLER: translates UI/CLI actions into Model service calls.
 * Default package is deliberate: the instructor's unchanged interface has no package.
 */
public final class Controller implements ControllerInterface {
    private final LearningModel model;
    private SwingView view;

    public Controller() { this(new LearningModel()); }
    public Controller(LearningModel model) { this.model = model; }

    @Override public int activate(String[] deactivations, String[] activations) {
        if (deactivations == null || activations == null) return 3;
        return model.changeFeatures(new FeatureChangeCommand(Arrays.asList(deactivations),
                Arrays.asList(activations)));
    }

    @Override public synchronized boolean enableUIView() {
        if (GraphicsEnvironment.isHeadless()) return false;
        if (view != null && view.isOpen()) return true;
        try {
            SwingView next = new SwingView(model, this::performUiAction);
            if (SwingUtilities.isEventDispatchThread()) next.show();
            else SwingUtilities.invokeAndWait(next::show);
            view = next;
            return true;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return false;
        } catch (InvocationTargetException exception) {
            return false;
        }
    }

    @Override public synchronized boolean disableUIView() {
        if (view == null) return true;
        SwingView current = view;
        view = null;
        if (SwingUtilities.isEventDispatchThread()) current.close();
        else {
            try { SwingUtilities.invokeAndWait(current::close); }
            catch (InterruptedException exception) { Thread.currentThread().interrupt(); return false; }
            catch (InvocationTargetException exception) { return false; }
        }
        return true;
    }

    @Override public String[] getStateAsLog() { return model.stateAsLog(); }

    // Convenience methods allow tests and the console to drive the exact same model as Swing.
    public boolean chooseSubject(Subject subject) {
        return activate(new String[0], new String[]{subject.featureName()}) == 0;
    }
    public boolean startLesson(Difficulty difficulty) { return model.startLesson(difficulty); }
    public boolean submitAnswer(int index) { return model.answer(index); }
    public boolean open(LearningModel.Screen screen) { return model.open(screen); }
    public boolean buyStats() { return model.buyStats(); }
    public boolean buyPremium() { return model.buyPremium(); }
    public boolean refillLives() { return model.refillLives(); }
    public boolean isActive(String feature) { return model.isActive(feature); }
    public ProgressSummary progressSummary() { return model.progressSummary(); }
    public String notice() { return model.notice(); }
    public LearningModel.Screen currentScreen() { return model.screen(); }

    /** Stable IDs are dispatched here, never embedded business logic in Swing listeners. */
    public void performUiAction(String id) {
        if (id.startsWith("nav.subject.")) {
            chooseSubject(Subject.valueOf(id.substring("nav.subject.".length())));
        } else if (id.startsWith("lesson.") && !id.equals("lesson.retry")) {
            startLesson(Difficulty.valueOf(id.substring("lesson.".length())));
        } else if (id.startsWith("answer.")) {
            submitAnswer(Integer.parseInt(id.substring("answer.".length())));
        } else if (id.startsWith("feature.")) {
            String feature = switch (id) {
                case "feature.premium" -> FeatureManager.PREMIUM;
                case "feature.streaks" -> FeatureManager.STREAKS;
                case "feature.daily" -> FeatureManager.GOAL;
                case "feature.theme" -> FeatureManager.THEME;
                case "feature.flash" -> FeatureManager.FLASH;
                default -> throw new IllegalArgumentException("Unknown UI action: " + id);
            };
            boolean enabled = model.isActive(feature);
            activate(enabled ? new String[]{feature} : new String[0],
                    enabled ? new String[0] : new String[]{feature});
        } else {
            switch (id) {
                case "nav.home" -> open(LearningModel.Screen.HOME);
                case "nav.shop" -> open(LearningModel.Screen.SHOP);
                case "nav.profile" -> open(LearningModel.Screen.PROFILE);
                case "nav.stats" -> open(LearningModel.Screen.STATS);
                case "nav.flashcards" -> open(LearningModel.Screen.FLASHCARDS);
                case "nav.settings" -> open(LearningModel.Screen.SETTINGS);
                case "lesson.retry" -> startLesson(model.currentDifficulty());
                case "shop.stats" -> buyStats();
                case "shop.premium" -> buyPremium();
                case "shop.refill" -> refillLives();
                default -> throw new IllegalArgumentException("Unknown UI action: " + id);
            }
        }
    }
}
