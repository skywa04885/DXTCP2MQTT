package nl.duflex.proxy.http;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class DXHttpPathTemplateParser {
    public static Pattern PlaceholderPattern = Pattern.compile("^\\{(?<name>[a-zA-Z0-9_]+)\\}$",
            Pattern.CASE_INSENSITIVE);

    public DXHttpPathTemplate Parse(final String template) {
        final List<DXHttpPathTemplateSegment> segments = Arrays.stream(template.split("/"))
                .map(templateSegment -> {
                    templateSegment = templateSegment.trim();

                    if (templateSegment.length() == 0) return null;

                    final var matcher = PlaceholderPattern.matcher(templateSegment);

                    if (matcher.matches()) return new DXHttpPathTemplatePlaceholderSegment(matcher.group("name"));
                    else return new DXHttpPathTemplateTextSegment(templateSegment);
                }).filter(Objects::nonNull).toList();

        return new DXHttpPathTemplate(segments);
    }
}
