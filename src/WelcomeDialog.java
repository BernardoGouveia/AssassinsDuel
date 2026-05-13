import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Arcade-cabinet welcome dialog:
 *  - Animated shuriken background (cyan, falling).
 *  - Centered red MARQUEE: ornamented frame, "ASSASSIN'S / DUEL" pixel-art title,
 *    pixel-art ninja silhouettes on the sides.
 *  - "Regras do Jogo" and "Sair" buttons below.
 *  - Plays an arcade intro fanfare on open.
 */
public class WelcomeDialog extends JDialog {
    private boolean shouldStart = false;
    private final JPanel cardPanel;
    private final CardLayout cardLayout;
    private String currentCard = "welcome";

    // Arcade red palette — shared with the rest of the game.
    static final Color BG = new Color(12, 4, 6);
    static final Color BG_DEEP = new Color(8, 2, 4);
    static final Color RED_BRIGHT = new Color(255, 60, 60);
    static final Color RED_DIM = new Color(140, 30, 35);
    static final Color RED_GLOW = new Color(255, 30, 30);
    static final Color GOLD = new Color(255, 200, 80);
    static final Color STEEL = new Color(220, 220, 230);
    static final Color SUBTLE = new Color(180, 130, 130);
    // Kept for backwards compatibility inside this file (used by buttons that take "accent" args).
    static final Color CYAN = RED_BRIGHT;
    static final Color MAGENTA = GOLD;

