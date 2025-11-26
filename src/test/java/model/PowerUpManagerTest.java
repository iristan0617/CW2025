package model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PowerUpManagerTest {

    private PowerUpManager manager;

    @BeforeEach
    void setUp() {
        manager = new PowerUpManager();
    }

    @Test
    void awardSkillPointsAccumulatesFractionalValues() {
        manager.awardSkillPoints(5);
        assertEquals(0, manager.getSkillPoints(), "Less than 10 score should not award a point yet");

        manager.awardSkillPoints(5);
        assertEquals(1, manager.getSkillPoints(), "Two partial awards should roll up to 1 point");

        manager.awardSkillPoints(15);
        assertEquals(2, manager.getSkillPoints(), "Further fractional awards should continue accumulating");
    }

    @Test
    void purchaseAndUsePowerUpUpdatesInventory() {
        assertTrue(manager.purchasePowerUp(PowerUp.ROW_CLEARER), "Zero-cost power ups should always be purchasable");
        assertEquals(1, manager.getPowerUpQuantity(PowerUp.ROW_CLEARER));

        assertTrue(manager.usePowerUp(PowerUp.ROW_CLEARER), "Owned power up should be consumable");
        assertEquals(0, manager.getPowerUpQuantity(PowerUp.ROW_CLEARER));
        assertFalse(manager.usePowerUp(PowerUp.ROW_CLEARER), "Cannot use a power up that is no longer owned");
    }

    @Test
    void resetClearsInventoryAndSkillPoints() {
        manager.awardSkillPoints(40); // 4 points
        int beforePurchase = manager.getSkillPoints();
        manager.purchasePowerUp(PowerUp.BOMB_PIECE);
        assertEquals(1, manager.getPowerUpQuantity(PowerUp.BOMB_PIECE));
        assertEquals(beforePurchase - PowerUp.BOMB_PIECE.getCost(), manager.getSkillPoints());

        manager.reset();

        assertEquals(0, manager.getSkillPoints());
        for (PowerUp powerUp : PowerUp.values()) {
            assertEquals(0, manager.getPowerUpQuantity(powerUp), "Reset should clear inventory for " + powerUp);
        }
    }
}

