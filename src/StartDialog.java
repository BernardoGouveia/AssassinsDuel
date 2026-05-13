import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Cyan cyberpunk modal for choosing 2..4 players. Three big cards with player tokens.
 * Click a card or press 2/3/4. Esc to cancel.
 */
public class StartDialog extends JDialog {
    private int chosenPlayers = -1;

    private static final Color BG = new Color(12, 4, 6);
    private static final Color RED_BRIGHT = new Color(255, 60, 60);
    private static final Color RED_DIM = new Color(140, 30, 35);
    private static final Color GOLD = new Color(255, 200, 80);
    private static final Color TEXT = new Color(255, 220, 220);
    private static final Color SUBTLE = new Color(190, 130, 130);

    private static final Color[] PLAYER_COLORS = {
            new Color(255, 70, 90),
            new Color(40, 230, 255),
            new Color(80, 255, 140),
            new Color(220, 130, 255)
    };

    public StartDialog() {
        super((Frame) null, "Duelo dos Assassinos", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setIconImages(AppIcon.images());
        setResizable(false);

        WelcomeDialog.CyberBackground root = new WelcomeDialog.CyberBackground();
        root.setLayout(new BorderLayout());
        setContentPane(root);

        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(34, 40, 22, 40));

        JLabel title = new JLabel("// SELECCIONA OS JOGADORES //");
        title.setFont(new Font("Monospaced", Font.BOLD, 24));
        title.setForeground(RED_BRIGHT);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);

        header.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("Escolhe quantos assassinos vão duelar");
        subtitle.setFont(new Font("Monospaced", Font.PLAIN, 13));
        subtitle.setForeground(SUBTLE);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(subtitle);

        root.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 22, 0));
        center.setOpaque(false);
        center.setBorder(new EmptyBorder(0, 30, 24, 30));
        for (int n = 2; n <= 4; n++) {
            center.add(buildChoiceCard(n));
        }
        root.add(center, BorderLayout.CENTER);

        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setOpaque(false);
        footer.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel hint = new JLabel("[ Atalho: 2 / 3 / 4    ·    Esc para sair ]");
        hint.setFont(new Font("Monospaced", Font.PLAIN, 11));
        hint.setForeground(SUBTLE);
        footer.add(hint);

        root.add(footer, BorderLayout.SOUTH);

        KeyAdapter ka = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int c = e.getKeyChar();
                if (c == '2') choose(2);
                else if (c == '3') choose(3);
                else if (c == '4') choose(4);
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE) { chosenPlayers = -1; dispose(); }
            }
        };
        addKeyListener(ka);
        setFocusable(true);

        pack();
        setLocationRelativeTo(null);
    }

    private void choose(int n) {
        chosenPlayers = n;
        dispose();
    }

    private JComponent buildChoiceCard(final int n) {
        final boolean[] hovered = {false};
        JPanel card = new JPanel() {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0;
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

                int w = getWidth();
                int h = getHeight();

                // Background (dark red-tinged with subtle inset)
                g.setColor(new Color(18, 6, 8, 235));
                g.fillRect(0, 0, w, h);

                // Outer border (red when hovered, dim otherwise) with cut corners
                Color border = hovered[0] ? RED_BRIGHT : RED_DIM;
                g.setColor(border);
                g.setStroke(new BasicStroke(hovered[0] ? 2.6f : 1.6f));
                int cut = 14;
                int[] xs = {cut, w - 1, w - 1, w - 1 - cut, 0, 0};
                int[] ys = {0, 0, h - 1 - cut, h - 1, h - 1, cut};
                g.drawPolygon(xs, ys, 6);

                // Inner glow when hovered
                if (hovered[0]) {
                    g.setColor(new Color(RED_BRIGHT.getRed(), RED_BRIGHT.getGreen(), RED_BRIGHT.getBlue(), 28));
                    g.fillRect(4, 4, w - 8, h - 8);
                }

                // Top label
                g.setColor(SUBTLE);
                g.setFont(new Font("Monospaced", Font.BOLD, 11));
                String top = "// PLAYERS";
                g.drawString(top, 12, 22);

                // Big number with red glow
                g.setFont(new Font("Monospaced", Font.BOLD, 78));
                FontMetrics fm = g.getFontMetrics();
                String s = String.valueOf(n);
                int tw = fm.stringWidth(s);
                int baselineY = 110;
                g.setColor(new Color(RED_BRIGHT.getRed(), RED_BRIGHT.getGreen(), RED_BRIGHT.getBlue(), 80));
                for (int i = 5; i > 0; i--) {
                    g.drawString(s, w / 2 - tw / 2 + i, baselineY);
                    g.drawString(s, w / 2 - tw / 2 - i, baselineY);
                }
                g.setColor(hovered[0] ? Color.WHITE : new Color(255, 200, 200));
                g.drawString(s, w / 2 - tw / 2, baselineY);

                // Gold accent line
                g.setColor(GOLD);
                g.setStroke(new BasicStroke(2f));
                g.drawLine(w / 2 - 24, baselineY + 14, w / 2 + 24, baselineY + 14);

                g.setColor(SUBTLE);
                g.setFont(new Font("Monospaced", Font.PLAIN, 12));
                String label = "JOGADORES";
                int lw = g.getFontMetrics().stringWidth(label);
                g.drawString(label, w / 2 - lw / 2, baselineY + 34);

                // Player tokens row
                int dot = 18;
                int gap = 8;
                int totalW = n * dot + (n - 1) * gap;
                int startX = w / 2 - totalW / 2;
                int dotY = h - 40;
                for (int i = 0; i < n; i++) {
                    int dx = startX + i * (dot + gap);
                    Color pc = PLAYER_COLORS[i];
                    g.setColor(new Color(pc.getRed(), pc.getGreen(), pc.getBlue(), 90));
                    g.fillOval(dx - 3, dotY - 3, dot + 6, dot + 6);
                    g.setColor(pc);
                    g.fillOval(dx, dotY, dot, dot);
                    g.setColor(new Color(0, 0, 0, 130));
                    g.drawOval(dx, dotY, dot, dot);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("Monospaced", Font.BOLD, 11));
                    String num = String.valueOf(i + 1);
                    FontMetrics nm = g.getFontMetrics();
                    int nw = nm.stringWidth(num);
                    g.drawString(num, dx + dot / 2 - nw / 2, dotY + dot / 2 + nm.getAscent() / 2 - 2);
                }
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(170, 220));
        card.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        card.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { hovered[0] = true; card.repaint(); }
            @Override public void mouseExited(MouseEvent e) { hovered[0] = false; card.repaint(); }
            @Override public void mouseClicked(MouseEvent e) { choose(n); }
        });
        return card;
    }

    public int getChosenPlayers() {
        return chosenPlayers;
    }
}
