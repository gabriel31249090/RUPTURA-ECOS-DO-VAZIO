package ruptura.combat;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/** Battle rules and delayed action resolution, independent from graphics and input. */
public final class CombatSystem {
    public static final int WIDTH = 10, HEIGHT = 8;
    public enum Phase { PLAYER, ANIMATING, ENEMY, VICTORY, DEFEAT }

    private final List<CombatUnit> units = new ArrayList<>();
    private final List<CombatUnit> unitView = Collections.unmodifiableList(units);
    private final List<CombatUnit> order = new ArrayList<>();
    private final List<String> history = new ArrayList<>();
    private final Difficulty difficulty;
    private final CombatCatalog abilities = new CombatCatalog("abilities.json");
    private final boolean[][] walls = new boolean[WIDTH][HEIGHT];
    private Phase phase;
    private int turn, potions = 3, round = 1, effectX, effectY, affinityHealing;
    private float delay, effectTime;
    private String message = "", effectKind = "";
    private boolean moved, reducedMotion;
    private Runnable pending;
    private CombatUnit cachedPathActor;
    private long cachedPathSignature;
    private int[][] cachedDistances;

    public CombatSystem(Difficulty difficulty, boolean bossEncounter, String heroName, String specialization) {
        this.difficulty = difficulty == null ? Difficulty.NORMAL : difficulty;
        potions = switch (this.difficulty) { case STORY -> 4; case NORMAL -> 3; case HARD -> 2; case NIGHTMARE -> 1; };
        walls[4][2] = walls[4][3] = walls[5][5] = walls[6][5] = true;
        CombatUnit hero = new CombatUnit("hero", heroName == null || heroName.isBlank() ? "Alex" : heroName,
                "hero", true, 2, 3, 110, 40, 25, 9, 19, 3, 1);
        String focus = specialization == null ? "" : specialization.toLowerCase(java.util.Locale.ROOT);
        if (focus.contains("tecno")) { hero.kindSkill = "pulso"; hero.technology += 8; }
        else if (focus.contains("executor")) { hero.attack += 5; hero.maxEnergy += 4; }
        else if (focus.contains("sobrevivente")) { hero.maxHp += 20; hero.hp = hero.maxHp; hero.defense += 3; }
        else if (focus.contains("médico") || focus.contains("medico")) { hero.maxEnergy += 24; hero.kindSkill = "cura"; }
        else { hero.maxEnergy += 8; hero.dimensionalPower += 6; }
        hero.energy = hero.maxEnergy;
        units.add(hero);
        units.add(new CombatUnit("lia", "Lia Torres", "lia", true, 2, 5, 90, 50, 18, 7, 14, 3, 3));
        CombatCatalog enemies = new CombatCatalog("enemies.json");
        addEnemy(enemies, "errante", 6, 2);
        addEnemy(enemies, bossEncounter ? "estilhacado" : "corredor", 7, 5);
        addEnemy(enemies, bossEncounter ? "sussurrante" : "estilhacado", 8, 3);
        order.addAll(units);
        order.sort(Comparator.comparingInt((CombatUnit u) -> u.speed).reversed());
        beginTurn();
    }

    private void addEnemy(CombatCatalog catalog, String id, int x, int y) {
        Map<String, String> data = catalog.get(id);
        CombatUnit unit = new CombatUnit(id, data.get("name"), id, false, x, y,
                CombatCatalog.number(data, "hp"), CombatCatalog.number(data, "energy"),
                CombatCatalog.number(data, "attack"), CombatCatalog.number(data, "defense"),
                CombatCatalog.number(data, "speed"), CombatCatalog.number(data, "move"), CombatCatalog.number(data, "range"));
        unit.weakness = data.get("weakness");
        units.add(unit);
    }

