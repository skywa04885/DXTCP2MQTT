package nl.duflex.proxy.http;

import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DXHttpConfig {
    public static final String ELEMENT_TAG_NAME = "Apis";

    public final Map<String, DXHttpConfigApi> HttpApis;

    public DXHttpConfig(final Map<String, DXHttpConfigApi> httpApis) {
        HttpApis = httpApis;
    }

    public DXHttpConfigApi GetApiByName(final String name) {
        return HttpApis.get(name);
    }

    public static DXHttpConfig FromElement(final Element element) {
        final Map<String, DXHttpConfigApi> httpApis = new HashMap<>();

        for (var i = 0; i < element.getChildNodes().getLength(); ++i) {
            final var childNode = element.getChildNodes().item(i);
            if (childNode instanceof Element childElement)
            {
                final DXHttpConfigApi api = DXHttpConfigApi.FromElement(childElement);
                if (httpApis.containsKey(api.Name)) throw new RuntimeException("Duplicate API name: " + api.Name);
                httpApis.put(api.Name, api);
            }
        }

        return new DXHttpConfig(httpApis);
    }

    public static DXHttpConfig ReadFromFile(final File file) throws FileNotFoundException, IOException {
        try (var inputStream = new FileInputStream(file)) {
            final var documentBuilderFactory = DocumentBuilderFactory.newInstance();
            final var documentBuilder = documentBuilderFactory.newDocumentBuilder();
            final var document = documentBuilder.parse(inputStream);

            final var documentElement = document.getDocumentElement();
            if (!documentElement.getTagName().equals(ELEMENT_TAG_NAME))
                throw new RuntimeException("Document tag name mismatch");

            return DXHttpConfig.FromElement(documentElement);
        } catch (ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }
    }
}
