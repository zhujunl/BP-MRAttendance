package org.nanohttpd.util;

import org.nanohttpd.NanoHTTPD;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;


public class ServerRunner {
    private static final Logger LOG;

    public static void executeInstance(final NanoHTTPD server) {
        try {
            server.start(5000, false);
        } catch (IOException ioe) {
            System.err.println("Couldn't start server:\n" + ioe);
            System.exit(-1);
        }
        System.out.println("Server started, Hit Enter to stop.\n");
        try {
            System.in.read();
        } catch (Throwable t) {
            t.printStackTrace();
        }
        server.stop();
        System.out.println("Server stopped.\n");
    }

    public static <T extends NanoHTTPD> void run(final Class<T> serverClass) {
        try {
            executeInstance(serverClass.newInstance());
        } catch (Exception e) {
            ServerRunner.LOG.log(Level.SEVERE, "Cound nor create server", e);
        }
    }

    static {
        LOG = Logger.getLogger(ServerRunner.class.getName());
    }
}
