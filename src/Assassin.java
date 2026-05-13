import java.awt.Color;

public class Assassin {
    public final int displayNumber; // 1..4 shown on the token
    public final String name;
    public final Color color;
    public int x, y;            // logical position
    public double visX, visY;   // visual position (animated toward x,y)
    public int hp = 150;
    public final int maxHp = 150;
    public int ap = 3;
    public final int maxAp = 3;
    public boolean usedHeal = false;
    public int damageBoost = 0; // bonus damage applied to next attack

    public Assassin(int displayNumber, String name, Color color, int x, int y) {
        this.displayNumber = displayNumber;
        this.name = name;
        this.color = color;
        this.x = x;
        this.y = y;
        this.visX = x;
        this.visY = y;
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
