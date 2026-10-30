package com.example.smartlearning;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

public final class SmartLearningView extends JFrame {
    private final SmartLearningController controller;
    private final JLabel livesLabel = new JLabel();
    private final JLabel scoreLabel = new JLabel();
    private final JLabel questionLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel feedbackLabel = new JLabel("", SwingConstants.CENTER);
    private final JTextField userNameField = new JTextField("Student");
    private final JComboBox<String> subjectBox = new JComboBox<>(new String[]{"ENGLISH", "GENERAL KNOWLEDGE", "PHYSICS"});
    private final JComboBox<String> difficultyBox = new JComboBox<>(new String[]{"EASY", "MEDIUM", "HARD"});
    private int lives = 3;
    private int score;

    public SmartLearningView(SmartLearningController controller) {
        super("Smart Learning System");
        this.controller = controller;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(560, 360));
        setLocationByPlatform(true);
        buildLayout();
        refreshFromController();
    }

    public void refreshFromController() {
        livesLabel.setText("Vies : " + lives);
        scoreLabel.setText("Score : " + score);
        boolean dark = controller.isEnabled(Feature.THEME);
        Color background = dark ? new Color(35, 38, 45) : Color.WHITE;
        Color foreground = dark ? Color.WHITE : Color.BLACK;
        getContentPane().setBackground(background);
        applyColors(getContentPane(), background, foreground);
    }

    private void buildLayout() {
        JPanel root = new JPanel(new BorderLayout(8, 8));
        root.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel profile = new JPanel(new GridLayout(2, 4, 8, 4));
        profile.add(new JLabel("Nom :"));
        profile.add(userNameField);
        profile.add(new JLabel("Sujet :"));
        profile.add(subjectBox);
        profile.add(new JLabel("Difficulté :"));
        profile.add(difficultyBox);
        profile.add(livesLabel);
        profile.add(scoreLabel);
        root.add(profile, BorderLayout.NORTH);

        JPanel quiz = new JPanel(new BorderLayout(8, 8));
        questionLabel.setText("Quelle est la capitale de la France ?");
        quiz.add(questionLabel, BorderLayout.CENTER);
        feedbackLabel.setText("Choisissez une réponse pour commencer.");
        quiz.add(feedbackLabel, BorderLayout.SOUTH);

        JPanel actions = new JPanel(new GridLayout(2, 2, 8, 8));
        addAnswerButton(actions, "Paris", true);
        addAnswerButton(actions, "Bruxelles", false);
        addAnswerButton(actions, "Londres", false);
        JButton themeButton = new JButton("Changer le thème");
        themeButton.addActionListener(event -> toggleTheme());
        actions.add(themeButton);
        quiz.add(actions, BorderLayout.SOUTH);
        root.add(quiz, BorderLayout.CENTER);

        JButton logsButton = new JButton("Afficher l'état dans la console");
        logsButton.addActionListener(event -> {
            for (String line : controller.getStateAsLog()) {
                System.out.println(line);
            }
        });
        root.add(logsButton, BorderLayout.SOUTH);
        setContentPane(root);
        pack();
    }

    private void addAnswerButton(JPanel parent, String answer, boolean correct) {
        JButton button = new JButton(answer);
        button.addActionListener(event -> {
            if (correct) {
                score += 10;
                feedbackLabel.setText("Bonne réponse, " + userNameField.getText() + " !");
            } else {
                lives--;
                feedbackLabel.setText("Réponse incorrecte.");
            }
            refreshFromController();
        });
        parent.add(button);
    }

    private void toggleTheme() {
        if (controller.isEnabled(Feature.THEME)) {
            controller.activate(new String[]{"THEME"}, new String[0]);
        } else {
            controller.activate(new String[0], new String[]{"THEME"});
        }
    }

    private void applyColors(java.awt.Component component, Color background, Color foreground) {
        component.setBackground(background);
        component.setForeground(foreground);
        if (component instanceof JPanel panel) {
            for (java.awt.Component child : panel.getComponents()) {
                applyColors(child, background, foreground);
            }
        }
    }
}
