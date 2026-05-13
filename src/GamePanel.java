import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

public class GamePanel extends JPanel {
    private final GameState state;
    private final Consumer<String> onAction;
    public static final int CELL = 52;
    public static final int PADDING = 14;
    private static final double MOVE_DURATION_MS = 150.0;

    // Arcade red palette
    private static final Color RED_BRIGHT = new Color(255, 60, 60);
    private static final Color RED_DIM = new Color(140, 30, 35);
    private static final Color RED_GLOW = new Color(255, 30, 30);
    private static final Color GOLD = new Color(255, 200, 80);
    private static final Color STEEL = new Color(220, 220, 230);
    private static final Color GRID_DARK_A = new Color(22, 8, 10);
    private static final Color GRID_DARK_B = new Color(16, 6, 8);
    // Legacy aliases so the rest of the file remains readable.
    private static final Color CYAN = RED_BRIGHT;
    private static final Color MAGENTA = GOLD;

    private long lastTickTime = System.currentTimeMillis();
    private final Random rng = new Random();
    private final List<Particle> particles = new ArrayList<>();
    private int lastHitSeq = 0;
    private long shakeEndTime = 0;
    private double shakeIntensity = 0;
    private long currentFrameTime = System.currentTimeMillis();

    public GamePanel(GameState state, Consumer<String> onAction) {
        this.state = state;
        this.onAction = onAction;
        int w = GameState.WIDTH * CELL + 2 * PADDING;
        int h = GameState.HEIGHT * CELL + 2 * PADDING;
        setPreferredSize(new Dimension(w, h));
        setBackground(new Color(12, 4, 6));

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

        Timer t = new Timer(16, e -> tick());
        t.start();
    }

    private void tick() {
        long now = System.currentTimeMillis();
        currentFrameTime = now;
        long dt = Math.max(1, now - lastTickTime);
        lastTickTime = now;
        boolean dirty = false;

        double speed = dt / MOVE_DURATION_MS;
        for (Assassin a : state.players) {
            double dx = a.x - a.visX;
            double dy = a.y - a.visY;
            double dist = Math.hypot(dx, dy);
            if (dist < 0.001) continue;
            if (dist <= speed) {
                a.visX = a.x;
                a.visY = a.y;
            } else {
                a.visX += dx / dist * speed;
                a.visY += dy / dist * speed;
            }
            dirty = true;
        }

        if (state.flashEndTime > 0 && now > state.flashEndTime) {
            state.flashX = -1;
            state.flashY = -1;
            state.flashEndTime = 0;
            dirty = true;
        }

        if (state.hitSeq != lastHitSeq) {
            lastHitSeq = state.hitSeq;
            spawnImpactParticles(state.hitX, state.hitY, state.hitAction);
            shakeIntensity = state.hitAction == Action.SHURIKEN ? 4.5 : 6.5;
            shakeEndTime = now + 220;
            dirty = true;
        }

        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            double pdt = dt / 1000.0;
            p.vy += 380 * pdt;
            p.x += p.vx * pdt;
            p.y += p.vy * pdt;
            p.life -= pdt;
            if (p.life <= 0) it.remove();
            else dirty = true;
        }

        if (now < state.shurikenEndTime || now < shakeEndTime) dirty = true;

