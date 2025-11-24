package model;

/**
 * Enum representing available power-ups that can be purchased with points
 */
public enum PowerUp {
    ROW_CLEARER("Row Clearer", 0, "Clears the bottom 3 rows"),
    SLOW_MOTION("Slow Motion", 0, "Slows falling speed for 10 seconds"),
    BOMB_PIECE("Bomb Piece", 0, "Next piece explodes in 4x4 area on placement");

    private final String name;
    private final int cost;
    private final String description;

    PowerUp(String name, int cost, String description) {
        this.name = name;
        this.cost = cost;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public int getCost() {
        return cost;
    }

    public String getDescription() {
        return description;
    }
}

