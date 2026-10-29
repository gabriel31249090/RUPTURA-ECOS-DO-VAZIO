package ruptura.combat;

/** Deterministic prototype balance: no hidden rolls and no unbounded extra turns. */
public final class DamageFormula {
    private DamageFormula() { }

    public static int calculate(int power, int defense, boolean guarded,
                                boolean weakness, boolean broken, double multiplier) {
        double result = Math.max(3, power - Math.max(0, defense) * .55);
        if (weakness) result *= 1.35;
        if (broken) result *= 1.30;
        if (guarded) result *= .45;
        return Math.max(1, (int) Math.round(result * multiplier));
    }
}
