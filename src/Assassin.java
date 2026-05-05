import java.awt.Color;

public class Assassin {
    public final String name;
    public final Color color;
    public int x, y;
    public int hp = 100;
    public final int maxHp = 100;
    public int ap = 3;
    public final int maxAp = 3;
    public boolean usedHeal = false;

    public Assassin(String name, Color color, int x, int y) {
        this.name = name;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public boolean isAlive() {
        return hp > 0;
    }

    public void resetTurn() {
        ap = maxAp;
    }

    public void damage(int amount) {
        hp = Math.max(0, hp - amount);
    }

    public void heal(int amount) {
        hp = Math.min(maxHp, hp + amount);
    }
}
