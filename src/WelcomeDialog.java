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

    static final Color BG = new Color(6, 10, 18);
    static final Color BG_DEEP = new Color(2, 4, 10);
    static final Color CYAN = new Color(40, 230, 255);
    static final Color MAGENTA = new Color(255, 50, 180);
    static final Color SUBTLE = new Color(140, 170, 200);

    public WelcomeDialog() {
        super((Frame) null, "Assassin's Duel", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(820, 640);
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
        hint.setForeground(CYAN);
        content.add(hint);

        content.add(Box.createVerticalStrut(20));

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
            int w = Math.max(getWidth(), 820);
            int h = Math.max(getHeight(), 640);
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
                    s.x = rng.nextInt(Math.max(getWidth(), 820));
                }
            }
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            GradientPaint gp = new GradientPaint(0, 0, BG_DEEP, 0, h, new Color(10, 18, 32));
            g.setPaint(gp);
            g.fillRect(0, 0, w, h);

            g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 22));
            int step = 40;
            for (int x = 0; x < w; x += step) g.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += step) g.drawLine(0, y, w, y);

            int horizon = (int) (h * 0.72);
            g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 80));
            g.setStroke(new BasicStroke(1.2f));
            g.drawLine(0, horizon, w, horizon);
            g.setColor(new Color(MAGENTA.getRed(), MAGENTA.getGreen(), MAGENTA.getBlue(), 40));
            g.drawLine(0, horizon + 2, w, horizon + 2);

            for (Shuriken s : shurikens) drawShuriken(g, s);

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
            g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), a / 3));
            g2.fillOval(-r - 4, -r - 4, (r + 4) * 2, (r + 4) * 2);
            int[] xs = {0, r / 3, r, r / 3, 0, -r / 3, -r, -r / 3};
            int[] ys = {-r, -r / 3, 0, r / 3, r, r / 3, 0, -r / 3};
            g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), a));
            g2.fillPolygon(xs, ys, 8);
            g2.setColor(new Color(220, 250, 255, Math.min(255, a + 60)));
            g2.setStroke(new BasicStroke(1.2f));
            g2.drawPolygon(xs, ys, 8);
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

    // ============================================================
    //                  ARCADE MARQUEE (the big sign)
    // ============================================================

    /**
     * Pixel-art arcade marquee: ornamented red frame with two-line pixel title
     * "ASSASSIN'S / DUEL" flanked by pixel-art ninja silhouettes.
     */
    private static class ArcadeMarquee extends JComponent {
        private static final int W = 680;
        private static final int H = 300;

        private static final Color FRAME_OUTER = new Color(110, 18, 22);
        private static final Color FRAME_INNER = new Color(200, 40, 50);
        private static final Color FRAME_HIGHLIGHT = new Color(255, 90, 95);
        private static final Color MARQUEE_BG = new Color(14, 4, 6);
        private static final Color DOT_DIM = new Color(60, 10, 15, 110);

        private static final Color TXT_SHADOW = new Color(90, 8, 14);
        private static final Color TXT_MAIN = new Color(255, 60, 60);
        private static final Color TXT_HIGHLIGHT = new Color(255, 170, 170);
        private static final Color GLOW = new Color(255, 30, 30);

        // 14x16 ninja pixel sprite. Facing right (sword in front). Mirror for the right side.
        // # = body filled (red)
        // . = empty
        private static final String[] NINJA = {
                "...####.......",
                "..######......",
                ".##.####.##...",
                ".########.....",
                "..######......",
                ".########.....",
                "##########....",
                "##.######.##..",
                "##.######.##..",
                "##.######.###.",
                ".########..###",
                ".##.##.##..#..",
                ".##.##.##.....",
                ".##.##.##.....",
                "##...#...##...",
                "##...#...##..."
        };

        private BufferedImage titleLine1;
        private BufferedImage titleLine2;
        private long birth = System.currentTimeMillis();
        private final Timer timer;

        ArcadeMarquee() {
            setOpaque(false);
            setPreferredSize(new Dimension(W, H));
            setMaximumSize(new Dimension(W, H));
            titleLine1 = textBitmap("ASSASSIN'S", 32);
            titleLine2 = textBitmap("DUEL", 32);
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

            // ----- outer frame -----
            // Outer dark red shell
            g.setColor(FRAME_OUTER);
            g.fillRoundRect(0, 0, w, h, 26, 26);
            // Inner bright red ring
            g.setColor(FRAME_INNER);
            g.fillRoundRect(6, 6, w - 12, h - 12, 22, 22);
            // Top highlight stripe
            g.setColor(FRAME_HIGHLIGHT);
            g.fillRect(8, 8, w - 16, 3);
            // Inner dark marquee panel
            g.setColor(MARQUEE_BG);
            g.fillRoundRect(14, 14, w - 28, h - 28, 16, 16);

            // Dot-matrix background inside marquee panel
            g.setColor(DOT_DIM);
            int dot = 3;
            for (int yy = 22; yy < h - 22; yy += dot * 2) {
                for (int xx = 22; xx < w - 22; xx += dot * 2) {
                    g.fillRect(xx, yy, 1, 1);
                }
            }

            // ----- art deco ornaments (top & bottom center) -----
            drawOrnament(g, w / 2, 14, true);
            drawOrnament(g, w / 2, h - 14, false);

            // ----- red glow behind text -----
            int titleTop = 36;
            int line1Y = titleTop;
            int line2Y = titleTop + 110;

            // Pixel art ninja sprites
            int ninjaScale = 8;
            int ninjaW = NINJA[0].length() * ninjaScale;
            int ninjaH = NINJA.length * ninjaScale;
            int ninjaY = h / 2 - ninjaH / 2;
            drawPixelSprite(g, NINJA, 28, ninjaY, ninjaScale, TXT_MAIN, false);
            drawPixelSprite(g, NINJA, w - 28 - ninjaW, ninjaY, ninjaScale, TXT_MAIN, true);

            // ----- title pixel-art text (two lines) -----
            int scale = 4;
            int line1W = titleLine1.getWidth() * scale;
            int line2W = titleLine2.getWidth() * scale;
            int line1X = w / 2 - line1W / 2;
            int line2X = w / 2 - line2W / 2;

            // Layered glow (multiple offsets for depth)
            for (int i = 8; i >= 2; i -= 2) {
                int a = (int) ((40 - (8 - i) * 4) * pulse);
                drawPixelText(g, titleLine1, line1X - i, line1Y, scale, withAlpha(GLOW, a));
                drawPixelText(g, titleLine1, line1X + i, line1Y, scale, withAlpha(GLOW, a));
                drawPixelText(g, titleLine2, line2X - i, line2Y, scale, withAlpha(GLOW, a));
                drawPixelText(g, titleLine2, line2X + i, line2Y, scale, withAlpha(GLOW, a));
            }
            // Shadow
            drawPixelText(g, titleLine1, line1X + 4, line1Y + 4, scale, TXT_SHADOW);
            drawPixelText(g, titleLine2, line2X + 4, line2Y + 4, scale, TXT_SHADOW);
            // Main red fill
            drawPixelText(g, titleLine1, line1X, line1Y, scale, TXT_MAIN);
            drawPixelText(g, titleLine2, line2X, line2Y, scale, TXT_MAIN);
            // Highlight strip (top 1/3 of each letter, lighter red)
            drawPixelTextTop(g, titleLine1, line1X, line1Y, scale, TXT_HIGHLIGHT);
            drawPixelTextTop(g, titleLine2, line2X, line2Y, scale, TXT_HIGHLIGHT);

            // Scanlines over the whole marquee for CRT feel
            g.setColor(new Color(0, 0, 0, 60));
            for (int yy = 14; yy < h - 14; yy += 3) g.drawLine(14, yy, w - 14, yy);

            // Subtle inner border line
            g.setColor(new Color(180, 40, 50, 160));
            g.drawRoundRect(14, 14, w - 29, h - 29, 16, 16);

            g.dispose();
        }

        /** Diamond-and-line art-deco ornament centered at (cx, y). */
        private void drawOrnament(Graphics2D g, int cx, int y, boolean topOriented) {
            Color c1 = new Color(255, 100, 100);
            Color c2 = new Color(200, 40, 50);
            // Central diamond
            int[] dx = {cx, cx + 8, cx, cx - 8};
            int[] dy = {y - 6, y, y + 6, y};
            g.setColor(c1);
            g.fillPolygon(dx, dy, 4);
            g.setColor(new Color(255, 200, 200));
            int[] dx2 = {cx, cx + 4, cx, cx - 4};
            int[] dy2 = {y - 3, y, y + 3, y};
            g.fillPolygon(dx2, dy2, 4);

            // Side spear lines
            g.setColor(c2);
            g.fillRect(cx + 12, y - 1, 60, 2);
            g.fillRect(cx - 72, y - 1, 60, 2);
            g.setColor(c1);
            g.fillRect(cx + 14, y - 1, 4, 2);
            g.fillRect(cx + 24, y - 1, 4, 2);
            g.fillRect(cx + 34, y - 1, 4, 2);
            g.fillRect(cx - 18, y - 1, 4, 2);
            g.fillRect(cx - 28, y - 1, 4, 2);
            g.fillRect(cx - 38, y - 1, 4, 2);

            // Side mini diamonds
            for (int sign : new int[]{-1, 1}) {
                int px = cx + sign * 78;
                int[] mx = {px, px + 4, px, px - 4};
                int[] my = {y - 4, y, y + 4, y};
                g.setColor(c1);
                g.fillPolygon(mx, my, 4);
            }
        }

        // Render text into a small monochrome (white-on-transparent) bitmap with AA off.
        private static BufferedImage textBitmap(String s, int fontSize) {
            Font f = new Font("Monospaced", Font.BOLD, fontSize);
            Canvas c = new Canvas();
            FontMetrics fm = c.getFontMetrics(f);
            int w = Math.max(1, fm.stringWidth(s));
            int h = fm.getAscent() + fm.getDescent();
            BufferedImage img = new BufferedImage(w + 2, h + 2, BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = img.createGraphics();
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
            g.setFont(f);
            g.setColor(Color.WHITE);
            g.drawString(s, 1, fm.getAscent());
            g.dispose();
            return img;
        }

        // Recolor the white pixels of `bm` to `color` and draw it scaled with nearest neighbor.
        private static void drawPixelText(Graphics2D g, BufferedImage bm, int dstX, int dstY, int scale, Color color) {
            BufferedImage tinted = new BufferedImage(bm.getWidth(), bm.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int colRGB = color.getRGB() & 0xFFFFFF;
            int colA = (color.getAlpha());
            for (int y = 0; y < bm.getHeight(); y++) {
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

        // Same as drawPixelText but only the upper 35% of each glyph row (cheap "highlight stripe").
        private static void drawPixelTextTop(Graphics2D g, BufferedImage bm, int dstX, int dstY, int scale, Color color) {
            int hRows = bm.getHeight();
            int topLimit = (int) (hRows * 0.40);
            BufferedImage tinted = new BufferedImage(bm.getWidth(), bm.getHeight(), BufferedImage.TYPE_INT_ARGB);
            int colRGB = color.getRGB() & 0xFFFFFF;
            int colA = color.getAlpha();
            for (int y = 0; y < topLimit; y++) {
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

        // Draw a string-array pixel sprite. Each '#' becomes a scale x scale red square.
        // Adds a small shadow on the lower-right pixels and a highlight on the upper-left.
        private static void drawPixelSprite(Graphics2D g, String[] sprite, int x, int y, int scale, Color color, boolean mirror) {
            int rows = sprite.length;
            int cols = sprite[0].length();
            Color shadow = new Color(80, 10, 15);
            Color highlight = new Color(255, 130, 130);
            for (int r = 0; r < rows; r++) {
                String row = sprite[r];
                for (int c = 0; c < cols; c++) {
                    char ch = row.charAt(mirror ? (cols - 1 - c) : c);
                    if (ch != '#') continue;
                    int px = x + c * scale;
                    int py = y + r * scale;
                    g.setColor(color);
                    g.fillRect(px, py, scale, scale);
                    // simple shading
                    g.setColor(shadow);
                    g.fillRect(px, py + scale - 1, scale, 1);
                    g.fillRect(px + scale - 1, py, 1, scale);
                    g.setColor(highlight);
                    g.fillRect(px, py, scale, 1);
                }
            }
        }

        private static Color withAlpha(Color c, int a) {
            return new Color(c.getRed(), c.getGreen(), c.getBlue(), Math.max(0, Math.min(255, a)));
        }
    }
}
