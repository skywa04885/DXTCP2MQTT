package nl.duflex.proxy.modbus;

import java.util.Arrays;
import java.util.List;

class ModbusProxyBitArrayEncoder {
    static String encode(final List<Boolean> values) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < values.size(); ++i) {
            if (i != 0) {
                stringBuilder.append(" ");
            }

            stringBuilder.append(values.get(i) ? "1" : "0");
        }

        return stringBuilder.toString();
    }

    static byte[] encodeToBinary(final List<Boolean> values) {
        // Creates the bytes array.
        final int nBytes = values.size() / 8 + (values.size() % 8 != 0 ? 1 : 0);
        final byte[] bytes = new byte[nBytes];

        // Clears the bytes array filling it with zero values.
        Arrays.fill(bytes, (byte) 0);

        // Sets the appropriate coil bits in the byte array.
        for (int i = 0; i < values.size(); ++i) {
            // If the value is false don't do anything since the bit will already be zero.
            if (!values.get(i)) continue;

            // Compute the byte and bit indices.
            final int j = i / 8;
            final int k = i % 8;

            // Set the bit in the byte.
            bytes[j] |= (byte) 1 << k;
        }

        // Returns the result.
        return bytes;
    }
}
