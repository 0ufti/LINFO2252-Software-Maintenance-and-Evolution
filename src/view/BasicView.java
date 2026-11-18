package view;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import controller.Controller;
import model.LearningModel;
import model.UiLog;

// for now we can only switch pages
public class BasicView {
    private Controller controller;
    private LearningModel model;
    private JFrame frame;
    private JPanel menu;
    private JLabel pageTitle;
    private Runnable update;

    public BasicView(Controller controller, LearningModel model) {
        this.controller = controller;
        this.model = model;
        update = () -> SwingUtilities.invokeLater(() -> refreshPage());
    }

    public void show() {
        frame = new JFrame("Smart Learning - SME33");
        frame.setSize(550, 300);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // buttons for changing pages
        menu = new JPanel();
        frame.add(menu, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(2, 1));
        pageTitle = new JLabel("", SwingConstants.CENTER);
        center.add(pageTitle);
        center.add(new JLabel("Use terminal for now.", SwingConstants.CENTER));
        frame.add(center, BorderLayout.CENTER);

        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                model.removeListener(update);
            }
        });

        frame.setVisible(true);
        model.addListener(update);
        refreshPage();
    }

    private void refreshPage() {
        if (frame == null || !frame.isDisplayable()) {
            return;
        }

        pageTitle.setText("Current page: " + model.getScreen());
        menu.removeAll();

        // create nav buttons based on logs
        List<UiLog.Item> items = model.getItems();
        for (UiLog.Item i : items) {
            if (i.place.equals("NAV") && i.type.equals("button")) {
                JButton button = new JButton(i.text);
                button.addActionListener(e -> controller.buttonClicked(i.id));
                menu.add(button);
            }
        }

        menu.revalidate();
        menu.repaint();
    }

    public boolean isOpen() {
        return frame != null && frame.isDisplayable();
    }

    public void close() {
        model.removeListener(update);
        if (frame != null) {
            frame.dispose();
        }
        frame = null;
    }
}
