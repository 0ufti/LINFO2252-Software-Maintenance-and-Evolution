package com.example.smartlearning;

import java.util.EnumSet;
import java.util.Set;

public final class FeatureModel {
    private final Set<Feature> enabled = EnumSet.noneOf(Feature.class);

    public FeatureModel() {
        enabled.addAll(EnumSet.of(
                Feature.PROFILE, Feature.PROFILE_INFO, Feature.USER_NAME,
                Feature.CONTENT, Feature.LEVELS, Feature.EASY, Feature.MEDIUM,
                Feature.HARD, Feature.SUBJECT_THEME, Feature.RULES,
                Feature.LIMITED_LIVE, Feature.ECONOMY, Feature.INGAME_CURRENCY,
                Feature.SHOP, Feature.LEARNING_SUBJECT, Feature.ENGLISH,
                Feature.SETTING, Feature.THEME));
    }

    public boolean isEnabled(Feature feature) {
        return enabled.contains(feature);
    }

    public void enable(Feature feature) {
        validate(feature, true);
        enabled.add(feature);
    }

    public void disable(Feature feature) {
        validate(feature, false);
        enabled.remove(feature);
    }

    public Set<Feature> enabledFeatures() {
        return EnumSet.copyOf(enabled);
    }

    private void validate(Feature feature, boolean enabling) {
        if (feature == null) {
            throw new IllegalArgumentException("Feature inconnue");
        }
        if (!enabling && isMandatory(feature)) {
            throw new IllegalStateException("La feature obligatoire ne peut pas être désactivée : "
                    + feature.displayName());
        }
        if (enabling && isSubject(feature)) {
            enabled.removeIf(this::isSubject);
        }
    }

    private boolean isMandatory(Feature feature) {
        return switch (feature) {
            case PROFILE, PROFILE_INFO, USER_NAME, CONTENT, LEVELS, EASY, MEDIUM,
                    HARD, SUBJECT_THEME, RULES, LIMITED_LIVE, ECONOMY,
                    INGAME_CURRENCY, SHOP, LEARNING_SUBJECT, SETTING -> true;
            default -> false;
        };
    }

    private boolean isSubject(Feature feature) {
        return feature == Feature.ENGLISH
                || feature == Feature.GENERAL_KNOWLEDGE
                || feature == Feature.PHYSICS;
    }
}
