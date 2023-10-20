package nl.duflex.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ProxyOutputStreamWriter {
    private final OutputStream outputStream;

    public ProxyOutputStreamWriter(final OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public ProxyOutputStreamWriter close() throws IOException {
        this.outputStream.close();
        return this;
    }

    public ProxyOutputStreamWriter flush() throws IOException {
        this.outputStream.flush();
        return this;
    }

    public ProxyOutputStreamWriter write(final String string, final Charset charset) throws IOException {
        return this.write(string.getBytes(charset));
    }

    public ProxyOutputStreamWriter write(final String string) throws IOException {
        return this.write(string, StandardCharsets.UTF_8);
    }

    public ProxyOutputStreamWriter writeLine(final String string) throws IOException {
        return this.write(string).write("\r\n");
    }

    public ProxyOutputStreamWriter write(final byte[] chunk) throws IOException {
        this.outputStream.write(chunk);
        return this;
    }
}
