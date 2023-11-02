package nl.duflex.proxy;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TcpServer implements Runnable {
    private final Logger logger = Logger.getLogger(TcpServer.class.getName());
    private final ServerSocket serverSocket;
    private final AtomicBoolean shutdown = new AtomicBoolean(false);
    private final ProxyProtocolClientHandlerFactory clientHandlerFactory;

    public TcpServer(final ServerSocket serverSocket, final ProxyProtocolClientHandlerFactory clientHandlerFactory) {
        this.serverSocket = serverSocket;
        this.clientHandlerFactory = clientHandlerFactory;
    }

    private Socket accept() {
        try {
            return serverSocket.accept();
        } catch (final IOException exception) {
            logger.log(Level.WARNING, "Failed to accept client socket, exception: " + exception);
            return null;
        }
    }

    @Override
    public void run() {
        try {
            while (!shutdown.get()) {
                Socket socket = this.accept();
                if (socket == null) continue;

                final ProxyTcpClient client = ProxyTcpClient.fromSocket(socket);
                new Thread(new ProxyClientHandler(client, this.clientHandlerFactory)).start();
            }
        } catch (final Exception exception) {
            logger.log(Level.SEVERE, "Failed to create server socket, exception: " + exception);
        }
    }
}
