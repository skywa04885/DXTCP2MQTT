package nl.duflex.proxy;

import java.io.*;
import java.net.Socket;

public class ProxyTcpClient {
    private final Socket socket;
    private final ProxyInputStreamReader inputStreamReader;
    private final ProxyOutputStreamWriter outputStreamWriter;
    private boolean isConnected = true;



    private ProxyTcpClient(final Socket socket, final InputStream inputStream, final OutputStream outputStream) {
        this.socket = socket;
        this.outputStreamWriter = new ProxyOutputStreamWriter(outputStream);
        this.inputStreamReader = new ProxyInputStreamReader(inputStream);
    }

    public ProxyOutputStreamWriter getOutputStreamWriter() {
        return this.outputStreamWriter;
    }

    public ProxyInputStreamReader getInputStreamReader() {
        return this.inputStreamReader;
    }

    public static ProxyTcpClient fromSocket(final Socket socket) throws IOException {
        final InputStream inputStream = socket.getInputStream();
        final OutputStream outputStream = socket.getOutputStream();

        return new ProxyTcpClient(socket, inputStream, outputStream);
    }

    public boolean isConnected() {
        return this.isConnected;
    }

    public void close() throws IOException {
        isConnected = false;
        this.socket.close();
    }

    @Override
    public String toString() {
        return this.socket.getRemoteSocketAddress().toString();
    }
}
