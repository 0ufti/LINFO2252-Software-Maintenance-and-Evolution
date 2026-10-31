package be.uclouvain.sme33.view;

import be.uclouvain.sme33.model.LearningModel;
import be.uclouvain.sme33.model.ModelListener;
import be.uclouvain.sme33.model.UiElement;
import be.uclouvain.sme33.model.feature.FeatureManager;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.util.List;
import java.util.function.Consumer;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

/**
 * VIEW: renders the exact UiElements also exposed as textual logs.
 * It never checks answers, awards coins or changes features directly.
 * The controller owns the callback that interprets clicked element IDs.
 */
public final class SwingView implements ModelListener {
    private final LearningModel model;
    private final Consumer<String> actionHandler;
    private JFrame frame;

    public SwingView(LearningModel model, Consumer<String> actionHandler) {
        this.model = model;
        this.actionHandler = actionHandler;
    }

    /** Must be called on Swing's Event Dispatch Thread. */
    public void show() {
        frame = new JFrame("Smart Learning · SME-33");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(680, 490));
        frame.setSize(940, 640);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent event) {
                model.removeListener(SwingView.this);
            }
        });
        frame.setVisible(true);
        model.addListener(this);
    }

    public void close() {
        model.removeListener(this);
        if (frame != null) frame.dispose();
        frame = null;
    }
    public boolean isOpen() { return frame != null && frame.isDisplayable(); }

    @Override public void onStateChanged(List<UiElement> elements) {
        // The model can be called by the CLI thread; Swing mutations MUST run on the EDT.
        if (SwingUtilities.isEventDispatchThread()) draw(elements);
        else SwingUtilities.invokeLater(() -> draw(elements));
    }

    private void draw(List<UiElement> elements) {
        if (frame == null || !frame.isDisplayable()) return;
        boolean dark = model.isActive(FeatureManager.THEME);
        Color background = dark ? new Color(29, 34, 48) : new Color(246, 248, 252);
        Color panelColor = dark ? new Color(41, 47, 65) : Color.WHITE;
        Color foreground = dark ? new Color(238, 240, 250) : new Color(26, 39, 61);
        Color accent = new Color(74, 120, 230);

        JPanel root = new JPanel(new BorderLayout(14, 14));
        root.setBackground(background);
        root.setBorder(BorderFactory.createEmptyBorder(14, 16, 14, 16));
        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 5));
        JPanel navigation = vertical(panelColor);
        JPanel content = vertical(panelColor);
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.LEFT));
        header.setBackground(background);
        footer.setBackground(background);
        navigation.setPreferredSize(new Dimension(220, 0));

        for (UiElement element : elements) {
            JPanel target = switch (element.area()) {
                case HEADER -> header;
                case NAVIGATION -> navigation;
                case MAIN -> content;
                case FOOTER -> footer;
            };
            if (element.area() == UiElement.Area.MAIN || element.area() == UiElement.Area.NAVIGATION)
                target.add(Box.createVerticalStrut(7));
            switch (element.kind()) {
                case BUTTON -> {
                    JButton button = new JButton(element.text());
                    button.setActionCommand(element.id());
                    button.addActionListener(event -> actionHandler.accept(event.getActionCommand()));
                    button.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 14));
                    button.setFocusPainted(false);
                    button.setBackground(accent);
                    button.setForeground(Color.WHITE);
                    button.setBorder(BorderFactory.createCompoundBorder(
                            BorderFactory.createLineBorder(accent, 1, true),
                            BorderFactory.createEmptyBorder(10, 12, 10, 12)));
                    button.setAlignmentX(0);
                    if (target == header || target == footer) target.add(button);
                    else {
                        button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
                        target.add(button);
                    }
                }
                case HEADING, TEXT, BADGE -> {
                    JLabel label = new JLabel(escapeHtml(element.text()));
                    label.setForeground(foreground);
                    label.setFont(new Font(Font.SANS_SERIF,
                            element.kind() == UiElement.Kind.HEADING ? Font.BOLD : Font.PLAIN,
                            element.kind() == UiElement.Kind.HEADING ? 22 : 14));
                    if (element.kind() == UiElement.Kind.BADGE)
                        label.setBorder(BorderFactory.createCompoundBorder(
                                BorderFactory.createLineBorder(accent, 1, true),
                                BorderFactory.createEmptyBorder(6, 10, 6, 10)));
                    label.setAlignmentX(0);
                    target.add(label);
                }
            }
        }
        JScrollPane scroll = new JScrollPane(content);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        root.add(header, BorderLayout.NORTH);
        root.add(navigation, BorderLayout.WEST);
        root.add(scroll, BorderLayout.CENTER);
        root.add(footer, BorderLayout.SOUTH);
        frame.setContentPane(root);
        frame.revalidate();
        frame.repaint();
    }

    private static JPanel vertical(Color background) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(background);
        panel.setBorder(BorderFactory.createEmptyBorder(20, 18, 20, 18));
        return panel;
    }
    private static String escapeHtml(String string) {
        // JLabel can render HTML, so escape user/content data rather than interpreting markup.
        return "<html>" + string.replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\n", "<br>") + "</html>";
    }
}
