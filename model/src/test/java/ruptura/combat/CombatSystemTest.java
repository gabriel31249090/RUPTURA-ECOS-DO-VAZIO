package ruptura.combat;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

final class CombatSystemTest {
    private CombatSystem battle() { return new CombatSystem(Difficulty.NORMAL, false, "Íris", "Ressonante"); }
    private void resolve(CombatSystem battle) { battle.update(1); }
    private void backToHero(CombatSystem battle) {
        for (int i = 0; i < 100 && !(battle.phase() == CombatSystem.Phase.PLAYER && battle.current().id.equals("hero")); i++) {
            if (battle.phase() == CombatSystem.Phase.PLAYER) battle.perform(ActionType.END_TURN, 0, 0);
            else resolve(battle);
        }
        assertEquals("hero", battle.current().id);
        assertEquals(CombatSystem.Phase.PLAYER, battle.phase());
    }

    @Test void initiativeStartsWithFastestAndIncludesEveryUnitOnce() {
        CombatSystem battle = battle();
        assertEquals("hero", battle.current().id);
        assertEquals(5, battle.initiative().size());
        int previous = Integer.MAX_VALUE;
        for (CombatUnit unit : battle.initiative()) { assertTrue(unit.speed <= previous); previous = unit.speed; }
        assertEquals(5, battle.initiative().stream().map(u -> u.id).distinct().count());
    }

    @Test void movementRejectsWallsOccupiedCellsAndUnreachableDetours() {
        CombatSystem battle = battle();
        assertTrue(battle.blocked(-1, 0));
        assertTrue(battle.blocked(10, 8));
        assertFalse(battle.reachable(4, 3));
        assertFalse(battle.reachable(2, 5));
        // Manhattan distance is three, but the wall forces a longer route.
        assertFalse(battle.reachable(5, 3));
        assertFalse(battle.perform(ActionType.MOVE, -1, 3));
        assertTrue(battle.perform(ActionType.MOVE, 3, 3));
        assertEquals(2, battle.current().x);
        assertFalse(battle.perform(ActionType.DEFEND, 0, 0));
        resolve(battle);
        assertEquals(3, battle.current().x);
        assertFalse(battle.perform(ActionType.MOVE, 3, 4));
        assertEquals(CombatSystem.Phase.PLAYER, battle.phase());
    }

    @Test void abilityDamageAndEnergyResolveExactlyOnceAfterAnimation() {
        CombatSystem battle = battle();
        CombatUnit target = battle.units().stream().filter(u -> u.kind.equals("estilhacado")).findFirst().orElseThrow();
        target.x = 3; target.y = 3;
        int originalHp = target.hp, originalEnergy = battle.current().energy;
        assertTrue(battle.perform(ActionType.SKILL, 3, 3));
        assertEquals(originalHp, target.hp);
        assertFalse(battle.perform(ActionType.SKILL, 3, 3));
        battle.update(.01f);
        assertEquals(originalHp, target.hp);
        resolve(battle);
        assertTrue(target.hp < originalHp);
        assertEquals(originalEnergy - 8, battle.units().getFirst().energy);
        int hpAfter = target.hp;
        battle.update(.01f);
        assertEquals(hpAfter, target.hp);
        assertEquals(1, target.posture);
    }

    @Test void defenseProtectsUntilNextOwnTurnAndRestoresEnergy() {
        CombatSystem battle = battle();
        CombatUnit hero = battle.current(); hero.energy = 5;
        assertTrue(battle.perform(ActionType.DEFEND, 0, 0)); resolve(battle);
        assertTrue(hero.guarded); assertEquals(10, hero.energy);
        backToHero(battle);
        assertFalse(hero.guarded);
    }

    @Test void liaisonCanHealButCannotSpendEnergyOnInvalidTargets() {
        CombatSystem battle = battle();
        battle.perform(ActionType.END_TURN, 0, 0);
        for (int i = 0; i < 10 && battle.phase() != CombatSystem.Phase.PLAYER; i++) resolve(battle);
        assertEquals("lia", battle.current().id);
        CombatUnit hero = battle.units().getFirst(); hero.hp = 50;
        int energy = battle.current().energy;
        assertFalse(battle.perform(ActionType.SKILL, 9, 7));
        assertEquals(energy, battle.current().energy);
        assertTrue(battle.perform(ActionType.SKILL, hero.x, hero.y));
        resolve(battle);
        assertEquals(80, hero.hp);
        assertEquals(energy - 10, battle.units().get(1).energy);
    }

