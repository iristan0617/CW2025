package model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages power-up purchases and inventory
 */
public class PowerUpManager {
    private final Map<PowerUp, Integer> powerUpInventory; // PowerUp -> quantity owned
    private final IntegerProperty skillPoints;
    
    public PowerUpManager() {
        this.skillPoints = new SimpleIntegerProperty(0);
        this.powerUpInventory = new HashMap<>();
        
        // Initialize all power-ups with 0 quantity
        for (PowerUp powerUp : PowerUp.values()) {
            powerUpInventory.put(powerUp, 0);
        }
    }

    /**
     * Get current skill points
     */
    public IntegerProperty skillPointsProperty() {
        return skillPoints;
    }

    public int getSkillPoints() {
        return skillPoints.get();
    }

    // Track fractional skill points (to accumulate even small scores)
    private double fractionalSkillPoints = 0.0;
    
    /**
     * Award skill points (called when player earns score)
     * Convert score to skill points at a rate (1 skill point per 10 score)
     * Accumulates fractional points so even small scores contribute
     */
    public void awardSkillPoints(int scoreEarned) {
        // Add fractional points (0.1 skill point per 1 score)
        fractionalSkillPoints += scoreEarned / 10.0;
        
        // Convert fractional points to whole skill points
        int wholePoints = (int) fractionalSkillPoints;
        if (wholePoints > 0) {
            skillPoints.set(skillPoints.get() + wholePoints);
            fractionalSkillPoints -= wholePoints; // Keep the remainder
        }
    }

    /**
     * Purchase a power-up
     * @return true if purchase was successful
     */
    public boolean purchasePowerUp(PowerUp powerUp) {
        if (skillPoints.get() >= powerUp.getCost()) {
            skillPoints.set(skillPoints.get() - powerUp.getCost());
            powerUpInventory.put(powerUp, powerUpInventory.get(powerUp) + 1);
            return true;
        }
        return false;
    }

    /**
     * Use a power-up (consume one from inventory)
     * @return true if power-up was available and used
     */
    public boolean usePowerUp(PowerUp powerUp) {
        if (powerUpInventory.get(powerUp) > 0) {
            powerUpInventory.put(powerUp, powerUpInventory.get(powerUp) - 1);
            return true;
        }
        return false;
    }

    /**
     * Get quantity of a power-up owned
     */
    public int getPowerUpQuantity(PowerUp powerUp) {
        return powerUpInventory.get(powerUp);
    }

    /**
     * Reset all power-ups (for new game)
     */
    public void reset() {
        for (PowerUp powerUp : PowerUp.values()) {
            powerUpInventory.put(powerUp, 0);
        }
        skillPoints.set(0);
        fractionalSkillPoints = 0.0;
    }
}