    public List<CombatUnit> units() { return unitView; }
    public List<CombatUnit> initiative() {
        List<CombatUnit> result = new ArrayList<>();
        for (int i = 0; i < order.size(); i++) {
            CombatUnit u = order.get((turn + i) % order.size());
            if (u.alive()) result.add(u);
        }
        return List.copyOf(result);
    }
    public CombatUnit current() { return order.get(turn); }
    public Phase phase() { return phase; }
    public String message() { return message; }
    public List<String> log() { return List.copyOf(history); }
    public float effectTime() { return effectTime; }
    public int effectX() { return effectX; }
    public int effectY() { return effectY; }
    public String effectKind() { return effectKind; }
    public int potions() { return potions; }
    public int round() { return round; }
    public boolean hasMoved() { return moved; }
    public void setReducedMotion(boolean value) { reducedMotion = value; }
    public void setPotions(int count) {
        if (count < 0 || count > 999) throw new IllegalArgumentException("Quantidade de kits inválida");
        potions = count;
    }
    /** Each positive relationship point gives Lia +3 healing, capped at three points. */
    public void setAffinity(int value) { affinityHealing = Math.max(0, Math.min(3, value)) * 3; }
    public boolean inBounds(int x, int y) { return x >= 0 && y >= 0 && x < WIDTH && y < HEIGHT; }
    public boolean blocked(int x, int y) { return !inBounds(x, y) || walls[x][y]; }
    public boolean hazardous(int x, int y) { return (x == 6 && y == 3) || (x == 7 && y == 3); }
    public CombatUnit unitAt(int x, int y) {
        for (CombatUnit unit : units) if (unit.alive() && unit.x == x && unit.y == y) return unit;
        return null;
    }

    public boolean reachable(int x, int y) {
        if (phase != Phase.PLAYER || moved || blocked(x, y) || unitAt(x, y) != null) return false;
        return distances(current())[x][y] <= current().move;
    }

    public String skillName() { return abilities.get(skillId(current())).get("name"); }
    public int skillCost() { return CombatCatalog.number(abilities.get(skillId(current())), "cost"); }
    public String skillDescription() { return abilities.get(skillId(current())).get("description"); }
    public boolean canTarget(ActionType action, int x, int y) {
        if (phase != Phase.PLAYER) return false;
        CombatUnit target = unitAt(x, y), actor = current();
        if (target == null) return false;
        if (action == ActionType.ATTACK) return target.ally != actor.ally && within(actor, target, actor.range);
        if (action == ActionType.ITEM) return potions > 0 && target.ally && target.hp < target.maxHp && within(actor, target, 5);
        if (action != ActionType.SKILL || actor.energy < skillCost()) return false;
        Map<String, String> skill = abilities.get(skillId(actor));
        boolean healing = skill.get("type").equals("healing");
        return within(actor, target, CombatCatalog.number(skill, "range"))
                && (healing ? target.ally && target.hp < target.maxHp : !target.ally);
    }
    private String skillId(CombatUnit unit) { return unit.id.equals("lia") ? "cura" : unit.kindSkill; }

    public boolean perform(ActionType action, int targetX, int targetY) {
        if (phase != Phase.PLAYER || action == null || !current().alive()) return false;
        CombatUnit actor = current();
        CombatUnit target = unitAt(targetX, targetY);
        switch (action) {
            case MOVE -> {
                if (!reachable(targetX, targetY)) return reject("Esta casa está inacessível ou o movimento já foi usado.");
                queue("move", targetX, targetY, () -> {
                    actor.x = targetX; actor.y = targetY; moved = true;
                    if (hazardous(targetX, targetY)) { actor.hp = Math.max(0, actor.hp - 5); say("O resíduo dimensional causa 5 de dano."); }
                    else say(actor.name + " reposicionou-se. Escolha uma ação.");
                    if (!checkOutcome()) phase = Phase.PLAYER;
                });
            }
            case ATTACK -> {
                if (target == null || target.ally == actor.ally || !within(actor, target, actor.range))
                    return reject("Escolha um inimigo no alcance (" + actor.range + ").");
                queue("attack", targetX, targetY, () -> { strike(actor, target, actor.attack, "physical"); endTurn(); });
            }
            case SKILL -> {
                Map<String, String> skill = abilities.get(skillId(actor));
                int cost = CombatCatalog.number(skill, "cost");
                int range = CombatCatalog.number(skill, "range");
                if (actor.energy < cost) return reject("Energia insuficiente.");
                boolean healing = skill.get("type").equals("healing");
                if (target == null || !within(actor, target, range) || (healing ? !target.ally : target.ally))
                    return reject(healing ? "Selecione um aliado a até cinco casas para curar." : "Selecione um inimigo a até quatro casas.");
                if (healing && target.hp == target.maxHp) return reject("Este aliado já está com a vida cheia.");
                queue(healing ? "heal" : skill.get("type"), targetX, targetY, () -> {
                    actor.energy -= cost;
                    if (healing) {
                        int healed = Math.min(CombatCatalog.number(skill, "power") + (actor.id.equals("lia") ? affinityHealing : 0), target.maxHp - target.hp);
                        target.hp += healed; say(actor.name + " estabilizou " + target.name + ": +" + healed + " PV.");
                    } else {
                        int bonus = skill.get("type").equals("electrical") ? Math.max(0, actor.technology - 12)
                                : Math.max(0, actor.dimensionalPower - 18);
                        strike(actor, target, CombatCatalog.number(skill, "power") + bonus, skill.get("type"));
                    }
                    endTurn();
                });
            }
            case DEFEND -> queue("guard", actor.x, actor.y, () -> {
                actor.guarded = true; actor.guardTurns = 1;
                actor.energy = Math.min(actor.maxEnergy, actor.energy + 5);
                say(actor.name + " assumiu a defesa e recuperou 5 de energia."); endTurn();
            });
            case ITEM -> {
                if (potions <= 0) return reject("Não há mais kits médicos nesta batalha.");
                if (target == null || !target.ally || !within(actor, target, 5)) return reject("Selecione um aliado a até cinco casas.");
                if (target.hp == target.maxHp) return reject("Este aliado já está com a vida cheia.");
                queue("heal", targetX, targetY, () -> {
                    potions--; int restored = Math.min(40, target.maxHp - target.hp); target.hp += restored;
                    say(actor.name + " usou um kit: +" + restored + " PV em " + target.name + "."); endTurn();
                });
            }
            case END_TURN -> { say(actor.name + " encerrou o turno."); endTurn(); }
        }
        return true;
    }

