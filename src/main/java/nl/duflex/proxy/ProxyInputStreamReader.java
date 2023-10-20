package nl.duflex.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class ProxyInputStreamReader {
    private final InputStream inputStream;

    /**
     * Constructs a new input stream reader from the given input stream.
     * @param inputStream the input stream.
     */
    public ProxyInputStreamReader(final InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * Reads a string from the input stream until the delimiter is reached.
     * @param delimiterString the delimiter to look form.
     * @param charset the charset of the result and the delimiter.
     * @return the string without the delimiter.
     * @throws IOException gets thrown when an io exception occurs during reading.
     */
    public String readStringUntilDelimiter(final String delimiterString, final Charset charset) throws IOException {
        final byte[] delimiterBytes = delimiterString.getBytes(charset);
        byte[] data = new byte[128];
        int dataOffset = 0;

        // Stays in loop until the delimiter was found.
        while (true) {
            // If the data array is about to overflow, make it larger so it can fit more data.
            if (dataOffset >= data.length) {
                data = Arrays.copyOf(data, data.length * 2);
            }

            // Reads the single char from the input stream.
            int ret = this.inputStream.read(data, dataOffset, 1);
            assert ret != 0;

            // Returns null if we have reached the end of the stream.
            if (ret == -1) return null;

            // Increases the data offset.
            dataOffset += 1;

            // Checks if the delimiter is found.
            if (dataOffset >= delimiterBytes.length) {
                int i = dataOffset - delimiterBytes.length, j = 0;
                for (; i < dataOffset; ++i, ++j) if (data[i] != delimiterBytes[j]) break;
                if (i == dataOffset) break;
            }
        }

        // Returns the string excluding the delimiter.
        return new String(data, 0, dataOffset - delimiterBytes.length, charset);
    }

    /**
     * Reads a string from the input stream until the delimiter is reached.
     * @param delimiterString the delimiter to look form.
     * @return the string without the delimiter.
     * @throws IOException gets thrown when an io exception occurs during reading.
     */
    public String readStringUntilDelimiter(final String delimiterString) throws IOException {
        return this.readStringUntilDelimiter(delimiterString, StandardCharsets.UTF_8);
    }

    /**
     * Reads a string from the input stream until a newline has been reached.
     * @return the string without the new line.
     * @throws IOException gets thrown when an io exception occurs during reading.
     */
    public String readStringUntilNewLine() throws IOException {
        return this.readStringUntilDelimiter("\r\n");
    }

    /**
     * Reads a string from the input stream until a double newline has been reached.
     * @return the string without the double new line.
     * @throws IOException gets thrown when an io exception occurs during reading.
     */
    public String readStringUntilDoubleNewLine() throws IOException {
        return this.readStringUntilDelimiter("\r\n\r\n");
    }

    /**
     * Reads n bytes from the input stream.
     * @param n the number of bytes to read.
     * @return the buffer containing the read bytes.
     * @throws IOException gets thrown when an io exception occurs during reading.
     */
    public byte[] readNBytes(final int n) throws IOException {
        // Allocates the buffer for the bytes.
        final byte[] data = new byte[n];

        // Reads the n bytes.
        int ret = this.inputStream.readNBytes(data, 0, n);

        // Returns null if an end of stream happened.
        if (ret != n) return null;

        // Returns the data.
        return data;
    }
}
