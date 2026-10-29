package ruptura.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class WorldMapTest {
    @Test void everyStoryLandmarkHasACardinalPathForTwoHundredSeeds() {
        int[][] points = {{3,4},{4,6},{7,7},{11,8},{15,11}};
        for (long seed = 0; seed < 200; seed++) {
            WorldMap world = new WorldMap(seed);
            for (int[] p : points) {
                var path = WorldMap.path(world,2,3,p[0],p[1]);
                assertFalse(path.isEmpty(), "Marco inacessível na semente " + seed);
                for (int i = 1; i < path.size(); i++) {
                    Cell a = path.get(i-1), b = path.get(i);
                    assertTrue(world.walkable(b.x(),b.y()));
                    assertEquals(1,Math.abs(a.x()-b.x())+Math.abs(a.y()-b.y()));
                }
            }
        }
    }

    @Test void buildingsAndBoundsBlockMovement() {
        WorldMap world = new WorldMap(42);
        assertFalse(world.walkable(-1,3));
        assertFalse(world.walkable(18,10));
        assertFalse(world.walkable(0,14));
        assertTrue(WorldMap.path(world,2,3,0,14).isEmpty());
    }

    @Test void abandonedVehicleBlocksItsCellWithoutClosingTheAvenue() {
        WorldMap world = new WorldMap(20410517L);
        assertFalse(world.walkable(5,5));
        var detour = WorldMap.path(world,4,5,6,5);
        assertFalse(detour.isEmpty());
        assertFalse(detour.contains(new Cell(5,5)));
        assertTrue(detour.size() > 3, "O caminho deve contornar o carro");
        for (int[] landmark : new int[][]{{3,4},{4,6},{7,7},{11,8},{15,11}})
            assertFalse(WorldMap.path(world,2,3,landmark[0],landmark[1]).isEmpty());
    }

    @Test void generationIsReproducible() {
        WorldMap first = new WorldMap(2041), second = new WorldMap(2041);
        for (int x = 0; x < WorldMap.WIDTH; x++)
            for (int y = 0; y < WorldMap.HEIGHT; y++) {
                assertEquals(first.tile(x,y),second.tile(x,y));
                assertEquals(first.elevation(x,y),second.elevation(x,y));
            }
    }
}
