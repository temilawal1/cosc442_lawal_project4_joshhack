package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.awt.Color;

public class CreatureTest {
    // starting hp, amount, expected hp, whether death path executes, why test is
    // needed

    private Creature creature;
    private World world;

    @BeforeEach
    public void setUp() {
        Tile[][][] tiles = new Tile[3][3][1];
        for (int x = 0; x < tiles.length; x++) {
            for (int y = 0; y < tiles[x].length; y++) {
                tiles[x][y][0] = Tile.FLOOR;
            }
        }

        world = new World(tiles);

        creature = new Creature(world, '@', Color.WHITE, "creature", 100, 5, 0);

        creature.x = 1;
        creature.y = 1;
        creature.z = 0;
        world.add(creature);
        new CreatureAi(creature);
    }

    @Test
    public void modifyHpCreatureIsNotDeadTest() {

        creature.modifyHp(0, "a feather touched the user");
        assertEquals(100, creature.hp());

        creature.modifyHp(-99, "almost died to a fall");
        assertEquals(1, creature.hp());
        assertEquals(creature, world.creature(1, 1, 0));
        assertNull(world.item(1, 1, 0));
    }

    @Test
    public void modifyHpCreatureIsDeadTest() {
        Item carriedItem = new Item('!', Color.RED, "potion", null);
        creature.inventory().add(carriedItem);

        assertEquals(creature, world.creature(1, 1, 0));

        creature.modifyHp(-100, "died to a fall");
        assertEquals(0, creature.hp());
        assertEquals("died to a fall", creature.causeOfDeath());
        assertNull(world.creature(1, 1, 0));

        Item corpse = world.item(1, 1, 0);
        assertNotNull(corpse);
        assertEquals("creature corpse", corpse.name());
        assertEquals(500, corpse.foodValue());
        assertFalse(creature.inventory().contains(carriedItem));

        boolean carriedItemDropped = false;
        for (int x = 0; x < world.width(); x++) {
            for (int y = 0; y < world.height(); y++) {
                if (world.item(x, y, 0) == carriedItem) {
                    carriedItemDropped = true;
                }
            }
        }
        assertTrue(carriedItemDropped);
    }

    @Test
    public void meleeAttackDoesNotDamageWhenAttackEqualsDefenseTest() {
        Creature defender = new Creature(world, 'd', Color.RED, "defender", 100, 5, 5);
        defender.x = 2;
        defender.y = 1;
        defender.z = 0;
        world.add(defender);
        new CreatureAi(defender);

        creature.meleeAttack(defender);

        assertEquals(100, defender.hp());
    }

    @Test
    public void meleeAttackDoesNotDamageWhenAttackIsBelowDefenseTest() {
        Creature defender = new Creature(world, 'd', Color.RED, "defender", 100, 5, 6);
        defender.x = 2;
        defender.y = 1;
        defender.z = 0;
        world.add(defender);
        new CreatureAi(defender);

        creature.meleeAttack(defender);

        assertEquals(100, defender.hp());
    }
}
