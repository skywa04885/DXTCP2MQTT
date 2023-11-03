package nl.duflex.proxy.http;

import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;
import java.util.Map;

public class DXHttpResponseWriter {
    private final ProxyOutputStreamWriter outputStreamWriter;

    public DXHttpResponseWriter(final ProxyOutputStreamWriter outputStreamWriter) {
        this.outputStreamWriter = outputStreamWriter;
    }

    private void writeKeyValueMap(final Map<String, String> map) throws IOException {
        for (final Map.Entry<String, String> entry : map.entrySet())
            outputStreamWriter.writeLine(entry.getKey() + ": " + entry.getValue());

        outputStreamWriter.writeLine();
    }

    public void write(final DXHttpResponse response) throws IOException {
        // Writes the response code.
        outputStreamWriter.writeLine(Integer.toString(response.getCode()));

        // Writes the headers if they are present.
        if (response.getHeaders() != null) writeKeyValueMap(response.getHeaders());

        // Writes the fields if they are present.
        if (response.getFields() != null) writeKeyValueMap(response.getFields());

        // Flushes.
        outputStreamWriter.flush();
    }
}
