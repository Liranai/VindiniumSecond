package vindinium;

import java.io.IOException;
import java.io.Reader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.lang3.tuple.ImmutablePair;

import vindinium.Board.Tile;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

/**
 * GSON utility for use with Vindinium JSON.
 */
public final class Json {
    // --- Shared ---

    /**
     * Token reader for board
     * @see Board
     */
    static final TokenReader<Board> boardReader = new TokenReader<Board>() {
        public Board next(final JsonReader r) throws IOException {
            r.beginObject(); // {

            int size = -1;
            String tilesString = null;

            for (final ImmutablePair<String,JsonReader> p :
                     objectProperties(r)) {

                if ("size".equals(p.left)) {
                    size = p.right.nextInt();
                } else {
                    tilesString = p.right.nextString();
                }
            }

            /*
            Construct the tile array from the string
            Split the string each 2 char
            (?<=\G...) matches an empty string that has the last match (\G)
            followed by two characters (..) before it ((?<= ))
            */
            final String[] tilesStringArray = tilesString.split("(?<=\\G..)");
            final Tile[][] tiles = new Tile[size][size];

            int x = 0;
            int y = 0;
            for (int i = 0; i < size*size; i++) {
                y = i % size;
                if (y == 0 && i != 0) x++;
                tiles[x][y] = Tile.of(tilesStringArray[i]);
            }

            r.endObject(); // }
            return new Board(tiles);
        }
    };

    /**
     * Token reader for hero
     * @see Hero
     */
    static final TokenReader<Hero> heroReader = new TokenReader<Hero>() {
        public Hero next(final JsonReader r) throws IOException {
            r.beginObject(); // {

            int id = -1;
            String name = null;
            ImmutablePair<Integer,Integer> pos = null;
            ImmutablePair<Integer,Integer> spawnPos = null;
            int life = 0;
            int gold = 0;
            boolean crashed = false;
            String userId = null;
            int elo = -1;
            int mineCount = -1;

            for (final ImmutablePair<String,JsonReader> p :
                     objectProperties(r)) {

                if ("id".equals(p.left)) {
                    id = p.right.nextInt();
                } else if ("name".equals(p.left)) {
                    name = p.right.nextString();
                } else if ("life".equals(p.left)) {
                    life = p.right.nextInt();
                } else if ("gold".equals(p.left)) {
                    gold = p.right.nextInt();
                } else if ("crashed".equals(p.left)) {
                    crashed = p.right.nextBoolean();
                } else if ("userId".equals(p.left)) {
                    userId = p.right.nextString();
                } else if ("elo".equals(p.left)) {
                    elo = p.right.nextInt();
                } else if ("mineCount".equals(p.left)) {
                    mineCount = p.right.nextInt();
                } else if ("spawnPos".equals(p.left)) {
                    p.right.beginObject(); // {

                    spawnPos = ImmutablePair.
                        of(nextInt(p.right, "x"), nextInt(p.right, "y"));

                    r.endObject(); // }
                } else {
                    assert "pos".equals(p.left);
                    // Position property as object

                    p.right.beginObject(); // {

                    pos = ImmutablePair.
                        of(nextInt(p.right, "x"), nextInt(p.right, "y"));

                    r.endObject(); // }
                } // end of else
            } // end of for

            r.endObject(); // }

            return new Hero(id, name, pos, spawnPos, life, gold, elo, crashed);
        }
    };

    /**
     * Token reader for game
     * @see Game
     */
    static final TokenReader<Game> gameReader = new TokenReader<Game>() {
        public Game next(final JsonReader r) throws IOException {
            r.beginObject(); // {

            String id = null;
            int turn = 0;
            int maxTurn = 0;
            boolean finished = false;
            List<Hero> heroes = null;
            Board board = null;

            for (final ImmutablePair<String,JsonReader> p :
                     objectProperties(r)) {
                if ("id".equals(p.left)) {
                    id = p.right.nextString();
                } else if ("turn".equals(p.left)) {
                    turn = p.right.nextInt();
                } else if ("maxTurns".equals(p.left)) {
                    maxTurn = p.right.nextInt();
                } else if ("finished".equals(p.left)) {
                    finished = p.right.nextBoolean();
                } else if ("heroes".equals(p.left)) {
                    heroes = nextArray(p.right, arrayListReader(heroReader),
                                       new ArrayList<Hero>());

                } else {
                    assert "board".equals(p.left);

                    board = boardReader.next(p.right);
                } // end of else
            } // end of for

            r.endObject(); // }

            return new Game(id, turn, maxTurn, finished, heroes, board);
        }
    };

    /**
     * Token reader for state
     * @see State
     */
    static final TokenReader<State> stateReader = new TokenReader<State>() {
        public State next(final JsonReader r) throws IOException {
            r.beginObject(); // {

            Game game = null;
            int heroId = -1;
            String tok = null;
            URL vurl = null;
            URL purl = null;

            for (final ImmutablePair<String,JsonReader> p :
                     objectProperties(r)) {

                if ("game".equals(p.left)) {
                    game = gameReader.next(p.right);
                } else if ("hero".equals(p.left)) {
                    p.right.beginObject(); // {

                    for (final ImmutablePair<String,JsonReader> hp :
                             objectProperties(p.right)) {

                        if ("id".equals(hp.left)) {
                            heroId = hp.right.nextInt();
                        } else {
                            hp.right.skipValue();
                        }
                    }

                    p.right.endObject(); // }
                } else if ("token".equals(p.left)) {
                    tok = p.right.nextString();
                } else if ("viewUrl".equals(p.left)) {
                    try {
                        vurl = new URL(p.right.nextString());
                    } catch (Exception e) {
                        throw new IOException("Invalid URL: " + e.getMessage());
                    } // end of catch
                } else {
                    assert "playUrl".equals(p.left);

                    try {
                        purl = new URL(p.right.nextString());
                    } catch (Exception e) {
                        throw new IOException("Invalid URL: " + e.getMessage());
                    } // end of catch
                } // end of else
            } // end of for

            r.endObject(); // }

            return new State(game, heroId, tok, vurl, purl);
        }
    };

