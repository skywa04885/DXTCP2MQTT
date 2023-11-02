package nl.duflex.proxy.mqtt;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public record MqttProxySubTopics(List<String> topics) {
    public static MqttProxySubTopics fromString(final String raw) {
        return new MqttProxySubTopics(Arrays
                .stream(raw.split("\r\n"))
                .map(String::trim)
                .collect(Collectors.toList()));
    }
}
