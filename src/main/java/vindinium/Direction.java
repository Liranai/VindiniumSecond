package vindinium;

/**
 * Play direction.
 */
public enum Direction {
    STAY("Stay"), NORTH("North"), SOUTH("South"), EAST("East"), WEST("West");

    // --- Properties ---

    /**
     * String representation
     */
    public final String name;

    private Direction(final String name) { this.name = name; }

    /**
     * Returns string representation/name.
     */
    public String toString() {
        return this.name;
    } // end of toString
} // end of enumeration Direction
        
