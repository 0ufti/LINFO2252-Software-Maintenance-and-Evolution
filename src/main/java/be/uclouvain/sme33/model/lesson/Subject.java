package be.uclouvain.sme33.model.lesson;

/** The three mutually exclusive options in LEARNING SUBJECT (FeatureIDE ALT group). */
public enum Subject {
    ENGLISH("ENGLISH", "English"),
    PHYSICS("PHYSICS", "Physics"),
    GENERAL_KNOWLEDGE("GENERAL KNOWLEDGE", "General knowledge");

    private final String featureName;
    private final String displayName;
    Subject(String featureName, String displayName) {
        this.featureName = featureName;
        this.displayName = displayName;
    }
    public String featureName() { return featureName; }
    public String displayName() { return displayName; }
    public static Subject fromFeature(String name) {
        for (Subject subject : values()) if (subject.featureName.equals(name)) return subject;
        throw new IllegalArgumentException("Unknown subject " + name);
    }
}
