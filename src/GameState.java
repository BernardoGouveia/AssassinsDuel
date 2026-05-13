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
    public static final int POWERUP_HEAL_AMOUNT = 25;
    public static final int POWERUP_DAMAGE_BONUS = 15;

    public final Tile[][] grid = new Tile[WIDTH][HEIGHT];
    public final Powerup[][] powerups = new Powerup[WIDTH][HEIGHT];
    public final Assassin[] players;
    public int currentPlayerIdx = 0;
    public Action selectedAction = Action.MOVE;
    public final List<String> log = new ArrayList<>();
    public int winnerIdx = -1;
    public int turnNumber = 1;
    public boolean paused = false;

    private final Random rng = new Random();

    // Visual flash effect after attacks
    public int flashX = -1, flashY = -1;
    public long flashEndTime = 0;

    // Shuriken projectile animation (cell-space). Active while now < shurikenEndTime.
    public double shurikenFromX, shurikenFromY, shurikenToX, shurikenToY;
    public long shurikenStartTime, shurikenEndTime;
    // Monotonic counter incremented on each attack — lets the view detect new hits.
    public int hitSeq = 0;
    public int hitX = -1, hitY = -1;
    public Action hitAction = null;

    private static final Color[] PLAYER_COLORS = {
            new Color(220, 60, 60),
            new Color(60, 130, 220),
            new Color(80, 200, 100),
            new Color(200, 130, 230)
    };
    private static final String[] PLAYER_NAMES = {
            "Sombra Vermelha",
            "Sombra Azul",
            "Sombra Verde",
            "Sombra Roxa"
    };

    public GameState(int numPlayers) {
        if (numPlayers < 2) numPlayers = 2;
        if (numPlayers > 4) numPlayers = 4;
        players = new Assassin[numPlayers];

        int[][] starts = {
                {1, 1},
                {WIDTH - 2, HEIGHT - 2},
                {WIDTH - 2, 1},
                {1, HEIGHT - 2}
        };
        for (int i = 0; i < numPlayers; i++) {
            players[i] = new Assassin(i + 1, PLAYER_NAMES[i], PLAYER_COLORS[i], starts[i][0], starts[i][1]);
        }

        for (int x = 0; x < WIDTH; x++)
            for (int y = 0; y < HEIGHT; y++)
                grid[x][y] = Tile.EMPTY;

        // Random walls (avoid all player starting areas)
        int wallsToPlace = 18;
        int placed = 0;
        int attempts = 0;
        while (placed < wallsToPlace && attempts < 400) {
            attempts++;
            int x = rng.nextInt(WIDTH);
            int y = rng.nextInt(HEIGHT);
            if (isInStartingArea(x, y)) continue;
            if (grid[x][y] == Tile.WALL) continue;
            grid[x][y] = Tile.WALL;
            placed++;
        }

        // Initial powerups (one extra per additional player)
        int initialPowerups = 3 + numPlayers; // 5..7
        for (int i = 0; i < initialPowerups; i++) spawnRandomPowerup();

        log.add("=== Duelo de Assassinos (" + numPlayers + " jogadores) ===");
        log.add("Turno " + turnNumber + ": " + currentPlayer().name);
    }

    private boolean isInStartingArea(int x, int y) {
        for (Assassin a : players) {
            if (Math.abs(x - a.x) <= 1 && Math.abs(y - a.y) <= 1) return true;
        }
        return false;
    }

    public Assassin currentPlayer() {
        return players[currentPlayerIdx];
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
        if (paused) return "Jogo pausado.";
        Assassin actor = currentPlayer();
        Action a = selectedAction;
        int apBefore = actor.ap;
        String result;

        switch (a) {
            case MOVE: {
                int dx = Math.abs(targetX - actor.x);
                int dy = Math.abs(targetY - actor.y);
                if (dx + dy != 1) { result = "Move-te apenas para casas adjacentes."; break; }
                if (!canMoveTo(targetX, targetY)) { result = "Casa bloqueada."; break; }
                if (actor.ap < Action.MOVE.cost) { result = "Sem AP suficiente."; break; }
                actor.ap -= Action.MOVE.cost;
                actor.x = targetX;
                actor.y = targetY;
                String moveMsg = actor.name + " moveu-se para (" + targetX + "," + targetY + ").";
                log.add(moveMsg);
                SoundFx.move();
                Powerup p = powerups[targetX][targetY];
                if (p != null) {
                    applyPowerup(actor, p);
                    powerups[targetX][targetY] = null;
                    SoundFx.powerup();
                }
                result = moveMsg;
                break;
            }
            case MELEE: {
                Assassin target = assassinAt(targetX, targetY);
                if (target == null || target == actor) { result = "Selecciona um adversário."; break; }
                int dist = Math.abs(targetX - actor.x) + Math.abs(targetY - actor.y);
                if (dist != 1) { result = "Apenas alvos adjacentes."; break; }
                if (actor.ap < Action.MELEE.cost) { result = "Sem AP suficiente."; break; }
                actor.ap -= Action.MELEE.cost;
                int dmg = MELEE_DAMAGE + actor.damageBoost;
                boolean hadBoost = actor.damageBoost > 0;
                actor.damageBoost = 0;
                int hpBefore = target.hp;
                target.damage(dmg);
                actor.dmgDealt += (hpBefore - target.hp);
                triggerFlash(targetX, targetY);
                triggerHit(targetX, targetY, Action.MELEE);
                SoundFx.melee();
                SoundFx.damage();
                String msg = actor.name + " atacou " + target.name + " (-" + dmg + " HP)" + (hadBoost ? " [BÓNUS]" : "");
                log.add(msg);
                if (!target.isAlive()) {
                    log.add(target.name + " caiu!");
                    actor.kills++;
                    SoundFx.death();
                }
                checkWinner();
                result = msg;
                break;
            }
            case SHURIKEN: {
                Assassin target = assassinAt(targetX, targetY);
                if (target == null || target == actor) { result = "Selecciona um adversário."; break; }
                int dist = Math.max(Math.abs(targetX - actor.x), Math.abs(targetY - actor.y));
                if (dist > SHURIKEN_RANGE) { result = "Fora de alcance."; break; }
                if (!hasLineOfSight(actor.x, actor.y, targetX, targetY)) { result = "Sem linha de visão."; break; }
                if (actor.ap < Action.SHURIKEN.cost) { result = "Sem AP suficiente."; break; }
                actor.ap -= Action.SHURIKEN.cost;
                int dmg = SHURIKEN_DAMAGE + actor.damageBoost;
                boolean hadBoost = actor.damageBoost > 0;
                actor.damageBoost = 0;
                int hpBefore2 = target.hp;
                target.damage(dmg);
                actor.dmgDealt += (hpBefore2 - target.hp);
                triggerFlash(targetX, targetY);
                triggerHit(targetX, targetY, Action.SHURIKEN);
                triggerShuriken(actor.x, actor.y, targetX, targetY);
                SoundFx.shuriken();
                // Damage hit lands when the projectile arrives, ~60ms per cell.
                int shDist = Math.max(Math.abs(targetX - actor.x), Math.abs(targetY - actor.y));
                javax.swing.Timer dt = new javax.swing.Timer(60 * Math.max(1, shDist), ev -> SoundFx.damage());
                dt.setRepeats(false);
                dt.start();
                String msg = actor.name + " atirou shuriken em " + target.name + " (-" + dmg + " HP)" + (hadBoost ? " [BÓNUS]" : "");
                log.add(msg);
                if (!target.isAlive()) {
                    log.add(target.name + " caiu!");
                    SoundFx.death();
                }
                checkWinner();
                result = msg;
                break;
            }
            case HEAL: {
                if (actor.usedHeal) { result = "Já usaste a tua cura."; break; }
                if (actor.ap < Action.HEAL.cost) { result = "Sem AP suficiente."; break; }
                actor.ap -= Action.HEAL.cost;
                actor.usedHeal = true;
                actor.heal(HEAL_AMOUNT);
                SoundFx.heal();
                String msg = actor.name + " curou-se (+" + HEAL_AMOUNT + " HP)";
                log.add(msg);
                result = msg;
                break;
            }
            default:
                result = null;
        }

        // Auto-end turn if AP exhausted (only when an action actually consumed AP)
        if (winnerIdx < 0 && actor.ap == 0 && actor.ap < apBefore && actor.isAlive()) {
            log.add(actor.name + " ficou sem AP — turno termina.");
            endTurn();
        }
        return result;
    }

    private void applyPowerup(Assassin actor, Powerup p) {
        switch (p) {
            case HEAL:
                actor.heal(POWERUP_HEAL_AMOUNT);
                log.add("✦ " + actor.name + " apanhou cura (+" + POWERUP_HEAL_AMOUNT + " HP)");
                break;
            case DAMAGE:
                actor.damageBoost += POWERUP_DAMAGE_BONUS;
                log.add("✦ " + actor.name + " apanhou dano bónus (+" + POWERUP_DAMAGE_BONUS + " no próximo ataque)");
                break;
        }
    }

    private void spawnRandomPowerup() {
        for (int attempt = 0; attempt < 200; attempt++) {
            int x = rng.nextInt(WIDTH);
            int y = rng.nextInt(HEIGHT);
            if (grid[x][y] == Tile.WALL) continue;
            if (powerups[x][y] != null) continue;
            if (assassinAt(x, y) != null) continue;
            if (isInStartingArea(x, y)) continue;
            powerups[x][y] = rng.nextBoolean() ? Powerup.HEAL : Powerup.DAMAGE;
            return;
        }
    }

    private void triggerFlash(int x, int y) {
        flashX = x;
        flashY = y;
        flashEndTime = System.currentTimeMillis() + 400;
    }

    private void triggerHit(int x, int y, Action a) {
        hitX = x;
        hitY = y;
        hitAction = a;
        hitSeq++;
    }

    private void triggerShuriken(int fromX, int fromY, int toX, int toY) {
        shurikenFromX = fromX;
        shurikenFromY = fromY;
        shurikenToX = toX;
        shurikenToY = toY;
        shurikenStartTime = System.currentTimeMillis();
        int dist = Math.max(Math.abs(toX - fromX), Math.abs(toY - fromY));
        shurikenEndTime = shurikenStartTime + 60L * Math.max(1, dist);
    }

    private void checkWinner() {
        int aliveCount = 0, aliveIdx = -1;
        for (int i = 0; i < players.length; i++) {
            if (players[i].isAlive()) { aliveCount++; aliveIdx = i; }
        }
        if (aliveCount <= 1) {
            winnerIdx = aliveIdx;
            if (winnerIdx >= 0) {
                log.add("=== " + players[winnerIdx].name + " VENCEU! ===");
                SoundFx.victory();
            }
        }
    }

    public void endTurn() {
        if (winnerIdx >= 0 || paused) return;
        players[currentPlayerIdx].turnsTaken++;
        int safety = 0;
        do {
            currentPlayerIdx = (currentPlayerIdx + 1) % players.length;
            safety++;
            if (safety > players.length + 1) break; // shouldn't happen
        } while (!currentPlayer().isAlive());
        currentPlayer().resetTurn();
        turnNumber++;
        selectedAction = Action.MOVE;
        log.add("--- Turno " + turnNumber + ": " + currentPlayer().name + " ---");

        // Spawn a powerup roughly every full round (= numPlayers turns) * 3
        if (turnNumber % (3 * players.length) == 0) {
            spawnRandomPowerup();
            log.add("✦ Novo power-up apareceu no mapa.");
        }
    }

    /** Bresenham line of sight: walls block. Diagonal steps cannot squeeze between two walls. */
    public boolean hasLineOfSight(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        int cx = x0, cy = y0;
        while (true) {
            int e2 = 2 * err;
            boolean stepX = e2 > -dy;
            boolean stepY = e2 < dx;
            if (stepX && stepY) {
                // Diagonal step: forbid passing between two ortho walls (e.g., L-corner).
                if (isWall(cx + sx, cy) && isWall(cx, cy + sy)) return false;
                err -= dy; cx += sx;
                err += dx; cy += sy;
            } else if (stepX) {
                err -= dy; cx += sx;
            } else if (stepY) {
                err += dx; cy += sy;
            }
            if (cx == x1 && cy == y1) return true;
            if (isWall(cx, cy)) return false;
        }
    }
}
