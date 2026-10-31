package be.uclouvain.sme33.model.feature;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Validates a proposed configuration on a copy and commits only if every condition passes.
 * This is an atomic transaction: a rejected command never partially modifies the feature set.
 *
 * Return codes: 0 success, 1 unknown name, 2 declared but not in prototype,
 * 3 invalid transition/mandatory feature, 4 access not purchased.
 *
 * The implementation intentionally handles a subset of the feature model for Mission 2;
 * a declared but unimplemented feature gets code 2 instead of silently doing nothing.
 */
public final class FeatureManager {
    public static final String PREMIUM = "PREMIUM";
    public static final String STATS = "STATS";
    public static final String SKIN = "PROFILE SKIN";
    public static final String FLASH = "FLASH CARD";
    public static final String STREAKS = "STREAKS";
    public static final String GOAL = "DAILY GOAL";
    public static final String SETTINGS = "SETTING";
    public static final String GRAPHICS = "GRAFICS"; // exact spelling from original XML
    public static final String THEME = "THEME";
    public static final String ENGLISH = "ENGLISH";
    public static final String PHYSICS = "PHYSICS";
    public static final String GENERAL = "GENERAL KNOWLEDGE";
    private static final Set<String> SUBJECTS = Set.of(ENGLISH, PHYSICS, GENERAL);
    private static final Set<String> OPTIONAL = Set.of(PREMIUM, STATS, SKIN, FLASH,
            STREAKS, GOAL, SETTINGS, GRAPHICS, THEME);

    private final FeatureCatalogue catalogue;
    private Set<String> enabled = new LinkedHashSet<>();
    private String selectedSubject = ENGLISH;

    public FeatureManager(FeatureCatalogue catalogue) { this.catalogue = catalogue; }

    public boolean isOn(String feature) {
        return SUBJECTS.contains(feature) ? selectedSubject.equals(feature) : enabled.contains(feature);
    }
    public String selectedSubject() { return selectedSubject; }
    public Set<String> enabledOptionalFeatures() { return Set.copyOf(enabled); }

    /** purchasedStats is an explicit exception: paid-free users can access statistics. */
    public int execute(FeatureChangeCommand command, boolean purchasedStats) {
        Set<String> turnOff = normalize(command.deactivate());
        Set<String> turnOn = normalize(command.activate());
        if (turnOff == null || turnOn == null) return 3;
        for (String name : turnOff) {
            if (!catalogue.contains(name)) return 1;
            if (!OPTIONAL.contains(name) && !SUBJECTS.contains(name)) return 2;
        }
        for (String name : turnOn) {
            if (!catalogue.contains(name)) return 1;
            if (!OPTIONAL.contains(name) && !SUBJECTS.contains(name)) return 2;
        }
        for (String name : turnOff) if (turnOn.contains(name)) return 3;

        Set<String> next = new LinkedHashSet<>(enabled);
        String nextSubject = selectedSubject;
        if (turnOn.stream().filter(SUBJECTS::contains).count() > 1) return 3;
        String requestedSubject = turnOn.stream().filter(SUBJECTS::contains).findFirst().orElse(null);
        if (requestedSubject != null) nextSubject = requestedSubject;
        if (turnOff.contains(nextSubject)) return 3; // ALT must have exactly one selected subject
        for (String feature : turnOff) if (OPTIONAL.contains(feature)) next.remove(feature);
        for (String feature : turnOn) if (OPTIONAL.contains(feature)) next.add(feature);

        // Reject attempted access to a premium-only feature before applying cascading rules.
        if (!next.contains(PREMIUM) && turnOn.contains(SKIN)) return 4;
        if (!next.contains(PREMIUM) && turnOn.contains(FLASH)) return 4;
        if (!next.contains(PREMIUM) && turnOn.contains(STATS) && !purchasedStats) return 4;
        // Dependent feature rules from the M1 text. XML does not currently encode these rules.
        if (next.contains(PREMIUM)) {
            next.add(STATS);
            next.add(SKIN);
            if (turnOff.contains(STATS) || turnOff.contains(SKIN)) return 3;
        } else {
            next.remove(FLASH);
            if (!purchasedStats) next.remove(STATS);
            next.remove(SKIN);
        }
        if (next.contains(STATS) && !next.contains(PREMIUM) && !purchasedStats) return 4;
        if (next.contains(FLASH) && !next.contains(PREMIUM)) return 4;
        if (next.contains(THEME)) { next.add(GRAPHICS); next.add(SETTINGS); }
        if (next.contains(GRAPHICS)) next.add(SETTINGS);
        if (!next.contains(SETTINGS)) { next.remove(GRAPHICS); next.remove(THEME); }
        if (!next.contains(GRAPHICS)) next.remove(THEME);
        // Don't allow an explicit parent deactivation to be cancelled silently by a child activation.
        if (turnOff.contains(SETTINGS) && (turnOn.contains(GRAPHICS) || turnOn.contains(THEME))) return 3;
        if (turnOff.contains(GRAPHICS) && turnOn.contains(THEME)) return 3;
        // When a parent is explicitly switched off, its descendants must be switched off too.
        if (turnOff.contains(SETTINGS)) { next.remove(GRAPHICS); next.remove(THEME); }
        if (turnOff.contains(GRAPHICS)) next.remove(THEME);
        enabled = next;
        selectedSubject = nextSubject;
        return 0;
    }

    private Set<String> normalize(Iterable<String> input) {
        if (input == null) return null;
        Set<String> result = new LinkedHashSet<>();
        for (String name : input) {
            if (name == null || name.isBlank()) return null;
            String canonical = name.trim().replaceAll("\\s+", " ").toUpperCase(Locale.ROOT);
            if (!result.add(canonical)) return null; // reject duplicates rather than hiding mistakes
        }
        return result;
    }
}
