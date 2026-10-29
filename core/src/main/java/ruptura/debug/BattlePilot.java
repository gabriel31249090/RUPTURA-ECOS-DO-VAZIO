package ruptura.debug;

import ruptura.combat.ActionType;
import ruptura.combat.CombatSystem;
import ruptura.combat.CombatUnit;

import java.util.Comparator;

/** Deterministic verification driver; all decisions use the normal public action API. */
public final class BattlePilot {
    private BattlePilot() { }

    /** Queues at most one action. The normal game loop remains responsible for update. */
    public static void act(CombatSystem battle) {
        if (battle.phase() != CombatSystem.Phase.PLAYER) return;
        CombatUnit actor = battle.current();
        CombatUnit hurt = battle.units().stream()
                .filter(unit -> unit.ally && unit.alive() && unit.hp < unit.maxHp - 25)
                .min(Comparator.comparingDouble(unit -> (double) unit.hp / unit.maxHp)).orElse(null);
        if (hurt != null) {
            if (battle.canTarget(ActionType.SKILL, hurt.x, hurt.y)) {
                battle.perform(ActionType.SKILL, hurt.x, hurt.y);
                return;
            }
            if (hurt.hp < hurt.maxHp * .5 && battle.canTarget(ActionType.ITEM, hurt.x, hurt.y)) {
                battle.perform(ActionType.ITEM, hurt.x, hurt.y);
                return;
            }
        }

        CombatUnit enemy = battle.units().stream().filter(unit -> !unit.ally && unit.alive())
                .min(Comparator.comparingInt(unit -> unit.maxHp
                        + 4 * (Math.abs(actor.x - unit.x) + Math.abs(actor.y - unit.y)))).orElse(null);
        if (enemy == null) return;
        if (battle.canTarget(ActionType.SKILL, enemy.x, enemy.y)) {
            battle.perform(ActionType.SKILL, enemy.x, enemy.y);
            return;
        }
        if (battle.canTarget(ActionType.ATTACK, enemy.x, enemy.y)) {
            battle.perform(ActionType.ATTACK, enemy.x, enemy.y);
            return;
        }

        if (!battle.hasMoved()) {
            int bestX = -1, bestY = -1, best = Integer.MAX_VALUE;
            for (int x = 0; x < CombatSystem.WIDTH; x++) {
                for (int y = 0; y < CombatSystem.HEIGHT; y++) {
                    int score = Math.abs(x - enemy.x) + Math.abs(y - enemy.y)
                            + (battle.hazardous(x, y) ? 8 : 0);
                    if (battle.reachable(x, y) && score < best) {
                        best = score;
                        bestX = x;
                        bestY = y;
                    }
                }
            }
            if (bestX >= 0) {
                battle.perform(ActionType.MOVE, bestX, bestY);
                return;
            }
        }
        battle.perform(ActionType.DEFEND, actor.x, actor.y);
    }
}