    public WelcomeDialog() {
        super((Frame) null, "Assassin's Duel", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(880, 680);
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
                SoundFx.intro();
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

        ArcadeMarquee marquee = new ArcadeMarquee();
        marquee.setAlignmentX(Component.CENTER_ALIGNMENT);
        content.add(marquee);

        content.add(Box.createVerticalStrut(14));

        JLabel sub = center(new JLabel("// 2 a 4 JOGADORES · DUELO TÁCTICO ARCADE //"));
        sub.setFont(new Font("Monospaced", Font.BOLD, 13));
        sub.setForeground(new Color(255, 130, 130));
        content.add(sub);

        content.add(Box.createVerticalStrut(10));

        JLabel hint = center(new JLabel("[ CLICA OU PRESSIONA QUALQUER TECLA PARA COMEÇAR ]"));
        hint.setFont(new Font("Monospaced", Font.BOLD, 12));
        hint.setForeground(GOLD);
        content.add(hint);

        content.add(Box.createVerticalStrut(20));

        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        btnRow.setOpaque(false);
        btnRow.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton rulesBtn = makeCyberButton("REGRAS DO JOGO", RED_BRIGHT);
        rulesBtn.addActionListener(e -> showCard("rules"));
        btnRow.add(rulesBtn);

        JButton quit = makeCyberButton("SAIR", GOLD);
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

        JButton back = makeCyberButton("← VOLTAR", RED_BRIGHT);
        back.setPreferredSize(new Dimension(130, 38));
        back.addActionListener(e -> showCard("welcome"));
        header.add(back, BorderLayout.WEST);

        JLabel title = new JLabel("// REGRAS DO JOGO //", SwingConstants.CENTER);
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        title.setForeground(RED_BRIGHT);
        header.add(title, BorderLayout.CENTER);

        header.add(Box.createHorizontalStrut(130), BorderLayout.EAST);

        root.add(header, BorderLayout.NORTH);

        JTextArea rules = new JTextArea();
        rules.setEditable(false);
        rules.setOpaque(false);
        rules.setForeground(new Color(220, 240, 252));
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
            int w = Math.max(getWidth(), 880);
            int h = Math.max(getHeight(), 680);
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
                    s.x = rng.nextInt(Math.max(getWidth(), 880));
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // Dark red-tinged gradient
            GradientPaint gp = new GradientPaint(0, 0, BG_DEEP, 0, h, new Color(28, 8, 12));
            g.setPaint(gp);
            g.fillRect(0, 0, w, h);

            // Red grid
            g.setColor(new Color(RED_GLOW.getRed(), RED_GLOW.getGreen(), RED_GLOW.getBlue(), 22));
            int step = 40;
            for (int x = 0; x < w; x += step) g.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += step) g.drawLine(0, y, w, y);

            // Red horizon w/ gold echo
            int horizon = (int) (h * 0.72);
            g.setColor(new Color(RED_GLOW.getRed(), RED_GLOW.getGreen(), RED_GLOW.getBlue(), 90));
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(0, horizon, w, horizon);
            g.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(), 40));
            g.drawLine(0, horizon + 2, w, horizon + 2);

            for (Shuriken s : shurikens) drawShuriken(g, s);

            g.setColor(new Color(0, 0, 0, 30));
            for (int y = 0; y < h; y += 3) g.drawLine(0, y, w, y);

            g.dispose();
        }

        // Steel shuriken with red glow halo (instead of cyan)
        private void drawShuriken(Graphics2D g, Shuriken s) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.translate(s.x, s.y);
            g2.rotate(s.angle);
            int r = s.size;
            int a = (int) (s.alpha * 255);
            g2.setColor(new Color(RED_GLOW.getRed(), RED_GLOW.getGreen(), RED_GLOW.getBlue(), a / 2));
            g2.fillOval(-r - 5, -r - 5, (r + 5) * 2, (r + 5) * 2);
            int[] xs = {0, r / 3, r, r / 3, 0, -r / 3, -r, -r / 3};
            int[] ys = {-r, -r / 3, 0, r / 3, r, r / 3, 0, -r / 3};
            // steel body
            g2.setColor(new Color(STEEL.getRed(), STEEL.getGreen(), STEEL.getBlue(), a));
            g2.fillPolygon(xs, ys, 8);
            // bright rim
            g2.setColor(new Color(255, 240, 240, Math.min(255, a + 60)));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawPolygon(xs, ys, 8);
            // red center stud
            g2.setColor(new Color(RED_BRIGHT.getRed(), RED_BRIGHT.getGreen(), RED_BRIGHT.getBlue(), a));
            g2.fillOval(-2, -2, 4, 4);
            g2.dispose();
        }
    }

    private static class Shuriken {
        double x, y, speed, angle, spin;
        int size;
        float alpha;
    }

    // ============================================================
    //                  ARCADE MARQUEE (the big sign)
    // ============================================================

    /**
     * Pixel-art arcade marquee inspired by classic neon arcade signs:
     *  - Ornamented red frame with rounded corners.
     *  - "ASSASSIN'S / DUEL" rendered with a decorative serif font, pixelated
     *    (rendered small, AA off, scaled up with nearest-neighbor).
     *  - Three colour layers (dark shadow / mid red / bright highlight) for depth.
     *  - Pixel-art ninja silhouettes flanking the title.
     */
    private static class ArcadeMarquee extends JComponent {
        private static final int W = 760;
        private static final int H = 340;

        // Frame palette
        private static final Color FRAME_OUTER = new Color(80, 12, 16);
        private static final Color FRAME_MID = new Color(150, 22, 28);
        private static final Color FRAME_HIGHLIGHT = new Color(255, 90, 100);
        private static final Color MARQUEE_BG = new Color(12, 3, 5);
        private static final Color DOT_DIM = new Color(80, 12, 18, 130);

        // Text palette (3-layer depth)
        private static final Color TXT_SHADOW = new Color(70, 6, 10);
        private static final Color TXT_MID = new Color(200, 30, 36);
        private static final Color TXT_BRIGHT = new Color(255, 60, 70);
        private static final Color TXT_HIGHLIGHT = new Color(255, 180, 180);
        private static final Color GLOW = new Color(255, 30, 30);

        /**
         * Ninja sprite — 18 columns, 22 rows. Facing right with raised sword.
         * '#' = body, 'S' = sword (rendered with brighter tint).
         */
        private static final String[] NINJA = {
                "....######........",
                "...########.......",
                "..##.####.##......",
                "..##########......",
                "..##########......",
                "...########.......",
                "...########.......",
                "....######....SSSS",
                "..##########.SSS..",
                ".############SS...",
                "###.######.###....",
                "##..######..##....",
                "..#.######.#......",
                "..##########......",
                "..####..####......",
                "..####..####......",
                "..####..####......",
                "..###....###......",
                "...##....##.......",
                "..####..####......",
                ".######.######....",
                ".######.######...."
        };

        private BufferedImage titleLine1;
        private BufferedImage titleLine2;
        private long birth = System.currentTimeMillis();
        private final Timer timer;
        private final Font baseFont;

        ArcadeMarquee() {
            setOpaque(false);
            setPreferredSize(new Dimension(W, H));
            setMaximumSize(new Dimension(W, H));
            this.baseFont = pickArcadeFont(30);
            this.titleLine1 = textBitmap("ASSASSIN'S", baseFont);
            this.titleLine2 = textBitmap("DUEL", baseFont);
            timer = new Timer(60, e -> repaint());
            timer.start();
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
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            int w = getWidth(), h = getHeight();

            double t = (System.currentTimeMillis() - birth) * 0.0028;
            float pulse = (float) (0.85 + 0.15 * (Math.sin(t) + 1) / 2);

            // ----- frame -----
            g.setColor(FRAME_OUTER);
            g.fillRoundRect(0, 0, w, h, 30, 30);
            g.setColor(FRAME_MID);
            g.fillRoundRect(5, 5, w - 10, h - 10, 26, 26);
            g.setColor(FRAME_HIGHLIGHT);
            g.fillRect(8, 7, w - 16, 2);
            // Inner dark panel
            g.setColor(MARQUEE_BG);
            g.fillRoundRect(14, 14, w - 28, h - 28, 18, 18);

            // ----- dot-matrix background inside panel -----
            g.setColor(DOT_DIM);
            for (int yy = 22; yy < h - 22; yy += 5) {
                for (int xx = 22; xx < w - 22; xx += 5) {
                    g.fillRect(xx, yy, 1, 1);
                }
            }

            // ----- art deco ornaments (top & bottom center) -----
            drawOrnament(g, w / 2, 16);
            drawOrnament(g, w / 2, h - 16);

            // ----- ninja sprites (flanking the title) -----
            int ninjaScale = 6;
            int ninjaW = NINJA[0].length() * ninjaScale;
            int ninjaH = NINJA.length * ninjaScale;
            int ninjaY = h / 2 - ninjaH / 2 + 10;
            drawPixelSprite(g, NINJA, 26, ninjaY, ninjaScale, false);
            drawPixelSprite(g, NINJA, w - 26 - ninjaW, ninjaY, ninjaScale, true);

            // ----- title pixel-art text (two lines) -----
            int scale = 3;
            int line1W = titleLine1.getWidth() * scale;
            int line2W = titleLine2.getWidth() * scale;
            int line1H = titleLine1.getHeight() * scale;
            int titleAreaTop = 38;
            int line1X = w / 2 - line1W / 2;
            int line2X = w / 2 - line2W / 2 + 30; // slight right offset like the reference
            int line1Y = titleAreaTop;
            int line2Y = titleAreaTop + line1H - 20;

            // Outer red glow (multiple offsets, low alpha) for the neon feel
            for (int r = 6; r >= 2; r--) {
                int a = (int) ((42 - (6 - r) * 6) * pulse);
                Color gc = withAlpha(GLOW, a);
                for (int dx = -r; dx <= r; dx += r) {
                    for (int dy = -r; dy <= r; dy += r) {
                        if (dx == 0 && dy == 0) continue;
                        drawPixelText(g, titleLine1, line1X + dx, line1Y + dy, scale, gc);
                        drawPixelText(g, titleLine2, line2X + dx, line2Y + dy, scale, gc);
                    }
                }
            }

            // Hard drop shadow (down-right)
            drawPixelText(g, titleLine1, line1X + 6, line1Y + 6, scale, TXT_SHADOW);
            drawPixelText(g, titleLine2, line2X + 6, line2Y + 6, scale, TXT_SHADOW);

            // Mid red layer (offset slightly down-right to fake bevel)
            drawPixelText(g, titleLine1, line1X + 2, line1Y + 2, scale, TXT_MID);
            drawPixelText(g, titleLine2, line2X + 2, line2Y + 2, scale, TXT_MID);

            // Bright red main fill
            drawPixelText(g, titleLine1, line1X, line1Y, scale, TXT_BRIGHT);
            drawPixelText(g, titleLine2, line2X, line2Y, scale, TXT_BRIGHT);

            // Top highlight strip (upper 35% of each glyph)
            drawPixelTextPartial(g, titleLine1, line1X, line1Y, scale, TXT_HIGHLIGHT, 0.0, 0.35);
            drawPixelTextPartial(g, titleLine2, line2X, line2Y, scale, TXT_HIGHLIGHT, 0.0, 0.35);

            // ----- scanlines over the whole marquee -----
            g.setColor(new Color(0, 0, 0, 70));
            for (int yy = 14; yy < h - 14; yy += 3) g.drawLine(14, yy, w - 14, yy);

            // Subtle inner red border
            g.setColor(new Color(180, 35, 45, 180));
            g.drawRoundRect(14, 14, w - 29, h - 29, 18, 18);

            g.dispose();
        }

        // -------- Helpers --------

        /** Pick a decorative serif/arcade-looking font from what's installed. Falls back to Serif Bold. */
        private static Font pickArcadeFont(int size) {
            String[] candidates = {
                    "Algerian", "Stencil", "Engravers MT", "Copperplate Gothic Bold",
                    "Castellar", "Old English Text MT", "Impact", "Serif"
            };
            String[] available = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
            for (String c : candidates) {
                for (String a : available) {
                    if (a.equalsIgnoreCase(c)) {
                        return new Font(a, Font.BOLD, size);
                    }
                }
            }
            return new Font("Serif", Font.BOLD, size);
        }

        /** Diamond-and-line art-deco ornament centered at (cx, y). */
        private void drawOrnament(Graphics2D g, int cx, int y) {
            Color c1 = new Color(255, 100, 110);
            Color c2 = new Color(200, 40, 50);
            // Central diamond
            int[] dx = {cx, cx + 9, cx, cx - 9};
            int[] dy = {y - 7, y, y + 7, y};
            g.setColor(c1);
            g.fillPolygon(dx, dy, 4);
            g.setColor(new Color(255, 210, 210));
            int[] dx2 = {cx, cx + 4, cx, cx - 4};
            int[] dy2 = {y - 3, y, y + 3, y};
            g.fillPolygon(dx2, dy2, 4);

            // Side spear lines
            g.setColor(c2);
            g.fillRect(cx + 14, y - 1, 80, 2);
            g.fillRect(cx - 94, y - 1, 80, 2);
            // Tick marks
            g.setColor(c1);
            for (int i = 0; i < 4; i++) {
                g.fillRect(cx + 18 + i * 12, y - 1, 4, 2);
                g.fillRect(cx - 22 - i * 12, y - 1, 4, 2);
            }

            // Side mini diamonds
            for (int sign : new int[]{-1, 1}) {
                int px = cx + sign * 100;
                int[] mx = {px, px + 5, px, px - 5};
                int[] my = {y - 5, y, y + 5, y};
                g.setColor(c1);
                g.fillPolygon(mx, my, 4);
            }
        }

        /** Render text into a small monochrome (white) bitmap with AA off. */
        private static BufferedImage textBitmap(String s, Font font) {
            Canvas c = new Canvas();
            FontMetrics fm = c.getFontMetrics(font);
            int w = Math.max(1, fm.stringWidth(s));
            int h = fm.getAscent() + fm.getDescent();
            BufferedImage img = new BufferedImage(w + 2, h + 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setFont(font);
            g.setColor(Color.WHITE);
            g.drawString(s, 1, fm.getAscent());
            g.dispose();
            return img;
        }

        /** Recolor `bm` to `color` and draw it scaled with nearest-neighbor. */
        private static void drawPixelText(Graphics2D g, BufferedImage bm, int dstX, int dstY, int scale, Color color) {
            drawPixelTextPartial(g, bm, dstX, dstY, scale, color, 0.0, 1.0);
        }

        /** Draw only a vertical slice of the glyph bitmap (rowStart..rowEnd as fractions). */
        private static void drawPixelTextPartial(Graphics2D g, BufferedImage bm, int dstX, int dstY, int scale,
                                                 Color color, double rowStart, double rowEnd) {
            int rs = (int) (bm.getHeight() * rowStart);
            int re = (int) (bm.getHeight() * rowEnd);
            if (re <= rs) return;
            BufferedImage tinted = new BufferedImage(bm.getWidth(), bm.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int colRGB = color.getRGB() & 0xFFFFFF;
            int colA = color.getAlpha();
            for (int y = rs; y < re; y++) {
                for (int x = 0; x < bm.getWidth(); x++) {
                    int p = bm.getRGB(x, y);
                    int a = (p >>> 24) & 0xff;
                    if (a > 0) {
                        int effA = (a * colA) / 255;
                        tinted.setRGB(x, y, (effA << 24) | colRGB);
                    }
                }
            }
            Object prev = g.getRenderingHint(RenderingHints.KEY_INTERPOLATION);
            g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
            g.drawImage(tinted, dstX, dstY, bm.getWidth() * scale, bm.getHeight() * scale, null);
            if (prev != null) g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, prev);
        }

        /** Draw the ninja sprite. '#' = body, 'S' = sword (brighter). Shading applied per pixel block. */
        private static void drawPixelSprite(Graphics2D g, String[] sprite, int x, int y, int scale, boolean mirror) {
            int rows = sprite.length;
            int cols = sprite[0].length();
            Color body = TXT_BRIGHT;
            Color bodyShadow = new Color(120, 14, 20);
            Color bodyHighlight = new Color(255, 130, 130);
            Color sword = new Color(255, 200, 200);
            Color swordEdge = new Color(255, 240, 240);
            for (int r = 0; r < rows; r++) {
                String row = sprite[r];
                for (int c = 0; c < cols; c++) {
                    char ch = row.charAt(mirror ? (cols - 1 - c) : c);
                    if (ch == '.') continue;
                    int px = x + c * scale;
                    int py = y + r * scale;
                    if (ch == 'S') {
                        g.setColor(sword);
                        g.fillRect(px, py, scale, scale);
                        g.setColor(swordEdge);
                        g.fillRect(px, py, scale, 1);
                    } else {
                        g.setColor(body);
                        g.fillRect(px, py, scale, scale);
                        g.setColor(bodyShadow);
                        g.fillRect(px, py + scale - 1, scale, 1);
                        g.fillRect(px + scale - 1, py, 1, scale);
                        g.setColor(bodyHighlight);
                        g.fillRect(px, py, scale, 1);
                    }
                }
            }
        }

        private static Color withAlpha(Color c, int a) {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
        }
    }
}
