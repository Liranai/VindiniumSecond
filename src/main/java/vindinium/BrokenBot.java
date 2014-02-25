
package vindinium;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

import lombok.Getter;
import lombok.Setter;

import org.apache.commons.lang3.tuple.ImmutablePair;

import vindinium.Board.Tile;
import vindinium.Node.Status;

/** Bot calculated randomly next move. */
public final class BrokenBot implements Bot {
	// --- Shared ---

	/** 'Free' tiles */
	static final List<Tile> freeTiles;

	static {
		final ArrayList<Tile> ts = new ArrayList<Tile>(3);
		ts.add(Tile.AIR);
		ts.add(Tile.FREE_MINE);
		ts.add(Tile.TAVERN);
		freeTiles = Collections.unmodifiableList(ts);
	}

	public final static int[][] DIRECTIONS = new int[][] { {-1, 1, 0, 0}, {0, 0, -1, 1}};

	private Vector<Point> mines;
	private Vector<Point> taverns;

	private DataCollection data;
	
	private Node[][] dataMap;
	private PriorityQueue<Node> openList;

	private Node[][] dataMap2;
	private PriorityQueue<Node> openList2;

	private Vector<Point> path;
	private Point destination;

	public BrokenBot () {
		super();
		data = new DataCollection();
		openList = new PriorityQueue<Node>(2, new NodeComparator());
	}

	/** {@inheritDoc} */
	public Direction nextMove (final State state) {
		Point position = new Point(state.hero().position.left, state.hero().position.right);

		if (state.game.turn < 4) {
			saveMap(state);
			data.make(state);
		}

		data.update(state);
		/*
		 * System.out.print("[Owned Mines: " + data.getMinesOwned() + "][Mines per Hero: {"); for (int i = 0; i <
		 * data.getMinesPerHero().length; i++) { System.out.print(i + "->" + data.getMinesPerHero()[i] + "},{"); }
		 * System.out.print("}\n");
		 */
/*
 * path = null; destination = closestMine(position, state); if (destination == null || state.hero().life < 35) { destination =
 * closestTavern(position, state); } if (destination == null) destination = position;
 */
		// }

		/*
		 * System.out.println("[Hero at: " + position.x + "," + position.y + "] [Destination: " + destination.x + "," +
		 * destination.y + "] [Turn: " + state.game.turn + "]");
		 */

		destination = data.getDestination(state, this);
		if (destination == null) destination = position;// return randomMove(state);

		Direction dir = findMove(position, destination, state);
		System.out.println("[Going: " + dir + " to " + destination.x + "," + destination.y + "]");
		return dir; // findMove(position, destination, state);
	} // end of nextMove

	public Direction randomMove (State state) {
		final Hero played = state.hero();
		final ImmutablePair<Integer, Integer> pos = played.position;
		final int x = pos.left, y = pos.right;
		final int last = state.game.board.size - 1;
		final ArrayList<Direction> around = new ArrayList<Direction>();

		if (y < 0 || y > last || x < 0 || x > last) {
			throw new IllegalArgumentException("Invalid position:" + pos);
		} // end of if

		// ---

		if (y > 0 && freeTiles.contains(state.game.board.tiles[x][y - 1])) {
			around.add(Direction.NORTH);
		} // end of if

		if (y < last && freeTiles.contains(state.game.board.tiles[x][y + 1])) {
			around.add(Direction.SOUTH);
		} // end of if

		if (x > 0 && freeTiles.contains(state.game.board.tiles[x - 1][y])) {
			around.add(Direction.WEST);
		} // end of if

		if (x < last && freeTiles.contains(state.game.board.tiles[x + 1][y])) {
			around.add(Direction.EAST);
		} // end of if

		if (!around.isEmpty()) {
			java.util.Collections.shuffle(around);
			return around.get(0);
		} else
			return Direction.STAY;
	}


