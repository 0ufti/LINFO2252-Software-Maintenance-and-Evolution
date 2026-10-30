package com.example.smartlearning;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import javax.swing.SwingUtilities;

public final class SmartLearningController implements ControllerInterface {
    private final FeatureModel featureModel = new FeatureModel();
    private SmartLearningView view;

    @Override
    public int activate(String[] deactivations, String[] activations) {
        try {
            for (String name : safeArray(deactivations)) {
                featureModel.disable(requireFeature(name));
            }
            for (String name : safeArray(activations)) {
                featureModel.enable(requireFeature(name));
            }
            refreshView();
            return 0;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            System.err.println("Activation refusée : " + exception.getMessage());
            return 1;
        }
    }

    @Override
    public boolean enableUIView() {
        if (view == null) {
            view = new SmartLearningView(this);
        }
        SwingUtilities.invokeLater(() -> view.setVisible(true));
        return true;
    }

    @Override
    public boolean disableUIView() {
        if (view == null) {
            return true;
        }
        SwingUtilities.invokeLater(() -> view.setVisible(false));
        return true;
    }

    @Override
    public String[] getStateAsLog() {
        Set<Feature> features = featureModel.enabledFeatures();
        List<String> state = new ArrayList<>();
        features.stream()
                .sorted(Comparator.comparing(Feature::displayName))
                .forEach(feature -> state.add("FEATURE_ENABLED=" + feature.displayName()));
        return state.toArray(String[]::new);
    }

    public boolean isEnabled(Feature feature) {
        return featureModel.isEnabled(feature);
    }

    public FeatureModel featureModel() {
        return featureModel;
    }

    private void refreshView() {
        if (view != null) {
            SwingUtilities.invokeLater(view::refreshFromController);
        }
    }

    private Feature requireFeature(String name) {
        Feature feature = Feature.fromName(name);
        if (feature == null) {
            throw new IllegalArgumentException("Feature absente du modèle : " + name);
        }
        return feature;
    }

    private String[] safeArray(String[] values) {
        return values == null ? new String[0] : values;
    }
}
