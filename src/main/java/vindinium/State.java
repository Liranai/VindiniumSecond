package vindinium;

import java.net.URL;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.EqualsBuilder;

/**
 * State/configuration of game on server.
 */
public final class State {
    // --- Properties ---

    /**
     * Game information
     */
    public final Game game;

    /**
     * ID of hero played by current client
     * @see Hero#id
     * @see Game#heroes
     */
    public final int heroId;

    /**
     * Client token
     */
    public final String token;

    /**
     * URL of view
     */
    public final URL viewUrl;

    /**
     * Play URL
     */
    public final URL playUrl;

    // --- Constructors ---

    /**
     * Initialize state.
     */
    public State(final Game game, final int heroId, final String token,
                 final URL viewUrl, final URL playUrl) {

        this.game = game;
        this.heroId = heroId;
        this.token = token;
        this.viewUrl = viewUrl;
        this.playUrl = playUrl;
    } // end of <init>

    // ---

    /**
     * Returns played hero.
     * @see #heroId
     * @see Game#heroes
     */
    public Hero hero() {
        return game.heroes.get(heroId-1);
    } // end of hero

    // --- Object support ---

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object o) {
        if (o == null || !(o instanceof State)) {
            return false;
        } // end of if

        final State other = (State) o;

        return new EqualsBuilder().
            append(this.game, other.game).
            append(this.heroId, other.heroId).
            append(this.token, other.token).
            append(this.viewUrl, other.viewUrl).
            append(this.playUrl, other.playUrl).
            isEquals();

    } // end of equals

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder(5, 9).
            append(this.game).append(this.heroId).append(this.token).
            append(this.viewUrl).append(this.playUrl).toHashCode();

    } // end of hashCode

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return new ToStringBuilder(this).
            append("game", this.game).
            append("heroId", this.heroId).
            append("token", this.token).
            append("viewUrl", this.viewUrl).
            append("playUrl", this.playUrl).
            toString();

    } // end of toString
} // end of class State