	private Direction findMove (Point position, Point destination, State state) {
		if (getManhattanDistance(position, destination) == 0)
			return Direction.STAY;
		if (getManhattanDistance(position, destination) == 1) {
			path = null;
			if (destination.x < position.x) return Direction.NORTH;
			if (destination.x > position.x) return Direction.SOUTH;
			if (destination.y < position.y) return Direction.WEST;
			if (destination.y > position.y) return Direction.EAST;
		}

		path = createPath(position, destination, state);

		Point n = path.remove(path.size() - 1);
		if (n.x == position.x && n.y == position.y) {
			n = path.remove(path.size() - 1);
		}
		if (n.x < position.x) return Direction.NORTH;
		if (n.x > position.x) return Direction.SOUTH;
		if (n.y < position.y) return Direction.WEST;
		if (n.y > position.y) return Direction.EAST;
		return Direction.STAY;
		// return randomMove(position, state);
	}

	private Vector<Point> createPath (Point position, Point destination, State state) {
		openList = new PriorityQueue<Node>(2, new NodeComparator());
		dataMap = new Node[state.game.board.tiles.length][state.game.board.tiles[0].length];
		for (int i = 0; i < state.game.board.tiles.length; i++)
			for (int j = 0; j < state.game.board.tiles[0].length; j++) {
				Node n = new Node(i, j);
				n.setH(getManhattanDistance(new Point(i, j), destination));
				dataMap[i][j] = n;
				if (state.game.board.tiles[i][j] == Tile.WALL && state.game.board.tiles[i][j] != Board.Hero(state.heroId)
					&& (i != destination.x && j != destination.y)) {
					n.status = Status.closedNode;
				}
			}

		openList.add(dataMap[position.x][position.y]);
		Node n = openList.poll();
		while (!((n.x == destination.x - 1 && n.y == destination.y) || (n.x == destination.x + 1 && n.y == destination.y)
			|| (n.x == destination.x && n.y == destination.y - 1) || (n.x == destination.x && n.y == destination.y + 1))) {
			assignNeighbours(n, state, destination);
			n = openList.poll();
		}
		;

		Vector<Point> path = new Vector<Point>();
		do {
			path.add(new Point(n.x, n.y));
			n = n.getParent();
		} while (n != null);
		return path;
	}

	private void assignNeighbours (Node n, State state, Point destination) {
		n.status = Status.closedNode;
		Node temp = null;

		for (int i = 0; i < 4; i++) {
			int y = n.y + DIRECTIONS[0][i];
			int x = n.x + DIRECTIONS[1][i];

			if (x == -1 || y == -1 || x == state.game.board.tiles[0].length || y == state.game.board.tiles.length) continue;

			if (state.game.board.tiles[x][y] != Tile.AIR && state.game.board.tiles[x][y] != Board.Hero(state.heroId)) {
				continue;
			}

			temp = dataMap[x][y];

			if (temp.status != Status.unexploredNode) if (temp.g <= n.g + 1) continue;

			temp.setParent(n);
			temp.g = n.g + ((state.game.board.tiles[x][y].getHeroId() > -1) ? 2 : 1);
			temp.f = temp.g + temp.h;

			if (temp.status != Status.unexploredNode) {
				openList.remove(temp);
				openList.add(temp);
			} else {
				temp.status = Status.openNode;
				openList.add(temp);
			}
		}
	}

	private Vector<Point> createPathIgnoreHeroes (Point position, Point destination, State state) {
		openList2 = new PriorityQueue<Node>(2, new NodeComparator());
		dataMap2 = new Node[state.game.board.tiles.length][state.game.board.tiles[0].length];
		for (int i = 0; i < state.game.board.tiles.length; i++)
			for (int j = 0; j < state.game.board.tiles[0].length; j++) {
				Node n = new Node(i, j);
				n.setH(getManhattanDistance(new Point(i, j), destination));
				dataMap2[i][j] = n;
				if (state.game.board.tiles[i][j] == Tile.WALL
					&& !(state.game.board.tiles[i][j] == Board.Hero(1) || state.game.board.tiles[i][j] == Board.Hero(2)
						|| state.game.board.tiles[i][j] == Board.Hero(3) || state.game.board.tiles[i][j] == Board.Hero(0))
					&& (i != destination.x && j != destination.y)) {
					n.status = Status.closedNode;
				}
			}

		try {
			openList2.add(dataMap2[position.x][position.y]);
		} catch (NullPointerException e) {
			return null;
		}
		Node n = openList2.poll();
		while (!((n.x == destination.x - 1 && n.y == destination.y) || (n.x == destination.x + 1 && n.y == destination.y)
			|| (n.x == destination.x && n.y == destination.y - 1) || (n.x == destination.x && n.y == destination.y + 1))) {
			assignNeighboursIgnoreHeroes(n, state, destination);
			n = openList2.poll();
			if (n == null) return null;
		}
		;

		Vector<Point> path = new Vector<Point>();
		do {
			path.add(new Point(n.x, n.y));
			n = n.getParent();
		} while (n != null);
		return path;
	}

