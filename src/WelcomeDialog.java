import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

/**
 * Splash / welcome dialog shown before the player-count chooser.
 * - Title screen: red neon "ASSASSIN'S DUEL". Click anywhere or press any key to start.
 * - "Regras do Jogo" and "Sair" buttons side by side.
 * - "Regras do Jogo" opens a rules card with a "← Voltar" button in the top-left.
 */
public class WelcomeDialog extends JDialog {
    private boolean shouldStart = false;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private String currentCard = "welcome";

    private static final Color BG = new Color(12, 12, 18);
    private static final Color TEXT = new Color(240, 240, 245);
    private static final Color SUBTLE = new Color(160, 165, 180);
    private static final Color ACCENT = new Color(180, 200, 230);

    public WelcomeDialog() {
        super((Frame) null, "Assassin's Duel", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(740, 560);
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
                } else { // rules
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

    // ---------- Welcome card ----------

    private JPanel buildWelcome() {
        JPanel root = new JPanel(new GridBagLayout());
        root.setBackground(BG);

        JPanel content = new JPanel();
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.setBackground(BG);

        // Small player-color dots above the title
        JPanel dots = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        dots.setBackground(BG);
        Color[] cols = {
                new Color(220, 60, 60),
                new Color(60, 130, 220),
                new Color(80, 200, 100),
                new Color(200, 130, 230)
        };
        for (Color c : cols) dots.add(new Dot(c, 14));
        dots.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(dots);

        content.add(Box.createVerticalStrut(10));

        // Big red neon title
        NeonTitle neon = new NeonTitle("ASSASSIN'S DUEL", 60);
        neon.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(neon);

        content.add(Box.createVerticalStrut(4));

        JLabel sub = center(new JLabel("Duelo táctico • 2 a 4 jogadores"));
        sub.setFont(new Font("SansSerif", Font.PLAIN, 14));
        sub.setForeground(SUBTLE);
        content.add(sub);

        content.add(Box.createVerticalStrut(56));

        JLabel hint = center(new JLabel("Clica ou pressiona qualquer tecla para começar"));
        hint.setFont(new Font("SansSerif", Font.ITALIC, 13));
        hint.setForeground(ACCENT);
        content.add(hint);

        content.add(Box.createVerticalStrut(36));

        // Buttons side by side
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setBackground(BG);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton rulesBtn = new JButton("Regras do Jogo");
        styleBtn(rulesBtn, new Color(60, 65, 85), Color.WHITE);
        rulesBtn.setPreferredSize(new Dimension(190, 42));
        rulesBtn.addActionListener(e -> showCard("rules"));
        btnRow.add(rulesBtn);

        JButton quit = new JButton("Sair");
        styleBtn(quit, new Color(140, 50, 50), Color.WHITE);
        quit.setPreferredSize(new Dimension(190, 42));
        quit.addActionListener(e -> { shouldStart = false; dispose(); });
        btnRow.add(quit);

        content.add(btnRow);

        root.add(content);
        return root;
    }

    private JLabel center(JLabel l) {
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        return l;
    }

    // ---------- Rules card ----------

    private JPanel buildRules() {
        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(BG);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(BG);
        header.setBorder(new EmptyBorder(12, 12, 4, 12));

        JButton back = new JButton("← Voltar");
        styleBtn(back, new Color(60, 65, 85), Color.WHITE);
        back.addActionListener(e -> showCard("welcome"));
        header.add(back, BorderLayout.WEST);

        JLabel title = new JLabel("REGRAS DO JOGO", SwingConstants.CENTER);
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        title.setForeground(TEXT);
        header.add(title, BorderLayout.CENTER);

        header.add(Box.createHorizontalStrut(110), BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        JTextArea rules = new JTextArea();
        rules.setEditable(false);
        rules.setBackground(BG);
        rules.setForeground(new Color(220, 222, 232));
        rules.setFont(new Font("SansSerif", Font.PLAIN, 13));
        rules.setLineWrap(true);
        rules.setWrapStyleWord(true);
        rules.setBorder(new EmptyBorder(18, 36, 20, 36));
        rules.setText(
                "OBJETIVO\n" +
                        "  Vence o último assassino vivo.\n\n" +
                        "ESTATÍSTICAS BASE\n" +
                        "  • 150 HP\n" +
                        "  • 3 AP (pontos de ação) por turno\n\n" +
                        "AÇÕES\n" +
                        "  • Mover (1 AP): para uma casa adjacente livre.\n" +
                        "  • Corpo-a-Corpo (1 AP): 30 dano num adversário adjacente.\n" +
                        "  • Shuriken (2 AP): 20 dano, alcance 5 casas. Precisa de linha de visão (paredes bloqueiam).\n" +
                        "  • Curar (2 AP): +30 HP. Apenas uma vez por jogo.\n\n" +
                        "POWER-UPS NO MAPA\n" +
                        "  • Verde \"+\" : +25 HP imediato ao passar por cima.\n" +
                        "  • Laranja \"!\" : +15 dano no próximo ataque. Aparece um anel dourado à volta do jogador.\n" +
                        "  • Aparecem power-ups novos a cada 3 rondas completas.\n\n" +
                        "FIM DE TURNO\n" +
                        "  • Carrega em \"Terminar Turno\" para passar a vez.\n" +
                        "  • Se esgotares os 3 AP, o turno passa automaticamente.\n\n" +
                        "MAPA\n" +
                        "  • Grelha 12×12 com paredes aleatórias que bloqueiam movimento e shurikens.\n" +
                        "  • Jogadores começam em cantos opostos. Cores: vermelha, azul, verde e roxa.\n\n" +
                        "INTERFACE\n" +
                        "  • Casas verdes mostram alvos válidos para a ação selecionada.\n" +
                        "  • Barra de HP por cima de cada assassino.\n" +
                        "  • Cartão com borda mais grossa = jogador atual.\n\n" +
                        "ATALHOS NO ECRÃ INICIAL\n" +
                        "  • Qualquer tecla / clique = começar.\n" +
                        "  • Esc = sair (no menu) ou voltar (nas regras).\n"
        );
        JScrollPane scroll = new JScrollPane(rules);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        root.add(scroll, BorderLayout.CENTER);

        JLabel footer = new JLabel("Esc / Backspace para voltar", SwingConstants.CENTER);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setForeground(new Color(120, 125, 140));
        footer.setBorder(new EmptyBorder(0, 0, 12, 0));
        root.add(footer, BorderLayout.SOUTH);

        return root;
    }

    // ---------- Helpers ----------

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

    private void styleBtn(JButton b, Color bg, Color fg) {
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("SansSerif", Font.BOLD, 13));
        b.setBorder(BorderFactory.createEmptyBorder(8, 16, 8, 16));
    }

    public boolean shouldStart() {
        return shouldStart;
    }

    /** Small colored circle used in the welcome decoration. */
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
            g.setColor(color);
            g.fillOval(0, 0, getWidth() - 1, getHeight() - 1);
            g.setColor(new Color(0, 0, 0, 130));
            g.drawOval(0, 0, getWidth() - 1, getHeight() - 1);
        }
    }

