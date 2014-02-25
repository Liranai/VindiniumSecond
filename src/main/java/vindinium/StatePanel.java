
package vindinium;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Stroke;
import java.util.HashMap;

import javax.swing.JPanel;

import vindinium.Board.Tile;

public class StatePanel extends JPanel {

	private static final float HERO_CIRCLE_TO_RECT_RATIO = 0.5f;

	private static final Color COLOR_HERO_CRASHED = new Color(0, 0, 0);

	private static final Stroke OWN_HERO_OUTLINE_STROKE = new BasicStroke(2);
	private static final Color OWN_HERO_OUTLINE_COLOR = new Color(255, 255, 255);

	private static final float TAVERN_CROSS_SIZE_RATIO = 0.6f;
	private static final float TAVERN_CROSS_THICKNESS_RATIO = 0.1f;
	private static final Color TAVERN_CROSS_COLOR = new Color(255, 0, 0);

	private static final Color MINE_NO_HERO_COLOR = new Color(255, 255, 255);
	private static final float MINE_ICON_TOP_SIZE_RATIO = 0.8f;
	private static final float MINE_ICON_BASE_SIZE_RATIO = 0.6f;
	private static final float MINE_ICON_HEIGHT_RATIO = 0.5f;

	private static final Color BACKGROUND_COLOR = new Color(255, 255, 255, 0);

	private static final HashMap<Tile, Color> tileMap;
	static {
		tileMap = new HashMap<Tile, Color>();
		tileMap.put(Tile.AIR, new Color(128, 192, 255));
		tileMap.put(Tile.FREE_MINE, new Color(255, 255, 128));
		tileMap.put(Tile.TAVERN, new Color(128, 255, 128));
		tileMap.put(Tile.WALL, new Color(64, 64, 64));
	}

	private State state;

	public StatePanel () {
		this.setOpaque(false);
	}

	public void drawState(State state){
		this.state = state;
		repaint();
	}
	
	@Override
	public void paintComponent (Graphics g) {
		super.paintComponent(g);

		Graphics2D g2d = (Graphics2D)g;

		int width = this.getWidth();
		int height = this.getHeight();

		g2d.setColor(BACKGROUND_COLOR);
		g2d.fillRect(0, 0, width, height);

		KnowledgeCollection knowledgeCollection = null; // BrokenBot.getKnowledge();
		if (knowledgeCollection != null) {
			// State state = debugState.getState();
			// Game game = state.game;
			// Board board = game.board;
			Tile[][] tiles = state.game.board.tiles;

			int size = knowledgeCollection.tiles.length;

			int rectSize = Math.min(width / size, height / size);

			int xOffset = (width - (rectSize * size)) / 2;
			int yOffset = (height - (rectSize * size)) / 2;

			for (int x = 0; x < size; x++) {
				for (int y = 0; y < size; y++) {
					Tile tile = tiles[x][y];

					int playerMine = tile.getHeroMine();

					if (tile == Tile.TAVERN) {
						drawTavern(g2d, xOffset + (x * rectSize), yOffset + (y * rectSize), rectSize);
					} else if (playerMine > -1) {
						drawHeroMine(playerMine, g2d, xOffset + (x * rectSize), yOffset + (y * rectSize), rectSize);
					} else if (tile.isHero()) {
						g2d.setColor(tileMap.get(Tile.AIR));
						g2d.fillRect(xOffset + (x * rectSize), yOffset + (y * rectSize), rectSize, rectSize);
					} else if (tile == Tile.WALL) {
						g2d.setColor(Color.black);
						g2d.fillRect(xOffset + (x * rectSize), yOffset + (y * rectSize), rectSize, rectSize);
					} else {
						g2d.setColor(tileMap.get(tile));
						g2d.fillRect(xOffset + (x * rectSize), yOffset + (y * rectSize), rectSize, rectSize);
					}
				}
			}

			int circleDiameter = (int)(HERO_CIRCLE_TO_RECT_RATIO * rectSize);
			int circleOffset = (rectSize - circleDiameter) / 2;

			Hero me = state.hero();

			for (Hero hero : state.game.heroes) {

				Color heroColor;

				if (hero.crashed) {
					heroColor = COLOR_HERO_CRASHED;
				} else {
					heroColor = DebugUIConstants.COLOR_HEROES[hero.id - 1];
				}

				g2d.setColor(heroColor);
				int x = hero.position.getLeft();
				int y = hero.position.getRight();
				g2d.fillOval(xOffset + (x * rectSize) + circleOffset, yOffset + (y * rectSize) + circleOffset, circleDiameter,
					circleDiameter);

				if (hero == me) {
					g2d.setStroke(OWN_HERO_OUTLINE_STROKE);
					g2d.setColor(OWN_HERO_OUTLINE_COLOR);
					g2d.drawOval(xOffset + (x * rectSize) + circleOffset, yOffset + (y * rectSize) + circleOffset, circleDiameter,
						circleDiameter);
				}
			}

			/*
			 * for (int x = 0; x < size; x++) { for (int y = 0; y < size; y++) { for (DebugLayer layer : layers) {
			 * layer.showState(debugState, x, y, xOffset + (x * rectSize), yOffset + (y * rectSize), rectSize, g2d); } } }
			 */
		}
	}

	private void drawHeroMine (int playerMine, Graphics2D g2d, int x, int y, int rectSize) {
		g2d.setColor(tileMap.get(Tile.AIR));
		g2d.fillRect(x, y, rectSize, rectSize);

		Polygon poly = new Polygon();

		int height = (int)(MINE_ICON_HEIGHT_RATIO * rectSize);
		int topY = (rectSize - height) / 2;
		int baseY = topY + height;

		int baseWidth = (int)(rectSize * MINE_ICON_BASE_SIZE_RATIO);
		int baseOffset = (rectSize - baseWidth) / 2;

		poly.addPoint(x + baseOffset, y + baseY);
		poly.addPoint(x + baseOffset + baseWidth, y + baseY);

		int topWidth = (int)(rectSize * MINE_ICON_TOP_SIZE_RATIO);
		int topOffset = (rectSize - topWidth) / 2;
		poly.addPoint(x + topOffset + topWidth, y + topY);
		poly.addPoint(x + topOffset, y + topY);

		if (playerMine == 0)
			g2d.setColor(MINE_NO_HERO_COLOR);
		else
			g2d.setColor(DebugUIConstants.COLOR_HEROES[playerMine - 1]);

		g2d.fill(poly);
	}

	private void drawTavern (Graphics2D g2d, int x, int y, int rectSize) {

		g2d.setColor(tileMap.get(Tile.AIR));
		g2d.fillRect(x, y, rectSize, rectSize);

		int crossRectSize = (int)(TAVERN_CROSS_SIZE_RATIO * rectSize);
		int crossRectOffset = (rectSize - crossRectSize) / 2;

		int crossRectThickness = (int)(TAVERN_CROSS_THICKNESS_RATIO * rectSize);

		g2d.setColor(TAVERN_CROSS_COLOR);
		g2d.fillRect(x + crossRectOffset, y + crossRectOffset + (crossRectSize / 2) - (crossRectThickness / 2), crossRectSize,
			crossRectThickness);
		g2d.fillRect(x + crossRectOffset + (crossRectSize / 2) - (crossRectThickness / 2), y + crossRectOffset, crossRectThickness,
			crossRectSize);
	}
}

