package vindinium;

import java.util.Map;

import java.io.InputStreamReader;
import java.io.DataOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Closeable;

import java.net.HttpURLConnection;
import java.net.URL;

import static java.net.URLEncoder.encode;

/**
 * I/O utility.
 */
public final class IO {

    /**
     * POST to given HTTP |url| and apply |function| on result.
     * Reader is automatically closed after function.
     *
     * @param ps POST parameters
     * @param outEncoding Request (POST parameters) encoding
     * @param url POST URL
     * @param inEncoding Response encoding
     * @param function Unary function with HTTP reader as argument
     * @throws IOException if fail to POST or read response
     */
    public static <A> A fromPost(final Map<String,String> ps,
                                 final String outEncoding,
                                 final URL url, final String inEncoding,
                                 final UnaryFunction<BufferedReader,A> function)
        throws IOException {

        final UnaryFunction<DataOutputStream,Void> setp =
            ((ps == null || ps.isEmpty())) ? null
            : parameterAppender(ps, outEncoding);

        return withPostConnection((HttpURLConnection) url.openConnection(),
                                  connectionMapper(inEncoding, setp, function));
    } // end of fromPost

    // ---

    /**
     * Applies given |function| on HTTP POST |connection|.
     */
    static <A> A withPostConnection(final HttpURLConnection connection, final UnaryFunction<HttpURLConnection,A> function) throws IOException {

        try {
            connection.setRequestMethod("POST");

            return function.apply(connection);
        } finally {
            try {
                connection.disconnect();
            } catch (Exception e) { e.printStackTrace(); }
        } // end of finally
    } // end of withURLConnection

    /**
     * Applies given |function| on given |closeable|.
     */
    static <A extends Closeable,B> B withCloseable(final A closeable, final UnaryFunction<A,B> function) {

        try {
            return function.apply(closeable);
        } finally {
            try {
                closeable.close();
            } catch (Exception e) { e.printStackTrace(); }
        } // end of finally
    } // end of withReader

    /**
     * Returns buffered reader for given stream.
     */
    static BufferedReader reader(final InputStream in, final String encoding)
        throws IOException {

        final InputStreamReader isr = (encoding == null)
            ? new InputStreamReader(in)
            : new InputStreamReader(in, encoding);

        return new BufferedReader(isr);
    } // end of reader

    /**
     * Returns function appending |parameters| on output stream.
     * @param parameters
     * @param encoding Parameter encoding
     */
    static UnaryFunction<DataOutputStream,Void> parameterAppender(final Map<String,String> parameters, final String encoding) {

        return new UnaryFunction<DataOutputStream,Void>() {
            public Void apply(final DataOutputStream out) {
                try {
                    int i = 0;
                    for (final String k : parameters.keySet()) {
                        if (i++ > 0) out.writeByte('&');

                        out.writeBytes(encode(k, encoding));
                        out.writeByte('=');
                        out.writeBytes(encode(parameters.get(k), encoding));
                        out.flush();
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Fails to encode parameters", e);
                }
                return null;
            }
        };
    } // end of parameterAppender

    /**
     * Maps a HTTP connection with given |function|.
     *
     * @param encoding Encoding to be used for reading HTTP response
     * @param pf Function appending parameter to HTTP output, or null
     * @param function Mapped function
     * @return Function result
     */
    static <A> UnaryFunction<HttpURLConnection,A> connectionMapper(final String encoding, final UnaryFunction<DataOutputStream,Void> pf, final UnaryFunction<BufferedReader,A> function) {

        return new UnaryFunction<HttpURLConnection,A>() {
            public A apply(final HttpURLConnection con) {
                try {
                    if (pf != null) {
                        con.setDoOutput(true);
                        withCloseable(new DataOutputStream(con.getOutputStream()), pf);
                    }

                    final int c = con.getResponseCode();

                    if (c != 200) {
                        final String body =
                            withCloseable(reader(con.getErrorStream(),
                                                 encoding), readAsString);

                        throw new IOException("Fails to get response: " +
                                              c + " (" +
                                              con.getResponseMessage() +
                                              "): " + body);
                    }

                    return withCloseable(reader(con.getInputStream(), encoding), function);
                } catch (IOException e) {
                    throw new RuntimeException("Fails to POST", e);
                }
            }
        };
    } // end of connectionMapper

    /**
     * Read content as string
     */
    static UnaryFunction<BufferedReader,String> readAsString =
        new UnaryFunction<BufferedReader,String>() {
        public String apply(final BufferedReader r) {
            final StringBuffer buf = new StringBuffer();

            try {
                String line;
                while ((line = r.readLine()) != null) buf.append(line);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return buf.toString();
        }
    };
} // end of class IO
