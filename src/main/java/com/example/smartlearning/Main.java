package com.example.smartlearning;

import javax.swing.SwingUtilities;

public final class Main {
    private Main() {
    }

    public static void main(String[] args) {
        SmartLearningController controller = new SmartLearningController();
        SwingUtilities.invokeLater(controller::enableUIView);
    }
}
