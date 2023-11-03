package nl.duflex.proxy.http;

import java.util.Map;

public class DXHttpResponse {
    public final int Code;
    public final Map<String, String> Headers;
    public final Map<String, String> Body;

    public DXHttpResponse(final int code, final Map<String, String> headers, final Map<String, String> body) {
        Code = code;
        Headers = headers;
        Body = body;
    }
}
