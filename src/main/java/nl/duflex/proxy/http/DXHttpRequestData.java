package nl.duflex.proxy.http;

import java.util.Map;

public class DXHttpRequestData {
    public final String ApiName;
    public final String EndpointName;
    public final String InstanceName;
    public final DXHttpRequestMethod Method;
    public final Map<String, String> PathSubstitutes;
    public final Map<String, String> QueryParameters;
    public final Map<String, String> Headers;
    public final Map<String, String> Body;

    public DXHttpRequestData(final String apiName, final String endpointName, final String instanceName,
                             final DXHttpRequestMethod method, final Map<String, String> pathSubstitutes,
                             final Map<String, String> queryParameters, final Map<String, String> headers,
                             final Map<String, String> body) {
        ApiName = apiName;
        EndpointName = endpointName;
        InstanceName = instanceName;
        Method = method;
        PathSubstitutes = pathSubstitutes;
        QueryParameters = queryParameters;
        Headers = headers;
        Body = body;
    }
}
