package nl.duflex.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpServer implements Runnable {
    private final InetAddress address;
    private final short port;
    private final int backlog;
    private volatile boolean stop;

    public TcpServer(final InetAddress address, final short port, final int backlog) {
        this.address = address;
        this.port = port;
        this.backlog = backlog;
        this.stop = false;
    }

    @Override
    public void run() {
        final Logger logger = Logger.getLogger(TcpServer.class.getName());

        try (ServerSocket serverSocket = new ServerSocket(this.port, this.backlog, this.address)) {
            logger.log(Level.INFO, "Listening on " + this.address + " with port " + this.port);

            while (!stop) {
                Socket socket;

                try {
                    socket = serverSocket.accept();
                } catch (final IOException exception) {
                    logger.log(Level.WARNING, "Failed to accept client socket, exception: " + exception);
                    continue;
                }

                final ProxyTcpClient client = ProxyTcpClient.fromSocket(socket);
                new Thread(new ProxyTcpClientHandler(client)).start();
            }
        } catch (final Exception exception) {
            logger.log(Level.SEVERE, "Failed to create server socket, exception: " + exception);
        }
    }
}
