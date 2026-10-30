package com.example.smartlearning;

public interface ControllerInterface {
    int activate(String[] deactivations, String[] activations);

    boolean enableUIView();

    boolean disableUIView();

    String[] getStateAsLog();
}