    @Test void victoryAndDefeatAreTerminalAndExclusive() {
        CombatSystem victory = battle();
        victory.units().stream().filter(u -> !u.ally).forEach(u -> u.hp = 0);
        victory.perform(ActionType.END_TURN, 0, 0);
        assertEquals(CombatSystem.Phase.VICTORY, victory.phase());
        assertFalse(victory.perform(ActionType.ATTACK, 0, 0));
        CombatSystem defeat = battle();
        defeat.perform(ActionType.DEFEND, 0, 0);
        defeat.units().stream().filter(u -> u.ally).forEach(u -> u.hp = 0);
        resolve(defeat);
        assertEquals(CombatSystem.Phase.DEFEAT, defeat.phase());
    }

    @Test void enemyTraversesValidCellsAndUsesHigherDifficultyDamage() {
        CombatSystem normal = battle();
        normal.perform(ActionType.END_TURN, 0, 0);
        for (int i = 0; i < 30 && normal.phase() != CombatSystem.Phase.PLAYER; i++) resolve(normal);
        for (CombatUnit unit : normal.units()) assertFalse(normal.blocked(unit.x, unit.y));
        assertEquals(normal.units().size(), normal.units().stream().map(u -> u.x + "," + u.y).distinct().count());
        assertTrue(Difficulty.NIGHTMARE.incomingDamage > Difficulty.STORY.incomingDamage);
        assertTrue(DamageFormula.calculate(20, 8, false, false, false, Difficulty.NIGHTMARE.incomingDamage)
                > DamageFormula.calculate(20, 8, false, false, false, Difficulty.STORY.incomingDamage));
    }

    @Test void bossEntersSecondPhaseOnceAndChangesItsWeakness() {
        CombatSystem battle = new CombatSystem(Difficulty.STORY, true, "Íris", "Ressonante");
        CombatUnit boss = battle.units().stream().filter(u -> u.kind.equals("sussurrante")).findFirst().orElseThrow();
        boss.x = 3; boss.y = 3; boss.hp = 68;
        assertTrue(battle.perform(ActionType.SKILL, 3, 3)); resolve(battle);
        assertEquals(2, boss.phaseStage);
        assertEquals("dimensional", boss.weakness);
        assertEquals(29, boss.attack);
    }

    @Test void brokenEnemyLosesExactlyOneTurnWithoutCreatingExtraTurns() {
        CombatSystem battle = battle();
        CombatUnit runner = battle.units().stream().filter(u -> u.kind.equals("corredor")).findFirst().orElseThrow();
        runner.broken = true; runner.posture = 0;
        battle.perform(ActionType.END_TURN, 0, 0);
        assertEquals("corredor", battle.current().id);
        assertEquals(CombatSystem.Phase.ANIMATING, battle.phase());
        resolve(battle);
        assertEquals("lia", battle.current().id);
        assertFalse(runner.broken); assertEquals(2, runner.posture);
    }

    @Test void formulaHasPositiveFloorAndDefenseAndWeaknessHaveMeaning() {
        int base = DamageFormula.calculate(20, 8, false, false, false, 1);
        assertTrue(DamageFormula.calculate(20, 8, true, false, false, 1) < base);
        assertTrue(DamageFormula.calculate(20, 8, false, true, false, 1) > base);
        assertEquals(3, DamageFormula.calculate(1, 999, false, false, false, 1));
    }

    @Test void entireStoryAndNormalEncountersCanBeWonThroughPublicActionsWithEverySpecialization() {
        for (Difficulty difficulty : new Difficulty[] {Difficulty.STORY, Difficulty.NORMAL})
        for (String focus : new String[] {"Ressonante", "Executor", "Tecnomante", "Sobrevivente", "Médico de campo"})
        for (boolean boss : new boolean[] {false, true}) {
            CombatSystem battle = new CombatSystem(difficulty, boss, "Íris", focus);
            for (int step = 0; step < 1600 && battle.phase() != CombatSystem.Phase.VICTORY
                    && battle.phase() != CombatSystem.Phase.DEFEAT; step++) {
                if (battle.phase() != CombatSystem.Phase.PLAYER) { resolve(battle); continue; }
                takeTacticalAction(battle);
            }
            assertEquals(CombatSystem.Phase.VICTORY, battle.phase(), "Dificuldade=" + difficulty + " foco=" + focus + " chefe=" + boss + " log=" + battle.log());
        }
    }