        if (dirty) repaint();
    }

    private void spawnImpactParticles(int gx, int gy, Action act) {
        int cx = PADDING + gx * CELL + CELL / 2;
        int cy = PADDING + gy * CELL + CELL / 2;
        int count = act == Action.SHURIKEN ? 18 : 24;
        Color base = act == Action.SHURIKEN ? CYAN : new Color(255, 230, 100);
        for (int i = 0; i < count; i++) {
            Particle p = new Particle();
            double ang = rng.nextDouble() * Math.PI * 2;
            double sp = 120 + rng.nextDouble() * 220;
            p.x = cx;
            p.y = cy;
            p.vx = Math.cos(ang) * sp;
            p.vy = Math.sin(ang) * sp - 60;
            p.life = 0.45 + rng.nextDouble() * 0.35;
            p.maxLife = p.life;
            p.size = 2 + rng.nextInt(3);
            p.color = i % 4 == 0 ? Color.WHITE
                    : i % 4 == 1 ? MAGENTA
                    : base;
            particles.add(p);
        }
    }

    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0.create();
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        double shakeAmt = 0;
        if (currentFrameTime < shakeEndTime) {
            double t = (shakeEndTime - currentFrameTime) / 220.0;
            shakeAmt = shakeIntensity * t;
        }
        int sx = (int) ((rng.nextDouble() - 0.5) * 2 * shakeAmt);
        int sy = (int) ((rng.nextDouble() - 0.5) * 2 * shakeAmt);
        g.translate(sx, sy);

        int W = getWidth(), H = getHeight();
        GradientPaint bg = new GradientPaint(0, 0, new Color(20, 6, 8), 0, H, new Color(6, 2, 3));
        g.setPaint(bg);
        g.fillRect(0, 0, W, H);

        g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 14));
        for (int x = 0; x < W; x += 24) g.drawLine(x, 0, x, H);
        for (int y = 0; y < H; y += 24) g.drawLine(0, y, W, y);

        int bx = PADDING - 4;
        int by = PADDING - 4;
        int bw = GameState.WIDTH * CELL + 8;
        int bh = GameState.HEIGHT * CELL + 8;
        g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 140));
        g.setStroke(new BasicStroke(2f));
        g.drawRect(bx, by, bw, bh);
        g.setColor(MAGENTA);
        int cm = 14;
        g.drawLine(bx, by, bx + cm, by);
        g.drawLine(bx, by, bx, by + cm);
        g.drawLine(bx + bw, by, bx + bw - cm, by);
        g.drawLine(bx + bw, by, bx + bw, by + cm);
        g.drawLine(bx, by + bh, bx + cm, by + bh);
        g.drawLine(bx, by + bh, bx, by + bh - cm);
        g.drawLine(bx + bw, by + bh, bx + bw - cm, by + bh);
        g.drawLine(bx + bw, by + bh, bx + bw, by + bh - cm);

        for (int x = 0; x < GameState.WIDTH; x++) {
            for (int y = 0; y < GameState.HEIGHT; y++) {
                int px = PADDING + x * CELL;
                int py = PADDING + y * CELL;
                if (state.grid[x][y] == Tile.WALL) {
                    drawStoneWall(g, px, py);
                } else {
                    Color base = (x + y) % 2 == 0 ? GRID_DARK_A : GRID_DARK_B;
                    g.setColor(base);
                    g.fillRect(px, py, CELL, CELL);
                    g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 28));
                    g.drawRect(px, py, CELL, CELL);
                }
            }
        }

        long now = currentFrameTime;
        double bob = Math.sin(now * 0.005) * 2;
        for (int x = 0; x < GameState.WIDTH; x++) {
            for (int y = 0; y < GameState.HEIGHT; y++) {
                Powerup p = state.powerups[x][y];
                if (p == null) continue;
                int px = PADDING + x * CELL;
                int py = PADDING + y * CELL + (int) bob;
                if (p == Powerup.HEAL) drawPowerup(g, px, py, new Color(60, 220, 100), "+");
                else drawPowerup(g, px, py, new Color(255, 170, 50), "!");
            }
        }

        Assassin actor = state.currentPlayer();
        if (state.winnerIdx < 0) {
            if (state.selectedAction == Action.SHURIKEN) {
                for (int x = 0; x < GameState.WIDTH; x++) {
                    for (int y = 0; y < GameState.HEIGHT; y++) {
                        int dist = Math.max(Math.abs(actor.x - x), Math.abs(actor.y - y));
                        if (dist <= GameState.SHURIKEN_RANGE && dist > 0
                                && !state.isWall(x, y)
                                && state.hasLineOfSight(actor.x, actor.y, x, y)) {
                            int px = PADDING + x * CELL;
                            int py = PADDING + y * CELL;
                            g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 26));
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
                        double pulse = 0.5 + 0.5 * Math.sin(now * 0.008);
                        int a = (int) (80 + 70 * pulse);
                        g.setColor(new Color(80, 255, 140, a));
                        g.fillRect(px, py, CELL, CELL);
                        g.setColor(new Color(150, 255, 180, 230));
                        Stroke prev = g.getStroke();
                        g.setStroke(new BasicStroke(2f));
                        g.drawRect(px + 2, py + 2, CELL - 4, CELL - 4);
                        g.setStroke(prev);
                    }
                }
            }
        }

        if (state.flashX >= 0 && state.flashEndTime > now) {
            int px = PADDING + state.flashX * CELL;
            int py = PADDING + state.flashY * CELL;
            float t = (state.flashEndTime - now) / 400f;
            g.setColor(new Color(255, 80, 80, (int) (200 * t)));
            g.fillRect(px, py, CELL, CELL);
        }

        for (Assassin a : state.players) {
            if (!a.isAlive()) continue;
            int px = PADDING + (int) Math.round(a.visX * CELL);
            int py = PADDING + (int) Math.round(a.visY * CELL);

            if (a == state.currentPlayer() && state.winnerIdx < 0) {
                double pulse = 0.6 + 0.4 * Math.sin(now * 0.006);
                int ringA = (int) (110 * pulse);
                g.setColor(new Color(a.color.getRed(), a.color.getGreen(), a.color.getBlue(), ringA));
                g.fillOval(px + 1, py + 1, CELL - 2, CELL - 2);
            }

            drawNinja(g, px, py, a);

            if (a.damageBoost > 0) {
                Stroke prev = g.getStroke();
                g.setStroke(new BasicStroke(3f));
                double dpulse = 0.7 + 0.3 * Math.sin(now * 0.012);
                g.setColor(new Color(255, 200, 70, (int) (255 * dpulse)));
                g.drawOval(px + 4, py + 4, CELL - 8, CELL - 8);
                g.setStroke(prev);
            }

            int barW = CELL - 8;
            int barH = 5;
            int bxh = px + 4;
            int byh = py - 1;
            g.setColor(new Color(0, 0, 0, 180));
            g.fillRect(bxh - 1, byh - 1, barW + 2, barH + 2);
            g.setColor(new Color(30, 36, 50));
            g.fillRect(bxh, byh, barW, barH);
            int hpW = (int) (barW * (a.hp / (double) a.maxHp));
            Color hpc = a.hp > a.maxHp * 0.5 ? new Color(80, 255, 140)
                    : a.hp > a.maxHp * 0.25 ? new Color(255, 220, 80)
                    : new Color(255, 80, 110);
            g.setColor(hpc);
            g.fillRect(bxh, byh, hpW, barH);
        }

        if (now < state.shurikenEndTime) {
            double t = (now - state.shurikenStartTime) / (double) (state.shurikenEndTime - state.shurikenStartTime);
            t = Math.max(0, Math.min(1, t));
            double cxF = state.shurikenFromX + (state.shurikenToX - state.shurikenFromX) * t;
            double cyF = state.shurikenFromY + (state.shurikenToY - state.shurikenFromY) * t;
            int xx = PADDING + (int) (cxF * CELL) + CELL / 2;
            int yy = PADDING + (int) (cyF * CELL) + CELL / 2;
            for (int i = 1; i <= 5; i++) {
                double tt = Math.max(0, t - i * 0.06);
                double tcxF = state.shurikenFromX + (state.shurikenToX - state.shurikenFromX) * tt;
                double tcyF = state.shurikenFromY + (state.shurikenToY - state.shurikenFromY) * tt;
                int tx = PADDING + (int) (tcxF * CELL) + CELL / 2;
                int ty = PADDING + (int) (tcyF * CELL) + CELL / 2;
                int alpha = 120 - i * 22;
                g.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), Math.max(0, alpha)));
                g.fillOval(tx - 3, ty - 3, 6, 6);
            }
            drawShurikenProjectile(g, xx, yy, now);
        }

        for (Particle p : particles) {
            float a = Math.max(0, Math.min(1, (float) (p.life / p.maxLife)));
            g.setColor(new Color(
                    p.color.getRed(), p.color.getGreen(), p.color.getBlue(),
                    (int) (255 * a)));
            g.fillOval((int) p.x - p.size, (int) p.y - p.size, p.size * 2, p.size * 2);
        }

        if (state.winnerIdx >= 0) {
            g.setColor(new Color(0, 0, 0, 200));
            g.fillRect(0, 0, getWidth(), getHeight());
            Color wc = state.players[state.winnerIdx].color;
            String msg = state.players[state.winnerIdx].name.toUpperCase() + " VENCE!";
            g.setFont(new Font("Monospaced", Font.BOLD, 38));
            FontMetrics fm = g.getFontMetrics();
            int tw = fm.stringWidth(msg);
            int tx = getWidth() / 2 - tw / 2;
            int ty = getHeight() / 2;
            for (int i = 6; i >= 1; i--) {
                g.setColor(new Color(wc.getRed(), wc.getGreen(), wc.getBlue(), 28));
                g.drawString(msg, tx - i, ty);
                g.drawString(msg, tx + i, ty);
            }
            g.setColor(wc);
            g.drawString(msg, tx, ty);
            g.setColor(new Color(220, 240, 255));
            g.setFont(new Font("Monospaced", Font.PLAIN, 14));
            String hint = "[ Carrega em \"Novo Jogo\" para outra ronda ]";
            int hw = g.getFontMetrics().stringWidth(hint);
            g.drawString(hint, getWidth() / 2 - hw / 2, getHeight() / 2 + 32);
        }

        g.dispose();
    }

    private void drawStoneWall(Graphics2D g, int px, int py) {
        // Warm stone gradient (dark grey-brown) that fits the red arcade theme.
        GradientPaint gp = new GradientPaint(px, py, new Color(80, 64, 64),
                px, py + CELL, new Color(40, 30, 32));
        g.setPaint(gp);
        g.fillRect(px, py, CELL, CELL);

        // Mortar lines
        g.setColor(new Color(20, 14, 16, 220));
        int half = CELL / 2;
        g.drawLine(px, py + half, px + CELL, py + half);
        g.drawLine(px + half, py, px + half, py + half);
        g.drawLine(px + half / 2, py + half, px + half / 2, py + CELL);
        g.drawLine(px + half + half / 2, py + half, px + half + half / 2, py + CELL);

        // Top/left highlight, bottom/right shadow
        g.setColor(new Color(160, 130, 130, 180));
        g.drawLine(px, py, px + CELL - 1, py);
        g.drawLine(px, py, px, py + CELL - 1);
        g.setColor(new Color(0, 0, 0, 180));
        g.drawLine(px, py + CELL - 1, px + CELL - 1, py + CELL - 1);
        g.drawLine(px + CELL - 1, py, px + CELL - 1, py + CELL - 1);

        // Subtle red rim
        g.setColor(new Color(RED_GLOW.getRed(), RED_GLOW.getGreen(), RED_GLOW.getBlue(), 70));
        g.drawRect(px, py, CELL - 1, CELL - 1);
    }

    private void drawPowerup(Graphics2D g, int px, int py, Color base, String glyph) {
        g.setColor(new Color(base.getRed(), base.getGreen(), base.getBlue(), 70));
        g.fillRoundRect(px + 4, py + 4, CELL - 8, CELL - 8, 14, 14);
        GradientPaint gp = new GradientPaint(px, py, base.brighter(), px, py + CELL, base.darker());
        g.setPaint(gp);
        g.fillRoundRect(px + 9, py + 9, CELL - 18, CELL - 18, 10, 10);
        g.setColor(new Color(255, 255, 255, 160));
        g.drawRoundRect(px + 9, py + 9, CELL - 18, CELL - 18, 10, 10);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 22));
        FontMetrics fm = g.getFontMetrics();
        int tw = fm.stringWidth(glyph);
        g.drawString(glyph, px + CELL / 2 - tw / 2, py + CELL / 2 + fm.getAscent() / 2 - 3);
    }

    private void drawNinja(Graphics2D g, int px, int py, Assassin a) {
        int cx = px + CELL / 2;

        g.setColor(new Color(a.color.getRed(), a.color.getGreen(), a.color.getBlue(), 70));
        g.fillOval(px + 4, py + 6, CELL - 8, CELL - 8);

        Polygon hood = new Polygon();
        int top = py + 8;
        int bottom = py + CELL - 4;
        int left = px + 8;
        int right = px + CELL - 8;
        hood.addPoint(cx, top);
        hood.addPoint(right, py + CELL / 2);
        hood.addPoint(right - 2, bottom);
        hood.addPoint(left + 2, bottom);
        hood.addPoint(left, py + CELL / 2);
        GradientPaint gp = new GradientPaint(cx, top, a.color.brighter(),
                cx, bottom, a.color.darker());
        g.setPaint(gp);
        g.fillPolygon(hood);

        g.setColor(new Color(0, 0, 0, 200));
        g.setStroke(new BasicStroke(1.4f));
        g.drawPolygon(hood);

        int eyeY = py + CELL / 2 - 3;
        g.setColor(new Color(0, 0, 0, 220));
        g.fillRect(left + 4, eyeY, right - left - 8, 6);
        g.setColor(GOLD);
        g.fillRect(left + 6, eyeY + 1, right - left - 12, 3);
        g.setColor(new Color(255, 240, 200, 220));
        g.drawLine(left + 6, eyeY + 1, right - 7, eyeY + 1);

        g.setColor(new Color(0, 0, 0, 200));
        g.fillRoundRect(px + 4, py + CELL - 14, 14, 12, 4, 4);
        g.setColor(a.color);
        g.setFont(new Font("Monospaced", Font.BOLD, 11));
        FontMetrics fm = g.getFontMetrics();
        String n = String.valueOf(a.displayNumber);
        int tw = fm.stringWidth(n);
        g.drawString(n, px + 4 + 7 - tw / 2, py + CELL - 14 + fm.getAscent() - 1);
    }

    private void drawShurikenProjectile(Graphics2D g, int x, int y, long now) {
        Graphics2D g2 = (Graphics2D) g.create();
        g2.translate(x, y);
        g2.rotate(now * 0.03);
        int r = 11;
        g2.setColor(new Color(CYAN.getRed(), CYAN.getGreen(), CYAN.getBlue(), 90));
        g2.fillOval(-r - 4, -r - 4, (r + 4) * 2, (r + 4) * 2);
        int[] xs = {0, r / 3, r, r / 3, 0, -r / 3, -r, -r / 3};
        int[] ys = {-r, -r / 3, 0, r / 3, r, r / 3, 0, -r / 3};
        g2.setColor(new Color(220, 250, 255));
        g2.fillPolygon(xs, ys, 8);
        g2.setColor(CYAN);
        g2.setStroke(new BasicStroke(1.6f));
        g2.drawPolygon(xs, ys, 8);
        g2.setColor(MAGENTA);
        g2.fillOval(-2, -2, 4, 4);
        g2.dispose();
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

    private static class Particle {
        double x, y, vx, vy, life, maxLife;
        int size;
        Color color;
    }
}
