package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.WriteSingleRegisterRequest;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;

public class ModbusProxyWriteSingleRegisterRequestMessage extends ModbusProxyRequestMessage {
    public static class Builder {
        private Integer address = null;
        private Integer value = null;

        public Builder setAddress(final Integer address) {
            this.address = address;
            return this;
        }

        public Builder setValue(final Integer value) {
            this.value = value;
            return this;
        }

        public Builder copyFrom(final WriteSingleRegisterRequest request) {
            this.address = request.getAddress();
            this.value = request.getValue();
            return this;
        }

        public ModbusProxyWriteSingleRegisterRequestMessage build() {
            assert this.address != null;
            assert this.value != null;

            return new ModbusProxyWriteSingleRegisterRequestMessage(this.address, this.value);
        }
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    private final int address;
    private final int value;

    public ModbusProxyWriteSingleRegisterRequestMessage(final int address, final int value) {
        super(ModbusProxyRequestType.WriteSingleRegisterRequest);

        this.address = address;
        this.value = value;
    }

    public int getAddress() {
        return this.address;
    }

    public int getValue() {
        return this.value;
    }

    protected void implWriteToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        writer.writeLine(this.address + " " + this.value).flush();
    }
}
