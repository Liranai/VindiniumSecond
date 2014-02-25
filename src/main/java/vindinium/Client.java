package vindinium;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * Vindinium client.
 */
public final class Client {

    /**
     * Launch client.
     * @param args args[0] Mode: arena or training
     * @param args args[1] Private key
     * @param args args[2] Number of games to play (unused for now)
     * @param args args[3] HTTP URL of Vindinium server
     */
    public static void main(final String[] args) {
        final int numberOfGamesToPlay = Integer.parseInt(args[2]);

        // Play numberOfGamesToPlay party in a row
        for (int i = 0; i < numberOfGamesToPlay; i++) {
            try {
				withModeKeyGamesAndServer(args[0], args[1], 300, new URL(args[3]));
            } catch (MalformedURLException e) {
                throw new RuntimeException("Invalid server URL", e);
            } // end of catch
            System.out.println("\nGame finished: " + (i+1) + "/" + numberOfGamesToPlay);
        }
    } // end of main

    /**
     * Runs client with given server |url|, AI |key| and |mode|.
     */
    static void withModeKeyGamesAndServer(final String mode, final String key, final int numberOfTurns, final URL serverUrl) {
        System.out.println("\nConnecting to Vindinium server at " + serverUrl);

        State state = null;

        final HashMap<String,String> initParams = new HashMap<String,String>(1);
        initParams.put("key", key);

        // Construct api url
        URL url;
        try {
            if ("training".equals(mode)) {
                url = new URL(serverUrl + "/api/training");
                initParams.put("turns", String.valueOf(numberOfTurns));
            } else if ("arena".equals(mode)) {
                url = new URL(serverUrl + "/api/arena");
                System.out.println("Connecting and waiting for other players to join ...");
            } else {
                throw new RuntimeException("Invalid mode, should be arena or training.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid generated URL", e);
        } // end of catch

        for (int t = 0; state == null && t < 3; t++) { // Initial state
            try {
                state = IO.
                    fromPost(initParams, "UTF-8", url, "UTF-8", getState);

            } catch (IOException e) {
                System.err.println("Fails to get initial state (" + t +
                                   "). Will try again.");
                e.printStackTrace();
            } // end of catch
        } // end of for

        if (state == null) {
            throw new RuntimeException("Fails to get initial state");
        } // end of if

        // ---

        System.out.println("Playing at: " + state.viewUrl);

		final Bot bot = new BrokenBot(); // Replace by other Bot
        final HashMap<String,String> ps = new HashMap<String,String>(1);
        ps.put("key", key);

        boolean finished = state.game.finished;
		long time;

        while (!finished) {
            ps.put("dir", bot.nextMove(state).toString());
			time = System.currentTimeMillis();
			// System.out.print(".");

            try {
                state = IO.
                    fromPost(ps, "UTF-8", state.playUrl, "UTF-8", getState);

                finished = state.game.finished;
            } catch (Exception e) {
				System.err.println("Fails to get next state. Time taken[" + (System.currentTimeMillis() - time) + "]");
                e.printStackTrace();
                finished = true;
            } // end of catch
        } // end of while
    } // end of withServerUrl

    /**
     * Function getting Vindinium state from I/O reader
     */
    static final UnaryFunction<BufferedReader,State> getState =
        new UnaryFunction<BufferedReader,State>() {
        public State apply(final BufferedReader r) {
            try {
                return Json.next(r, Json.stateReader);
            } catch (IOException e) {
                throw new RuntimeException("Fails to get next state", e);
            }
        }
    };

} // end of class Client
