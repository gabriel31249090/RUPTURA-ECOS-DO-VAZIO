package ruptura.world;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Gerador pronto para futuras masmorras; não substitui a rua autoral do prólogo. */
public final class DungeonGenerator {
    private DungeonGenerator() { }

    public record Room(int x, int y, int width, int height) {
        public Cell center() { return new Cell(x + width / 2, y + height / 2); }
    }
    public static final class Dungeon {
        private final boolean[][] floor;
        private final List<Room> rooms;
        private Dungeon(boolean[][] floor, List<Room> rooms) {
            this.floor = floor;
            this.rooms = List.copyOf(rooms);
        }
        public int width() { return floor.length; }
        public int height() { return floor[0].length; }
        public boolean walkable(int x, int y) {
            return x >= 0 && y >= 0 && x < width() && y < height() && floor[x][y];
        }
        public List<Room> rooms() { return rooms; }
        public Cell entrance() { return rooms.get(0).center(); }
        public Cell exit() { return rooms.get(rooms.size() - 1).center(); }
        public boolean connected() {
            boolean[][] seen = new boolean[width()][height()];
            ArrayDeque<Cell> queue = new ArrayDeque<>();
            queue.add(entrance());
            seen[entrance().x()][entrance().y()] = true;
            int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};
            while (!queue.isEmpty()) {
                Cell cell = queue.remove();
                for (int[] dir : dirs) {
                    int x = cell.x() + dir[0], y = cell.y() + dir[1];
                    if (walkable(x,y) && !seen[x][y]) {
                        seen[x][y] = true;
                        queue.add(new Cell(x,y));
                    }
                }
            }
            for (int x = 0; x < width(); x++)
                for (int y = 0; y < height(); y++)
                    if (floor[x][y] && !seen[x][y]) return false;
            return true;
        }
    }

    public static Dungeon generate(long seed, int width, int height) {
        if (width < 12 || height < 12 || width > 256 || height > 256)
            throw new IllegalArgumentException("Dimensões da masmorra devem estar entre 12 e 256");
        Random random = new Random(seed);
        boolean[][] floor = new boolean[width][height];
        List<Room> rooms = new ArrayList<>();
        int count = Math.max(4, Math.min(18, width * height / 80));
        for (int index = 0; index < count; index++) {
            int rw = 3 + random.nextInt(Math.min(5, width - 5));
            int rh = 3 + random.nextInt(Math.min(5, height - 5));
            Room room = new Room(1 + random.nextInt(width - rw - 1),
                    1 + random.nextInt(height - rh - 1), rw, rh);
            for (int x = room.x(); x < room.x() + rw; x++)
                for (int y = room.y(); y < room.y() + rh; y++) floor[x][y] = true;
            if (!rooms.isEmpty()) {
                Cell from = rooms.get(rooms.size() - 1).center(), to = room.center();
                int x = from.x(), y = from.y();
                while (x != to.x()) { floor[x][y] = true; x += Integer.signum(to.x() - x); }
                while (y != to.y()) { floor[x][y] = true; y += Integer.signum(to.y() - y); }
                floor[x][y] = true;
            }
            rooms.add(room);
        }
        return new Dungeon(floor, rooms);
    }
}
