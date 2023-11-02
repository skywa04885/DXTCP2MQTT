package nl.duflex.proxy.http;

import org.w3c.dom.Element;

import java.util.HashMap;

public class DXHttpConfigApi {
    public static final String NAME_ATTRIBUTE = "Name";
    public static final String HTTP_VERSION_ATTRIBUTE = "HttpVersion";
    public static final String INSTANCES_ELEMENT_TAG_NAME = "Instances";
    public static final String ENDPOINTS_ELEMENT_TAG_NAME = "Endpoints";

    public final String Name;
    public final String HttpVersion;
    public final HashMap<String, DxHttpConfigInstance> Instances;
    public final HashMap<String, DXHttpConfigEndpoint> Endpoints;

    public DXHttpConfigApi(final String name, final String httpVersion,
                           final HashMap<String, DxHttpConfigInstance> instances,
                           final HashMap<String, DXHttpConfigEndpoint> endpoints) {
        Name = name;
        HttpVersion = httpVersion;
        Instances = instances;
        Endpoints = endpoints;
    }

    public DXHttpConfigEndpoint GetEndpointByName(final String name) {
        return Endpoints.get(name);
    }

    public DxHttpConfigInstance GetInstanceByName(final String name) {
        return Instances.get(name);
    }

    public static DXHttpConfigApi FromElement(final Element element) {
        final var name = element.getAttribute(NAME_ATTRIBUTE);
        final var httpVersion = element.getAttribute(HTTP_VERSION_ATTRIBUTE);

        final var instancesElements = element.getElementsByTagName(INSTANCES_ELEMENT_TAG_NAME);

        if (instancesElements.getLength() == 0) throw new RuntimeException("Instances element is missing");
        else if (instancesElements.getLength() > 1) throw new RuntimeException("Too many instances elements");

        final var instancesElement = (Element) instancesElements.item(0);
        final var instanceElements = instancesElement.getElementsByTagName(DxHttpConfigInstance.TAG_NAME);
        final var instances = new HashMap<String, DxHttpConfigInstance>();

        for (var i = 0; i < instanceElements.getLength(); ++i) {
            final var instanceElement = (Element) instanceElements.item(i);
            final var instance = DxHttpConfigInstance.FromElement(instanceElement);
            instances.put(instance.Name, instance);
        }

        final var endpointsElements = element.getElementsByTagName(ENDPOINTS_ELEMENT_TAG_NAME);

        if (endpointsElements.getLength() == 0) throw new RuntimeException("Endpoints element is missing");
        else if (endpointsElements.getLength() > 1) throw new RuntimeException("Too many endpoints elements");

        final var endpointsElement = (Element) endpointsElements.item(0);
        final var endpointElements = endpointsElement.getElementsByTagName(DXHttpConfigEndpoint.TAG_NAME);
        final var endpoints = new HashMap<String, DXHttpConfigEndpoint>();

        for (var i = 0; i < endpointElements.getLength(); ++i) {
            final var endpointElement = (Element) endpointElements.item(i);
            final var endpoint = DXHttpConfigEndpoint.FromElement(endpointElement);
            endpoints.put(endpoint.Name, endpoint);
        }

        return new DXHttpConfigApi(name, httpVersion, instances, endpoints);
    }
}
