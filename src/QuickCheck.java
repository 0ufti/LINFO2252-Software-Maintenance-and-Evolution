// Small tests without JUnit, useful if Maven is not installed.
public class QuickCheck {
    private static int checks = 0;
    private static void test(boolean ok, String message) {
        checks++;
        if (!ok) throw new AssertionError(message);
    }

    public static void main(String[] args) {
        Controller c = new Controller();
        test(c.getSubject().equals("ENGLISH"), "Initial subject");
        test(c.chooseSubject("PHYSICS"), "Switch physics");
        test(c.startLesson("EASY"), "Start physics");
        test(c.answer(1), "Physics correct");
        test(c.getModel().getProgress().getCoins() == 10, "Reward");
        test(c.startLesson("EASY"), "Repeat physics");
        test(c.answer(1), "Repeat answer");
        test(c.getModel().getProgress().getCoins() == 10, "No repeat coins");
        test(c.activate(new String[0], new String[]{"STATS"}) == 4, "Stats restricted");
        test(c.activate(new String[0], new String[]{"PREMIUM"}) == 0, "Premium");
        test(c.getModel().getLivesText().equals("Lives: unlimited"), "Unlimited lives");
        test(c.getModel().isOn("STATS"), "Premium implies stats");
        test(c.getModel().isOn("PROFILE SKIN"), "Premium implies skin");
        test(c.open("STATS"), "Stats page");
        test(c.activate(new String[]{"PREMIUM"}, new String[0]) == 0, "Disable premium");
        test(!c.getModel().isOn("STATS"), "Stats hidden after premium");
        test(c.activate(new String[0], new String[]{"PREMIUM", "NOT_REAL"}) == 1, "Unknown name");
        test(!c.getModel().isOn("PREMIUM"), "No partial activation");
        test(c.activate(new String[0], new String[]{"THEME"}) == 0, "Theme on");
        test(c.getModel().isOn("SETTING"), "Theme parent setting");
        test(c.activate(new String[]{"SETTING"}, new String[0]) == 0, "Disable settings");
        test(!c.getModel().isOn("THEME"), "Disable nested theme");
        System.out.println("QuickCheck: " + checks + " checks passed.");
    }
}
