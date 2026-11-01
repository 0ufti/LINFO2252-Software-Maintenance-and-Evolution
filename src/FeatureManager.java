import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

// Handles optional features. The names below come from our M1 feature model.
// The full XML constraints are NOT checked automatically yet (WIP).
public class FeatureManager {
    private static final Set<String> SUBJECTS = Set.of("ENGLISH", "PHYSICS", "GENERAL KNOWLEDGE");
    private static final Set<String> OPTIONAL = Set.of(
        "PREMIUM", "STATS", "PROFILE SKIN", "FLASH CARD", "STREAKS",
        "DAILY GOAL", "SETTING", "GRAFICS", "THEME"
    );
    // All the names in the XML. Some are not implemented yet.
    private static final Set<String> MODEL_NAMES = new HashSet<>(Arrays.asList(
        "SMART LEARNING SYSTEM", "PROFILE", "PROFILE INFO", "USER NAME", "STATS",
        "PROFILE SKIN", "CONTENT", "LEVELS", "DIFFICULTIES", "EASY", "MEDIUM",
        "HARD", "SUBJECT / THEME", "RULES", "STREAKS", "LEADERBROAD", "BY COUNTRY",
        "FOR THE WORLD", "LEARNING SUBJECT", "ENGLISH", "GENERAL KNOWLEDGE", "PHYSICS",
        "PASS", "FREE TO PLAY", "LIMITED LIVE", "ECONOMY", "INGAME CURRENCY",
        "SHOP", "DAILY GOAL", "PREMIUM", "INFINITE LIVE", "FLASH CARD", "SETTING",
        "SOUND", "VOLUME", "SOUND OUTPUT", "GRAFICS", "THEME", "RESOLUTION"
    ));

    private Set<String> active = new HashSet<>();
    private String subject = "ENGLISH";

    public String getSubject() { return subject; }
    public boolean isOn(String name) {
        name = name.toUpperCase();
        if (SUBJECTS.contains(name)) return subject.equals(name);
        return active.contains(name);
    }

    // A return code instead of exceptions, because ControllerInterface asks for one.
    // 0 success / 1 unknown / 2 not implemented / 3 bad combination / 4 locked.
    public int change(String[] off, String[] on, boolean statsBought) {
        if (off == null || on == null) return 3;
        Set<String> disable = new HashSet<>();
        Set<String> enable = new HashSet<>();

        for (String s : off) {
            if (s == null || s.trim().isEmpty()) return 3;
            String name = s.trim().toUpperCase();
            if (!MODEL_NAMES.contains(name)) return 1;
            if (!OPTIONAL.contains(name) && !SUBJECTS.contains(name)) return 2;
            if (!disable.add(name)) return 3;
        }
        for (String s : on) {
            if (s == null || s.trim().isEmpty()) return 3;
            String name = s.trim().toUpperCase();
            if (!MODEL_NAMES.contains(name)) return 1;
            if (!OPTIONAL.contains(name) && !SUBJECTS.contains(name)) return 2;
            if (!enable.add(name) || disable.contains(name)) return 3;
        }

        // First calculate everything on a copy, then apply it all at once.
        Set<String> newActive = new HashSet<>(active);
        String newSubject = subject;
        int subjectCount = 0;
        for (String s : enable) {
            if (SUBJECTS.contains(s)) {
                newSubject = s;
                subjectCount++;
            } else newActive.add(s);
        }
        if (subjectCount > 1 || disable.contains(newSubject)) return 3;
        for (String s : disable) if (OPTIONAL.contains(s)) newActive.remove(s);

        if (!newActive.contains("PREMIUM") && enable.contains("PROFILE SKIN")) return 4;
        if (!newActive.contains("PREMIUM") && enable.contains("FLASH CARD")) return 4;
        if (!newActive.contains("PREMIUM") && enable.contains("STATS") && !statsBought) return 4;
        if (newActive.contains("PREMIUM")) {
            if (disable.contains("STATS") || disable.contains("PROFILE SKIN")) return 3;
            newActive.add("STATS");
            newActive.add("PROFILE SKIN");
        } else {
            newActive.remove("PROFILE SKIN");
            newActive.remove("FLASH CARD");
            if (!statsBought) newActive.remove("STATS");
        }

        if (disable.contains("SETTING") && (enable.contains("GRAFICS") || enable.contains("THEME"))) return 3;
        if (disable.contains("GRAFICS") && enable.contains("THEME")) return 3;
        if (disable.contains("SETTING")) { newActive.remove("GRAFICS"); newActive.remove("THEME"); }
        if (disable.contains("GRAFICS")) newActive.remove("THEME");
        if (newActive.contains("THEME")) newActive.add("GRAFICS");
        if (newActive.contains("GRAFICS")) newActive.add("SETTING");

        // Commit - an invalid request never changes anything.
        active = newActive;
        subject = newSubject;
        return 0;
    }
}