    public void update(float dt) {
        if (dt <= 0 || !Float.isFinite(dt)) return;
        effectTime = Math.max(0, effectTime - dt);
        if (phase != Phase.ANIMATING && phase != Phase.ENEMY) return;
        delay -= dt;
        if (delay > 0) return;
        if (phase == Phase.ANIMATING) {
            Runnable action = pending; pending = null;
            if (action != null) action.run();
        } else enemyAction();
    }

    private void queue(String kind, int x, int y, Runnable result) {
        phase = Phase.ANIMATING; pending = result;
        delay = reducedMotion ? .08f : kind.equals("move") ? .16f : .42f;
        effectTime = delay + .20f; effectKind = kind; effectX = x; effectY = y;
    }

    private boolean reject(String reason) { message = reason; return false; }
    private void say(String text) {
        message = text; history.add(text);
        if (history.size() > 40) history.remove(0);
    }

    private void strike(CombatUnit actor, CombatUnit target, int power, String type) {
        boolean weak = type.equals(target.weakness);
        int damage = DamageFormula.calculate(power, target.defense, target.guarded, weak,
                target.broken, actor.ally ? 1 : difficulty.incomingDamage);
        target.hp = Math.max(0, target.hp - damage);
        String extra = "";
        if (weak && target.alive()) {
            target.posture = Math.max(0, target.posture - 1);
            extra = " Fraqueza!";
            if (target.posture == 0) { target.broken = true; target.status = "Vulnerável"; extra += " Postura quebrada!"; }
        }
        say(actor.name + " atingiu " + target.name + ": " + damage + " de dano." + extra);
        if (!target.alive()) { target.status = "Derrotado"; say(target.name + " foi derrotado."); }
        if (target.kind.equals("sussurrante") && target.alive() && target.hp <= target.maxHp / 2 && target.phaseStage == 1) {
            target.phaseStage = 2; target.attack += 7; target.move++; target.range = 4;
            target.weakness = "dimensional";
            say("O Sussurrante rasga a própria sombra. Fase II: agora é vulnerável ao vazio!");
        }
    }

    private boolean within(CombatUnit actor, CombatUnit target, int range) {
        return Math.abs(actor.x - target.x) + Math.abs(actor.y - target.y) <= range && lineOfSight(actor.x, actor.y, target.x, target.y);
    }