    @Test void specializationsApplyActualDistinctStatsAndAbilities() {
        CombatUnit resonant = new CombatSystem(Difficulty.NORMAL, false, "A", "Ressonante").current();
        CombatUnit executor = new CombatSystem(Difficulty.NORMAL, false, "A", "Executor").current();
        CombatUnit techno = new CombatSystem(Difficulty.NORMAL, false, "A", "Tecnomante").current();
        CombatUnit survivor = new CombatSystem(Difficulty.NORMAL, false, "A", "Sobrevivente").current();
        CombatUnit medic = new CombatSystem(Difficulty.NORMAL, false, "A", "Médico de campo").current();
        assertEquals(48, resonant.maxEnergy); assertEquals(24, resonant.dimensionalPower);
        assertEquals(30, executor.attack); assertEquals(44, executor.maxEnergy);
        assertEquals("pulso", techno.kindSkill); assertEquals(20, techno.technology);
        assertEquals(130, survivor.maxHp); assertEquals(12, survivor.defense);
        assertEquals(64, medic.maxEnergy); assertEquals("cura", medic.kindSkill);
    }

    private void takeTacticalAction(CombatSystem battle) {
        CombatUnit actor = battle.current();
        CombatUnit hurt = battle.units().stream().filter(u -> u.ally && u.alive() && u.hp < u.maxHp - 25)
                .min(java.util.Comparator.comparingDouble(u -> (double) u.hp / u.maxHp)).orElse(null);
        if (hurt != null) {
            if (battle.canTarget(ActionType.SKILL, hurt.x, hurt.y)) {
                assertTrue(battle.perform(ActionType.SKILL, hurt.x, hurt.y)); return;
            }
            if (hurt.hp < hurt.maxHp * .5 && battle.canTarget(ActionType.ITEM, hurt.x, hurt.y)) {
                assertTrue(battle.perform(ActionType.ITEM, hurt.x, hurt.y)); return;
            }
        }
        CombatUnit enemy = battle.units().stream().filter(u -> !u.ally && u.alive())
                .min(java.util.Comparator.comparingInt(u -> u.maxHp + 4 * (Math.abs(actor.x-u.x) + Math.abs(actor.y-u.y)))).orElseThrow();
        if (battle.canTarget(ActionType.SKILL, enemy.x, enemy.y)) {
            assertTrue(battle.perform(ActionType.SKILL, enemy.x, enemy.y)); return;
        }
        if (battle.canTarget(ActionType.ATTACK, enemy.x, enemy.y)) {
            assertTrue(battle.perform(ActionType.ATTACK, enemy.x, enemy.y)); return;
        }
        if (!battle.hasMoved()) {
            int bestX = -1, bestY = -1, best = Integer.MAX_VALUE;
            for (int x = 0; x < CombatSystem.WIDTH; x++) for (int y = 0; y < CombatSystem.HEIGHT; y++) {
                int score = Math.abs(x-enemy.x) + Math.abs(y-enemy.y) + (battle.hazardous(x,y) ? 8 : 0);
                if (battle.reachable(x,y) && score < best) { best = score; bestX = x; bestY = y; }
            }
            if (bestX >= 0) { assertTrue(battle.perform(ActionType.MOVE, bestX, bestY)); return; }
        }
        assertTrue(battle.perform(ActionType.DEFEND, actor.x, actor.y));
    }

    @Test void campaignResourcesAndAffinityApplyWithoutAccumulatingBonuses() {
        CombatSystem battle = battle();
        battle.setPotions(7); assertEquals(7, battle.potions());
        assertThrows(IllegalArgumentException.class, () -> battle.setPotions(-1));
        battle.setAffinity(2); battle.setAffinity(2);
        battle.perform(ActionType.END_TURN, 0, 0);
        for (int i = 0; i < 10 && battle.phase() != CombatSystem.Phase.PLAYER; i++) resolve(battle);
        CombatUnit hero = battle.units().getFirst(); hero.hp = 30;
        assertEquals("lia", battle.current().id);
        assertTrue(battle.perform(ActionType.SKILL, hero.x, hero.y)); resolve(battle);
        assertEquals(66, hero.hp); // 30 + base 30 + two relationship points * 3.
    }

    @Test void movementCacheTracksOccupancyDeathsAndActorPositions() {
        CombatSystem battle = battle();
        assertTrue(battle.reachable(3, 4));
        CombatUnit runner = battle.units().get(3), crystal = battle.units().get(4);
        runner.x = 3; runner.y = 3;
        crystal.x = 2; crystal.y = 4;
        assertFalse(battle.reachable(3, 4));
        runner.hp = 0;
        assertTrue(battle.reachable(3, 4));
        battle.current().x = 0;
        assertFalse(battle.reachable(3, 4));
        assertThrows(UnsupportedOperationException.class, () -> battle.units().clear());
    }
}
