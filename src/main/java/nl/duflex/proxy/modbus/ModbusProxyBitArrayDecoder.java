package nl.duflex.proxy.modbus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class ModbusProxyBitArrayDecoder {
    static List<Boolean> decodeFromBinary(final byte[] bytes, final int quantity) {
        final List<Boolean> result = new ArrayList<>(quantity);

        for (int i = 0; i < quantity; ++i) {
            final int j = i / 8;
            final int k = i % 8;
            result.add((bytes[j] & (0x1 << k)) != 0);
        }

        return result;
    }

    /**
     * Decodes the list of coil values from the given string, this string has all the coil values listed
     * as 1's and 0's separated by spaces.
     *
     * @param string The string to decode.
     * @return The decoded coil values.
     */
    static List<Boolean> decodeFromString(final String string) {
        return Arrays.stream(string.split(" ")).map(segment -> {
            if (segment.equals("1")) return true;
            else if (segment.equals("0")) return false;
            else throw new RuntimeException("Invalid coil value, expected 0 or 1, got: " + segment);
        }).collect(Collectors.toList());
    }
}
