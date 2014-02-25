
package vindinium;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/** Game board. */
public final class Board {
	// --- Properties ---

	/** Size (width or height, expect same for both) */
	public final int size;

	/** Block/tiles */
	public final Tile[/* x */][/* y */] tiles;

	// --- Constructors ---

	/** Bulk constructor. */
	public Board (final Tile[][] tiles) {
		if (tiles == null || tiles.length == 0) {
			throw new IllegalArgumentException();
		} // end of if

		// ---

		this.size = tiles[0].length;
		this.tiles = tiles;
	} // end of <init>

	// --- Object support ---

	/** {@inheritDoc} */
	public boolean equals (Object o) {
		if (o == null || !(o instanceof Board)) {
			return false;
		} // end of if

		final Board other = (Board)o;

		return new EqualsBuilder().append(this.size, other.size).append(this.tiles, other.tiles).isEquals();

	} // end of equals

	/** {@inheritDoc} */
	public int hashCode () {
		return new HashCodeBuilder(1, 3).append(this.size).append(this.tiles).toHashCode();

	} // end of hashCode

	/** {@inheritDoc} */
	public String toString () {
		return new ToStringBuilder(this).append("size", this.size).append("tiles", this.tiles).toString();

	} // end of toString

	// ---

	/** Returns a mine tile owner by specified |hero|.
	 * 
	 * @param hero Hero numeric ID (greater or equal to 0)
	 * @throws IllegalArgumentException if |hero| ID less than 0 */
	public static Tile Mine (final int hero) {
		if (hero < 0) throw new IllegalArgumentException();

		return new Tile("$" + hero);
	} // end of Mine

	/** Returns a hero tile owner by specified |id|.
	 * 
	 * @param id Play numeric ID (greater or equal to 0)
	 * @throws IllegalArgumentException if |id| ID less than 0 */
	public static Tile Hero (final int id) {
		if (id < 0) throw new IllegalArgumentException();

		return new Tile("@" + id);
	} // end of Hero

	/** Board block/tile. */
	public static final class Tile {
		// --- Well-know instances ---

		/** Air/empty block */
		public static final Tile AIR = new Tile("  ");

		/** Free mine (not owned) */
		public static final Tile FREE_MINE = new Tile("$-");

		public static final Tile TAVERN = new Tile("[]");
		public static final Tile WALL = new Tile("##");

		// --- Properties ---

		/** String representation */
		private final String repr;

		// --- Constructors ---

		/** Constructor from string |representation|. */
		private Tile (final String representation) {
			this.repr = representation;
		} // end of <init>

		/** Returns tiles matching given |representation|.
		 * @throws IllegalArgumentException if representation is not supported */
		public static Tile of (final String representation) {
			if ("  ".equals(representation)) {
				return AIR;
			} else if ("##".equals(representation)) {
				return WALL;
			} else if ("[]".equals(representation)) {
				return TAVERN;
			} else if ("$-".equals(representation)) {
				return FREE_MINE;
			} else if (representation.startsWith("$")) {
				try {
					return Mine(Integer.parseInt(representation.substring(1)));
				} catch (Exception e) {
					throw new IllegalArgumentException("Invalid mine tile: " + representation.substring(1));

				} // end of catch
			} else if (representation.startsWith("@")) {
				try {
					return Hero(Integer.parseInt(representation.substring(1)));
				} catch (Exception e) {
					throw new IllegalArgumentException("Invalid hero tile: " + representation.substring(1));

				} // end of catch
			} // end of else if

			throw new IllegalArgumentException("Unsupported tile: " + representation);

		} // end of valueOf

		// --- Object support ---

		/** {@inheritDoc} */
		public String toString () {
			return this.repr;
		}

		/** {@inheritDoc} */
		public boolean equals (Object o) {
			if (o == null || !(o instanceof Tile)) {
				return false;
			} // end of if

			final Tile other = (Tile)o;

			return ((this.repr == null && other.repr == null) || (this.repr.equals(other.repr)));

		} // end of equals

		/** {@inheritDoc} */
		public int hashCode () {
			return this.repr.hashCode();
		} // end of hashCode

		public boolean isHero () {
			if (this.repr.contains("@")) return true;
			return false;
		}

		public int getHeroMine() {
			return (this.repr.contains("$")) ? this.repr.charAt(1) - 48 : -1; 
		}

		public int getHeroId () {
			return (this.repr.contains("@")) ? this.repr.charAt(1) - 48 : -1;
		}
	} // end of class Tile
} // end of class Board
