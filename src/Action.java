public enum Action {
    MOVE("Mover", 1),
    MELEE("Corpo-a-Corpo", 1),
    SHURIKEN("Shuriken", 2),
    HEAL("Curar", 2);

    public final String label;
    public final int cost;

    Action(String label, int cost) {
        this.label = label;
        this.cost = cost;
    }
}
