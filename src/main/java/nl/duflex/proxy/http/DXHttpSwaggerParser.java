package nl.duflex.proxy.http;

import io.swagger.parser.OpenAPIParser;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

public class DXHttpSwaggerParser {
    private void parseRequestFromOperation(final DXHttpRequestMethod requestMethod, final Operation operation) {

    }

    private String createEndpointNameFromPath(final String path) {
        return Arrays.stream(path.split("/"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.replaceAll("[!a-zA-Z0-9]]", ""))
                .map(s -> s.length() > 3 ? s.substring(0, 3) : s)
                .map(StringUtils::capitalize)
                .collect(Collectors.joining(""));
    }

    private void parseEndpointFromPathItem(final String path, final PathItem pathItem) {
        // Creates the name based on the path.
        System.out.println(createEndpointNameFromPath(path));

        if (pathItem.getGet() != null) parseRequestFromOperation(DXHttpRequestMethod.Get, pathItem.getGet());
    }

    public void parse(final String swaggerContents) {
        final var openApiParser = new OpenAPIParser();
        final SwaggerParseResult parseResult = openApiParser.readContents(swaggerContents, null, null);
        final OpenAPI openAPI = parseResult.getOpenAPI();

        openAPI.getPaths().forEach((path, pathItem) -> {
            parseEndpointFromPathItem(path, pathItem);
        });
    }
}
