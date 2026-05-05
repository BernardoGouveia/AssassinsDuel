import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;

public class GameFrame extends JFrame {
    private final GameState state = new GameState();
    private GamePanel gamePanel;
    private JLabel turnLabel;
    private final JLabel[] hpLabels = new JLabel[2];
    private final JLabel[] apLabels = new JLabel[2];
    private final JLabel[] healLabels = new JLabel[2];
    private JTextArea logArea;
    private final JButton[] actionButtons = new JButton[Action.values().length];

    public GameFrame() {
        setTitle("Duelo dos Assassinos");
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

        // Right: side panel
        add(buildSidePanel(), BorderLayout.EAST);

        // Bottom: action buttons
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
        side.setPreferredSize(new Dimension(280, 0));

        for (int i = 0; i < 2; i++) {
            side.add(buildPlayerCard(i));
            side.add(Box.createVerticalStrut(10));
        }

        JLabel logTitle = new JLabel("Registo:");
        logTitle.setForeground(Color.WHITE);
        logTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        logTitle.setFont(new Font("SansSerif", Font.BOLD, 13));
        side.add(logTitle);
        side.add(Box.createVerticalStrut(4));

        logArea = new JTextArea(14, 22);
        logArea.setEditable(false);
        logArea.setBackground(new Color(15, 17, 22));
        logArea.setForeground(new Color(200, 200, 210));
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 11));
        logArea.setLineWrap(true);
        logArea.setWrapStyleWord(true);
        JScrollPane sp = new JScrollPane(logArea);
        sp.setAlignmentX(Component.LEFT_ALIGNMENT);
        sp.setBorder(BorderFactory.createLineBorder(new Color(60, 60, 70)));
        side.add(sp);

        return side;
    }

    private JPanel buildPlayerCard(int idx) {
        Assassin a = state.players[idx];
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(35, 38, 46));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(a.color, 2),
                new EmptyBorder(6, 8, 6, 8)
        ));
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 110));

        JLabel name = new JLabel(a.name);
        name.setForeground(a.color);
        name.setFont(new Font("SansSerif", Font.BOLD, 14));
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
        l.setFont(new Font("SansSerif", Font.PLAIN, 12));
        return l;
    }

    private JPanel buildActionPanel() {
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 8));
        bottom.setBackground(new Color(25, 27, 34));

        Action[] acts = Action.values();
        for (int i = 0; i < acts.length; i++) {
            Action a = acts[i];
            JButton b = new JButton();
            b.setFocusPainted(false);
            b.setForeground(Color.WHITE);
            b.setFont(new Font("SansSerif", Font.BOLD, 12));
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
        end.setBackground(new Color(140, 60, 60));
        end.setForeground(Color.WHITE);
        end.setFocusPainted(false);
        end.setFont(new Font("SansSerif", Font.BOLD, 12));
        end.addActionListener(e -> {
            state.endTurn();
            refresh();
            gamePanel.repaint();
        });
        bottom.add(end);

        JButton newGame = new JButton("Novo Jogo");
        newGame.setBackground(new Color(60, 100, 60));
        newGame.setForeground(Color.WHITE);
        newGame.setFocusPainted(false);
        newGame.setFont(new Font("SansSerif", Font.BOLD, 12));
        newGame.addActionListener(e -> {
            dispose();
            GameFrame f = new GameFrame();
            f.setVisible(true);
        });
        bottom.add(newGame);

        return bottom;
    }

    private void handleActionResult(String msg) {
        refresh();
    }

    private void refresh() {
        Assassin curr = state.currentPlayer();
        turnLabel.setText("Turno " + state.turnNumber + "  |  Vez de: " + curr.name + "  |  AP: " + curr.ap + "/" + curr.maxAp);
        turnLabel.setForeground(curr.color);

        for (int i = 0; i < 2; i++) {
            Assassin a = state.players[i];
            hpLabels[i].setText("HP: " + a.hp + " / " + a.maxHp);
            apLabels[i].setText("AP: " + a.ap + " / " + a.maxAp);
            healLabels[i].setText("Cura disponível: " + (a.usedHeal ? "não" : "sim"));
        }

        StringBuilder sb = new StringBuilder();
        int start = Math.max(0, state.log.size() - 80);
        for (int i = start; i < state.log.size(); i++) {
            sb.append(state.log.get(i)).append("\n");
        }
        logArea.setText(sb.toString());
        logArea.setCaretPosition(logArea.getDocument().getLength());

        // Update action button labels and selection highlight
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
