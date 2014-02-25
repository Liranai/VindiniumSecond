package vindinium;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.tuple.ImmutablePair;

import vindinium.Board.Tile;

/**
 * Bot calculated randomly next move.
 */
public final class RandomBot implements Bot {
    // --- Shared ---

    /**
     * 'Free' tiles
     */
    static final List<Tile> freeTiles;

    static {
        final ArrayList<Tile> ts = new ArrayList<Tile>(3);
        ts.add(Tile.AIR);
        ts.add(Tile.FREE_MINE);
        ts.add(Tile.TAVERN);
        freeTiles = Collections.unmodifiableList(ts);
    }

    /**
     * {@inheritDoc}
     */
    public Direction nextMove(final State state) {
        final Hero played = state.hero();
        final ImmutablePair<Integer,Integer> pos = played.position;
        final int x = pos.left, y = pos.right;
        final int last = state.game.board.size - 1;
        final ArrayList<Direction> around = new ArrayList<Direction>();

        if (y < 0 || y > last || x < 0 || x > last) {
            throw new IllegalArgumentException("Invalid position:" + pos);
        } // end of if

        // ---

        if (y > 0 && freeTiles.contains(state.game.board.tiles[x][y-1])) {
            around.add(Direction.NORTH);
        } // end of if

        if (y < last && freeTiles.contains(state.game.board.tiles[x][y+1])) {
            around.add(Direction.SOUTH);
        } // end of if

        if (x > 0 && freeTiles.contains(state.game.board.tiles[x-1][y])) {
            around.add(Direction.WEST);
        } // end of if

        if (x < last && freeTiles.contains(state.game.board.tiles[x+1][y])) {
            around.add(Direction.EAST);
        } // end of if

        if (!around.isEmpty()) {
            java.util.Collections.shuffle(around); 
            return around.get(0); 
        } else return Direction.STAY;
    } // end of nextMove
} // end of class RandomBot
