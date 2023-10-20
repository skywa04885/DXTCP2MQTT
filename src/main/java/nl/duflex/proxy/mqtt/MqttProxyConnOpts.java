package nl.duflex.proxy.mqtt;

import org.eclipse.paho.client.mqttv3.MqttConnectOptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class MqttProxyConnOpts {
    public static class Builder {
        private InetAddress address = null;
        private Short port = null;
        private String clientId = null;
        private String username = null;
        private String password = null;

        public Builder setAddress(final InetAddress address) {
            this.address = address;

            return this;
        }

        public Builder setPort(final Short port) {
            this.port = port;

            return this;
        }

        public Builder setClientId(final String clientId) {
            this.clientId = clientId;

            return this;
        }

        public Builder setUsername(final String username) {
            this.username = username;

            return this;
        }

        public Builder setPassword(final String password) {
            this.password = password;

            return this;
        }

        public MqttProxyConnOpts build() {
            assert this.address != null;
            assert this.port != null;
            assert this.clientId != null;

            return new MqttProxyConnOpts(this.address, this.port, this.clientId, this.username, this.password);
        }

        public static Builder fromString(final String line) throws RuntimeException {
            final Builder builder = new Builder();
            String[] lineSegments;

            // Splits the line into the segments.
            lineSegments = line.split(" ");

            // Makes sure the proper number of segments is present.
            if (!(lineSegments.length == 3 || lineSegments.length == 4 || lineSegments.length == 5)) {
                throw new RuntimeException("The broker line does not have the correct number of segments");
            }

            // Sets the address.
            try {
                builder.setAddress(InetAddress.getByName(lineSegments[0]));
            } catch (final UnknownHostException exception) {
                throw new RuntimeException("Could not parse address, exception: " + exception);
            }

            // Sets the port.
            try {
                builder.setPort(Short.parseShort(lineSegments[1]));
            } catch (final NumberFormatException exception) {
                throw new RuntimeException("Could not parse port, exception: " + exception);
            }

            // Gets the id.
            builder.setClientId(lineSegments[2]);

            // Sets the username.
            if (lineSegments.length == 4 || lineSegments.length == 5) {
                builder.setUsername(lineSegments[3]);
            }

            // Sets the password.
            if (lineSegments.length == 5) {
                builder.setPassword(lineSegments[4]);
            }

            // Returns the builder.
            return builder;
        }
    }

    private final InetAddress address;
    private final short port;
    private final String clientId;
    private final String username;
    private final String password;

    public MqttProxyConnOpts(final InetAddress address, final short port, final String clientId) {
        this.address = address;
        this.port = port;
        this.clientId = clientId;
        this.username = null;
        this.password = null;
    }

    public MqttProxyConnOpts(final InetAddress address, final short port, final String clientId, final String username, final String password) {
        this.address = address;
        this.port = port;
        this.clientId = clientId;
        this.username = username;
        this.password = password;
    }

    public InetAddress getAddress() {
        return this.address;
    }

    public short getPort() {
        return this.port;
    }

    public String getClientId() {
        return this.clientId;
    }

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public String toServerURI() {
        return "tcp://" + this.address.getHostAddress() + ":" + this.port;
    }

    public MqttConnectOptions toMqttConnectOptions() {
        final MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();

        mqttConnectOptions.setCleanSession(true);

        if (this.username != null) {
            mqttConnectOptions.setUserName(this.username);
        }

        if (this.password != null) {
            mqttConnectOptions.setPassword(this.password.toCharArray());
        }

        return mqttConnectOptions;
    }
}
