package nl.duflex.proxy.mqtt;

public record MqttProxySubTopics(String[] topics) {
    public static MqttProxySubTopics fromString(final String raw) {
        final String[] topics = raw.split("\r\n");
        return new MqttProxySubTopics(topics);
    }
}
