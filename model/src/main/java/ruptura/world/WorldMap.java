package ruptura.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/** Rua autoral com variação decorativa determinística; rotas narrativas protegidas. */
public final class WorldMap {
    public static final int WIDTH = 18, HEIGHT = 15;
    public static final int ROAD = 0, PAVEMENT = 1, BUILDING = 2, RUBBLE = 3, ANOMALY = 4;
    private final int[][] tiles = new int[WIDTH][HEIGHT];
    private final int[][] heights = new int[WIDTH][HEIGHT];
    private final long seed;

    public WorldMap(long seed) {
        this.seed = seed;
        Random random = new Random(seed);
        for (int x = 0; x < WIDTH; x++) {
            for (int y = 0; y < HEIGHT; y++) {
                boolean road = Math.abs(y - (x * .56f + 2)) < 2.5f;
                tiles[x][y] = road ? ROAD : BUILDING;
                heights[x][y] = road ? 0 : 1 + random.nextInt(3);
            }
        }
        // Uma avenida larga e uma praça em cada interação preservam colisões legíveis.
        for (int x = 1; x < WIDTH - 1; x++) {
            int cy = Math.min(HEIGHT - 3, 3 + x / 2);
            carve(x, cy, 2);
        }
        int[][] landmarks = {{2, 3}, {3, 4}, {4, 6}, {7, 7}, {11, 8}, {15, 11}};
        for (int[] point : landmarks) carve(point[0], point[1], 1);
        for (int x = 1; x < WIDTH - 1; x++) {
            for (int y = 1; y < HEIGHT - 1; y++) {
                if (tiles[x][y] == ROAD && random.nextFloat() < .14f) tiles[x][y] = PAVEMENT;
            }
        }
        tiles[12][10] = ANOMALY;
        tiles[13][10] = ANOMALY;
        // Fragmentos laterais, nunca sobre a rota da missão.
        for (int[] p : new int[][]{{2, 8}, {5, 2}, {9, 3}, {16, 5}, {8, 12}}) {
            tiles[p[0]][p[1]] = RUBBLE;
            heights[p[0]][p[1]] = 1;
        }
        // O veículo abandonado usa a mesma célula no renderizador e na colisão.
        tiles[5][5] = RUBBLE;
        heights[5][5] = 0;
    }

    private void carve(int cx, int cy, int radius) {
        for (int x = Math.max(1, cx - radius); x <= Math.min(WIDTH - 2, cx + radius); x++)
            for (int y = Math.max(1, cy - radius); y <= Math.min(HEIGHT - 2, cy + radius); y++) {
                tiles[x][y] = ROAD;
                heights[x][y] = 0;
            }
    }

    public long seed() { return seed; }
    public boolean walkable(int x, int y) {
        return x >= 0 && y >= 0 && x < WIDTH && y < HEIGHT
                && tiles[x][y] != BUILDING && tiles[x][y] != RUBBLE;
    }
    public int tile(int x, int y) { return inBounds(x, y) ? tiles[x][y] : BUILDING; }
    public int elevation(int x, int y) { return inBounds(x, y) ? heights[x][y] : 0; }
    private boolean inBounds(int x, int y) { return x >= 0 && y >= 0 && x < WIDTH && y < HEIGHT; }

    /** Busca cardinal evita atravessar quinas de edifícios. */
    public static List<Cell> path(WorldMap map, int sx, int sy, int tx, int ty) {
        if (!map.walkable(sx, sy) || !map.walkable(tx, ty)) return List.of();
        int[] previous = new int[WIDTH * HEIGHT];
        Arrays.fill(previous, -1);
        int start = sx + sy * WIDTH, goal = tx + ty * WIDTH;
        previous[start] = start;
        ArrayDeque<Integer> queue = new ArrayDeque<>();
        queue.add(start);
        int[][] directions = {{1,0},{0,1},{-1,0},{0,-1}};
        while (!queue.isEmpty() && previous[goal] == -1) {
            int current = queue.remove();
            for (int[] dir : directions) {
                int x = current % WIDTH + dir[0], y = current / WIDTH + dir[1];
                if (!map.walkable(x, y)) continue;
                int next = x + y * WIDTH;
                if (previous[next] != -1) continue;
                previous[next] = current;
                queue.add(next);
            }
        }
        if (previous[goal] == -1) return List.of();
        List<Cell> result = new ArrayList<>();
        for (int at = goal; at != start; at = previous[at]) result.add(new Cell(at % WIDTH, at / WIDTH));
        result.add(new Cell(sx, sy));
        Collections.reverse(result);
        return List.copyOf(result);
    }
}
