package nl.duflex.proxy.http;

public enum DXHttpFieldsFormat {
    JSON("JSON", "application/json"),
    XML("XML", "application/xml"),
    FormURLEncoded("FormURLEncoded", "x-www-form-urlencoded");

    public final String Name;
    public final String MimeType;

    DXHttpFieldsFormat(final String name, final String mimeType) {
        Name = name;
        MimeType = mimeType;
    }

    public static DXHttpFieldsFormat GetByName(String name) {
        name = name.trim();

        for (final var value : values())
            if (value.Name.equalsIgnoreCase(name)) return value;

        throw new RuntimeException("Unknown field format name: " + name);
    }

    public static DXHttpFieldsFormat GetByMimeType(String mimeType) {
        mimeType = mimeType.trim();

        for (final var value : values())
            if (value.MimeType.equalsIgnoreCase(mimeType)) return value;

        throw new RuntimeException("Unknown field format mime-type: " + mimeType);
    }
}
