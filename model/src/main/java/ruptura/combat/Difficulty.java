package ruptura.combat;

public enum Difficulty {
    STORY("História", .65, .25), NORMAL("Normal", 1, .55),
    HARD("Difícil", 1.20, .80), NIGHTMARE("Pesadelo", 1.40, 1);

    private final String label;
    public final double incomingDamage;
    public final double aggression;

    Difficulty(String label, double incomingDamage, double aggression) {
        this.label = label;
        this.incomingDamage = incomingDamage;
        this.aggression = aggression;
    }

    public String label() { return label; }
    @Override public String toString() { return label; }
}
