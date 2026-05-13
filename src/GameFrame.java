import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.KeyEvent;

public class GameFrame extends JFrame {
    private final GameState state;
    private final GamePanel gamePanel;
    private final JLabel turnLabel;
    private final PlayerCard[] playerCards;
    private JTextArea logArea;
    private final ActionButton[] actionButtons = new ActionButton[Action.values().length];
    private final JCheckBox soundToggle;

    private static final Color BG = new Color(12, 4, 6);
    private static final Color BG_PANEL = new Color(18, 8, 10);
    private static final Color RED_BRIGHT = new Color(255, 60, 60);
    private static final Color RED_DIM = new Color(140, 30, 35);
    private static final Color GOLD = new Color(255, 200, 80);
    private static final Color TEXT = new Color(255, 220, 220);
    private static final Color SUBTLE = new Color(190, 130, 130);
    // Legacy aliases (so the rest of the file's references resolve to the new palette)
    private static final Color CYAN = RED_BRIGHT;
    private static final Color CYAN_DIM = RED_DIM;
    private static final Color MAGENTA = GOLD;

    public GameFrame(int numPlayers) {
        this.state = new GameState(numPlayers);
        this.playerCards = new PlayerCard[numPlayers];

        setTitle("Assassin's Duel — " + numPlayers + " jogadores");
        setIconImages(AppIcon.images());
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                confirmQuit();
            }
        });
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(BG);

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG);
        top.setBorder(new EmptyBorder(10, 14, 6, 14));
        JLabel title = new JLabel("// ASSASSIN'S DUEL");
        title.setForeground(CYAN);
        title.setFont(new Font("Monospaced", Font.BOLD, 22));
        top.add(title, BorderLayout.WEST);
        turnLabel = new JLabel();
        turnLabel.setFont(new Font("Monospaced", Font.BOLD, 14));
        top.add(turnLabel, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        gamePanel = new GamePanel(state, this::handleActionResult);
        JPanel centerWrap = new JPanel();
        centerWrap.setBackground(BG);
        centerWrap.add(gamePanel);
        add(centerWrap, BorderLayout.CENTER);

        soundToggle = new JCheckBox("SOM", true);
        add(buildSidePanel(), BorderLayout.EAST);
        add(buildActionPanel(), BorderLayout.SOUTH);

        refresh();
        pack();
        setResizable(false);
        setLocationRelativeTo(null);

        // Refresh HUD on a timer so HP bars and turn timer stay in sync without action ticks
        new Timer(80, e -> refreshLight()).start();

        installKeyBindings();
    }

    private void installKeyBindings() {
        InputMap im = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getRootPane().getActionMap();

        // Esc → pause toggle
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "togglePause");
        am.put("togglePause", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (state.winnerIdx >= 0) return;
                state.paused = !state.paused;
                refresh();
                gamePanel.repaint();
            }
        });

        // 1..4 → select action
        Action[] acts = Action.values();
        for (int i = 0; i < acts.length; i++) {
            final Action a = acts[i];
            String key = "action" + i;
            im.put(KeyStroke.getKeyStroke(KeyEvent.VK_1 + i, 0), key);
            am.put(key, new AbstractAction() {
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (state.paused || state.winnerIdx >= 0) return;
                    state.selectedAction = a;
                    if (a == Action.HEAL) {
                        String msg = state.performAction(state.currentPlayer().x, state.currentPlayer().y);
                        if (msg != null) handleActionResult(msg);
                    }
                    refresh();
                    gamePanel.repaint();
                }
            });
        }

        // Space → end turn
        im.put(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0), "endTurn");
        am.put("endTurn", new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent e) {
                if (state.paused || state.winnerIdx >= 0) return;
                state.endTurn();
                refresh();
                gamePanel.repaint();
            }
        });
    }

    private static String tooltipFor(Action a) {
        switch (a) {
            case MOVE: return "Move para uma casa adjacente (1 AP). Atalho: 1";
            case MELEE: return "Ataque corpo-a-corpo: 30 dano em casa adjacente (1 AP). Atalho: 2";
            case SHURIKEN: return "Shuriken: 20 dano, alcance 5, precisa linha de visão (2 AP). Atalho: 3";
            case HEAL: return "Cura +30 HP, apenas uma vez por jogo (2 AP). Atalho: 4";
            default: return "";
        }
    }

    private void confirmQuit() {
        if (state.winnerIdx < 0) {
            int r = JOptionPane.showConfirmDialog(this,
                    "Tens a certeza que queres sair do duelo a meio?",
                    "Sair",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
            if (r != JOptionPane.YES_OPTION) return;
        }
        System.exit(0);
    }

    private JPanel buildSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(BG_PANEL);
        side.setBorder(new EmptyBorder(10, 10, 10, 10));
        side.setPreferredSize(new Dimension(310, 0));

        for (int i = 0; i < state.players.length; i++) {
            playerCards[i] = new PlayerCard(state.players[i]);
            side.add(playerCards[i]);
            side.add(Box.createVerticalStrut(8));
        }

        JLabel logTitle = new JLabel("// REGISTO");
        logTitle.setForeground(CYAN);
        logTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        logTitle.setFont(new Font("Monospaced", Font.BOLD, 12));
        side.add(logTitle);
        side.add(Box.createVerticalStrut(4));

        int logRows = Math.max(6, 14 - state.players.length * 2);
        logArea = createLogArea(logRows);
        JScrollPane sp = new JScrollPane(logArea);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setBorder(BorderFactory.createLineBorder(CYAN_DIM));
        side.add(sp);

        return side;
    }

    private JTextArea createLogArea(int rows) {
        JTextArea ta = new JTextArea(rows, 22);
        ta.setEditable(false);
        ta.setBackground(BG);
        ta.setForeground(new Color(200, 220, 240));
        ta.setFont(new Font("Monospaced", Font.PLAIN, 11));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        ta.setCaretColor(CYAN);
        return ta;
    }

    private JPanel buildActionPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 8, 10));
        bottom.setBackground(BG_PANEL);

        Action[] acts = Action.values();
        for (int i = 0; i < acts.length; i++) {
            final Action a = acts[i];
            ActionButton b = new ActionButton(a);
            b.setToolTipText(tooltipFor(a));
            b.addActionListener(e -> {
                state.selectedAction = a;
                if (a == Action.HEAL) {
                    String msg = state.performAction(state.currentPlayer().x, state.currentPlayer().y);
                    if (msg != null) handleActionResult(msg);
                }
                refresh();
                gamePanel.repaint();
            });
            actionButtons[i] = b;
            bottom.add(b);
        }

        JButton end = makeRetroButton("TERMINAR TURNO", new Color(255, 110, 130));
        end.setToolTipText("Passa a vez ao próximo jogador (atalho: Espaço)");
        end.addActionListener(e -> {
            state.endTurn();
            refresh();
            gamePanel.repaint();
        });
        bottom.add(end);

        JButton newGame = makeRetroButton("NOVO JOGO", new Color(80, 255, 140));
        newGame.setToolTipText("Começa um novo duelo");
        newGame.addActionListener(e -> {
            int n = Main.chooseNumPlayers();
            if (n < 0) return;
            dispose();
            new GameFrame(n).setVisible(true);
        });
        bottom.add(newGame);

        soundToggle.setBackground(BG_PANEL);
        soundToggle.setForeground(TEXT);
        soundToggle.setOpaque(true);
        soundToggle.setFocusPainted(false);
        soundToggle.setFont(new Font("Monospaced", Font.BOLD, 12));
        soundToggle.addActionListener(e -> SoundFx.setEnabled(soundToggle.isSelected()));
        bottom.add(soundToggle);

        return bottom;
    }

    private JButton makeRetroButton(String label, Color accent) {
        JButton b = new JButton(label) {
            @Override
            protected void paintComponent(Graphics g0) {
                Graphics2D g = (Graphics2D) g0.create();
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g.setColor(new Color(8, 14, 22));
                g.fillRect(0, 0, w, h);
                g.setColor(getModel().isRollover() ? accent : accent.darker());
                g.setStroke(new BasicStroke(getModel().isRollover() ? 2.2f : 1.4f));
                g.drawRect(2, 2, w - 5, h - 5);
                g.fillPolygon(new int[]{0, 8, 0}, new int[]{0, 0, 8}, 3);
                g.fillPolygon(new int[]{w, w - 8, w}, new int[]{h, h, h - 8}, 3);
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
        b.setFont(new Font("Monospaced", Font.BOLD, 12));
        b.setPreferredSize(new Dimension(150, 38));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private void handleActionResult(String msg) {
        refresh();
    }

    private void refreshLight() {
        for (PlayerCard pc : playerCards) pc.repaint();
        for (ActionButton b : actionButtons) b.repaint();
    }

    private void refresh() {
        Assassin curr = state.currentPlayer();
        String apTxt = curr.ap > curr.maxAp
                ? "AP " + curr.ap + " (+" + (curr.ap - curr.maxAp) + " BÓNUS)"
                : "AP " + curr.ap + "/" + curr.maxAp;
        turnLabel.setText("TURNO " + state.turnNumber + " | " + curr.name.toUpperCase() + " | " + apTxt);
        turnLabel.setForeground(curr.color);

        for (int i = 0; i < state.players.length; i++) {
            playerCards[i].setIsCurrent(i == state.currentPlayerIdx && state.winnerIdx < 0);
            playerCards[i].repaint();
        }

        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, state.log.size() - 80);
        for (int i = start; i < state.log.size(); i++) {
            sb.append(state.log.get(i)).append("\n");
        }
        logArea.setText(sb.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength());

        for (ActionButton b : actionButtons) {
            b.setActionSelected(state.selectedAction == b.action);
            b.repaint();
        }
    }

    // ---------- Player card with big HP bar ----------

    private class PlayerCard extends JComponent {
        private final Assassin a;
        private boolean isCurrent = false;

        PlayerCard(Assassin a) {
            this.a = a;
            setPreferredSize(new Dimension(290, 88));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 88));
            setAlignmentX(Component.LEFT_ALIGNMENT);
        }

        void setIsCurrent(boolean c) { this.isCurrent = c; }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            // Card background
            g.setColor(a.isAlive() ? new Color(16, 22, 34) : new Color(12, 14, 20));
            g.fillRect(0, 0, w, h);

            // Side accent bar (player color)
            g.setColor(a.color);
            g.fillRect(0, 0, 4, h);

            // Border (cyan if current, dim otherwise)
            Color border = isCurrent ? CYAN : CYAN_DIM;
            g.setColor(border);
            g.setStroke(new BasicStroke(isCurrent ? 2.2f : 1.2f));
            g.drawRect(1, 1, w - 3, h - 3);

            // Avatar circle
            int avx = 10, avy = 8, avs = 36;
            g.setColor(new Color(a.color.getRed(), a.color.getGreen(), a.color.getBlue(), 80));
            g.fillOval(avx - 2, avy - 2, avs + 4, avs + 4);
            g.setColor(a.color);
            g.fillOval(avx, avy, avs, avs);
            g.setColor(Color.BLACK);
            g.drawOval(avx, avy, avs, avs);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 18));
            FontMetrics fm = g.getFontMetrics();
            String n = String.valueOf(a.displayNumber);
            int nw = fm.stringWidth(n);
            g.drawString(n, avx + avs / 2 - nw / 2, avy + avs / 2 + fm.getAscent() / 2 - 2);

            // Name
            g.setColor(a.color);
            g.setFont(new Font("Monospaced", Font.BOLD, 13));
            g.drawString(a.name.toUpperCase(), 56, 22);

            // Status
            String status = a.isAlive()
                    ? (a.usedHeal ? "CURA: usada" : "CURA: disponivel")
                    : "☠ ELIMINADO";
            g.setColor(a.isAlive() ? SUBTLE : new Color(255, 80, 110));
            g.setFont(new Font("Monospaced", Font.PLAIN, 11));
            g.drawString(status, 56, 38);

            // Big HP bar
            int barX = 10, barY = 52, barW = w - 20, barH = 14;
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(barX - 1, barY - 1, barW + 2, barH + 2);
            g.setColor(new Color(28, 34, 48));
            g.fillRect(barX, barY, barW, barH);
            int hpW = (int) (barW * (a.hp / (double) a.maxHp));
            Color hpc = a.hp > a.maxHp * 0.5 ? new Color(80, 255, 140)
                    : a.hp > a.maxHp * 0.25 ? new Color(255, 220, 80)
                    : new Color(255, 80, 110);
            // gradient on HP fill
            GradientPaint gp = new GradientPaint(barX, barY, hpc.brighter(), barX, barY + barH, hpc.darker());
            g.setPaint(gp);
            g.fillRect(barX, barY, hpW, barH);
            // scanlines on bar
            g.setColor(new Color(255, 255, 255, 30));
            for (int y = barY + 1; y < barY + barH; y += 3) g.drawLine(barX, y, barX + hpW, y);
            // outline
            g.setColor(CYAN_DIM);
            g.drawRect(barX, barY, barW, barH);

            // HP text
            String hpTxt = a.hp + " / " + a.maxHp;
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 11));
            FontMetrics fm2 = g.getFontMetrics();
            g.drawString(hpTxt, barX + barW / 2 - fm2.stringWidth(hpTxt) / 2, barY + barH - 3);

            // AP pips — show extras (beyond maxAp) in gold so the bonus is visible.
            int apY = 72;
            int pipR = 7;
            g.setFont(new Font("Monospaced", Font.BOLD, 10));
            g.setColor(SUBTLE);
            g.drawString("AP", 10, apY + 5);
            int totalPips = Math.max(a.ap, a.maxAp);
            for (int i = 0; i < totalPips; i++) {
                int px = 36 + i * (pipR * 2 + 4);
                boolean filled = i < a.ap;
                boolean bonus = i >= a.maxAp;
                if (filled && bonus) {
                    g.setColor(new Color(255, 200, 80));
                } else if (filled) {
                    g.setColor(CYAN);
                } else {
                    g.setColor(new Color(30, 40, 56));
                }
                g.fillOval(px, apY - 2, pipR * 2, pipR * 2);
                if (filled && bonus) {
                    g.setColor(new Color(255, 240, 200));
                } else if (filled) {
                    g.setColor(Color.WHITE);
                } else {
                    g.setColor(CYAN_DIM);
                }
                g.drawOval(px, apY - 2, pipR * 2, pipR * 2);
            }

            // Damage boost indicator (offset further right so it doesn't clash with extra pips).
            if (a.damageBoost > 0) {
                int dbX = Math.max(w - 56, 36 + totalPips * (pipR * 2 + 4) + 10);
                g.setColor(new Color(255, 200, 70));
                g.setFont(new Font("Monospaced", Font.BOLD, 11));
                g.drawString("⚡+" + a.damageBoost, dbX, apY + 6);
            }

            g.dispose();
        }
    }

    // ---------- Action button with icon ----------

    private class ActionButton extends JButton {
        final Action action;
        private boolean selectedAct = false;

        ActionButton(Action a) {
            this.action = a;
            setOpaque(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setPreferredSize(new Dimension(108, 56));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setFont(new Font("Monospaced", Font.BOLD, 11));
        }

        void setActionSelected(boolean s) { this.selectedAct = s; }

        @Override
        protected void paintComponent(Graphics g0) {
            Graphics2D g = (Graphics2D) g0.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth(), h = getHeight();

            Color accent = colorFor(action);
            g.setColor(new Color(8, 14, 22));
            g.fillRect(0, 0, w, h);

            if (selectedAct) {
                g.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 50));
                g.fillRect(0, 0, w, h);
            }

            g.setColor(selectedAct ? accent : (getModel().isRollover() ? accent : accent.darker().darker()));
            g.setStroke(new BasicStroke(selectedAct ? 2.4f : (getModel().isRollover() ? 2.0f : 1.4f)));
            g.drawRect(2, 2, w - 5, h - 5);
            g.fillPolygon(new int[]{0, 8, 0}, new int[]{0, 0, 8}, 3);
            g.fillPolygon(new int[]{w, w - 8, w}, new int[]{h, h, h - 8}, 3);

            // Icon (left side)
            int iconX = 10, iconY = h / 2;
            drawActionIcon(g, action, iconX, iconY, accent);

            // Text (right side)
            g.setColor(selectedAct ? Color.WHITE : accent);
            g.setFont(getFont());
            FontMetrics fm = g.getFontMetrics();
            g.drawString(action.label.toUpperCase(), 36, h / 2 - 2);
            g.setColor(selectedAct ? Color.WHITE : SUBTLE);
            g.setFont(new Font("Monospaced", Font.PLAIN, 10));
            g.drawString(action.cost + " AP", 36, h / 2 + fm.getAscent() - 2);

            g.dispose();
        }

        private Color colorFor(Action a) {
            switch (a) {
                case MOVE: return GOLD;                       // gold for movement
                case MELEE: return RED_BRIGHT;                // bright red for aggressive
                case SHURIKEN: return new Color(220, 220, 235); // steel/silver
                case HEAL: return new Color(80, 220, 130);    // green still reads as heal
                default: return RED_BRIGHT;
            }
        }
    }

    /** Tiny pixel-style icon per action, drawn around (cx, cy). */
    private static void drawActionIcon(Graphics2D g, Action a, int cx, int cy, Color accent) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.translate(cx, cy);
        switch (a) {
            case MOVE: {
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(-7, 0, 7, 0);
                g2.drawLine(0, -7, 0, 7);
                int[] xs = {7, 2, 2};
                int[] ys = {0, -4, 4};
                g2.fillPolygon(xs, ys, 3);
                break;
            }
            case MELEE: {
                g2.setColor(accent);
                g2.setStroke(new BasicStroke(2f));
                g2.drawLine(-7, 7, 7, -7);
                int[] xs = {7, 4, 7};
                int[] ys = {-7, -4, -1};
                g2.fillPolygon(xs, ys, 3);
                g2.setColor(new Color(170, 170, 180));
                g2.fillRect(-9, 5, 6, 4);
                break;
            }
            case SHURIKEN: {
                int r = 8;
                int[] xs = {0, r / 3, r, r / 3, 0, -r / 3, -r, -r / 3};
                int[] ys = {-r, -r / 3, 0, r / 3, r, r / 3, 0, -r / 3};
                g2.setColor(new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 100));
                g2.fillOval(-r - 2, -r - 2, (r + 2) * 2, (r + 2) * 2);
                g2.setColor(accent);
                g2.fillPolygon(xs, ys, 8);
                g2.setColor(Color.WHITE);
                g2.fillOval(-1, -1, 3, 3);
                break;
            }
            case HEAL: {
                g2.setColor(accent);
                g2.fillRect(-2, -8, 4, 16);
                g2.fillRect(-8, -2, 16, 4);
                g2.setColor(Color.WHITE);
                g2.drawRect(-2, -8, 4, 16);
                g2.drawRect(-8, -2, 16, 4);
                break;
            }
        }
        g2.dispose();
    }
}
