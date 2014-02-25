
package vindinium;

import java.awt.Point;
import java.util.Vector;

import lombok.Getter;
import lombok.Setter;
import vindinium.Board.Tile;

@Getter
@Setter
public class DataCollection {

	public static final double stealRate = 0.40;
	public static final int aggroRange = 5;

	private Vector<Point> mines, taverns, spawns;
	private int life, gold, minesOwned = 0;
	private Point position;
	private Vector<Hero> enemies;
	private int[] minesPerHero;

	public DataCollection () {
		mines = new Vector<Point>();
		taverns = new Vector<Point>();
		spawns = new Vector<Point>();
		enemies = new Vector<Hero>();
		minesPerHero = new int[3];
		minesOwned = 0;
	}

	public void make (State state) {
		for (int i = 0; i < state.game.board.tiles.length; i++) {
			for (int j = 0; j < state.game.board.tiles[0].length; j++) {
				if (state.game.board.tiles[i][j].equals(Tile.FREE_MINE) || state.game.board.tiles[i][j].getHeroMine() > -1)
					mines.add(new Point(i, j));
				else if (state.game.board.tiles[i][j].equals(Tile.TAVERN)) {
					taverns.add(new Point(i, j));
				}
			}
		}
		for (Hero hero : state.game.heroes) {
			if (hero.id != state.heroId) {
				enemies.add(hero);
				spawns.add(new Point(hero.spawnPosition.left, hero.spawnPosition.right));
			}
		}
		position = new Point(state.hero().position.left, state.hero().position.right);
	}

	public void update (State state) {
		life = state.hero().life;
		gold = state.hero().gold;
		position.setLocation(state.hero().position.left, state.hero().position.right);
		minesPerHero = new int[3];
		minesOwned = 0;
		enemies = new Vector<Hero>();
		for (Point p : mines) {
			if (state.game.board.tiles[p.x][p.y].equals(Board.Mine(state.heroId))) {
				minesOwned++;
			} else if (state.game.board.tiles[p.x][p.y].equals(Tile.FREE_MINE)) {
				continue;
			} else {
				int heroId = state.game.board.tiles[p.x][p.y].getHeroMine();
				minesPerHero[(heroId > state.heroId) ? heroId - 2 : heroId - 1]++;
			}
		}
		for (Hero hero : state.game.heroes) {
			if (hero.id != state.heroId) {
				enemies.add(hero);
			}
		}
	}

	public Point getDestination (State state, BrokenBot bot) {
		Point point = null;
		if (life < 95 && state.hero().gold > 2) {
			for (Point p : taverns)
				if (bot.getManhattanDistance(position, p) <= 1) {
					System.out.println("[HEALING]");
					return p;
				}
		}
		if (life >= 95 && (minesOwned > minesPerHero[0] && minesOwned > minesPerHero[1] && minesOwned > minesPerHero[2])) {
// minesOwned > mines.size() / 2.0
			System.out.println("[HIDING]");
			return position;
		}
		Hero target = closestHero(position, state, bot);
		if (!((bot.getDistance(target.getLocation(), position, state) <= 4) && life > target.life + 19)
			&& ((life < 30 && gold > 2) || minesOwned > mines.size() / 2.0)) {
			System.out.println("[GOING TO TAVERN]");
			return closestTavern(position, state, bot);
		}

		if (minesOwned < 1 && gold < 2) {
			point = closestMine(position, state, bot);
			System.out.println("[CAPTURING MINE]");
			return point;
		}

		if (life < target.life - 19) {
			System.out.println("[HEALING UP FOR BATTLE]");
			return closestTavern(position, state, bot);
		}
		System.out.print("[FIGHT: ");
		if (bot.getDistance(position, target.getLocation(), state) == 3) {
			System.out.print("strategic STAY]\n");
			return position;
		} else {
			System.out.print("ATTACK]\n");
			return target.getLocation();
		}

		/*
		 * Hero h = getHighValueTarget(bot, state); if (h != null) return h.getLocation(); point = closestMine(position, state,
		 * bot); if (life - bot.getDistance(point, position, state) < 25) return closestTavern(position, state, bot); return point
		 * != null ? point : position;
		 * 
		 * /* for (int i = 0; i < minesPerHero.length; i++) { if (minesPerHero[i] > mines.size() * stealRate) return
		 * (enemies.get(i).getLocation()); }
		 * 
		 * point = closestMine(position, state, bot); if (point != null) return point; return position;
		 */
	}

	private Hero closestHero (Point position, State state, BrokenBot bot) {
		int distance = Integer.MAX_VALUE;
		Hero p = null;
		for (Hero hero : enemies) {
			int tempDis = bot.getDistance(position, hero.getLocation(), state);
			if (tempDis < distance) {
				distance = tempDis;
				p = hero;
			}
		}
		return p;
	}

	private Hero getHighValueTarget (BrokenBot bot, State state) {
		Hero p = null;
		for (int i = 0; i < enemies.size(); i++) {
			if (enemies.get(i).name.equals(state.hero().name))
			;
			// System.out.println("[Hero: " + enemies.get(i).id + "->" + minesPerHero[i] + "]");
			if (bot.getDistance(position, enemies.get(i).getLocation(), state) <= 2) {
				boolean closeToTavern = false;
				for (Point point : taverns) {
					int dis = bot.getDistance(point, enemies.get(i).getLocation(), state);
					if (dis <= 2) {
						closeToTavern = true;
						break;
					}
					if (closeToTavern) continue;
				}
				if (minesPerHero[i] > minesOwned) {
					if (life > enemies.get(i).life + 10) p = enemies.get(i);
				}
			}
		}
		return p;
	}

	private Point closestMine (Point position, State state, BrokenBot bot) {
		int distance = Integer.MAX_VALUE;
		Point p = null;
		for (Point pt : mines) {
			if (!state.game.board.tiles[pt.x][pt.y].toString().contains(Board.Mine(state.heroId).toString())) {
				int tempDis = bot.getDistance(position, pt, state);
				if (tempDis < distance) {
					distance = tempDis;
					p = pt;
				}
			}
		}
		return p;
	}

	private Point closestTavern (Point position, State state, BrokenBot bot) {
		int distance = Integer.MAX_VALUE;
		Point p = null;
		for (Point pt : taverns) {
			int tempDis = bot.getDistance(position, pt, state);
			if (tempDis < distance) {
				distance = tempDis;
				p = pt;
			}
		}
		return p;
	}

}
