import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Polished modal dialog for choosing 2..4 players.
 * Three big cards with the player tokens that would join. Click or press 2/3/4.
 */
public class StartDialog extends JDialog {
    private int chosenPlayers = -1;

    private static final Color BG = new Color(15, 17, 22);
    private static final Color CARD_BG = new Color(35, 38, 46);
    private static final Color CARD_HOVER = new Color(55, 62, 80);
    private static final Color BORDER = new Color(70, 75, 90);
    private static final Color BORDER_HOVER = new Color(140, 160, 220);

    private static final Color[] PLAYER_COLORS = {
            new Color(220, 60, 60),
            new Color(60, 130, 220),
            new Color(80, 200, 100),
            new Color(200, 130, 230)
    };

    public StartDialog() {
        super((Frame) null, "Duelo dos Assassinos", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        // Header
        JPanel header = new JPanel();
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(34, 40, 22, 40));

        JLabel title = new JLabel("DUELO DOS ASSASSINOS");
        title.setFont(new Font("SansSerif", Font.BOLD, 28));
        title.setForeground(new Color(240, 240, 245));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(title);

        header.add(Box.createVerticalStrut(8));

        JLabel subtitle = new JLabel("Escolhe quantos assassinos vão duelar");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 14));
        subtitle.setForeground(new Color(160, 165, 180));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        header.add(subtitle);

        add(header, BorderLayout.NORTH);

        // Cards row
        JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 18, 0));
        center.setBackground(BG);
        center.setBorder(new EmptyBorder(0, 30, 24, 30));
        for (int n = 2; n <= 4; n++) {
            center.add(buildChoiceCard(n));
        }
        add(center, BorderLayout.CENTER);

        // Footer
        JPanel footer = new JPanel(new FlowLayout(FlowLayout.CENTER));
        footer.setBackground(BG);
        footer.setBorder(new EmptyBorder(0, 0, 18, 0));

        JLabel hint = new JLabel("Atalho: tecla 2, 3 ou 4    ·    Esc para sair");
        hint.setFont(new Font("SansSerif", Font.PLAIN, 11));
        hint.setForeground(new Color(120, 125, 140));
        footer.add(hint);

        add(footer, BorderLayout.SOUTH);

        // Keyboard shortcuts
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

                // Background
                g.setColor(hovered[0] ? CARD_HOVER : CARD_BG);
                g.fillRoundRect(0, 0, w, h, 18, 18);

                // Border
                Stroke prev = g.getStroke();
                g.setStroke(new BasicStroke(hovered[0] ? 2.4f : 1.6f));
                g.setColor(hovered[0] ? BORDER_HOVER : BORDER);
                g.drawRoundRect(1, 1, w - 3, h - 3, 18, 18);
                g.setStroke(prev);

                // Big number
                g.setColor(new Color(245, 245, 250));
                g.setFont(new Font("SansSerif", Font.BOLD, 64));
                FontMetrics fm = g.getFontMetrics();
                String s = String.valueOf(n);
                int tw = fm.stringWidth(s);
                int baselineY = 92;
                g.drawString(s, w / 2 - tw / 2, baselineY);

                // Subtitle
                g.setColor(new Color(170, 175, 190));
                g.setFont(new Font("SansSerif", Font.PLAIN, 13));
                String label = "jogadores";
                int lw = g.getFontMetrics().stringWidth(label);
                g.drawString(label, w / 2 - lw / 2, baselineY + 22);

                // Player tokens
                int dot = 18;
                int gap = 8;
                int totalW = n * dot + (n - 1) * gap;
                int startX = w / 2 - totalW / 2;
                int dotY = h - 38;
                for (int i = 0; i < n; i++) {
                    int dx = startX + i * (dot + gap);
                    g.setColor(PLAYER_COLORS[i]);
                    g.fillOval(dx, dotY, dot, dot);
                    g.setColor(new Color(0, 0, 0, 130));
                    g.drawOval(dx, dotY, dot, dot);
                    g.setColor(Color.WHITE);
                    g.setFont(new Font("SansSerif", Font.BOLD, 11));
                    String num = String.valueOf(i + 1);
                    FontMetrics nm = g.getFontMetrics();
                    int nw = nm.stringWidth(num);
                    g.drawString(num, dx + dot / 2 - nw / 2, dotY + dot / 2 + nm.getAscent() / 2 - 2);
                }
            }
        };
        card.setOpaque(false);
        card.setPreferredSize(new Dimension(160, 200));
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
