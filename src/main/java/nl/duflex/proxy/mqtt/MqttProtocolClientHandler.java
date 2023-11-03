package nl.duflex.proxy.mqtt;

import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;
import nl.duflex.proxy.ProxyTcpClient;
import nl.duflex.proxy.ProxyProtocolClientHandler;
import org.eclipse.paho.client.mqttv3.*;

import java.io.IOException;
import java.util.logging.Logger;

public class MqttProtocolClientHandler extends ProxyProtocolClientHandler implements MqttCallback {
    private final Logger logger = Logger.getLogger(this.getClass().getName());
    private MqttClient mqttClient = null;

    public MqttProtocolClientHandler(final ProxyTcpClient client) {
        super(client);
    }

    /**
     * Connects to the MQTT broker.
     *
     * @return false if there was an end of the stream.
     * @throws IOException      gets thrown when reading from the tcp client goes wrong.
     * @throws MqttException    gets thrown when connecting to the MQTT broker goes wrong.
     * @throws RuntimeException gets thrown when parsing the connection options goes wrong.
     */
    private boolean connectToMQTT() throws IOException, MqttException, RuntimeException {
        final ProxyInputStreamReader inputStreamReader = this.client.getInputStreamReader();

        // Reads the connection options line.
        final String connOptsLine = inputStreamReader.readStringUntilNewLine();
        if (connOptsLine == null) return false;

        // Parses the connection options from the line.
        final MqttProxyConnOpts connOpts = MqttProxyConnOpts.Builder.fromString(connOptsLine).build();

        // Logs that we're connecting to MQTT.
        logger.info("Connecting to " + connOpts.toServerURI() + " with client id " + connOpts.getClientId());

        // Creates the MQTT client, sets the callbacks to the current instance and connects to it.
        this.mqttClient = new MqttClient(connOpts.toServerURI(), connOpts.getClientId(), null);
        this.mqttClient.setCallback(this);
        this.mqttClient.connect(connOpts.toMqttConnectOptions());

        // Logs that we're connected to MQTT.
        logger.info("Connected to " + connOpts.toServerURI() + " ");

        // Return true since the stream did not end.
        return true;
    }

    /**
     * Subscribes to all the topics sent by the client.
     *
     * @return false if there was an end of the stream.
     * @throws IOException   gets thrown when reading from the TCP client goes wrong.
     * @throws MqttException gets thrown when subscribing to the topics goes wrong.
     */
    private boolean subscribeToTopics() throws IOException, MqttException {
        final ProxyInputStreamReader inputStreamReader = this.client.getInputStreamReader();

        // Reads the subscription topic lines.
        final String topicLines = inputStreamReader.readStringUntilDoubleNewLine();
        if (topicLines == null) return false;

        // Parses the subscription topics from the topic lines.
        final MqttProxySubTopics subTopics = MqttProxySubTopics.fromString(topicLines);

        // Subscribes to all the topics.
        for (final String topic : subTopics.topics()) {
            logger.info("Subscribing to topic " + topic);

            this.mqttClient.subscribe(topic);

            logger.info("subscribed to topic " + topic);
        }

        // Return true since the stream did not end.
        return true;
    }

    /**
     * Publishes a message onto the MQTT bus.
     *
     * @return false if there was an end of the stream.
     * @throws IOException      gets thrown when reading from the TCP client goes wrong.
     * @throws MqttException    gets thrown when publishing to the MQTT broker goes wrong.
     * @throws RuntimeException gets thrown when parsing the header goes wrong.
     */
    private boolean publishMessage() throws IOException, MqttException, RuntimeException {
        final ProxyInputStreamReader inputStreamReader = this.client.getInputStreamReader();

        // Reads the topic line and the header line.
        final String topicLine = inputStreamReader.readStringUntilNewLine();
        if (topicLine == null) return false;
        final String headerLine = inputStreamReader.readStringUntilNewLine();
        if (headerLine == null) return false;

        // Parses the header line.
        final MqttProxyMessageHeader messageHeader = MqttProxyMessageHeader.Builder.from(headerLine).build();

        // Reads the message body.
        final byte[] body = inputStreamReader.readNBytes(messageHeader.getBodySize());
        if (body == null) return false;

        // Publishes the message onto the MQTT bus.
        this.mqttClient.publish(topicLine, body, messageHeader.getQos(), false);

        // Return true since the stream did not end.
        return true;
    }

    /**
     * Creates the MQTT connection and handles the messages.
     */
    @Override
    public void run() {
        try {
            if (connectToMQTT()) {
                if (subscribeToTopics()) {
                    while (true) {
                        if (publishMessage()) continue;
                        else {
                            this.close();
                            break;
                        }
                    }
                } else {
                    this.close();
                }
            } else {
                this.close();
            }
        } catch (final IOException exception) {
            this.logger.warning("An IO exception occurred, message: " + exception.getMessage());
            this.close();
        } catch (final MqttException exception) {
            this.logger.severe("An MQTT exception occurred, message: " + exception.getMessage());
            this.close();
        } catch (final RuntimeException exception) {
            this.logger.severe("An runtime exception occurred, message: " + exception.getMessage());
            this.close();
        }
    }

    /**
     * Closes the MQTT client and the client socket.
     */
    private void close() {
        try {
            // Only close the client if we're still connected.
            synchronized (this.client) {
                if (this.client.isConnected()) this.client.close();
            }

            // Only close the MQTT client if it's present.
            if (this.mqttClient != null) {
                if (this.mqttClient.isConnected()) this.mqttClient.disconnect();
                this.mqttClient.close();
            }
        } catch (final IOException exception) {
            this.logger.severe("Failed to close client socket, message: " + exception.getMessage());
        } catch (final MqttException exception) {
            this.logger.severe("Failed to close MQTT connection, message: " + exception.getMessage());
        }
    }

    @Override
    public void connectionLost(Throwable throwable) {
        this.close();
    }

    @Override
    public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
        final MqttProxyMessageHeader messageHeader = MqttProxyMessageHeader.newBuilder()
                .setQos(mqttMessage.getQos())
                .setBodySize(mqttMessage.getPayload().length)
                .build();

        synchronized (this.client) {
            if (this.client.isConnected()) {
                final ProxyOutputStreamWriter outputStreamWriter = this.client.getOutputStreamWriter();
                outputStreamWriter.writeLine(topic)
                        .writeLine(messageHeader.toString())
                        .write(mqttMessage.getPayload())
                        .flush();
            }
        }
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }
}
