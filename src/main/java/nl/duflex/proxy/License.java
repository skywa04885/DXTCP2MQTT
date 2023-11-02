package nl.duflex.proxy;

import java.util.List;

public class License {
    private final List<String> features;
    private final String details;

    public License(final List<String> features, final String details) {
        this.features = features;
        this.details = details;
    }

    public List<String> getFeatures() {
        return features;
    }

    public String getDetails() {
        return details;
    }
}
