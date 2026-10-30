package com.example.smartlearning;

import java.util.Locale;

public enum Feature {
    PROFILE("PROFILE"),
    PROFILE_INFO("PROFILE INFO"),
    USER_NAME("USER NAME"),
    STATS("STATS"),
    CONTENT("CONTENT"),
    LEVELS("LEVELS"),
    EASY("EASY"),
    MEDIUM("MEDIUM"),
    HARD("HARD"),
    SUBJECT_THEME("SUBJECT / THEME"),
    RULES("RULES"),
    LIMITED_LIVE("LIMITED LIVE"),
    ECONOMY("ECONOMY"),
    INGAME_CURRENCY("INGAME CURRENCY"),
    SHOP("SHOP"),
    LEARNING_SUBJECT("LEARNING SUBJECT"),
    ENGLISH("ENGLISH"),
    GENERAL_KNOWLEDGE("GENERAL KNOWLEDGE"),
    PHYSICS("PHYSICS"),
    SETTING("SETTING"),
    THEME("THEME");

    private final String displayName;

    Feature(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    public static Feature fromName(String name) {
        String normalized = name.trim().toUpperCase(Locale.ROOT).replace('_', ' ');
        for (Feature feature : values()) {
            if (feature.displayName.equals(normalized) || feature.name().equals(normalized.replace(' ', '_'))) {
                return feature;
            }
        }
        return null;
    }
}
