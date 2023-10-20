package nl.duflex.proxy.mqtt;

public class MqttProxyMessageHeader {
    public static class Builder {
        private Integer qos;
        private Integer bodySize;

        public Builder setQos(final Integer qos) {
            this.qos = qos;

            return this;
        }

        public Builder setBodySize(final Integer bodySize) {
            this.bodySize = bodySize;

            return this;
        }

        public MqttProxyMessageHeader build() {
            assert this.qos != null;
            assert this.bodySize != null;

            return new MqttProxyMessageHeader(this.qos, this.bodySize);
        }

        public static Builder from(final String raw) throws RuntimeException {
            final Builder builder = new Builder();

            // Splits the raw string into its segments.
            final String[] rawSegments = raw.split(" ");

            // Make sure that there are two raw segments.
            if (rawSegments.length != 2) {
                throw new RuntimeException("The message header must consist of two segments, got "
                        + rawSegments.length);
            }

            // Parse the qos.
            try {
                builder.setQos(Integer.parseInt(rawSegments[0]));
            } catch (final NumberFormatException exception) {
                throw new RuntimeException("Invalid quality of service value");
            }

            // Parse the body size.
            try {
                builder.setBodySize(Integer.parseInt(rawSegments[1]));
            } catch (final NumberFormatException exception) {
                throw new RuntimeException("Invalid body size value");
            }

            // Returns the builder.
            return builder;
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private final int qos;
    private final int bodySize;

    public MqttProxyMessageHeader(final int qos, final int bodySize) {
        this.qos = qos;
        this.bodySize = bodySize;
    }

    public int getQos() {
        return this.qos;
    }

    public int getBodySize() {
        return this.bodySize;
    }

    public String toString() {
        return this.qos + " " + this.bodySize;
    }
}
