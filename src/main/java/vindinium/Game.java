package vindinium;

import java.util.List;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * Game information.
 */
public final class Game {
    // --- Properties ---

    /**
     * Game ID
     */
    public final String id;

    /**
     * Count of passed turns
     */
    public final int turn;

    /**
     * Max count of turn
     */
    public final int maxTurns;

    /**
     * Is game finished?
     */
    public final boolean finished;

    /**
     * List of game heroes
     */
    public final List<Hero> heroes;

    /**
     * Board of game
     */
    public final Board board;

    // --- Constructors ---

    /**
     * Initialize game
     */
    public Game(final String id, final int turn, final int maxTurns,
                final boolean finished, final List<Hero> heroes,
                final Board board) {

        this.id = id;
        this.turn = turn;
        this.maxTurns = maxTurns;
        this.finished = finished;
        this.heroes = heroes;
        this.board = board;
    } // end of <init>

    // --- Object support ---

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof Game)) {
            return false;
        } // end of if

        // ---

        final Game other = (Game) o;

        return new EqualsBuilder().
            append(this.id, other.id).
            append(this.turn, other.turn).
            append(this.maxTurns, other.maxTurns).
            append(this.finished, other.finished).
            append(this.heroes, other.heroes).
            append(this.board, other.board).
            isEquals();

    } // end of equals

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder(3, 7).
            append(this.id).append(this.turn).append(this.maxTurns).
            append(this.finished).append(this.heroes).append(this.board).
            toHashCode();

    } // end of hashCode

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("id", this.id).
            append("turn", this.turn).
            append("maxTurns", this.maxTurns).
            append("finished", this.finished).
            toString();

    } // end of toString
} // end of class Game
