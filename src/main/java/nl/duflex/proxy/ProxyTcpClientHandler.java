package nl.duflex.proxy;

import org.eclipse.paho.client.mqttv3.MqttException;

import java.io.IOException;
import java.util.logging.Logger;

public class ProxyTcpClientHandler implements Runnable {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private final ProxyTcpClient client;

    public ProxyTcpClientHandler(final ProxyTcpClient client) {
        this.client = client;
    }

    /**
     * Closes the client and the client socket.
     */
    private void close() {
        try {
            this.client.close();
        } catch (final IOException exception) {
            this.logger.severe("Failed to close client socket, message: " + exception.getMessage());
        }
    }

    @Override
    public void run() {
        try {
            final ProxyInputStreamReader inputStreamReader = this.client.getInputStreamReader();

            // reads the protocol line.
            final String protocolLine = inputStreamReader.readStringUntilNewLine();
            if (protocolLine == null) {
                this.client.close();
                return;
            }

            // Gets the protocol from the line.
            final ProxyProtocol proxyProtocol = ProxyProtocol.fromLabel(protocolLine);

            // Creates the client handler.
            final ProxyProtocolClientHandler clientHandler = ProxyProtocolClientHandlerFactory.create(proxyProtocol,
                    this.client);

            // Runs the client.
            clientHandler.run();
        } catch (final IOException exception) {
            this.logger.severe("Got IO exception, message: " + exception.getMessage());
            this.close();
        } catch (final RuntimeException exception) {
            this.logger.severe("Got runtime exception, message: " + exception.getMessage());
            this.close();
        }
    }
}