	private void assignNeighboursIgnoreHeroes (Node n, State state, Point destination) {
		n.status = Status.closedNode;
		Node temp = null;

		for (int i = 0; i < 4; i++) {
			int y = n.y + DIRECTIONS[0][i];
			int x = n.x + DIRECTIONS[1][i];

			if (x == -1 || y == -1 || x == state.game.board.tiles[0].length || y == state.game.board.tiles.length) continue;

			if (state.game.board.tiles[x][y] != Tile.AIR && state.game.board.tiles[x][y] != Board.Hero(state.heroId)) {
				continue;
			}

			temp = dataMap2[x][y];

			if (temp.status != Status.unexploredNode) if (temp.g <= n.g + 1) continue;

			temp.setParent(n);
			temp.g = n.g + 1;
			temp.f = temp.g + temp.h;

			if (temp.status != Status.unexploredNode) {
				openList2.remove(temp);
				openList2.add(temp);
			} else {

				openList2.add(temp);
				temp.status = Status.openNode;
			}
		}
	}

	private Point closestMine (Point position, State state) {
		int distance = Integer.MAX_VALUE;
		Point p = null;
		for (Point pt : mines) {
			if (!state.game.board.tiles[pt.x][pt.y].toString().contains(Board.Mine(state.heroId).toString())) {
				int tempDis = getDistance(position, pt, state);
				if (tempDis < distance) {
					distance = tempDis;
					p = pt;
				}
			}
		}
		return p;
	}

	private Point closestTavern (Point position, State state) {
		int distance = Integer.MAX_VALUE;
		Point p = null;
		for (Point pt : taverns) {
			int tempDis = getDistance(position, pt, state);
			if (tempDis < distance) {
				distance = tempDis;
				p = pt;
			}
		}
		return p;
	}

	public int getManhattanDistance (Point p1, Point p2) {
		return (Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y));
	}

	public int getDistance (Point p1, Point p2, State state) {
		// return (Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y));
		Vector<Point> path = createPathIgnoreHeroes(p1, p2, state);
		if (path != null) return path.size();
		return Integer.MAX_VALUE;
	}

	private void saveMap (State state) {
		mines = new Vector<Point>();
		taverns = new Vector<Point>();
		for (int i = 0; i < state.game.board.size; i++) {
			for (int j = 0; j < state.game.board.size; j++) {
				if (state.game.board.tiles[i][j] == Tile.FREE_MINE) mines.add(new Point(i, j));
				if (state.game.board.tiles[i][j] == Tile.TAVERN) taverns.add(new Point(i, j));
			}
		}
	}

} // end of class RandomBot

@Getter
@Setter
class Node extends Object {

	public enum Status {
		openNode, closedNode, unexploredNode;
	}

	public int x, y;
	public int g = Integer.MAX_VALUE, h = 0, f = Integer.MAX_VALUE;
	public Status status = Status.unexploredNode;
	private Node parent;

	public Node (Point p) {
		super();
		this.x = p.x;
		this.y = p.y;
	}

	public Node (int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals (Object obj) {
		if (x == ((Node)obj).x && y == ((Node)obj).y) return true;
		return false;
	}
}

class NodeComparator implements Comparator<Node> {

	public int compare (Node obj1, Node obj2) {
		double c1 = obj1.f;
		double c2 = obj2.f;

		if (c1 > c2) return 1;
		if (c1 < c2)
			return -1;
		else
			return 0;
	}
}
