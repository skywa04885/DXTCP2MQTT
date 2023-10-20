package nl.duflex.proxy.modbus;

import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

public enum ModbusProxyRequestType {
    WriteSingleRegisterRequest("WRITE_SINGLE_REGISTER_REQUEST");

    private final String opcode;

    ModbusProxyRequestType(final String opcode) {
        this.opcode = opcode;
    }

    public String getOpcode() {
        return this.opcode;
    }

    public void writeToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.opcode);
    }
}
