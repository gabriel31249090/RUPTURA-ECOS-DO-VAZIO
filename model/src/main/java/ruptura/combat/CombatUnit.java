package ruptura.combat;

/** Mutable battle state. Persistent progression belongs to the campaign model. */
public final class CombatUnit {
    public final String id, name, kind;
    public final boolean ally;
    public int x, y, hp, maxHp, energy, maxEnergy, attack, defense, speed, move, range;
    public int accuracy = 100, resistance = 0, technology = 12, dimensionalPower = 18;
    public int criticalChance = 5, posture = 2, phaseStage = 1;
    public String weakness = "", status = "", kindSkill = "eco";
    public boolean guarded, broken;
    int guardTurns;

    public CombatUnit(String id, String name, String kind, boolean ally, int x, int y,
                      int hp, int energy, int attack, int defense, int speed, int move, int range) {
        this.id = id; this.name = name; this.kind = kind; this.ally = ally;
        this.x = x; this.y = y; this.hp = this.maxHp = hp;
        this.energy = this.maxEnergy = energy; this.attack = attack; this.defense = defense;
        this.speed = speed; this.move = move; this.range = range;
    }

    public boolean alive() { return hp > 0; }
}
