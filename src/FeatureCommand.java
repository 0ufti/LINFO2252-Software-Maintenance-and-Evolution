// Command pattern, basic version: group several feature changes in one request.
public class FeatureCommand {
    private String[] toDisable;
    private String[] toEnable;

    public FeatureCommand(String[] toDisable, String[] toEnable) {
        this.toDisable = toDisable;
        this.toEnable = toEnable;
    }

    public int execute(FeatureManager manager, boolean statsBought) {
        return manager.change(toDisable, toEnable, statsBought);
    }
}
