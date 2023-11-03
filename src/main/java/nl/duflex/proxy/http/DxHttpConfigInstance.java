package nl.duflex.proxy.http;

import org.w3c.dom.Element;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class DxHttpConfigInstance {
    public static final String TAG_NAME = "Instance";
    public static final String NAME_ATTRIBUTE_NAME = "Name";
    public static final String HOST_ATTRIBUTE_NAME = "Host";
    public static final String PORT_ATTRIBUTE_NAME = "Port";
    public static final String PROTOCOL_ATTRIBUTE_NAME = "Protocol";

    public final String Name;
    public final String Host;
    public final short Port;
    public final String Protocol;

    public DxHttpConfigInstance(final String name, final String host, final short port, final String protocol) {
        Name = name;
        Host = host;
        Port = port;
        Protocol = protocol;
    }

    public static DxHttpConfigInstance FromElement(final Element element) {
        final var name = element.getAttribute(NAME_ATTRIBUTE_NAME).trim();
        if (name.isEmpty()) throw new RuntimeException("Name attribute is missing");

        final var host = element.getAttribute(HOST_ATTRIBUTE_NAME).trim();
        if (host.isEmpty()) throw new RuntimeException("Host attribute is missing");

        final var portString = element.getAttribute(PORT_ATTRIBUTE_NAME).trim();
        if (portString.isEmpty()) throw new RuntimeException("Port attribute is missing");

        short port;

        try {
            port = Short.parseShort(portString);
        } catch (final NumberFormatException exception) {
            throw new RuntimeException("Invalid port");
        }

        final var protocol = element.getAttribute(PROTOCOL_ATTRIBUTE_NAME).trim();
        if (protocol.isEmpty()) throw new RuntimeException("Protocol missing");

        return new DxHttpConfigInstance(name, host, port, protocol);
    }
}