    // ---

    /**
     * Token reader for tile
     * @see Tile
     */
    static final ArrayReader<ImmutablePair<Tile[][],Integer>> tilesReader(final int size) {
        final double s = size;

        return new ArrayReader<ImmutablePair<Tile[][],Integer>>() {
            public ImmutablePair<Tile[][],Integer> next(final JsonReader r, ImmutablePair<Tile[][],Integer> p) throws IOException {

                final Tile[][] tiles = p.left;
                final int i = p.right;
                final String repr = r.nextString();

                if (repr == null) {
                    throw new IOException("Null tile");
                } // end of if

                final double dd = i / s;
                final int x;
                final int y;

                if (dd < 1) {
                    y = 0;
                    x = i;
                } else {
                    final int id = (int) dd;
                    x = (int) Math.round((dd - id) * s);
                    y = id;
                } // end of else

                try {
					tiles[y][x] = Tile.of(repr);

                    return ImmutablePair.of(tiles, i+1);
                } catch (Exception e) {
                    throw new IOException("Invalid tile: " + repr);
                } // end of catch
            } // end of next
        };
    } // end of tilesReader

    /**
     * Returns array read for list of T.
     */
    public static <T> ArrayReader<List<T>> arrayListReader(final TokenReader<T> tr) {
        return new ArrayReader<List<T>>() {
            public List<T> next(final JsonReader r, List<T> list)
                throws IOException {

                list.add(tr.next(r));

                return list;
            }
        };
    } // end of arrayListReader

    // ---

    /**
     * Reads next tokens as T.
     * @throws IOException if fails to read tokens as T
     * @see #next(com.google.gson.stream.JsonReader,TokenReader)
     */
    public static <T> T next(final Reader r, final TokenReader<T> tr)
        throws IOException {

        return next(new JsonReader(r), tr);
    } // end of next

    /**
     * Reads next tokens as T.
     * @throws IOException if fails to read tokens as T
     */
    public static <T> T next(final JsonReader r, final TokenReader<T> tr)
        throws IOException {

        return tr.next(r);
    } // end of next

    /**
     * Reads next |property| token as array of T.
     * @throws IOException if fails to read array of T property
     */
    public static <T> T nextArray(final JsonReader r, final String property,
                                  final ArrayReader<T> ar, final T initial)
        throws IOException {

        return nextArray(propertyReader(r, property), ar, initial);
    } // end of nextArray

    /**
     * Reads next token as array of T.
     * @throws IOException if fails to read array of T property
     */
    public static <T> T nextArray(final JsonReader r,
                                  final ArrayReader<T> ar,
                                  final T initial)
        throws IOException {

        r.beginArray(); // [

        T cur = initial;

        while (r.hasNext() && !JsonToken.END_ARRAY.equals(r.peek())) {
            cur = ar.next(r, cur);
        } // end of while

        r.endArray(); // ]

        return cur;
    } // end of nextArray

    /**
     * Reads next token as integer |property|.
     * @throws IOException if fails to read int property
     */
    public static int nextInt(final JsonReader r, final String property)
        throws IOException {

        return propertyReader(r, property).nextInt();
    } // end of nextInt

    /**
     * Returns JSON reader for |property|.
     * @throws IOException if property doesn't match
     */
    public static JsonReader propertyReader(final JsonReader r,
                                            final String property)
        throws IOException {

        final String name = r.nextName();

        if (property == null || !property.equals(name)) {
            throw new IOException("Unexpected property: " +
                                  name + " != " + property);

        } // end of if

        return r;
    } // end of propertyReader

    /**
     */
    public static Iterable<ImmutablePair<String,JsonReader>> objectProperties(final JsonReader r) throws IOException {

        final ObjPropIterator iter = new ObjPropIterator(r);

        return new Iterable<ImmutablePair<String,JsonReader>>() {
            public ObjPropIterator iterator() { return iter; }
        };
    } // end of objectProperties

    // --- Inner classes ---

    /**
     * Token reader.
     */
    public static interface TokenReader<T> {
        public T next(JsonReader r) throws IOException;
    } // end of interface TokenReader

    /**
     * Array reader.
     */
    public static interface ArrayReader<T> {

        /**
         * Returns parsed next element.
         * @param v Last T value
         */
        public T next(JsonReader r, T v) throws IOException;
    } // end of interface ArrayReader

    /**
     * Object property iterator.
     */
    private static final class ObjPropIterator
        implements Iterator<ImmutablePair<String,JsonReader>> {

        private final JsonReader r;
        ObjPropIterator(final JsonReader r) { this.r = r; }

        /**
         * {@inheritDoc}
         */
        public boolean hasNext() {
            try {
                return !JsonToken.END_OBJECT.equals(r.peek());
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            } // end of catch
        } // end of hasNext

        /**
         * Throws UnsupportedOperationException.
         */
        public void remove() { throw new UnsupportedOperationException(); }

        /**
         * Returns name and reader for next object property.
         */
        public ImmutablePair<String,JsonReader> next()
            throws NoSuchElementException {

            try {
                return ImmutablePair.of(r.nextName(), r);
            } catch (IOException e) {
                throw new NoSuchElementException(e.getMessage());
            } // end of catch
        } // end of next
    } // end of class ObjPropIterator
} // end of class Json