    private boolean lineOfSight(int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0), sx = x0 < x1 ? 1 : -1;
        int dy = -Math.abs(y1 - y0), sy = y0 < y1 ? 1 : -1, error = dx + dy;
        while (x0 != x1 || y0 != y1) {
            int twice = 2 * error;
            if (twice >= dy) { error += dy; x0 += sx; }
            if (twice <= dx) { error += dx; y0 += sy; }
            if ((x0 != x1 || y0 != y1) && blocked(x0, y0)) return false;
        }
        return true;
    }

    private int[][] distances(CombatUnit actor) {
        // The renderer asks about every grid cell. Reuse BFS until occupancy changes.
        // A signature also handles direct unit changes made by tests and debug tools.
        long signature = 17;
        for (CombatUnit unit : units) {
            signature = signature * 31 + unit.x;
            signature = signature * 31 + unit.y;
            signature = signature * 31 + (unit.alive() ? 1 : 0);
        }
        if (cachedDistances != null && cachedPathActor == actor && cachedPathSignature == signature)
            return cachedDistances;
        int[][] distance = new int[WIDTH][HEIGHT];
        for (int[] column : distance) Arrays.fill(column, 999);
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        distance[actor.x][actor.y] = 0; queue.add(new int[] {actor.x, actor.y});
        int[][] directions = {{1,0},{-1,0},{0,1},{0,-1}};
        while (!queue.isEmpty()) {
            int[] tile = queue.removeFirst();
            for (int[] d : directions) {
                int x = tile[0] + d[0], y = tile[1] + d[1];
                if (blocked(x, y) || unitAt(x, y) != null || distance[x][y] <= distance[tile[0]][tile[1]] + 1) continue;
                distance[x][y] = distance[tile[0]][tile[1]] + 1; queue.addLast(new int[] {x,y});
            }
        }
        cachedPathActor = actor;
        cachedPathSignature = signature;
        cachedDistances = distance;
        return distance;
    }

    private void enemyAction() {
        CombatUnit actor = current();
        if (checkOutcome()) return;
        CombatUnit chosen = null;
        int chosenX = actor.x, chosenY = actor.y;
        double best = Double.POSITIVE_INFINITY;
        int[][] distance = distances(actor);
        for (CombatUnit target : units) {
            if (!target.ally || !target.alive()) continue;
            for (int x = 0; x < WIDTH; x++) for (int y = 0; y < HEIGHT; y++) {
                if (distance[x][y] > actor.move) continue;
                int gap = Math.abs(x - target.x) + Math.abs(y - target.y);
                double score = gap * 8 + distance[x][y] * .15 + (hazardous(x,y) ? 18 : 0)
                        + (double) target.hp / target.maxHp * difficulty.aggression * 14;
                if (gap <= actor.range && lineOfSight(x,y,target.x,target.y)) score -= 35;
                if (score < best) { best = score; chosen = target; chosenX = x; chosenY = y; }
            }
        }
        if (chosen == null) { endTurn(); return; }
        final CombatUnit target = chosen;
        final int toX = chosenX, toY = chosenY;
        // Movement and attack are one enemy action; no input can interrupt resolution.
        queue("enemy", target.x, target.y, () -> {
            actor.x = toX; actor.y = toY;
            if (hazardous(toX, toY)) actor.hp = Math.max(0, actor.hp - 5);
            if (actor.alive() && target.alive() && within(actor, target, actor.range)) {
                boolean special = actor.energy >= 8 && difficulty.aggression >= .55 && (actor.kind.equals("estilhacado") || actor.phaseStage == 2);
                if (special) actor.energy -= 8;
                strike(actor, target, actor.attack + (special ? 6 : 0), special ? "dimensional" : "physical");
            } else say(actor.name + " avança entre os escombros.");
            endTurn();
        });
    }

    private void endTurn() {
        if (checkOutcome()) return;
        do { turn++; if (turn == order.size()) { turn = 0; round++; } } while (!current().alive());
        beginTurn();
    }

    private void beginTurn() {
        if (checkOutcome()) return;
        CombatUnit actor = current(); moved = false;
        if (actor.guardTurns > 0) { actor.guardTurns = 0; actor.guarded = false; }
        if (actor.broken) {
            actor.broken = false; actor.posture = 2; actor.status = "";
            say(actor.name + " perde a ação recuperando a postura.");
            phase = Phase.ANIMATING; delay = reducedMotion ? .08f : .45f;
            pending = this::endTurn;
            return;
        }
        phase = actor.ally ? Phase.PLAYER : Phase.ENEMY;
        delay = reducedMotion ? .08f : .55f;
        message = actor.ally ? "Vez de " + actor.name + ". Mova-se e escolha uma ação." : actor.name + " prepara uma ação…";
    }

    private boolean checkOutcome() {
        boolean allies = false, enemies = false;
        for (CombatUnit unit : units) if (unit.alive()) { if (unit.ally) allies = true; else enemies = true; }
        if (!allies || !enemies) {
            phase = allies ? Phase.VICTORY : Phase.DEFEAT;
            pending = null;
            say(allies ? "Área segura. O sinal ainda está no ar." : "O silêncio tomou o assentamento.");
            return true;
        }
        return false;
    }
}
