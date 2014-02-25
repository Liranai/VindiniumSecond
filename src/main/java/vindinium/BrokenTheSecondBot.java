package vindinium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import vindinium.Board.Tile;

/**
 * Bot calculated randomly next move.
 */
public final class BrokenTheSecondBot implements Bot {
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

	private static KnowledgeCollection knowledge;
	DebugWindow window;

	public BrokenTheSecondBot () {
		super();
		knowledge = null;
	}

    /**
     * {@inheritDoc}
     */
    public Direction nextMove(final State state) {
		knowledge = new KnowledgeCollection(knowledge, state);
		
		if (state.game.turn < 4) {
			window = new DebugWindow();
		} else
			window.repaint(state);
   	 
   	 
   	 
   	 
   	 
   	 return Direction.STAY;
    } // end of nextMove

	public static KnowledgeCollection getKnowledge () {
		return knowledge;
	}
} // end of class RandomBot
