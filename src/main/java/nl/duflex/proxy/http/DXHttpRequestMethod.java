package nl.duflex.proxy.http;

public enum DXHttpRequestMethod {
    Get("GET"),
    Post("POST"),
    Put("PUT"),
    Delete("DELETE");

    public final String Label;

    DXHttpRequestMethod(final String label) {
        Label = label;
    }

    public static DXHttpRequestMethod FromLabel(String label) {
        label = label.trim().toUpperCase();

        for (final var method : values()) {
            if (method.Label.equals(label)) return method;
        }

        throw new RuntimeException("Unrecognized request method: " + label);
    }
}
