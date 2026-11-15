package game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

public class CombatRulesTest {

    private int currentHp;
    private int maxHp;

    @BeforeEach
    public void setUp() {
        currentHp = 50;
        maxHp = 100;
    }

    @Test
    public void calculateNewHpValidTest() {
        
        currentHp = CombatRules.calculateNewHp(currentHp, maxHp, -49);
        assertEquals(1, currentHp);
        assertFalse(CombatRules.isDead(currentHp));
        // should be 1 and alive

        currentHp = CombatRules.calculateNewHp(currentHp, maxHp, 40);
        assertEquals(41, currentHp);
        assertFalse(CombatRules.isDead(currentHp));
        // should be 90 and alive
    }


    @Test
    public void calculateNewHpExceedMaxTest() {

        assertEquals(100, CombatRules.calculateNewHp(currentHp, maxHp, 50));
        // should remain 100 because max hp cannot exceed 100
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, -50})
    public void isDeadTest(int currentHp) {
        assertTrue(CombatRules.isDead(currentHp));
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 2, 99, 100})
    public void isNotDeadTest(int currentHp) {
        assertFalse(CombatRules.isDead(currentHp));
    }

}
