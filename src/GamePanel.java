import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

public class GamePanel extends JPanel {
    private final GameState state;
    private final Consumer<String> onAction;
    public static final int CELL = 48;
    public static final int PADDING = 12;

    public GamePanel(GameState state, Consumer<String> onAction) {
        this.state = state;
        this.onAction = onAction;
        int w = GameState.WIDTH * CELL + 2 * PADDING;
        int h = GameState.HEIGHT * CELL + 2 * PADDING;
        setPreferredSize(new Dimension(w, h));
        setBackground(new Color(20, 22, 28));

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int gx = (e.getX() - PADDING) / CELL;
                int gy = (e.getY() - PADDING) / CELL;
                if (state.isInBounds(gx, gy)) {
                    String msg = state.performAction(gx, gy);
                    if (msg != null) onAction.accept(msg);
                    repaint();
                }
            }
        });

        // Animation timer to clear flash effect
        Timer t = new Timer(50, e -> {
            if (state.flashEndTime > 0 && System.currentTimeMillis() > state.flashEndTime) {
                state.flashX = -1;
                state.flashY = -1;
                state.flashEndTime = 0;
                repaint();
            }
        });
        t.start();
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw cells
        for (int x = 0; x < GameState.WIDTH; x++) {
            for (int y = 0; y < GameState.HEIGHT; y++) {
                int px = PADDING + x * CELL;
                int py = PADDING + y * CELL;
                Color base = (x + y) % 2 == 0 ? new Color(45, 48, 58) : new Color(38, 41, 50);
                if (state.grid[x][y] == Tile.WALL) {
                    base = new Color(85, 80, 95);
                }
                g.setColor(base);
                g.fillRect(px, py, CELL, CELL);
                g.setColor(new Color(0, 0, 0, 60));
                g.drawRect(px, py, CELL, CELL);

                if (state.grid[x][y] == Tile.WALL) {
                    g.setColor(new Color(55, 50, 65));
                    g.fillRect(px + 5, py + 5, CELL - 10, CELL - 10);
                    g.setColor(new Color(130, 120, 140));
                    g.drawRect(px + 5, py + 5, CELL - 10, CELL - 10);
                }
            }
        }

        // Highlight valid action targets
        Assassin actor = state.currentPlayer();
        if (state.winnerIdx < 0) {
            // For shuriken, also draw a faint range overlay
            if (state.selectedAction == Action.SHURIKEN) {
                for (int x = 0; x < GameState.WIDTH; x++) {
                    for (int y = 0; y < GameState.HEIGHT; y++) {
                        int dist = Math.max(Math.abs(actor.x - x), Math.abs(actor.y - y));
                        if (dist <= GameState.SHURIKEN_RANGE && dist > 0
                                && !state.isWall(x, y)
                                && state.hasLineOfSight(actor.x, actor.y, x, y)) {
                            int px = PADDING + x * CELL;
                            int py = PADDING + y * CELL;
                            g.setColor(new Color(255, 220, 120, 30));
                            g.fillRect(px, py, CELL, CELL);
                        }
                    }
                }
            }
            for (int x = 0; x < GameState.WIDTH; x++) {
                for (int y = 0; y < GameState.HEIGHT; y++) {
                    if (isValidTarget(actor, x, y)) {
                        int px = PADDING + x * CELL;
                        int py = PADDING + y * CELL;
                        g.setColor(new Color(120, 220, 120, 90));
                        g.fillRect(px, py, CELL, CELL);
                        g.setColor(new Color(150, 240, 150, 230));
                        Stroke prev = g.getStroke();
                        g.setStroke(new BasicStroke(2f));
                        g.drawRect(px + 2, py + 2, CELL - 4, CELL - 4);
                        g.setStroke(prev);
                    }
                }
            }
        }

        // Draw flash on hit
        if (state.flashX >= 0 && state.flashEndTime > System.currentTimeMillis()) {
            int px = PADDING + state.flashX * CELL;
            int py = PADDING + state.flashY * CELL;
            g.setColor(new Color(255, 80, 80, 180));
            g.fillRect(px, py, CELL, CELL);
        }

        // Draw players
        for (Assassin a : state.players) {
            if (!a.isAlive()) continue;
            int px = PADDING + a.x * CELL;
            int py = PADDING + a.y * CELL;
            int r = CELL - 16;

            // Glow if current player
            if (a == state.currentPlayer() && state.winnerIdx < 0) {
                g.setColor(new Color(255, 255, 200, 110));
                g.fillOval(px + 3, py + 3, CELL - 6, CELL - 6);
            }

            g.setColor(a.color);
            g.fillOval(px + 8, py + 8, r, r);
            g.setColor(Color.BLACK);
            g.drawOval(px + 8, py + 8, r, r);
            g.setColor(Color.WHITE);
            g.setFont(new Font("SansSerif", Font.BOLD, 18));
            FontMetrics fm = g.getFontMetrics();
            String letter = a.name.substring(a.name.length() - 1).toUpperCase();
            int tw = fm.stringWidth(letter);
            int th = fm.getAscent();
            g.drawString(letter, px + CELL / 2 - tw / 2, py + CELL / 2 + th / 3);

            // HP bar above
            int barW = CELL - 10;
            int barH = 4;
            int bx = px + 5;
            int by = py + 2;
            g.setColor(new Color(50, 50, 50));
            g.fillRect(bx, by, barW, barH);
            int hpW = (int) (barW * (a.hp / (double) a.maxHp));
            Color hpc = a.hp > 50 ? new Color(80, 200, 80)
                    : a.hp > 25 ? new Color(220, 200, 80)
                    : new Color(220, 80, 80);
            g.setColor(hpc);
            g.fillRect(bx, by, hpW, barH);
        }

        // Game over overlay
        if (state.winnerIdx >= 0) {
            g.setColor(new Color(0, 0, 0, 170));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.setColor(state.players[state.winnerIdx].color);
            g.setFont(new Font("SansSerif", Font.BOLD, 36));
            String msg = state.players[state.winnerIdx].name + " VENCE!";
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(msg);
            g.drawString(msg, getWidth() / 2 - tw / 2, getHeight() / 2);
            g.setColor(new Color(220, 220, 220));
            g.setFont(new Font("SansSerif", Font.PLAIN, 14));
            String hint = "Clica em \"Novo Jogo\" para outra ronda.";
            int hw = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, getWidth() / 2 - hw / 2, getHeight() / 2 + 30);
        }
    }

    private boolean isValidTarget(Assassin actor, int x, int y) {
        switch (state.selectedAction) {
            case MOVE: {
                int dist = Math.abs(actor.x - x) + Math.abs(actor.y - y);
                return dist == 1 && state.canMoveTo(x, y) && actor.ap >= Action.MOVE.cost;
            }
            case MELEE: {
                Assassin t = state.assassinAt(x, y);
                if (t == null || t == actor) return false;
                int dist = Math.abs(actor.x - x) + Math.abs(actor.y - y);
                return dist == 1 && actor.ap >= Action.MELEE.cost;
            }
            case SHURIKEN: {
                Assassin t = state.assassinAt(x, y);
                if (t == null || t == actor) return false;
                int dist = Math.max(Math.abs(actor.x - x), Math.abs(actor.y - y));
                return dist <= GameState.SHURIKEN_RANGE
                        && state.hasLineOfSight(actor.x, actor.y, x, y)
                        && actor.ap >= Action.SHURIKEN.cost;
            }
            case HEAL:
            default:
                return false;
        }
    }
}
