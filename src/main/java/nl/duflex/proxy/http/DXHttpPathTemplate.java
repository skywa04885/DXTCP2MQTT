package nl.duflex.proxy.http;

import java.util.List;

public class DXHttpPathTemplate {
    public final List<DXHttpPathTemplateSegment> Segments;

    public DXHttpPathTemplate(final List<DXHttpPathTemplateSegment> segments) {
        Segments = segments;
    }
}
