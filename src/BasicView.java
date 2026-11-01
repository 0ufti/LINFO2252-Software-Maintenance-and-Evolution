import javax.swing.*;
import java.awt.*;
import java.util.List;

// Small Swing view for M2. The terminal is still the main interface.
// It uses the UiLog items so we don't have to decide feature visibility twice.
public class BasicView {
    private Controller controller;
    private LearningModel model;
    private JFrame frame;
    private Runnable update;

    public BasicView(Controller controller, LearningModel model) {
        this.controller = controller;
        this.model = model;
        update = () -> SwingUtilities.invokeLater(() -> draw());
    }

    public void show() {
        frame = new JFrame("Smart Learning WIP");
        frame.setSize(650, 500);
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent e) {
                model.removeListener(update);
            }
        });
        model.addListener(update);
        draw();
        frame.setVisible(true);
    }

    private void draw() {
        if (frame == null || !frame.isDisplayable()) return;
        List<UiLog.Item> items = model.getItems();
        JPanel root = new JPanel(new BorderLayout());
        JPanel top = new JPanel(new FlowLayout());
        JPanel left = new JPanel();
        JPanel center = new JPanel();
        boolean dark = model.isOn("THEME");
        Color bg = dark ? new Color(45, 45, 45) : Color.WHITE;
        Color fg = dark ? Color.WHITE : Color.BLACK;
        for (JPanel p : new JPanel[]{root, top, left, center}) p.setBackground(bg);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        for (UiLog.Item i : items) {
            JPanel target = center;
            if (i.place.equals("HEADER")) target = top;
            if (i.place.equals("NAV")) target = left;

            if (i.type.equals("button")) {
                JButton b = new JButton(i.text);
                b.addActionListener(e -> controller.buttonClicked(i.id));
                if (dark) { b.setBackground(new Color(70, 70, 70)); b.setForeground(fg); }
                target.add(b);
            } else {
                JLabel label = new JLabel(i.text);
                if (i.type.equals("title")) label.setFont(label.getFont().deriveFont(18f));
                label.setForeground(fg);
                target.add(label);
            }
        }
        root.add(top, BorderLayout.NORTH);
        root.add(left, BorderLayout.WEST);
        root.add(new JScrollPane(center), BorderLayout.CENTER);
        frame.setContentPane(root);
        frame.revalidate();
        frame.repaint();
    }

    public boolean isOpen() { return frame != null && frame.isDisplayable(); }

    public void close() {
        model.removeListener(update);
        if (frame != null) frame.dispose();
        frame = null;
    }
}