    /** Big red neon title with layered glow and a soft pulse. */
    private static class NeonTitle extends JComponent {
        private final String text;
        private final Font font;
        private float pulse = 1.0f;
        private Timer timer;

        NeonTitle(String text, int size) {
            this.text = text;
            this.font = new Font("SansSerif", Font.BOLD, size);
            timer = new Timer(50, e -> {
                double t = System.currentTimeMillis() * 0.0028;
                pulse = (float) (0.85 + 0.15 * (Math.sin(t) + 1) / 2);
                repaint();
            });
            timer.setRepeats(true);
            timer.start();
        }

        @Override
        public Dimension getPreferredSize() {
            FontMetrics fm = getFontMetrics(font);
            int w = fm.stringWidth(text) + 100;
            int h = fm.getHeight() + 40;
            return new Dimension(w, h);
        }

        @Override
        public Dimension getMaximumSize() {
            return getPreferredSize();
        }

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
            g.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);

            FontRenderContext frc = g.getFontRenderContext();
            TextLayout layout = new TextLayout(text, font, frc);
            Rectangle2D bounds = layout.getBounds();
            double x = (getWidth() - bounds.getWidth()) / 2.0 - bounds.getX();
            double y = (getHeight() - bounds.getHeight()) / 2.0 - bounds.getY();
            Shape outline = layout.getOutline(AffineTransform.getTranslateInstance(x, y));

            // Outer wide red glow (multiple stroke layers, low alpha)
            for (int i = 32; i >= 6; i -= 2) {
                float baseAlpha = 0.05f + (32 - i) * 0.006f;
                int a = Math.min(255, (int) (baseAlpha * pulse * 255));
                g.setColor(new Color(255, 30, 30, a));
                g.setStroke(new BasicStroke(i, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g.draw(outline);
            }

            // Sharper red rim
            g.setColor(new Color(255, 70, 70, (int) (210 * pulse)));
            g.setStroke(new BasicStroke(3.2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.draw(outline);

            // Bright fill (hot pink/white core)
            g.setColor(new Color(255, 215, 215));
            g.fill(outline);

            // Inner highlight
            g.setColor(new Color(255, 245, 245, 210));
            g.setStroke(new BasicStroke(0.8f));
            g.draw(outline);

            g.dispose();
        }
    }
}
