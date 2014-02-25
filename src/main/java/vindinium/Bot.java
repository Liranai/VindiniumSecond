package vindinium;

/**
 * Bot contract.
 * @see Client#withServerUrl
 */
public interface Bot {
    
    /**
     * Returns direction of next move.
     * @param state Current state
     */
    public Direction nextMove(final State state);

} // end of interface Bot
