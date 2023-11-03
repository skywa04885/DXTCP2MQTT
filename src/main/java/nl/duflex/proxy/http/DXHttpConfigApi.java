package nl.duflex.proxy.http;

import nl.duflex.proxy.DXDomUtils;
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

        final var instancesElements = DXDomUtils.GetChildElementsWithTagName(element, INSTANCES_ELEMENT_TAG_NAME);

        if (instancesElements.size() == 0)
            throw new RuntimeException("Instances element is missing");
        else if (instancesElements.size() > 1)
            throw new RuntimeException("Too many instances elements");

        final var instancesElement = (Element) instancesElements.get(0);
        final var instanceElements = DXDomUtils.GetChildElementsWithTagName(instancesElement, DxHttpConfigInstance.TAG_NAME);
        final var instances = new HashMap<String, DxHttpConfigInstance>();

        for (final Element item : instanceElements) {
            final var instance = DxHttpConfigInstance.FromElement(item);
            System.out.println(instance.Name);
            instances.put(instance.Name, instance);
        }

        final var endpointsElements = DXDomUtils.GetChildElementsWithTagName(element, ENDPOINTS_ELEMENT_TAG_NAME);

        if (endpointsElements.size() == 0)
            throw new RuntimeException("Endpoints element is missing");
        else if (endpointsElements.size() > 1)
            throw new RuntimeException("Too many endpoints elements");

        final var endpointsElement = (Element) endpointsElements.get(0);
        final var endpointElements = DXDomUtils.GetChildElementsWithTagName(endpointsElement, DXHttpConfigEndpoint.TAG_NAME);
        final var endpoints = new HashMap<String, DXHttpConfigEndpoint>();

        for (final Element value : endpointElements) {
            final var endpoint = DXHttpConfigEndpoint.FromElement(value);
            endpoints.put(endpoint.Name, endpoint);
        }

        return new DXHttpConfigApi(name, httpVersion, instances, endpoints);
    }
}
