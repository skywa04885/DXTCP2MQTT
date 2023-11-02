package nl.duflex.proxy.http;

import org.w3c.dom.Element;

public class DXHttpConfigHeader {
    public static final String ELEMENT_TAG_NAME = "Header";
    public static final String KEY_ATTRIBUTE_NAME = "Key";
    public static final String VALUE_ATTRIBUTE_NAME = "Value";

    public final String Key;
    public final String Value;

    public DXHttpConfigHeader(final String key, final String value) {
        Key = key;
        Value = value;
    }

    public static DXHttpConfigHeader FromElement(final Element element) {
        if (!element.getTagName().equals(ELEMENT_TAG_NAME))
            throw new RuntimeException("Tag name mismatch");

        final String key = element.getAttribute(KEY_ATTRIBUTE_NAME).trim();
        if (key.isEmpty()) throw new RuntimeException("Key attribute missing");

        String value = element.getAttribute(VALUE_ATTRIBUTE_NAME).trim();
        value = value.isEmpty() ? null : value;

        return new DXHttpConfigHeader(key, value);
    }
}
