import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameState {
    public static final int WIDTH = 12;
    public static final int HEIGHT = 12;
    public static final int SHURIKEN_RANGE = 5;
    public static final int MELEE_DAMAGE = 30;
    public static final int SHURIKEN_DAMAGE = 20;
    public static final int HEAL_AMOUNT = 30;

    public final Tile[][] grid = new Tile[WIDTH][HEIGHT];
    public final Assassin[] players = new Assassin[2];
    public int currentPlayerIdx = 0;
    public Action selectedAction = Action.MOVE;
    public final List<String> log = new ArrayList<>();
    public int winnerIdx = -1;
    public int turnNumber = 1;

    // Visual flash effect after attacks
    public int flashX = -1, flashY = -1;
    public long flashEndTime = 0;

    public GameState() {
        Random r = new Random();
        for (int x = 0; x < WIDTH; x++)
            for (int y = 0; y < HEIGHT; y++)
                grid[x][y] = Tile.EMPTY;

        players[0] = new Assassin("Sombra Vermelha", new Color(220, 60, 60), 1, 1);
        players[1] = new Assassin("Sombra Azul", new Color(60, 130, 220), WIDTH - 2, HEIGHT - 2);

        // Random walls (keep starting areas clear)
        int wallsToPlace = 18;
        int placed = 0;
        int attempts = 0;
        while (placed < wallsToPlace && attempts < 300) {
            attempts++;
            int x = r.nextInt(WIDTH);
            int y = r.nextInt(HEIGHT);
            if ((x <= 2 && y <= 2) || (x >= WIDTH - 3 && y >= HEIGHT - 3)) continue;
            if (grid[x][y] == Tile.WALL) continue;
            grid[x][y] = Tile.WALL;
            placed++;
        }

        log.add("=== Duelo de Assassinos ===");
        log.add("Turno " + turnNumber + ": " + currentPlayer().name + " começa.");
    }

    public Assassin currentPlayer() {
        return players[currentPlayerIdx];
    }

    public Assassin opponent() {
        return players[1 - currentPlayerIdx];
    }

    public boolean isInBounds(int x, int y) {
        return x >= 0 && x < WIDTH && y >= 0 && y < HEIGHT;
    }

    public boolean isWall(int x, int y) {
        return isInBounds(x, y) && grid[x][y] == Tile.WALL;
    }

    public Assassin assassinAt(int x, int y) {
        for (Assassin a : players) {
            if (a.isAlive() && a.x == x && a.y == y) return a;
        }
        return null;
    }

    public boolean canMoveTo(int x, int y) {
        if (!isInBounds(x, y)) return false;
        if (isWall(x, y)) return false;
        if (assassinAt(x, y) != null) return false;
        return true;
    }

    /** Tries to perform the selected action on the target tile. Returns a status message. */
    public String performAction(int targetX, int targetY) {
        if (winnerIdx >= 0) return "Jogo terminado.";
        Assassin actor = currentPlayer();
        Action a = selectedAction;

        switch (a) {
            case MOVE: {
                int dx = Math.abs(targetX - actor.x);
                int dy = Math.abs(targetY - actor.y);
                if (dx + dy != 1) return "Move-te apenas para casas adjacentes.";
                if (!canMoveTo(targetX, targetY)) return "Casa bloqueada.";
                if (actor.ap < Action.MOVE.cost) return "Sem AP suficiente.";
                actor.ap -= Action.MOVE.cost;
                actor.x = targetX;
                actor.y = targetY;
                String msg = actor.name + " moveu-se para (" + targetX + "," + targetY + ").";
                log.add(msg);
                return msg;
            }
            case MELEE: {
                Assassin target = assassinAt(targetX, targetY);
                if (target == null || target == actor) return "Selecciona o oponente.";
                int dist = Math.abs(targetX - actor.x) + Math.abs(targetY - actor.y);
                if (dist != 1) return "Apenas alvos adjacentes.";
                if (actor.ap < Action.MELEE.cost) return "Sem AP suficiente.";
                actor.ap -= Action.MELEE.cost;
                target.damage(MELEE_DAMAGE);
                triggerFlash(targetX, targetY);
                String msg = actor.name + " desferiu um golpe em " + target.name + " (-" + MELEE_DAMAGE + " HP)";
                log.add(msg);
                checkWinner();
                return msg;
            }
            case SHURIKEN: {
                Assassin target = assassinAt(targetX, targetY);
                if (target == null || target == actor) return "Selecciona o oponente.";
                int dist = Math.max(Math.abs(targetX - actor.x), Math.abs(targetY - actor.y));
                if (dist > SHURIKEN_RANGE) return "Fora de alcance da shuriken.";
                if (!hasLineOfSight(actor.x, actor.y, targetX, targetY)) return "Sem linha de visão.";
                if (actor.ap < Action.SHURIKEN.cost) return "Sem AP suficiente.";
                actor.ap -= Action.SHURIKEN.cost;
                target.damage(SHURIKEN_DAMAGE);
                triggerFlash(targetX, targetY);
                String msg = actor.name + " atirou shuriken em " + target.name + " (-" + SHURIKEN_DAMAGE + " HP)";
                log.add(msg);
                checkWinner();
                return msg;
            }
            case HEAL: {
                if (actor.usedHeal) return "Já usaste a tua cura.";
                if (actor.ap < Action.HEAL.cost) return "Sem AP suficiente.";
                actor.ap -= Action.HEAL.cost;
                actor.usedHeal = true;
                actor.heal(HEAL_AMOUNT);
                String msg = actor.name + " curou-se (+" + HEAL_AMOUNT + " HP)";
                log.add(msg);
                return msg;
            }
        }
        return null;
    }

    private void triggerFlash(int x, int y) {
        flashX = x;
        flashY = y;
        flashEndTime = System.currentTimeMillis() + 400;
    }

    private void checkWinner() {
        for (int i = 0; i < players.length; i++) {
            if (!players[i].isAlive()) {
                winnerIdx = 1 - i;
                log.add("=== " + players[winnerIdx].name + " VENCEU! ===");
                return;
            }
        }
    }

    public void endTurn() {
        if (winnerIdx >= 0) return;
        currentPlayerIdx = 1 - currentPlayerIdx;
        currentPlayer().resetTurn();
        if (currentPlayerIdx == 0) turnNumber++;
        selectedAction = Action.MOVE;
        log.add("--- Turno " + turnNumber + ": " + currentPlayer().name + " ---");
    }

    /** Bresenham line of sight: checks tiles between (excluding start, including end). Walls block. */
    public boolean hasLineOfSight(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int cx = x0, cy = y0;
        while (true) {
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; cx += sx; }
            if (e2 < dx) { err += dx; cy += sy; }
            if (cx == x1 && cy == y1) return true;
            if (isWall(cx, cy)) return false;
        }
    }
}
