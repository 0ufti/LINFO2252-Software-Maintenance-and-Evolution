// Command pattern, this is a fancy record
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
