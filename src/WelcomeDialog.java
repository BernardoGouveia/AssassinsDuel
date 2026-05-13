import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Cyan cyberpunk welcome dialog: scanline grid background with falling shurikens,
 * neon cyan title. Click or press a key to start; "Regras do Jogo" and "Sair" buttons.
 */
public class WelcomeDialog extends JDialog {
    private boolean shouldStart = false;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private String currentCard = "welcome";

    static final Color BG = new Color(6, 10, 18);
    static final Color BG_DEEP = new Color(2, 4, 10);
    static final Color CYAN = new Color(40, 230, 255);
    static final Color CYAN_DIM = new Color(20, 120, 150);
    static final Color MAGENTA = new Color(255, 50, 180);
    static final Color TEXT = new Color(230, 245, 255);
    static final Color SUBTLE = new Color(120, 160, 190);

    public WelcomeDialog() {
        super((Frame) null, "Assassin's Duel", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(760, 580);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        cardPanel = new JPanel(cardLayout);
        cardPanel.setBackground(BG);

        JPanel welcome = buildWelcome();
        cardPanel.add(welcome, "welcome");
        wireStartClicks(welcome);

        cardPanel.add(buildRules(), "rules");

        add(cardPanel, BorderLayout.CENTER);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if ("welcome".equals(currentCard)) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        shouldStart = false;
                        dispose();
                    } else {
                        shouldStart = true;
                        dispose();
                    }
                } else {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE
                            || e.getKeyCode() == KeyEvent.VK_BACK_SPACE) {
                        showCard("welcome");
                    }
                }
            }
        });
        setFocusable(true);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowOpened(WindowEvent e) {
                requestFocusInWindow();
            }
        });
    }

    private void showCard(String name) {
        currentCard = name;
        cardLayout.show(cardPanel, name);
        requestFocusInWindow();
    }

    private JPanel buildWelcome() {
        CyberBackground root = new CyberBackground();
        root.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setOpaque(false);

        JPanel dots = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 0));
        dots.setOpaque(false);
        Color[] cols = {
                new Color(255, 70, 90),
                new Color(40, 230, 255),
                new Color(80, 255, 140),
                new Color(220, 130, 255)
        };
        for (Color c : cols) dots.add(new Dot(c, 16));
        dots.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(dots);

        content.add(Box.createVerticalStrut(12));

        NeonTitle neon = new NeonTitle("ASSASSIN'S DUEL", 62, CYAN, MAGENTA);
        neon.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(neon);

        content.add(Box.createVerticalStrut(4));

        JLabel sub = center(new JLabel("// Duelo táctico arcade · 2 a 4 jogadores //"));
        sub.setFont(new Font("Monospaced", Font.BOLD, 14));
        sub.setForeground(SUBTLE);
        content.add(sub);

        content.add(Box.createVerticalStrut(56));

        JLabel hint = center(new JLabel("[ CLICA OU PRESSIONA QUALQUER TECLA PARA COMEÇAR ]"));
        hint.setFont(new Font("Monospaced", Font.BOLD, 13));
        hint.setForeground(CYAN);
        content.add(hint);

        content.add(Box.createVerticalStrut(36));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton rulesBtn = makeCyberButton("REGRAS DO JOGO", CYAN);
        rulesBtn.addActionListener(e -> showCard("rules"));
        btnRow.add(rulesBtn);

        JButton quit = makeCyberButton("SAIR", new Color(255, 80, 110));
        quit.addActionListener(e -> { shouldStart = false; dispose(); });
        btnRow.add(quit);

        content.add(btnRow);

        root.add(content);
        return root;
    }

    static JButton makeCyberButton(String label, Color accent) {
        JButton b = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g.setColor(new Color(8, 14, 22));
                g.fillRect(0, 0, w, h);
                g.setColor(getModel().isRollover() ? accent : accent.darker());
                g.setStroke(new BasicStroke(getModel().isRollover() ? 2.4f : 1.6f));
                g.drawRect(2, 2, w - 5, h - 5);
                // corner cuts
                g.fillPolygon(new int[]{0, 10, 0}, new int[]{0, 0, 10}, 3);
                g.fillPolygon(new int[]{w, w - 10, w}, new int[]{h, h, h - 10}, 3);
                FontMetrics fm = g.getFontMetrics(getFont());
                String t = getText();
                int tw = fm.stringWidth(t);
                g.setFont(getFont());
                g.setColor(getModel().isRollover() ? Color.WHITE : accent);
                g.drawString(t, w / 2 - tw / 2, h / 2 + fm.getAscent() / 2 - 2);
                g.dispose();
            }
        };
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setFont(new Font("Monospaced", Font.BOLD, 13));
        b.setForeground(accent);
        b.setPreferredSize(new Dimension(200, 44));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JLabel center(JLabel l) {
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    private JPanel buildRules() {
        CyberBackground root = new CyberBackground();
        root.setLayout(new BorderLayout());

        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);
        header.setBorder(new EmptyBorder(12, 12, 4, 12));

        JButton back = makeCyberButton("← VOLTAR", CYAN);
        back.setPreferredSize(new Dimension(130, 38));
        back.addActionListener(e -> showCard("welcome"));
        header.add(back, BorderLayout.WEST);

        JLabel title = new JLabel("// REGRAS DO JOGO //", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        title.setForeground(CYAN);
        header.add(title, BorderLayout.CENTER);

        header.add(Box.createHorizontalStrut(130), BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        JTextArea rules = new JTextArea();
        rules.setEditable(false);
        rules.setOpaque(false);
        rules.setForeground(new Color(210, 235, 250));
        rules.setFont(new Font("Monospaced", Font.PLAIN, 13));
        rules.setLineWrap(true);
        rules.setWrapStyleWord(true);
        rules.setBorder(new EmptyBorder(18, 36, 20, 36));
        rules.setText(
                "OBJETIVO\n" +
                        "  Vence o último assassino vivo.\n\n" +
                        "ESTATÍSTICAS BASE\n" +
                        "  > 150 HP\n" +
                        "  > 3 AP (pontos de acção) por turno\n\n" +
                        "ACÇÕES\n" +
                        "  > Mover (1 AP): para uma casa adjacente livre.\n" +
                        "  > Corpo-a-Corpo (1 AP): 30 dano num adversário adjacente.\n" +
                        "  > Shuriken (2 AP): 20 dano, alcance 5 casas. Precisa de linha de visão.\n" +
                        "  > Curar (2 AP): +30 HP. Apenas uma vez por jogo.\n\n" +
                        "POWER-UPS NO MAPA\n" +
                        "  > Verde \"+\" : +25 HP imediato ao passar por cima.\n" +
                        "  > Laranja \"!\" : +15 dano no próximo ataque.\n" +
                        "  > Aparecem novos a cada 3 rondas completas.\n\n" +
                        "FIM DE TURNO\n" +
                        "  > Carrega em \"Terminar Turno\" para passar a vez.\n" +
                        "  > Se esgotares os 3 AP, o turno passa automaticamente.\n\n" +
                        "MAPA\n" +
                        "  > Grelha 12x12 com paredes aleatórias que bloqueiam.\n" +
                        "  > Jogadores em cantos opostos. Cores: vermelha, ciano, verde, roxa.\n"
        );
        JScrollPane scroll = new JScrollPane(rules);
        scroll.setOpaque(false);
        scroll.getViewport().setOpaque(false);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        JLabel footer = new JLabel("[ Esc / Backspace para voltar ]", SwingConstants.CENTER);
        footer.setFont(new Font("Monospaced", Font.PLAIN, 11));
        footer.setForeground(SUBTLE);
        footer.setBorder(new EmptyBorder(0, 0, 12, 0));
        root.add(footer, BorderLayout.SOUTH);

        return root;
    }

    private void wireStartClicks(Component c) {
        if (c instanceof JButton) return;
        c.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                shouldStart = true;
                dispose();
            }
        });
        if (c instanceof Container) {
            for (Component child : ((Container) c).getComponents()) {
                wireStartClicks(child);
            }
        }
    }

    public boolean shouldStart() {
        return shouldStart;
    }

    private static class Dot extends JComponent {
        private final Color color;
        Dot(Color c, int size) {
            this.color = c;
            setPreferredSize(new Dimension(size, size));
        }
        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0;
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 80));
            g.fillOval(-3, -3, getWidth() + 6, getHeight() + 6);
            g.setColor(color);
            g.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(Color.WHITE);
            g.fillOval(getWidth() / 3, getHeight() / 3, getWidth() / 4, getHeight() / 4);
        }
    }

    /**
     * Animated cyberpunk background: vertical gradient + cyan grid + falling
     * spinning shurikens. Used by both welcome and rules cards.
     */
    static class CyberBackground extends JPanel {
        private final List<Shuriken> shurikens = new ArrayList<>();
        private final Random rng = new Random();
        private long lastTick = System.currentTimeMillis();
        private final Timer timer;

        CyberBackground() {
            setOpaque(true);
            setBackground(BG);
            for (int i = 0; i < 14; i++) shurikens.add(newShuriken(true));
            timer = new Timer(33, e -> tick());
            timer.start();
        }

        private Shuriken newShuriken(boolean anywhere) {
            Shuriken s = new Shuriken();
            int w = Math.max(getWidth(), 760);
            int h = Math.max(getHeight(), 580);
            s.x = rng.nextInt(w);
            s.y = anywhere ? rng.nextInt(h) : -30 - rng.nextInt(200);
            s.size = 10 + rng.nextInt(18);
            s.speed = 30 + rng.nextDouble() * 70;
            s.spin = (rng.nextDouble() - 0.5) * 6;
            s.angle = rng.nextDouble() * Math.PI * 2;
            s.alpha = 0.18f + rng.nextFloat() * 0.35f;
            return s;
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            if (timer != null) timer.stop();
        }

        private void tick() {
            long now = System.currentTimeMillis();
            double dt = (now - lastTick) / 1000.0;
            lastTick = now;
            int h = getHeight();
            for (Shuriken s : shurikens) {
                s.y += s.speed * dt;
                s.angle += s.spin * dt;
                if (s.y > h + 40) {
                    s.y = -30 - rng.nextInt(120);
                    s.x = rng.nextInt(Math.max(getWidth(), 760));
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // Vertical gradient
            GradientPaint gp = new GradientPaint(0, 0, BG_DEEP, 0, h, new Color(10, 18, 32));
            g.setPaint(gp);
            g.fillRect(0, 0, w, h);

            // Cyan grid
            g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 22));
            int step = 40;
            for (int x = 0; x < w; x += step) g.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += step) g.drawLine(0, y, w, y);

            // Horizon glow line
            int horizon = (int) (h * 0.65);
            g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 80));
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(0, horizon, w, horizon);
            g.setColor(new Color(MAGENTA.getRed(), MAGENTA.getGreen(), MAGENTA.getBlue(), 40));
            g.drawLine(0, horizon + 2, w, horizon + 2);

            // Falling shurikens
            for (Shuriken s : shurikens) drawShuriken(g, s);

            // Scanline overlay
            g.setColor(new Color(0, 0, 0, 24));
            for (int y = 0; y < h; y += 3) g.drawLine(0, y, w, y);

            g.dispose();
        }

        private void drawShuriken(Graphics2D g, Shuriken s) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(s.x, s.y);
            g2.rotate(s.angle);
            int r = s.size;
            int a = (int) (s.alpha * 255);
            // glow halo
            g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), a / 3));
            g2.fillOval(-r - 4, -r - 4, (r + 4) * 2, (r + 4) * 2);
            // 4-blade star
            int[] xs = {0, r / 3, r, r / 3, 0, -r / 3, -r, -r / 3};
            int[] ys = {-r, -r / 3, 0, r / 3, r, r / 3, 0, -r / 3};
            g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), a));
            g2.fillPolygon(xs, ys, 8);
            g2.setColor(new Color(220, 250, 255, Math.min(255, a + 60)));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawPolygon(xs, ys, 8);
            // center
            g2.setColor(new Color(255, 255, 255, a));
            g2.fillOval(-2, -2, 4, 4);
            g2.dispose();
        }
    }

    private static class Shuriken {
        double x, y, speed, angle, spin;
        int size;
        float alpha;
    }

    /** Neon title with two-color glow (cyan + magenta) and soft pulse. */
    private static class NeonTitle extends JComponent {
        private final String text;
        private final Font font;
        private final Color outer;
        private final Color rim;
        private float pulse = 1.0f;
        private final Timer timer;

        NeonTitle(String text, int size, Color outer, Color rim) {
            this.text = text;
            this.font = new Font("Monospaced", Font.BOLD, size);
            this.outer = outer;
            this.rim = rim;
            timer = new Timer(40, e -> {
                double t = System.currentTimeMillis() * 0.0028;
                pulse = (float) (0.82 + 0.18 * (Math.sin(t) + 1) / 2);
                repaint();
            });
            timer.setRepeats(true);
            timer.start();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(font);
            return new Dimension(fm.stringWidth(text) + 100, fm.getHeight() + 50);
        }

        @Override
        public Dimension getMaximumSize() { return getPreferredSize(); }

        @Override
        public void removeNotify() {
            super.removeNotify();
            if (timer != null) timer.stop();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

            FontRenderContext frc = g.getFontRenderContext();
            TextLayout layout = new TextLayout(text, font, frc);
            Rectangle2D bounds = layout.getBounds();
            double x = (getWidth() - bounds.getWidth()) / 2.0 - bounds.getX();
            double y = (getHeight() - bounds.getHeight()) / 2.0 - bounds.getY();
            Shape outline = layout.getOutline(AffineTransform.getTranslateInstance(x, y));

            // Magenta chromatic-aberration offset behind
            Shape shifted = layout.getOutline(AffineTransform.getTranslateInstance(x + 3, y + 1));
            g.setColor(new Color(rim.getRed(), rim.getGreen(), rim.getBlue(), (int) (180 * pulse)));
            g.fill(shifted);

            // Outer cyan glow
            for (int i = 36; i >= 6; i -= 2) {
                float baseAlpha = 0.04f + (36 - i) * 0.006f;
                int a = Math.min(255, (int) (baseAlpha * pulse * 255));
                g.setColor(new Color(outer.getRed(), outer.getGreen(), outer.getBlue(), a));
                g.setStroke(new BasicStroke(i, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(outline);
            }

            // Sharp rim
            g.setColor(new Color(outer.getRed(), outer.getGreen(), outer.getBlue(), (int) (230 * pulse)));
            g.setStroke(new BasicStroke(3.0f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(outline);

            // Bright core fill
            g.setColor(new Color(220, 255, 255));
            g.fill(outline);

            g.dispose();
        }
    }
}
