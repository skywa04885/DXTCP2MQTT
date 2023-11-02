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

        public static Builder from(final String line) throws RuntimeException {
            final Builder builder = new Builder();

            final var lineSegments = line.trim().split(" ");
            if (lineSegments.length != 2)
                throw new RuntimeException("The message header must consist of two segments, got "
                        + lineSegments.length);

            final var qosLineSegment = lineSegments[0].trim();
            final var bodySizeLineSegment = lineSegments[1].trim();

            try {
                builder.setQos(Integer.parseInt(qosLineSegment));
            } catch (final NumberFormatException exception) {
                throw new RuntimeException("Invalid quality of service value");
            }

            try {
                builder.setBodySize(Integer.parseInt(bodySizeLineSegment));
            } catch (final NumberFormatException exception) {
                throw new RuntimeException("Invalid body size value");
            }

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
