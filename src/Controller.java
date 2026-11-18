import java.awt.GraphicsEnvironment;
import javax.swing.SwingUtilities;

// Controller: takes actions from input
public class Controller implements ControllerInterface {
    private LearningModel model;
    private BasicView view;

    public Controller() { 
        model = new LearningModel(); 
    }
    public Controller(LearningModel model) { 
        this.model = model; 
    }

    // implement interface
    @Override
    public int activate(String[] deactivations, String[] activations) {
        return model.changeFeatures(new FeatureCommand(deactivations, activations));
    }

    @Override
    public String[] getStateAsLog() { return model.getLogs(); }

    @Override
    public boolean enableUIView() {
        if (GraphicsEnvironment.isHeadless()) return false;
        if (view != null && view.isOpen()) return true;
        if (view != null) view.close();

        try {
            SwingUtilities.invokeAndWait(() -> {
                view = new BasicView(this, model);
                view.show();
            });
            return true;
        } catch (Exception e) {
            System.out.println("Could not open window: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean disableUIView() {
        if (view == null) return true;
        BasicView oldView = view;
        view = null;
        SwingUtilities.invokeLater(() -> oldView.close());
        return true;
    }

    // main behaviour, links inputs to model
    public boolean chooseSubject(String name) { return activate(new String[0], new String[]{name}) == 0; }

    public boolean startLesson(String difficulty) { return model.startLesson(difficulty); }

    public boolean answer(int index) { return model.answer(index); }

    public boolean open(String screen) { return model.open(screen); }

    public boolean buy(String item) { return model.buy(item); }

    public String getScreen() { return model.getScreen(); }

    public String getSubject() { return model.getSubject(); }

    public String getMessage() { return model.getMessage(); }

    public Question getQuestion() { return model.getQuestion(); }

    public LearningModel getModel() { return model; }

    // UI links
    public void buttonClicked(String id) {
        if (id.equals("home") || id.equals("shop") || id.equals("profile") ||
            id.equals("stats") || id.equals("flashcards") || id.equals("settings")) {
            open(id);
        } else if (id.startsWith("subject.")) {
            String name = id.substring(8);
            if (name.equals("general")) name = "GENERAL KNOWLEDGE";
            chooseSubject(name);
        } else if (id.startsWith("lesson.")) {
            startLesson(id.substring(7));
        } else if (id.startsWith("answer.")) {
            answer(Integer.parseInt(id.substring(7)));
        } else if (id.startsWith("buy.")) {
            buy(id.substring(4));
        } else if (id.equals("retry")) {
            startLesson(model.getDifficulty());
        }
    }
}
