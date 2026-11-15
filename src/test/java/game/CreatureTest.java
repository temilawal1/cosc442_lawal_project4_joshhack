package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
        Tile[][][] tiles = new Tile[1][1][1];
        tiles[0][0][0] = Tile.FLOOR;

        world = new World(tiles);

        creature = new Creature(world, '@', Color.WHITE, "creature", 100, 5, 0);

        creature.x = 0;
        creature.y = 0;
        creature.z = 0;
    }

    @Test
    public void modifyHpCreatureIsNotDeadTest() {

        creature.modifyHp(0, "a feather touched the user");
        assertEquals(100, creature.hp());

        creature.modifyHp(-99, "almost died to a fall");
        assertEquals(1, creature.hp());
        
    }

    @Test
    public void modifyHpCreatureIsDeadTest() {
        creature.modifyHp(-100, "died to a fall");
        assertEquals(0, creature.hp());

    }
}
