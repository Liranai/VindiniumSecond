
package vindinium;

import java.awt.Point;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import vindinium.Board.Tile;

public class KnowledgeCollection {

	Vector<Point> mines;
	Vector<Point> taverns;
	Tile[][] tiles;

	Set<Pair<Point, Hero>> heroes;

	public KnowledgeCollection (KnowledgeCollection prevKnowledge, State state) {
		heroes = new HashSet<Pair<Point, Hero>>();
		mines = new Vector<Point>();
		taverns = new Vector<Point>();
		tiles = new Tile[state.game.board.tiles.length][state.game.board.tiles[0].length];
		for (int i = 0; i < state.game.board.tiles.length; i++) {
			for (int j = 0; j < state.game.board.tiles[0].length; j++) {
				if (!RandomBot.freeTiles.contains(state.game.board.tiles[i][j])) {
					if (state.game.board.tiles[i][j].toString().contains("$")) {
						mines.add(new Point(i, j));
						tiles[i][j] = Tile.FREE_MINE;
					} else {
						if (state.game.board.tiles[i][j].getHeroId() > -1) {
							Hero hero = state.game.heroes.get(state.game.board.tiles[i][j].getHeroId() - 1);
							heroes.add(new Pair<Point, Hero>(hero.getLocation(), hero));
						}
						tiles[i][j] = Tile.AIR;
					}
				} else {
					if (state.game.board.tiles[i][j] == Tile.FREE_MINE)
						mines.add(new Point(i, j));
					else if (state.game.board.tiles[i][j] == Tile.TAVERN) taverns.add(new Point(i, j));
					tiles[i][j] = state.game.board.tiles[i][j];
				}
			}
		}
	}
}

class Pair<A, B> {
	private A first;
	private B second;

	public Pair (A first, B second) {
		super();
		this.first = first;
		this.second = second;
	}

	public int hashCode () {
		int hashFirst = first != null ? first.hashCode() : 0;
		int hashSecond = second != null ? second.hashCode() : 0;

		return (hashFirst + hashSecond) * hashSecond + hashFirst;
	}

	public boolean equals (Object other) {
		if (other instanceof Pair) {
			Pair otherPair = (Pair)other;
			return ((this.first == otherPair.first || (this.first != null && otherPair.first != null && this.first
				.equals(otherPair.first))) && (this.second == otherPair.second || (this.second != null && otherPair.second != null && this.second
				.equals(otherPair.second))));
		}

		return false;
	}

	public String toString () {
		return "(" + first + ", " + second + ")";
	}

	public A getFirst () {
		return first;
	}

	public void setFirst (A first) {
		this.first = first;
	}

	public B getSecond () {
		return second;
	}

	public void setSecond (B second) {
		this.second = second;
	}
}
