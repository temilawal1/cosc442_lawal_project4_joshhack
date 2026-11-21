package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class WorldTest {

    private Tile[][][] tiles;
    private World world;

    @BeforeEach
    public void setUp() {
        tiles = new Tile[3][3][2];
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                for (int z = 0; z < 2; z++) {
                    tiles[x][y][z] = Tile.FLOOR;
                }
            }
        }
        world = new World(tiles);
    }

    @Test
    public void constructorExposesWorldDimensions() {
        assertEquals(3, world.width());
        assertEquals(3, world.height());
        assertEquals(2, world.depth());
    }

    @Test
    public void creatureAndItemLookupReturnPresentObjectsAndNullWhenAbsent() {
        Creature creature = new Creature(world, '@', Color.WHITE, "player", 100, 5, 0);
        Item item = new Item('!', Color.RED, "potion", null);
        creature.x = 1;
        creature.y = 1;
        creature.z = 0;

        world.add(creature);
        world.addAtEmptyLocation(item, 0);

        assertSame(creature, world.creature(1, 1, 0));
        assertNull(world.creature(0, 0, 0));
        assertTrue(worldContainsItem(item));
        assertNull(world.item(2, 2, 1));
    }

    @Test
    public void tileReturnsConfiguredTilesAndBoundsOutsideWorld() {
        tiles[0][0][0] = Tile.WALL;

        assertEquals(Tile.WALL, world.tile(0, 0, 0));
        assertEquals(Tile.FLOOR, world.tile(2, 2, 1));
        assertEquals(Tile.BOUNDS, world.tile(-1, 0, 0));
        assertEquals(Tile.BOUNDS, world.tile(3, 0, 0));
        assertEquals(Tile.BOUNDS, world.tile(0, -1, 0));
        assertEquals(Tile.BOUNDS, world.tile(0, 3, 0));
        assertEquals(Tile.BOUNDS, world.tile(0, 0, -1));
        assertEquals(Tile.BOUNDS, world.tile(0, 0, 2));
    }

    @Test
    public void itemLookupThrowsOutsideWorldArrayBounds() {
        assertThrows(IndexOutOfBoundsException.class, () -> world.item(-1, 0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> world.item(3, 0, 0));
        assertThrows(IndexOutOfBoundsException.class, () -> world.item(0, 0, 2));
    }

    @Test
    public void glyphAndColorPreferCreatureThenItemThenTile() {
        Creature creature = new Creature(world, '@', Color.BLUE, "player", 100, 5, 0);
        creature.x = 1;
        creature.y = 1;
        creature.z = 0;
        world.add(creature);

        Item item = new Item('!', Color.RED, "potion", null);
        world.addAtEmptyLocation(item, 1);

        assertEquals('@', world.glyph(1, 1, 0));
        assertEquals(Color.BLUE, world.color(1, 1, 0));

        tiles[2][2][1] = Tile.STAIRS_UP;
        assertEquals(item.glyph(), world.glyph(
                findItemX(item),
                findItemY(item),
                1));
        assertEquals(item.color(), world.color(
                findItemX(item),
                findItemY(item),
                1));
        assertEquals(Tile.STAIRS_UP.glyph(), world.glyph(2, 2, 1));
        assertEquals(Tile.STAIRS_UP.color(), world.color(2, 2, 1));
    }

    @Test
    public void digOnlyChangesDiggableWallTiles() {
        tiles[0][0][0] = Tile.WALL;
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }

        world.dig(0, 0, 0);
        world.dig(1, 1, 0);
        world.dig(-1, 0, 0);

        assertEquals(Tile.FLOOR, world.tile(0, 0, 0));
        assertEquals(Tile.FLOOR, world.tile(1, 1, 0));
    }

    @Test
    public void addAtEmptyLocationAddsCreatureToGroundWithoutAnExistingCreature() {
        tiles[0][0][0] = Tile.WALL;
        Creature creature = new Creature(world, 'm', Color.GREEN, "monster", 10, 2, 1);

        world.addAtEmptyLocation(creature, 0);

        assertTrue(creature.x >= 0 && creature.x < world.width());
        assertTrue(creature.y >= 0 && creature.y < world.height());
        assertEquals(0, creature.z);
        assertEquals(Tile.FLOOR, world.tile(creature.x, creature.y, 0));
        assertSame(creature, world.creature(creature.x, creature.y, 0));
    }

    @Test
    public void updateCallsUpdateOnEveryCreatureInWorld() {
        CountingCreature first = new CountingCreature(world);
        CountingCreature second = new CountingCreature(world);
        first.x = 0;
        first.y = 0;
        first.z = 0;
        second.x = 1;
        second.y = 1;
        second.z = 0;

        world.add(first);
        world.add(second);
        world.update();

        assertEquals(1, first.updateCount);
        assertEquals(1, second.updateCount);
    }

    @Test
    public void removeCreatureStopsItFromBeingFound() {
        Creature creature = new Creature(world, '@', Color.WHITE, "player", 100, 5, 0);
        creature.x = 1;
        creature.y = 1;
        creature.z = 0;
        world.add(creature);

        world.remove(creature);

        assertNull(world.creature(1, 1, 0));
    }

    @Test
    public void removeItemObjectAndRemoveByCoordinateClearItems() {
        Item first = new Item('%', Color.YELLOW, "bread", null);
        Item second = new Item('!', Color.RED, "potion", null);
        world.addAtEmptyLocation(first, 0);
        world.addAtEmptyLocation(second, 1);

        world.remove(first);
        assertFalse(worldContainsItem(first));

        int secondX = findItemX(second);
        int secondY = findItemY(second);
        int secondZ = findItemZ(second);
        world.remove(secondX, secondY, secondZ);
        assertNull(world.item(secondX, secondY, secondZ));
    }

    @Test
    public void addAtEmptyLocationPlacesItemOnGroundWithoutAnExistingItem() {
        tiles[0][0][0] = Tile.WALL;
        Item item = new Item(',', Color.YELLOW, "rock", null);

        world.addAtEmptyLocation(item, 0);

        assertTrue(worldContainsItem(item));
    }

    @Test
    public void addAtEmptySpacePlacesNullOrFindsNearbyEmptyFloor() {
        Item item = new Item('%', Color.YELLOW, "bread", null);

        assertTrue(world.addAtEmptySpace(null, 1, 1, 0));
        assertTrue(world.addAtEmptySpace(item, 1, 1, 0));
        assertSame(item, world.item(1, 1, 0));
    }

    @Test
    public void addAtEmptySpaceReturnsFalseWhenNoGroundOrEmptyCellIsAvailable() {
        Item blocked = new Item('!', Color.RED, "potion", null);
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tiles[x][y][0] = Tile.WALL;
            }
        }
        assertFalse(world.addAtEmptySpace(blocked, 1, 1, 0));

        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }
        Item existing = new Item('!', Color.RED, "existing", null);
        assertTrue(world.addAtEmptySpace(existing, 1, 1, 0));
        assertTrue(world.addAtEmptySpace(new Item('!', Color.RED, "neighbor", null), 1, 1, 0));
    }

    @Test
    public void addDirectlyRegistersCreature() {
        Creature creature = new Creature(world, 'm', Color.GREEN, "monster", 10, 2, 1);
        creature.x = 2;
        creature.y = 2;
        creature.z = 1;

        world.add(creature);

        assertSame(creature, world.creature(2, 2, 1));
    }

    private boolean worldContainsItem(Item item) {
        for (int x = 0; x < world.width(); x++) {
            for (int y = 0; y < world.height(); y++) {
                for (int z = 0; z < world.depth(); z++) {
                    if (world.item(x, y, z) == item)
                        return true;
                }
            }
        }
        return false;
    }

    private int findItemX(Item item) {
        for (int x = 0; x < world.width(); x++) {
            for (int y = 0; y < world.height(); y++) {
                for (int z = 0; z < world.depth(); z++) {
                    if (world.item(x, y, z) == item)
                        return x;
                }
            }
        }
        return -1;
    }

    private int findItemY(Item item) {
        for (int x = 0; x < world.width(); x++) {
            for (int y = 0; y < world.height(); y++) {
                for (int z = 0; z < world.depth(); z++) {
                    if (world.item(x, y, z) == item)
                        return y;
                }
            }
        }
        return -1;
    }

    private int findItemZ(Item item) {
        for (int x = 0; x < world.width(); x++) {
            for (int y = 0; y < world.height(); y++) {
                for (int z = 0; z < world.depth(); z++) {
                    if (world.item(x, y, z) == item)
                        return z;
                }
            }
        }
        return -1;
    }

    private static class CountingCreature extends Creature {
        private int updateCount;

        CountingCreature(World world) {
            super(world, 'c', Color.CYAN, "counter", 10, 1, 1);
        }

        @Override
        public void update() {
            updateCount++;
        }
    }
}
