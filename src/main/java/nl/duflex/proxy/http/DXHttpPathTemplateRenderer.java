package nl.duflex.proxy.http;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class DXHttpPathTemplateRenderer {
    public final DXHttpPathTemplate Template;

    public DXHttpPathTemplateRenderer(final DXHttpPathTemplate template) {
        Template = template;
    }

    public String Render(final Map<String, String> substitutions) {
        final var stringBuilder = new StringBuilder();

        for (final var segment : Template.Segments) {
            stringBuilder.append('/');

            if (segment instanceof DXHttpPathTemplatePlaceholderSegment placeholderSegment) {
                final var substitution = substitutions.get(placeholderSegment.Name);
                if (substitution == null)
                    throw new RuntimeException("No substitution provided for placeholder " + placeholderSegment.Name);
                stringBuilder.append(URLEncoder.encode(substitution, StandardCharsets.UTF_8));
            } else if (segment instanceof DXHttpPathTemplateTextSegment textSegment) {
                stringBuilder.append(URLEncoder.encode(textSegment.Text, StandardCharsets.UTF_8));
            }
        }

        return stringBuilder.toString();
    }
}
