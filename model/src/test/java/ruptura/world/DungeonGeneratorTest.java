package ruptura.world;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DungeonGeneratorTest {
    @Test void allFloorAndExitsRemainConnectedForTwoHundredSeedsAndSizes() {
        for (long seed = 0; seed < 200; seed++) {
            var dungeon = DungeonGenerator.generate(seed,12+(int)(seed%28),12+(int)(seed%31));
            assertTrue(dungeon.connected(), "Piso desconectado na semente " + seed);
            assertTrue(dungeon.walkable(dungeon.entrance().x(),dungeon.entrance().y()));
            assertTrue(dungeon.walkable(dungeon.exit().x(),dungeon.exit().y()));
            for (int x = 0; x < dungeon.width(); x++) {
                assertFalse(dungeon.walkable(x,0));
                assertFalse(dungeon.walkable(x,dungeon.height()-1));
            }
        }
    }

    @Test void sameSeedRecreatesRoomsAndEveryTile() {
        var a = DungeonGenerator.generate(9182,32,24);
        var b = DungeonGenerator.generate(9182,32,24);
        assertEquals(a.rooms(),b.rooms());
        for (int x = 0; x < a.width(); x++)
            for (int y = 0; y < a.height(); y++) assertEquals(a.walkable(x,y),b.walkable(x,y));
    }

    @Test void unreasonableDimensionsAreRejected() {
        assertThrows(IllegalArgumentException.class,()->DungeonGenerator.generate(1,5,5));
        assertThrows(IllegalArgumentException.class,()->DungeonGenerator.generate(1,10000,12));
    }
}
