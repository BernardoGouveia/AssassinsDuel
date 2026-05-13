import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GameFrame extends JFrame {
    private final GameState state;
    private final GamePanel gamePanel;
    private final JLabel turnLabel;
    private final JLabel[] hpLabels;
    private final JLabel[] apLabels;
    private final JLabel[] healLabels;
    private final JPanel[] playerCards;
    private JTextArea logArea;
    private final JButton[] actionButtons = new JButton[Action.values().length];
    private final JCheckBox soundToggle;

    public GameFrame(int numPlayers) {
        this.state = new GameState(numPlayers);
        this.hpLabels = new JLabel[numPlayers];
        this.apLabels = new JLabel[numPlayers];
        this.healLabels = new JLabel[numPlayers];
        this.playerCards = new JPanel[numPlayers];

        setTitle("Duelo dos Assassinos — " + numPlayers + " jogadores");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));
        getContentPane().setBackground(new Color(15, 17, 22));

        // Top header
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(new Color(15, 17, 22));
        top.setBorder(new EmptyBorder(8, 12, 4, 12));
        JLabel title = new JLabel("Duelo dos Assassinos");
        title.setForeground(new Color(230, 230, 240));
        title.setFont(new Font("SansSerif", Font.BOLD, 22));
        top.add(title, BorderLayout.WEST);
        turnLabel = new JLabel();
        turnLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        top.add(turnLabel, BorderLayout.EAST);
        add(top, BorderLayout.NORTH);

        // Center: game grid
        gamePanel = new GamePanel(state, this::handleActionResult);
        JPanel centerWrap = new JPanel();
        centerWrap.setBackground(new Color(15, 17, 22));
        centerWrap.add(gamePanel);
        add(centerWrap, BorderLayout.CENTER);

        soundToggle = new JCheckBox("Som", true);
        add(buildSidePanel(), BorderLayout.EAST);
        add(buildActionPanel(), BorderLayout.SOUTH);

        refresh();
        pack();
        setLocationRelativeTo(null);
    }

    private JPanel buildSidePanel() {
        JPanel side = new JPanel();
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBackground(new Color(25, 27, 34));
        side.setBorder(new EmptyBorder(8, 8, 8, 8));
        side.setPreferredSize(new Dimension(290, 0));

        for (int i = 0; i < state.players.length; i++) {
            playerCards[i] = buildPlayerCard(i);
            side.add(playerCards[i]);
            side.add(Box.createVerticalStrut(8));
        }

        JLabel logTitle = new JLabel("Registo:");
        logTitle.setForeground(Color.WHITE);
        logTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        logTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        side.add(logTitle);
        side.add(Box.createVerticalStrut(4));

        int logRows = Math.max(6, 14 - state.players.length * 2);
        logArea = createLogArea(logRows);
        JScrollPane sp = new JScrollPane(logArea);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70)));
        side.add(sp);

        return side;
    }

    private JTextArea createLogArea(int rows) {
        JTextArea ta = new JTextArea(rows, 22);
        ta.setEditable(false);
        ta.setBackground(new Color(15, 17, 22));
        ta.setForeground(new Color(200, 200, 210));
        ta.setFont(new Font("Monospaced", Font.PLAIN, 11));
        ta.setLineWrap(true);
        ta.setWrapStyleWord(true);
        return ta;
    }

    private JPanel buildPlayerCard(int idx) {
        Assassin a = state.players[idx];
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(35, 38, 46));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(a.color, 2),
                new EmptyBorder(5, 8, 5, 8)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 80));

        JLabel name = new JLabel(a.displayNumber + " — " + a.name);
        name.setForeground(a.color);
        name.setFont(new Font("SansSerif", Font.BOLD, 13));
        card.add(name);

        hpLabels[idx] = makeInfoLabel("");
        apLabels[idx] = makeInfoLabel("");
        healLabels[idx] = makeInfoLabel("");
        card.add(hpLabels[idx]);
        card.add(apLabels[idx]);
        card.add(healLabels[idx]);
        return card;
    }

    private JLabel makeInfoLabel(String txt) {
        JLabel l = new JLabel(txt);
        l.setForeground(new Color(220, 220, 230));
        l.setFont(new Font("SansSerif", Font.PLAIN, 11));
        return l;
    }

    /** Configure a button so its background and foreground colors are actually painted on every L&F. */
    private void styleButton(JButton b, Color bg, Color fg) {
        b.setOpaque(true);
        b.setBorderPainted(false);
        b.setContentAreaFilled(true);
        b.setFocusPainted(false);
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setBorder(BorderFactory.createEmptyBorder(7, 12, 7, 12));
    }

    private JPanel buildActionPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        bottom.setBackground(new Color(25, 27, 34));

        Action[] acts = Action.values();
        for (int i = 0; i < acts.length; i++) {
            final Action a = acts[i];
            JButton b = new JButton();
            styleButton(b, new Color(60, 65, 80), Color.WHITE);
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

        JButton end = new JButton("Terminar Turno");
        styleButton(end, new Color(150, 60, 60), Color.WHITE);
        end.addActionListener(e -> {
            state.endTurn();
            refresh();
            gamePanel.repaint();
        });
        bottom.add(end);

        JButton newGame = new JButton("Novo Jogo");
        styleButton(newGame, new Color(60, 110, 70), Color.WHITE);
        newGame.addActionListener(e -> {
            int n = Main.chooseNumPlayers();
            if (n < 0) return;
            dispose();
            new GameFrame(n).setVisible(true);
        });
        bottom.add(newGame);

        soundToggle.setBackground(new Color(25, 27, 34));
        soundToggle.setForeground(Color.WHITE);
        soundToggle.setOpaque(true);
        soundToggle.setFocusPainted(false);
        soundToggle.setFont(new Font("SansSerif", Font.PLAIN, 12));
        soundToggle.addActionListener(e -> SoundFx.setEnabled(soundToggle.isSelected()));
        bottom.add(soundToggle);

        return bottom;
    }

    private void handleActionResult(String msg) {
        refresh();
    }

    private void refresh() {
        Assassin curr = state.currentPlayer();
        turnLabel.setText("Turno " + state.turnNumber + "  |  Vez de: " + curr.name + "  |  AP: " + curr.ap + "/" + curr.maxAp);
        turnLabel.setForeground(curr.color);

        for (int i = 0; i < state.players.length; i++) {
            Assassin a = state.players[i];
            hpLabels[i].setText("HP: " + a.hp + " / " + a.maxHp + (a.damageBoost > 0 ? "  ⚡+" + a.damageBoost : ""));
            apLabels[i].setText("AP: " + a.ap + " / " + a.maxAp);
            healLabels[i].setText("Cura: " + (a.usedHeal ? "usada" : "disponível") + (a.isAlive() ? "" : "   ☠ ELIMINADO"));

            playerCards[i].setBackground(a.isAlive() ? new Color(35, 38, 46) : new Color(28, 28, 32));
            javax.swing.border.Border outline = (i == state.currentPlayerIdx && state.winnerIdx < 0)
                    ? BorderFactory.createLineBorder(a.color, 3)
                    : BorderFactory.createLineBorder(a.color, 2);
            playerCards[i].setBorder(BorderFactory.createCompoundBorder(outline, new EmptyBorder(5, 8, 5, 8)));
        }

        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, state.log.size() - 80);
        for (int i = start; i < state.log.size(); i++) {
            sb.append(state.log.get(i)).append("\n");
        }
        logArea.setText(sb.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength());

        Action[] acts = Action.values();
        for (int i = 0; i < acts.length; i++) {
            Action a = acts[i];
            boolean selected = (state.selectedAction == a);
            String prefix = selected ? "▶ " : "";
            actionButtons[i].setText(prefix + a.label + " (" + a.cost + " AP)");
            actionButtons[i].setBackground(selected ? new Color(90, 110, 180) : new Color(60, 65, 80));
        }
    }
}
