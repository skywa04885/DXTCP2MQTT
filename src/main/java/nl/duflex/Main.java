package nl.duflex;

import nl.duflex.proxy.ProxyProtocolClientHandler;
import nl.duflex.proxy.ProxyProtocolClientHandlerFactory;
import nl.duflex.proxy.ProxyTcpClientHandler;
import nl.duflex.proxy.TcpServer;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Logger;

// Press Shift twice to open the Search Everywhere dialog and type `show whitespaces`,
// then press Enter. You can now see whitespace characters in your code.
public class Main {
    private static Logger logger = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws UnknownHostException {
        final TcpServer tcpServer = new TcpServer(InetAddress.getByName("0.0.0.0"), (short) 5000, 4);

        tcpServer.run();
    }
}