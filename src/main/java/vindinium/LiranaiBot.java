
package vindinium;

import java.awt.Point;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Vector;

import org.apache.commons.lang3.tuple.ImmutablePair;

import vindinium.Board.Tile;
import vindinium.Node.Status;

/** Bot calculated randomly next move. */
public final class LiranaiBot implements Bot {
	// --- Shared ---

	/** 'Free' tiles */
	static final List<Tile> freeTiles;
	private Vector<Point> mines;
	private Vector<Point> taverns;
	private DebugWindow window;

	public final static int[][] DIRECTIONS = new int[][] { {-1, 1, 0, 0}, {0, 0, -1, 1}};
	private Point enroute;
	private Point at;
	private Vector<Node> path;

	static {
		final ArrayList<Tile> ts = new ArrayList<Tile>(3);
		ts.add(Tile.AIR);
		ts.add(Tile.FREE_MINE);
		ts.add(Tile.TAVERN);
		freeTiles = Collections.unmodifiableList(ts);
	}

	/** {@inheritDoc} */
	public Direction nextMove (final State state) {
		final Hero played = state.hero();
		final ImmutablePair<Integer, Integer> pos = played.position;
		final Point position = new Point(pos.left, pos.right);
		final int x = pos.left, y = pos.right;
		final int last = state.game.board.size;

		if (state.game.turn < 4) {
			saveMap(state);
		}

		if (state.game.turn > 4) {
			// window.repaint();
		}

		if (enroute != null) {
			if (!state.game.board.tiles[enroute.x][enroute.y].toString().contains(state.game.board.Mine(state.heroId).toString())) {
				return getStep(position, enroute, state);
			} else {
				enroute = null;
				path = null;
			}
		}

		int distance = Integer.MAX_VALUE;
		Point closestMine = null;
		System.out.println("Mines: " + mines.size());
		for (Point p : mines) {
			if (/* state.game.board.tiles[p.x][p.y].toString().contains(state.game.board.Mine(state.heroId).toString()) */
			state.game.board.tiles[p.x][p.y] != Tile.FREE_MINE)
				continue;
			else if (distance >= getDistance(p, position)) {
				closestMine = p;
				distance = getDistance(p, position);
			}
		}

		enroute = closestMine;

		return getStep(position, closestMine, state);
	}

	private Direction getStep (Point position, Point destination, State state) {
		System.out.println("At: " + position + " Destination: " + destination);


		if (getDistance(position, destination) == 1) {
			path = null;
			enroute = null;
			if (destination.x < position.x) return Direction.NORTH;
			if (destination.x > position.x) return Direction.SOUTH;
			if (destination.y < position.y) return Direction.WEST;
			if (destination.y > position.y) return Direction.EAST;
		}

		// if (path == null || path.size() == 0)
		makeRoute(position, destination, state);
		for (Node n : path) {
			System.out.print("Going to: " + n.x + "," + n.y);
			System.out.print(state.game.board.tiles[n.x][n.y] + "\n");
		}

		// System.out.println(path.size());
		Node n = path.remove(path.size() - 1);
		
		// System.out.print("Going to: " + n.x + "," + n.y);
		// System.out.print(state.game.board.tiles[n.x][n.y] + "\n");

		if (n.x < position.x) return Direction.NORTH;
		if (n.x > position.x) return Direction.SOUTH;
		if (n.y < position.y) return Direction.WEST;
		if (n.y > position.y) return Direction.EAST;

		return Direction.STAY;
		/*
		 * if (path == null) path = makeRoute(position, destination, state); Node n = path.remove(path.size() - 1);
		 * 
		 * if (n.x > position.x) return Direction.SOUTH; if (n.x < position.y) return Direction.NORTH; if (n.y > position.y) return
		 * Direction.EAST; else return Direction.WEST;
		 */
	}

	private Vector<Node> makeRoute (Point position, Point destination, State state) {
		Node n = new Node(position);
		Vector<Node> nodes = new Vector<Node>();
		PriorityQueue<Node> openList = new PriorityQueue<Node>(10, new NodeComparator());

		nodes.add(n);
		do {
			assignNeighbours(n, destination, state, nodes, openList);
			n = openList.poll();
		} while ((n.x != destination.x - 1 && n.y != destination.y) && (n.x != destination.x + 1 && n.y != destination.y)
			&& (n.x != destination.x && n.y != destination.y - 1) && (n.x != destination.x && n.y != destination.y + 1));

		Node temp = n;
		path = new Vector<Node>();
		
		do{
			path.add(temp);
			temp = temp.getParent();
		} while (temp != null);
		
		path.remove(path.size() - 1);
		
		return path;
	}

	private void assignNeighbours (Node n, Point destination, State state, Vector<Node> nodes, PriorityQueue<Node> openList) {
		n.status = Status.closedNode;
		Node temp = null;
		
		for (int i = 0; i < 4; i++) {
			int x = n.x + DIRECTIONS[0][i];
			int y = n.y + DIRECTIONS[1][i];
			
			if (x == -1 || y == -1 || x == state.game.board.tiles[0].length || y == state.game.board.tiles.length) continue;

			if (state.game.board.tiles[x][y] != Tile.AIR && !(n.x == destination.x && n.y == destination.y)) continue;

			temp = new Node(x, y);
			int m = nodes.indexOf(temp);
			if (m >= 0) {
				temp = nodes.remove(m);
			}

			if (temp.status != Status.unexploredNode) if (temp.g <= n.g + 1) continue;

			temp.setParent(n);
			temp.g = n.g + 1;
			temp.f = temp.g;

			if (temp.status != Status.unexploredNode) {
				openList.remove(temp);
				openList.add(temp);
			} else {
				openList.add(temp);
				temp.status = Status.openNode;
			}
			nodes.add(temp);
		}
	}

	private Hero closeEnemy (State state) {
		System.out.println((int)state.heroId);
		for (int i = 0; i < state.game.heroes.size(); i++) {
			if (i != state.heroId - 1)
				if (getDistance(new Point(state.game.heroes.get(i).position.left, state.game.heroes.get(i).position.right),
					new Point(state.hero().position.left, state.hero().position.right)) < 3) return state.game.heroes.get(i);
		}
		return null;
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

	private int getDistance (Point p1, Point p2) {
		return (Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y));
	}

}// end of class LiranaiBot
/*
 * @Getter
 * 
 * @Setter class Node extends Object {
 * 
 * public enum Status { openNode, closedNode, unexploredNode; }
 * 
 * public int x, y; public double g, h, f; public Status status = Status.unexploredNode; private Node parent;
 * 
 * public Node (Point p) { super(); this.x = p.x; this.y = p.y; }
 * 
 * public Node (int x, int y) { super(); this.x = x; this.y = y; }
 * 
 * @Override public boolean equals (Object obj) { if (x == ((Node)obj).x && y == ((Node)obj).y) return true; return false; } }
 * 
 * class NodeComparator implements Comparator<Node> {
 * 
 * public int compare (Node obj1, Node obj2) { double c1 = obj1.f; double c2 = obj2.f;
 * 
 * if(c1 > c2) return 1; if(c1 < c2) return -1; else return 0; } }
 */