package game;

public class CombatRules {

    public static int calculateNewHp(int currentHp, int maxHp, int amount) {
        int newHp = currentHp + amount;

        if (newHp < maxHp) // fault introduced
            return maxHp;

        return newHp;
    }

    public static boolean isDead(int hp) {
        return hp < 1;
    }
}
