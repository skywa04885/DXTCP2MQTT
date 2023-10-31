package nl.duflex.proxy.modbus;

import com.digitalpetri.modbus.requests.WriteMultipleCoilsRequest;
import nl.duflex.proxy.ProxyInputStreamReader;
import nl.duflex.proxy.ProxyOutputStreamWriter;

import java.io.IOException;
import java.util.List;

class WriteMultipleCoilsRequestMessage extends RequestMessage<WriteMultipleCoilsRequest> {
    static class Builder extends RequestMessage.Builder<com.digitalpetri.modbus.requests.WriteMultipleCoilsRequest, WriteMultipleCoilsRequestMessage> {
        private List<Boolean> values = null;
        private Integer address = null;

        @Override
        Builder copyFromModbusRequest(final WriteMultipleCoilsRequest request) {
            this.address = request.getAddress();
            this.values = ModbusProxyBitArrayDecoder.decodeFromBinary(
                    io.netty.buffer.ByteBufUtil.getBytes(request.getValues()),
                    request.getQuantity());

            return this;
        }

        @Override
        Builder readFromInputStreamReader(final ProxyInputStreamReader inputStreamReader)
                throws RuntimeException, IOException {
            final String addressLine = inputStreamReader.readStringUntilNewLine();
            if (addressLine == null) return null;

            final String valuesLine = inputStreamReader.readStringUntilNewLine();
            if (valuesLine == null) return null;

            int address;

            try {
                address = Integer.parseInt(addressLine);
            } catch (final NumberFormatException numberFormatException) {
                throw new RuntimeException("Invalid address in address line of write multiple coils request");
            }

            List<Boolean> values;

            try {
                values = ModbusProxyBitArrayDecoder.decodeFromString(valuesLine);
            } catch (final RuntimeException runtimeException) {
                throw new RuntimeException("Invalid values given in values line of write multiple coils request");
            }

            this.address = address;
            this.values = values;

            return this;
        }

        WriteMultipleCoilsRequestMessage build() {
            return new WriteMultipleCoilsRequestMessage(this.values, this.address);
        }
    }

    static Builder newBuilder() {
        return new Builder();
    }

    private final List<Boolean> values;
    private final int address;

    WriteMultipleCoilsRequestMessage(final List<Boolean> values, final int address) {
        super(ModbusProxyRequestType.WriteMultipleCoils);

        this.values = values;
        this.address = address;
    }

    public int getAddress() {
        return address;
    }

    public List<Boolean> getValues() {
        return values;
    }

    @Override
    void implWriteToOutputStreamWriter(final ProxyOutputStreamWriter writer) throws IOException {
        writer
                .writeLine(Integer.toString(this.address))
                .writeLine(ModbusProxyBitArrayEncoder.encode(this.values))
                .flush();
    }

    @Override
    public String toString() {
        return this.getClass().getName() + " { address: " + this.address + " }";
    }
}
