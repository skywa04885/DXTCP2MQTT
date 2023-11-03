package nl.duflex.proxy;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

public class DXDomUtils {
    public static List<Element> GetChildElementsWithTagName(final Element element, final String tagName) {
        final var childNodes = element.getChildNodes();
        final var result = new ArrayList<Element>();

        for (var i = 0; i < childNodes.getLength(); ++i)
        {
            final var childNode = childNodes.item(i);
            if (childNode.getNodeType() != Node.ELEMENT_NODE) continue;

            final var childElement = (Element) childNode;
            if (!childElement.getTagName().equals(tagName)) continue;;

            result.add(childElement);
        }

        return result;
    }
}
