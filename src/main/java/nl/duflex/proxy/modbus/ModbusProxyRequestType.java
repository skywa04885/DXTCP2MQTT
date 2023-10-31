package nl.duflex.proxy.modbus;

import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

enum ModbusProxyRequestType {
    WriteSingleRegister("WRITE_SINGLE_REGISTER"),
    ReadWriteMultipleRegisters("WRITE_MULTIPLE_REGISTERS"),
    ReadHoldingRegisters("READ_HOLDING_REGISTERS"),
    WriteSingleCoil("WRITE_SINGLE_COIL"),
    WriteMultipleCoils("WRITE_MULTIPLE_COILS"),
    ReadCoils("READ_COILS"),
    WriteMultipleRegisters("WRITE_MULTIPLE_REGISTERS"),
    ReadDiscreteInputs("READ_DISCRETE_INPUTS"),
    ReadInputRegisters("READ_INPUT_REGISTERS"),
    MaskWriteRegister("MASK_WRITE_REGISTER");

    private final String opcode;

    ModbusProxyRequestType(final String opcode) {
        this.opcode = opcode;
    }

    private String getOpcode() {
        return this.opcode;
    }

    void writeToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.opcode);
    }

    public static ModbusProxyRequestType readFromInputStreamReader(
            final ProxyInputStreamReader inputStreamReader) throws IOException{
        // Reads the opcode (which is a single line) and returns null if we've reached the end of the stream.
        String opcode = inputStreamReader.readStringUntilNewLine();
        if (opcode == null) return null;

        // Trims of any whitespace and makes all the characters uppercase.
        opcode = opcode.trim().toUpperCase();

        // Tries to get the request type with the given opcode.
        for (final ModbusProxyRequestType requestType : ModbusProxyRequestType.values()) {
            if (requestType.opcode.equals(opcode)) return requestType;
        }

        // Throw an exception since we could not find any request type with the read opcode.
        throw new RuntimeException("Unrecognized request type opcode: " + opcode);
    }
}
