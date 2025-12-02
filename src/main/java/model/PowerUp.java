package model;

/**
 * Enum representing available power-ups that can be purchased with skill points.
 * Each power-up has a name, cost, and description. Power-ups provide special
 * abilities to help the player during gameplay.
 * 
 * @author COMP2042 Coursework
 */
public enum PowerUp {
    ROW_CLEARER("Row Clearer", 0, "Clears the bottom 3 rows"),
    SLOW_MOTION("Slow Motion", 0, "Slows falling speed for 10 seconds"),
    BOMB_PIECE("Bomb Piece", 0, "Next piece explodes in 4x4 area on placement");

    private final String name;
    private final int cost;
    private final String description;

    /**
     * Constructs a PowerUp enum value.
     * 
     * @param name the display name of the power-up
     * @param cost the skill points cost to purchase (currently 0 for all)
     * @param description the description of what the power-up does
     */
    PowerUp(String name, int cost, String description) {
        this.name = name;
        this.cost = cost;
        this.description = description;
    }

    /**
     * Gets the display name of the power-up.
     * 
     * @return the power-up name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the skill points cost to purchase this power-up.
     * 
     * @return the cost in skill points
     */
    public int getCost() {
        return cost;
    }

    /**
     * Gets the description of what this power-up does.
     * 
     * @return the power-up description
     */
    public String getDescription() {
        return description;
    }
}

